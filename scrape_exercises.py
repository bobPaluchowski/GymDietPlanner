import asyncio
import httpx
import os
import sqlite3
import sys
import io
import threading
from bs4 import BeautifulSoup
from urllib.parse import urljoin
from PIL import Image

# --- CONFIGURATION ---
BASE_URL = "https://liftmanual.com"

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.join(SCRIPT_DIR, "app", "src", "main")
IMG_DIR = os.path.join(PROJECT_ROOT, "assets", "exercises")
DB_PATH = os.path.join(PROJECT_ROOT, "assets", "exercises_preseed.db")

CONCURRENT_LIMIT = 3          # Low to avoid rate limiting
REQUEST_DELAY = 0.3           # Seconds between requests
MAX_RETRIES = 3               # Retries on 429/503

os.makedirs(IMG_DIR, exist_ok=True)

HEADERS = {"User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"}

# SQLite is not thread/async safe for concurrent writes — use a lock
db_lock = threading.Lock()


# --- DATABASE SETUP ---
def setup_db(fresh_start=False):
    conn = sqlite3.connect(DB_PATH, check_same_thread=False)
    # Enable WAL mode for better concurrent read performance
    conn.execute("PRAGMA journal_mode=WAL")
    cursor = conn.cursor()
    if fresh_start:
        cursor.execute("DROP TABLE IF EXISTS exercises")
        print("🗑️   Cleared old exercises table for fresh run.")
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS exercises (
            id        INTEGER PRIMARY KEY AUTOINCREMENT,
            name      TEXT    NOT NULL,
            equipment TEXT    NOT NULL DEFAULT '',
            category  TEXT    NOT NULL DEFAULT '',
            isCustom  INTEGER NOT NULL DEFAULT 0,
            iconName  TEXT
        )
    ''')
    conn.commit()
    return conn


# --- LAZY LOAD DETECTION ---
def get_real_image_url(img_tag):
    """Check lazy-load data attributes before falling back to src. Skip SVG placeholders."""
    if not img_tag:
        return None
    for attr in ["data-src", "data-lazy-src", "data-original", "data-lazy", "data-url", "src"]:
        url = img_tag.get(attr, "")
        if url and "image/svg+xml" not in url and not url.startswith("data:") and "svg" not in url.lower():
            return url
    return None


# --- IMAGE PROCESSING ---
def process_image(img_content, name_slug):
    """Convert raw bytes → WebP → assets/exercises/. Returns filename or ''."""
    final_filename = f"{name_slug}.webp"
    final_path = os.path.join(IMG_DIR, final_filename)

    try:
        img = Image.open(io.BytesIO(img_content))
        if img.mode in ("RGBA", "P"):
            img = img.convert("RGBA")
        else:
            img = img.convert("RGB")
        img.thumbnail((800, 600), Image.LANCZOS)
        img.save(final_path, "WEBP", quality=80, method=6)
        return final_filename
    except Exception as e:
        print(f"  ⚠️  Image processing failed for {name_slug}: {e}")
        return ""


# --- DB WRITE (thread-safe) ---
def db_insert_exercise(conn, name, equipment, category, icon_name):
    """Thread-safe insert. Returns True if inserted, False if duplicate."""
    with db_lock:
        cursor = conn.cursor()
        cursor.execute("SELECT id FROM exercises WHERE name = ?", (name,))
        if cursor.fetchone():
            return False  # duplicate
        cursor.execute(
            "INSERT INTO exercises (name, equipment, category, isCustom, iconName) VALUES (?, ?, ?, 0, ?)",
            (name, equipment, category, icon_name or None)
        )
        conn.commit()
        return True


# --- PROGRESS COUNTER (shared) ---
class Progress:
    def __init__(self, total):
        self.lock = threading.Lock()
        self.done = 0
        self.saved = 0
        self.skipped = 0
        self.total = total

    def tick(self, saved=False, skipped=False):
        with self.lock:
            self.done += 1
            if saved:
                self.saved += 1
            if skipped:
                self.skipped += 1
            if self.done % 50 == 0 or self.done == self.total:
                print(f"  📊 Progress: {self.done}/{self.total} done, {self.saved} saved, {self.skipped} skipped")


# --- FETCH WITH RETRY ---
async def fetch_with_retry(client, url, timeout=30.0):
    """Fetch a URL, retrying on 429/503 with exponential backoff."""
    for attempt in range(MAX_RETRIES):
        try:
            res = await client.get(url, timeout=timeout)
            if res.status_code in (429, 503):
                wait = 2 ** attempt * 5  # 5s, 10s, 20s
                print(f"  ⏳ Rate limited ({res.status_code}), waiting {wait}s... [{url}]")
                await asyncio.sleep(wait)
                continue
            return res
        except Exception as e:
            if attempt == MAX_RETRIES - 1:
                raise
            await asyncio.sleep(2 ** attempt)
    return None


# --- SCRAPE ONE EXERCISE ---
async def scrape_detail(client, url, semaphore, db_conn, progress):
    async with semaphore:
        await asyncio.sleep(REQUEST_DELAY)  # polite delay
        try:
            res = await fetch_with_retry(client, url)
            if res is None or res.status_code != 200:
                print(f"  ❌ HTTP {res.status_code if res else 'None'}: {url}")
                progress.tick()
                return

            soup = BeautifulSoup(res.text, "html.parser")
            article = soup.select_one("article.dynamic-content-template")
            if not article:
                # Log a sample to diagnose structure issues
                title = soup.title.get_text(strip=True) if soup.title else "no title"
                print(f"  ⚠️  No article found | {title} | {url}")
                progress.tick()
                return

            h1 = article.select_one("h1")
            if not h1:
                progress.tick()
                return
            name = h1.get_text(strip=True)
            name_slug = name.lower().replace(" ", "_").replace("-", "_").replace("/", "_")

            # Image (lazy-load aware)
            img_tag = article.select_one("img.dynamic-featured-image")
            real_img_url = get_real_image_url(img_tag)
            image_filename = ""
            if real_img_url:
                try:
                    img_res = await fetch_with_retry(client, urljoin(BASE_URL, real_img_url), timeout=20.0)
                    if img_res and img_res.status_code == 200:
                        image_filename = process_image(img_res.content, name_slug)
                except Exception as img_err:
                    print(f"  ⚠️  Image error for {name}: {img_err}")

            # Extract muscles and equipment
            muscles, equipment_list = [], []
            for grid in article.select(".gb-grid-wrapper"):
                text = grid.get_text()
                val_cols = grid.select(".gb-grid-column")
                if not val_cols:
                    continue
                val_col = val_cols[-1]
                if "Muscle Group" in text or "Primary Muscle" in text:
                    muscles = [a.get_text(strip=True) for a in val_col.select("a")]
                elif "Equipment" in text:
                    equipment_list = [a.get_text(strip=True) for a in val_col.select("a")]

            category = ", ".join(muscles) if muscles else "Other"
            equipment = ", ".join(equipment_list) if equipment_list else "Bodyweight"

            inserted = db_insert_exercise(db_conn, name, equipment, category, image_filename)
            if inserted:
                img_status = f"🖼 {image_filename}" if image_filename else "⚠️  no image"
                print(f"✅  {name}  [{category}]  {img_status}")
                progress.tick(saved=True)
            else:
                progress.tick(skipped=True)  # duplicate

        except Exception as e:
            print(f"❌  Error on {url}: {e}")
            progress.tick()


# --- DISCOVER ALL EXERCISE URLS ---
async def get_exercises_from_category(client, cat_url):
    """Collect all exercise URLs from a category page, following pagination."""
    exercise_urls = set()
    page = 1

    while True:
        url = f"{cat_url}?page={page}" if page > 1 else cat_url
        try:
            res = await client.get(url, timeout=15.0)
            if res.status_code != 200:
                break

            soup = BeautifulSoup(res.text, "html.parser")
            links = [
                urljoin(BASE_URL, a["href"])
                for a in soup.select("div.index-block a[href]")
            ]

            if not links:
                break

            exercise_urls.update(links)

            # Stop if no pagination link found
            has_next = soup.select_one("a[href*='?page='], a.next, .pagination .next")
            if not has_next:
                break

            page += 1

        except Exception as e:
            print(f"  ⚠️  Pagination error at {url}: {e}")
            break

    return exercise_urls


async def discover_exercise_urls(client):
    """
    Find all category pages across the entire site, then collect exercise URLs.
    FIX: Does NOT filter exercise URLs by path — exercise URLs may legitimately
    contain /equipment/ or /muscle/ as part of their path structure.
    """
    print("🔍  Step 1: Discovering category pages...")

    cat_links = set()

    # Check multiple entry points for category links
    entry_points = [
        BASE_URL,
        f"{BASE_URL}/equipment/",
        f"{BASE_URL}/muscle/",
        f"{BASE_URL}/exercises/",
    ]

    for entry in entry_points:
        try:
            res = await client.get(entry, timeout=15.0)
            soup = BeautifulSoup(res.text, "html.parser")
            for a in soup.find_all("a", href=True):
                href = a["href"]
                full_url = urljoin(BASE_URL, href)
                # A category page has exactly one segment after /equipment/ or /muscle/
                # e.g. /equipment/band/ but NOT /equipment/ itself
                for prefix in ["/equipment/", "/muscle/"]:
                    if prefix in full_url:
                        # Must have content after the prefix
                        after = full_url.split(prefix)[-1].strip("/")
                        if after and "/" not in after:  # leaf category, no sub-path
                            cat_links.add(full_url.rstrip("/") + "/")
        except Exception as e:
            print(f"  ⚠️  Entry point {entry}: {e}")

    print(f"   Found {len(cat_links)} category pages")

    # Step 2: Collect exercise URLs from every category page (with pagination)
    print("🔍  Step 2: Collecting exercise URLs (with pagination)...")
    exercise_urls = set()

    for cat_url in sorted(cat_links):
        found = await get_exercises_from_category(client, cat_url)
        cat_name = cat_url.rstrip("/").split("/")[-1]
        if found:
            print(f"   {cat_name}: {len(found)} exercises")
        else:
            print(f"   {cat_name}: 0 (check manually)")
        exercise_urls.update(found)

    # Only exclude the category index pages themselves, NOT exercise sub-pages
    exercise_urls = {
        url for url in exercise_urls
        if url.rstrip("/") not in {c.rstrip("/") for c in cat_links}
        and url.rstrip("/") not in {e.rstrip("/") for e in entry_points}
    }

    return exercise_urls


# --- RETRY MODE ---
async def retry_missing_images(db_conn, client, semaphore):
    with db_lock:
        cursor = db_conn.cursor()
        cursor.execute("SELECT id, name FROM exercises WHERE iconName IS NULL OR iconName = ''")
        missing = cursor.fetchall()

    if not missing:
        print("✅  No missing images found.")
        return

    print(f"🔁  {len(missing)} exercises have missing images. Re-fetching...\n")
    exercise_urls = await discover_exercise_urls(client)
    url_by_slug = {url.rstrip("/").split("/")[-1].replace("-", "_"): url for url in exercise_urls}

    async def fix_one(ex_id, name):
        name_slug = name.lower().replace(" ", "_").replace("-", "_").replace("/", "_")
        url = url_by_slug.get(name_slug)
        if not url:
            print(f"  ❓  URL not found for: {name}")
            return
        async with semaphore:
            try:
                res = await client.get(url, timeout=30.0)
                soup = BeautifulSoup(res.text, "html.parser")
                article = soup.select_one("article.dynamic-content-template")
                if not article:
                    return
                img_tag = article.select_one("img.dynamic-featured-image")
                real_img_url = get_real_image_url(img_tag)
                if not real_img_url:
                    return
                img_res = await client.get(urljoin(BASE_URL, real_img_url), timeout=20.0)
                if img_res.status_code == 200:
                    icon_name = process_image(img_res.content, name_slug)
                    if icon_name:
                        with db_lock:
                            c = db_conn.cursor()
                            c.execute("UPDATE exercises SET iconName = ? WHERE id = ?", (icon_name, ex_id))
                            db_conn.commit()
                        print(f"✅  Fixed: {name} → {icon_name}")
            except Exception as e:
                print(f"  ❌  {name}: {e}")

    await asyncio.gather(*[fix_one(ex_id, name) for ex_id, name in missing])


# --- MAIN ---
async def main():
    retry_mode = "--retry" in sys.argv
    fresh_start = "--fresh" in sys.argv

    db_conn = setup_db(fresh_start=fresh_start)
    semaphore = asyncio.Semaphore(CONCURRENT_LIMIT)

    async with httpx.AsyncClient(headers=HEADERS, timeout=None, follow_redirects=True) as client:
        if retry_mode:
            print("🔁  RETRY MODE: fixing missing images only\n")
            await retry_missing_images(db_conn, client, semaphore)
        else:
            exercise_urls = await discover_exercise_urls(client)
            print(f"\n🚀  {len(exercise_urls)} unique exercise URLs found. Scraping...\n")
            progress = Progress(total=len(exercise_urls))
            tasks = [scrape_detail(client, url, semaphore, db_conn, progress) for url in exercise_urls]
            await asyncio.gather(*tasks)

    with db_lock:
        cursor = db_conn.cursor()
        cursor.execute("SELECT COUNT(*), COUNT(iconName) FROM exercises")
        total, with_img = cursor.fetchone()
    db_conn.close()

    print(f"\n🏁  Done!")
    print(f"   Exercises  : {total}")
    print(f"   With image : {with_img}")
    print(f"   Missing    : {total - with_img}")
    print(f"   DB  → {DB_PATH}")
    print(f"   IMG → {IMG_DIR}")


if __name__ == "__main__":
    asyncio.run(main())
