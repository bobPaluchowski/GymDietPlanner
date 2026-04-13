package com.example.gymdietplanner.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {
    private const val BASE_URL = "https://edb-with-videos-and-images-by-ascendapi.p.rapidapi.com/"
    private const val API_HOST = "edb-with-videos-and-images-by-ascendapi.p.rapidapi.com"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-RapidAPI-Key", "REMOVED")
                .addHeader("X-RapidAPI-Host", API_HOST)
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val instance: ExerciseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ExerciseApiService::class.java)
    }
}
