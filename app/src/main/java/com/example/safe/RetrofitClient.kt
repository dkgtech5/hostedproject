package com.example.safe

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface ApiService {
    @POST("predict")
    fun predictUrl(@Body request: ScanRequest): Call<ScanResponse>

    @POST("send-otp")
    fun sendOtp(@Body request: OtpRequest): Call<OtpResponse>

    @POST("verify-otp")
    fun verifyOtp(@Body request: OtpVerifyRequest): Call<OtpResponse>
}

/**
 * Singleton Retrofit client to manage API requests to the FastAPI backend.
 */
object RetrofitClient {
    // Change this to http://10.0.2.2:8000/ if using Emulator
    private const val BASE_URL = "https://safeguard-api-h021.onrender.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
