package com.example.carteiravirtual

import retrofit2.http.GET
import retrofit2.http.Path

interface AwesomeAPI {
    // Endpoint da API para obter a cotação
    @GET("json/last/{moedas}")
    suspend fun getCotacao(
        @Path("moedas") moedas: String
    ): CambioDado
}
