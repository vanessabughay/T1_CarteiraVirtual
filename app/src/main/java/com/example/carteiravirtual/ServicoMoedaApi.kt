package com.example.carteiravirtual

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface ServicoMoedaApi {
    @GET("last/{moedas}")
    suspend fun obterCotacao(@Path("moedas") moedas: String): RespostaCotacao
}

object InstanciaRetrofit {
    private const val BASE_URL = "https://economia.awesomeapi.com.br/json/"

    val api: ServicoMoedaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServicoMoedaApi::class.java)
    }
}