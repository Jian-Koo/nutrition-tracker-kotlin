package com.example.foodcalorie

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// API used : https://calorieninjas.com/api (CalorieNinjas)

data class ResponseModel(
    @SerializedName("items")
    val hits: List<FoodItem>
)

data class FoodItem(
    // only name, calories, servingSize, fatTotal, protein, and carb are used
    @SerializedName("name")
    val name: String,
    @SerializedName("calories")
    val calories: Double,
    @SerializedName("serving_size_g")
    val servingSize: Double,
    @SerializedName("fat_total_g")
    val fatTotal: Double,
    @SerializedName("protein_g")
    val protein: Double,
    @SerializedName("carbohydrates_total_g")
    val carb: Double,
    @SerializedName("fat_saturated_g")
    val fatSaturated: Double,
    @SerializedName("sodium_mg")
    val sodium: Double,
    @SerializedName("potassium_mg")
    val potassium: Double,
    @SerializedName("cholesterol_mg")
    val cholesterol: Double,
    @SerializedName("fiber_g")
    val fiber: Double,
    @SerializedName("sugar_g")
    val sugar: Double
)

interface RemoteAPICalls {
    @GET("/v1/nutrition")
    suspend fun getNutritionInfo(
        @Header("X-Api-Key") apiKey: String,
        @Query("query") searchValue: String
    ): ResponseModel
}

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.calorieninjas.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: RemoteAPICalls by lazy {
        retrofit.create(RemoteAPICalls::class.java)
    }
}