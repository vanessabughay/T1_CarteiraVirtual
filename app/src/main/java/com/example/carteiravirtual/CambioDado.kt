package com.example.carteiravirtual

data class CambioDado(

    //BRL-outras
    val BRLETH: MoedaCotacao?, //não encontrada
    val BRLEUR: MoedaCotacao,
    val BRLBTC: MoedaCotacao?, //não encontrada
    val BRLUSD: MoedaCotacao,

    //ETH-outras
    val ETHBRL: MoedaCotacao,
    val ETHEUR: MoedaCotacao,
    val ETHBTC: MoedaCotacao?, //não encontrada
    val ETHUSD: MoedaCotacao,

    //EUR-outras
    val EURBRL: MoedaCotacao,
    val EURETH: MoedaCotacao?, //não encontrada
    val EURBTC: MoedaCotacao?, //não encontrada
    val EURUSD: MoedaCotacao,

    //USD-outras
    val USDBRL: MoedaCotacao,
    val USDETH: MoedaCotacao?, //não encontrada
    val USDEUR: MoedaCotacao,
    val USDBTC: MoedaCotacao?, //não encontrada

    //BTC-outras
    val BTCBRL: MoedaCotacao,
    val BTCETH: MoedaCotacao?, //não encontrada
    val BTCEUR: MoedaCotacao,
    val BTCUSD: MoedaCotacao
) {

    // Cálculos derivados de outras cotações
    // BRLETH=1/ETHBRL
    val calculoBRLETH: MoedaCotacao?
        get() {
            return ETHBRL?.let {
                val bid = 1 / it.bid
                MoedaCotacao(bid)
            }
        }
    // BRLBTC=1/BTCBRL
    val calculoBRLBTC: MoedaCotacao?
        get() {
            return BTCBRL?.let {
                val bid = 1 / it.bid
                MoedaCotacao(bid)
            }
        }

    // ETHBTC=ETHUSD/BTCUSD
    val calculoETHBTC: MoedaCotacao?
        get() {
            return if (ETHUSD != null && BTCUSD != null) {
            val bid = ETHUSD.bid / BTCUSD.bid
            MoedaCotacao(bid)
        } else null
        }

    // EURETH=1/ETHEUR
    val calculoEURETH: MoedaCotacao?
        get() {
            return ETHEUR?.let {
                val bid = 1 / it.bid
                MoedaCotacao(bid)
            }
        }

    // EURBTC=1/BTCEUR
    val calculoEURBTC: MoedaCotacao?
        get() {
            return BTCEUR?.let {
                val bid = 1 / it.bid
                MoedaCotacao(bid)
            }
        }
    // USDETH=1/ETHUSD
    val calculoUSDETH: MoedaCotacao?
        get() {
            return ETHUSD?.let {
                val bid = 1 / it.bid
                MoedaCotacao(bid)
            }
        }

    // USDBTC=1/BTCUSD
    val calculoUSDBTC: MoedaCotacao?
        get() {
            return BTCUSD?.let {
                val bid = 1 / it.bid
                MoedaCotacao(bid)
            }
        }

    // BTCETH=BTCUSD/ETHUSCD
    val calculoBTCETH: MoedaCotacao?
        get() {
            return if (ETHUSD != null && BTCUSD != null) {
                val bid = BTCUSD.bid / ETHUSD.bid
                MoedaCotacao(bid)
            } else null
        }
}

data class MoedaCotacao(
    val bid: Double
)

