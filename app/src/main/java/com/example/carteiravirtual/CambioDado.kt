package com.example.carteiravirtual

data class CambioDado(

    //BRL-outras
    // val BRLETH: MoedaCotacao?, //não encontrada
    val BRLEUR: MoedaCotacao,
    //val BRLBTC: MoedaCotacao?, //não encontrada
    val BRLUSD: MoedaCotacao,

    //ETH-outras
    val ETHBRL: MoedaCotacao,
    val ETHEUR: MoedaCotacao,
    // val ETHBTC: MoedaCotacao?, //não encontrada
    val ETHUSD: MoedaCotacao,

    //EUR-outras
    val EURBRL: MoedaCotacao,
    // val EURETH: MoedaCotacao?, //não encontrada
    // val EURBTC: MoedaCotacao?, //não encontrada
    val EURUSD: MoedaCotacao,

    //USD-outras
    val USDBRL: MoedaCotacao,
    // val USDETH: MoedaCotacao?, //não encontrada
    val USDEUR: MoedaCotacao,
    // val USDBTC: MoedaCotacao?, //não encontrada

    //BTC-outras
    val BTCBRL: MoedaCotacao,
    // val BTCETH: MoedaCotacao?, //não encontrada
    val BTCEUR: MoedaCotacao,
    val BTCUSD: MoedaCotacao
)

data class MoedaCotacao(
    val bid: Double
)

