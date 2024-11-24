package com.example.carteiravirtual

 data class CambioDado(
    val BRLUSD: MoedaCotacao,
    val BRLEUR: MoedaCotacao
)


/*
data class CambioDado(

    val cotações: Map<String, MoedaCotacao>
)
 */

data class MoedaCotacao(
    val bid: Double // Aqui é onde armazenamos o valor da cotação de compra
)
