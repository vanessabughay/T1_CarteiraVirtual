package com.example.carteiravirtual

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import com.example.carteiravirtual.R.id.btnConverter
//import androidx.compose.ui.semantics.text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class ConverterRecursosActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    // Variáveis para os componentes da interface
    private lateinit var comboBoxOrigem: Spinner
    private lateinit var comboBoxDestino: Spinner
    private lateinit var etValor: EditText
    private lateinit var btnComprar: Button
    private lateinit var btnConverter: Button
    private lateinit var tvResultado: TextView
    private lateinit var tvResultadoCompra: TextView
    private lateinit var progressBar: ProgressBar

    private val moedaMap = mapOf(
        "BRL - Real Brasileiro" to "BRL",
        "USD - Dólar Americano" to "USD",
        "EUR - Euro" to "EUR",
        "ETH - Ethereum" to "ETH",
        "BTC - Bitcoin" to "BTC"
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter_recursos)

        dbHelper = DBHelper(this)

        // Inicializando os componentes da interface
        comboBoxOrigem = findViewById(R.id.comboBoxOrigem)
        comboBoxDestino = findViewById(R.id.comboBoxDestino)
        etValor = findViewById(R.id.etValor)
        btnConverter = findViewById(R.id.btnConverter)
        btnComprar = findViewById(R.id.btnComprar)
        tvResultado = findViewById(R.id.tvResultado)
        tvResultadoCompra= findViewById(R.id.ResultadoCompra)
        progressBar = findViewById(R.id.progressBar)


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

        // Ação do botão de Converter
        btnConverter.setOnClickListener {

            val origem =
                moedaMap[comboBoxOrigem.selectedItem.toString()] ?: return@setOnClickListener
            val destino =
                moedaMap[comboBoxDestino.selectedItem.toString()] ?: return@setOnClickListener


            val numValor = obterDoubleDoInput(etValor, origem)
            val valor = numValor.toString().toDoubleOrNull()

            // Verifica se o valor é válido antes de proceder
            if (valor == null || valor <= 0) {
                Toast.makeText(this, "Digite um valor válido para a conversão!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Exibir ProgressBar
            progressBar.visibility = View.VISIBLE


            // Inicia a conversão em uma corrotina
            CoroutineScope(Dispatchers.Main).launch {
                val startTime = System.currentTimeMillis()

                try {
                    val cotacao = obterCotacao(origem, destino)
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val delayTime = maxOf(0, 2000 - elapsedTime)

                    // Aguarde o tempo restante
                    kotlinx.coroutines.delay(delayTime)
                    progressBar.visibility = View.GONE
                    if (cotacao != null) {
                        val valorConvertido = valor * cotacao

                        // Exibe o resultado da conversão
                        tvResultado.text =
                            "Valor convertido: ${valorConvertido.formatarMoeda(destino)} $destino"

                    } else {
                        tvResultado.text = "Erro ao obter cotação. Tente novamente."
                    }
                } catch (e: Exception) {
                    progressBar.visibility = View.GONE
                    tvResultado.text = "Erro na conversão: ${e.message}"
                }
            }
        }

        // botão voltar
        val btnVoltar3: Button = findViewById(R.id.btnVoltar3)
        btnVoltar3.setOnClickListener {
            finish()
        }


        // Ação do botão de Comprar
        btnComprar.setOnClickListener {

            val origem =
                moedaMap[comboBoxOrigem.selectedItem.toString()] ?: return@setOnClickListener
            val destino =
                moedaMap[comboBoxDestino.selectedItem.toString()] ?: return@setOnClickListener
            val numValor = obterDoubleDoInput(etValor, origem)
            val valor = numValor.toString().toDoubleOrNull()

            tvResultadoCompra.text = ""

            // Verifica se o valor é válido antes de proceder
            if (valor == null || valor <= 0) {
                Toast.makeText(this, "Digite um valor válido para a conversão!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Exibir ProgressBar
            progressBar.visibility = View.VISIBLE

            // Verificando saldo antes de tentar COMPRAR
            val saldoOrigem = dbHelper.buscarSaldo(origem)
            if (saldoOrigem >= valor) {
                // Inicia a conversão e COMRPA em uma corrotina
                CoroutineScope(Dispatchers.Main).launch {
                    val startTime = System.currentTimeMillis()
                    try {
                        val cotacao = obterCotacao(origem, destino)
                        val elapsedTime = System.currentTimeMillis() - startTime
                        val delayTime = maxOf(0, 2000 - elapsedTime)
                        kotlinx.coroutines.delay(delayTime)
                        progressBar.visibility = View.GONE
                        if (cotacao != null) {
                            val valorConvertido = valor * cotacao

                            // Atualiza os saldos após a conversão

                            dbHelper.salvarSaldo(origem, saldoOrigem - valor)
                            val saldoDestino = dbHelper.buscarSaldo(destino)
                            dbHelper.salvarSaldo(destino, saldoDestino + valorConvertido)

                            // Atualiza Saldo
                            val textViewSaldo1 = findViewById<TextView>(R.id.tvSaldo1)
                            val saldo1 = dbHelper.buscarSaldo(origem)
                            textViewSaldo1.text =
                                "Saldo: ${saldo1.formatarMoeda(origem)} $origem" //$valorSelecionado1"

                            val textViewSaldo2 = findViewById<TextView>(R.id.tvSaldo2)
                            val saldo2 = dbHelper.buscarSaldo(destino)
                            textViewSaldo2.text =
                                "Saldo: ${saldo2.formatarMoeda(destino)} $destino" //$valorSelecionado1"

                            // Retorna o saldo atualizado para a MainActivity
                            setResult(
                                RESULT_OK,
                                intent.putExtra("novoSaldo", dbHelper.buscarSaldo("BRL"))
                            )
                            tvResultadoCompra.setTextColor(getColor(R.color.text_color))
                            tvResultadoCompra.text = "Compra realizada com sucesso!"

                        } else {
                            tvResultadoCompra.text = "Erro ao obter cotação. Tente novamente."
                        }
                    } catch (e: Exception) {
                        tvResultadoCompra.text = "Erro na conversão: ${e.message}"
                    }
                }
            } else {
                progressBar.visibility = View.GONE
                tvResultadoCompra.setTextColor(getColor(R.color.error_color))
                tvResultadoCompra.text = "Saldo insuficiente na moeda de origem."
            }
        }


        //saldo 1 campo de origem
        comboBoxOrigem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            val textViewSaldo1 = findViewById<TextView>(R.id.tvSaldo1)

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val valorSelecionado1 = parent?.getItemAtPosition(position).toString()

                // Verifica se a opção "Selecionar valor" está selecionada
                if (valorSelecionado1 == "(selecionar moeda)") {
                    textViewSaldo1.text = "-"
                } else {
                    val valorSelecionado1 =
                        moedaMap[parent?.getItemAtPosition(position).toString()] ?: return
                    val saldo1 = dbHelper.buscarSaldo(valorSelecionado1)
                    textViewSaldo1.text =
                        "Saldo: ${saldo1.formatarMoeda(valorSelecionado1)} $valorSelecionado1"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        //saldo 2 campo de destino
        comboBoxDestino.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            val textViewSaldo2 = findViewById<TextView>(R.id.tvSaldo2)

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val valorSelecionado2 = parent?.getItemAtPosition(position).toString()

                // Verifica se a opção "Selecionar valor" está selecionada
                if (valorSelecionado2 == "(selecionar moeda)") {
                    textViewSaldo2.text = "-"
                } else {
                    val valorSelecionado2 =
                        moedaMap[parent?.getItemAtPosition(position).toString()] ?: return
                    val saldo1 = dbHelper.buscarSaldo(valorSelecionado2)
                    textViewSaldo2.text =
                        "Saldo: ${saldo1.formatarMoeda(valorSelecionado2)} $valorSelecionado2"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
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

    // Função de extensão para formatar números conforme a moeda e o formato brasileiro
    fun Double.formatarMoeda(moeda: String): String {
        val formato = NumberFormat.getNumberInstance(Locale("pt", "BR"))
        formato.maximumFractionDigits = when (moeda) {
            "BRL", "USD", "EUR" -> 2
            "ETH", "BTC" -> 8
            else -> 2
        }
        formato.minimumFractionDigits = formato.maximumFractionDigits
        return formato.format(this)
    }


    private fun obterDoubleDoInput(editText: EditText, moeda: String): Double? {
        val texto = editText.text.toString().trim()
        return try {
            if (texto.isNotEmpty()) {
                // Substitui vírgula por ponto para conversão
                val textoNormalizado = texto.replace(',', '.')

                // Define o limite de casas decimais com base na moeda
                val limiteCasasDecimais = when (moeda.uppercase()) {
                    "BRL", "USD", "EUR" -> 2
                    "ETH", "BTC" -> 8
                    else -> throw IllegalArgumentException("Moeda não suportada")
                }

                // Verifica o número de casas decimais
                if (textoNormalizado.contains('.')) {
                    val partes = textoNormalizado.split('.')
                    if (partes.size > 1 && partes[1].length > limiteCasasDecimais) {
                        throw IllegalArgumentException("Número com mais de $limiteCasasDecimais casas decimais para a moeda $moeda")
                    }
                }

                // Converte para Double
                textoNormalizado.toDouble()
            } else {
                null // Retorna null se o campo estiver vazio
            }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            null
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Entrada inválida", Toast.LENGTH_SHORT).show()
            null
        }
    }

}
