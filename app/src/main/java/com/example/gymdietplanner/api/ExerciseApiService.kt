package com.example.gymdietplanner.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.Response

import com.google.gson.annotations.SerializedName

data class ExerciseResponse(
    @SerializedName("exerciseId") val exerciseId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("bodyParts") val bodyParts: List<String>?,
    @SerializedName("targetMuscles") val targetMuscles: List<String>?,
    @SerializedName("secondaryMuscles") val secondaryMuscles: List<String>?,
    @SerializedName("equipments") val equipments: List<String>?,
    @SerializedName("imageUrl") val imageUrl: String?,       // API returns single URL, not array
    @SerializedName("videoUrls") val videoUrls: List<String>?,
    @SerializedName("instructions") val instructions: List<String>?,
    @SerializedName("exerciseType") val exerciseType: String?
)

data class ExercisesListResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: List<ExerciseResponse>?  // API uses "data", not "results"
)

interface ExerciseApiService {
    @GET("api/v1/exercises")
    suspend fun getExercises(
        @retrofit2.http.Query("limit") limit: Int = 200
    ): Response<ExercisesListResponse>

    @GET("api/v1/exercises/search")
    suspend fun searchExercises(
        @retrofit2.http.Query("query") query: String
    ): Response<ExercisesListResponse>
}
