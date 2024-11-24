package com.example.carteiravirtual

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://economia.awesomeapi.com.br/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val awesomeAPI: AwesomeAPI = retrofit.create(AwesomeAPI::class.java)
}
