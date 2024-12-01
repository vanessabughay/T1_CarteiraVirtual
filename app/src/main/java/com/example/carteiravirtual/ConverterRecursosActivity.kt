package com.example.carteiravirtual

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConverterRecursosActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    // Variáveis para os componentes da interface
    private lateinit var comboBoxOrigem: Spinner
    private lateinit var comboBoxDestino: Spinner
    private lateinit var etValor: EditText
    private lateinit var btnConverter: Button
    private lateinit var tvResultado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter_recursos)

        dbHelper = DBHelper(this)

        // Inicializando os componentes da interface
        comboBoxOrigem = findViewById(R.id.comboBoxOrigem)
        comboBoxDestino = findViewById(R.id.comboBoxDestino)
        etValor = findViewById(R.id.etValor)
        btnConverter = findViewById(R.id.btnConverter)
        tvResultado = findViewById(R.id.tvResultado)

        // Configurando os Spinners com as moedas disponíveis
        ArrayAdapter.createFromResource(
            this,
            R.array.moedas_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            comboBoxOrigem.adapter = adapter
            comboBoxDestino.adapter = adapter
        }

        // Ação do botão de conversão
        btnConverter.setOnClickListener {
            val origem = comboBoxOrigem.selectedItem.toString()
            val destino = comboBoxDestino.selectedItem.toString()
            val valor = etValor.text.toString().toDoubleOrNull()

            if (valor == null || valor <= 0) {
                Toast.makeText(this, "Digite um valor válido para a conversão!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificando saldo antes de tentar converter
            val saldoOrigem = dbHelper.buscarSaldo(origem)
            if (saldoOrigem >= valor) {
                // Inicia a conversão em uma corrotina
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val cotacao = obterCotacao(origem, destino)
                        if (cotacao != null) {
                            val valorConvertido = valor * cotacao

                            // Atualiza os saldos após a conversão
                            dbHelper.salvarSaldo(origem, saldoOrigem - valor)
                            val saldoDestino = dbHelper.buscarSaldo(destino)
                            dbHelper.salvarSaldo(destino, saldoDestino + valorConvertido)

                            // Exibe o resultado da conversão
                            tvResultado.text = "Valor convertido: %.2f $destino".format(valorConvertido)

                            // Retorna o saldo atualizado para a MainActivity
                            setResult(RESULT_OK, intent.putExtra("novoSaldo", dbHelper.buscarSaldo("BRL")))
                        } else {
                            tvResultado.text = "Erro ao obter cotação. Tente novamente."
                        }
                    } catch (e: Exception) {
                        tvResultado.text = "Erro na conversão: ${e.message}"
                    }
                }
            } else {
                tvResultado.text = "Saldo insuficiente na moeda de origem."
            }
        }
    }

    // Função para obter a cotação da API
    private suspend fun obterCotacao(origem: String, destino: String): Double? {
        return try {
            val moedas = "$origem$destino"  // Exemplo: "BRLUSD"

            // Lista de cotações específicas que usam funções separadas
            val cotacoesEspecificas = listOf(
                "BRLETH", "BRLBTC", "ETHBTC", "EURETH",
                "EURBTC", "USDETH", "USDBTC", "BTCETH"
            )

            if (moedas in cotacoesEspecificas) {
                // Chamar a função específica para a cotação
                when (moedas) {
                    "BRLETH" -> calculoBRLETH()
                    "BRLBTC" -> calculoBRLBTC()
                    "ETHBTC" -> calculoETHBTC()
                    "EURETH" -> calculoEURETH()
                    "EURBTC" -> calculoEURBTC()
                    "USDETH" -> calculoUSDETH()
                    "USDBTC" -> calculoUSDBTC()
                    "BTCETH" -> calculoBTCETH()
                    else -> null
                }
            } else {
                // Obter cotação de forma dinâmica para outras combinações
                val resposta = ApiClient.awesomeAPI.getCotacao("$origem-$destino")
                resposta?.let {
                    val property = it::class.members.firstOrNull { member -> member.name == moedas }
                    val cotacao = property?.call(it) as? MoedaCotacao
                    cotacao?.bid
                }
            }
        } catch (e: Exception) {
            null  // Retorna null em caso de erro
        }
    }

    private suspend fun calculoBRLETH(): Double? {
        return try {
            // Buscar a cotação de ETH para BRL
            val resposta = ApiClient.awesomeAPI.getCotacao("ETH-BRL")

            resposta?.let {
                val cotacao = it.ETHBRL?.bid // Obtém o valor do bid (preço de venda)
                cotacao?.let { valor ->
                    1 / valor // Retorna o inverso do câmbio
                }
            }
        } catch (e: Exception) {
            null // Retorna null em caso de erro
        }
    }

    private suspend fun calculoBRLBTC(): Double? {
        return try {
            // Buscar a cotação de BTC para BRL
            val resposta = ApiClient.awesomeAPI.getCotacao("BTC-BRL")

            resposta?.let {
                val cotacao = it.BTCBRL?.bid // Obtém o valor do bid (preço de venda)
                cotacao?.let { valor ->
                    1 / valor // Retorna o inverso do câmbio
                }
            }
        } catch (e: Exception) {
            null // Retorna null em caso de erro
        }
    }

    private suspend fun calculoETHBTC(): Double? {
        return try {
            // Buscar as cotações ETH-USD e BTC-USD
            val respostaETHUSD = ApiClient.awesomeAPI.getCotacao("ETH-USD")
            val respostaBTCUSD = ApiClient.awesomeAPI.getCotacao("BTC-USD")

            val cotacaoETHUSD = respostaETHUSD?.ETHUSD?.bid // Cotação de ETH para USD
            val cotacaoBTCUSD = respostaBTCUSD?.BTCUSD?.bid // Cotação de BTC para USD

            // Verifica se ambas as cotações são válidas e calcula ETHBTC como ETHUSD / BTCUSD
            if (cotacaoETHUSD != null && cotacaoBTCUSD != null && cotacaoBTCUSD != 0.0) {
                cotacaoETHUSD / cotacaoBTCUSD
            } else {
                null // Retorna null se houver problema com as cotações
            }
        } catch (e: Exception) {
            null // Retorna null em caso de erro
        }
    }


    private suspend fun calculoEURETH(): Double? {
        return try {
            // Buscar a cotação de BTC para BRL
            val resposta = ApiClient.awesomeAPI.getCotacao("ETH-EUR")

            resposta?.let {
                val cotacao = it.ETHEUR?.bid // Obtém o valor do bid (preço de venda)
                cotacao?.let { valor ->
                    1 / valor // Retorna o inverso do câmbio
                }
            }
        } catch (e: Exception) {
            null // Retorna null em caso de erro
        }
    }

    private suspend fun calculoEURBTC(): Double? {
        return try {
            // Buscar a cotação de BTC para BRL
            val resposta = ApiClient.awesomeAPI.getCotacao("BTC-EUR")

            resposta?.let {
                val cotacao = it.BTCEUR?.bid // Obtém o valor do bid (preço de venda)
                cotacao?.let { valor ->
                    1 / valor // Retorna o inverso do câmbio
                }
            }
        } catch (e: Exception) {
            null // Retorna null em caso de erro
        }
    }

    private suspend fun calculoUSDETH(): Double? {
        return try {
            // Buscar a cotação de BTC para BRL
            val resposta = ApiClient.awesomeAPI.getCotacao("USD-ETH")

            resposta?.let {
                val cotacao = it.ETHUSD?.bid // Obtém o valor do bid (preço de venda)
                cotacao?.let { valor ->
                    1 / valor // Retorna o inverso do câmbio
                }
            }
        } catch (e: Exception) {
            null // Retorna null em caso de erro
        }
    }

    private suspend fun calculoUSDBTC(): Double? {
        return try {
            // Buscar a cotação de BTC para BRL
            val resposta = ApiClient.awesomeAPI.getCotacao("BTC-USC")

            resposta?.let {
                val cotacao = it.BTCUSD?.bid // Obtém o valor do bid (preço de venda)
                cotacao?.let { valor ->
                    1 / valor // Retorna o inverso do câmbio
                }
            }
        } catch (e: Exception) {
            null // Retorna null em caso de erro
        }
    }

    private suspend fun calculoBTCETH(): Double? {
        return try {
            // Buscar as cotações ETH-USD e BTC-USD
            val respostaETHUSD = ApiClient.awesomeAPI.getCotacao("ETH-USD")
            val respostaBTCUSD = ApiClient.awesomeAPI.getCotacao("BTC-USD")

            val cotacaoETHUSD = respostaETHUSD?.ETHUSD?.bid // Cotação de ETH para USD
            val cotacaoBTCUSD = respostaBTCUSD?.BTCUSD?.bid // Cotação de BTC para USD

            // Verifica se ambas as cotações são válidas e calcula ETHBTC como ETHUSD / BTCUSD
            if (cotacaoETHUSD != null && cotacaoBTCUSD != null && cotacaoBTCUSD != 0.0) {
                cotacaoBTCUSD / cotacaoETHUSD
            } else {
                null // Retorna null se houver problema com as cotações
            }
        } catch (e: Exception) {
            null // Retorna null em caso de erro
        }
    }


}
