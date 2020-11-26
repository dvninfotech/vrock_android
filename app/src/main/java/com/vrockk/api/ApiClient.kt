package com.vrockk.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class ApiClient {

    companion object{

        val apiService : ApiCallInterface
            get() {

                val  retrofit =
                    Retrofit.Builder().baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(getUnsafeOkHttpClient())
                        .build()

                return  retrofit.create(ApiCallInterface::class.java)
            }

        fun getUnsafeOkHttpClient(): OkHttpClient {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val builder = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor { chain ->
                    val newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", UUID.randomUUID().toString())
                        .build()
                    chain.proceed(newRequest)
                }.build()
            return builder
        }
    }
}