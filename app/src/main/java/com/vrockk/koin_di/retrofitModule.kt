package com.vrockk.koin_di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.vrockk.api.ApiCallInterface
import com.vrockk.api.BASE_URL
import com.vrockk.api.Repository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val retrofitModule: Module = module {
    single {
        okHttp()
    }
    single {
        provideGson()
    }
    single {
        retrofit(get(), get())
    }
    single {
        get<Retrofit>().create(ApiCallInterface::class.java)
    }
    single {
        Repository(get())
    }
}


private fun okHttp(): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor()
    loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    val httpClient = OkHttpClient.Builder()
    httpClient.addInterceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .build()
        chain.proceed(request)
    }
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.MINUTES)
        .writeTimeout(10, TimeUnit.MINUTES)
        .readTimeout(10, TimeUnit.MINUTES)
    return httpClient.build()
}

fun provideGson() = GsonBuilder().setLenient().create()

private fun retrofit(gson: Gson, okHttpClient: OkHttpClient) = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create(gson))
    .build()