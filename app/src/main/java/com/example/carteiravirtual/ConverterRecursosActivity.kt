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
            val resposta = ApiClient.awesomeAPI.getCotacao("$origem-$destino")

            resposta?.let {
                val cotacao = when (moedas) {
                    "BRLETH" -> it.calculoBRLETH
                    "BRLBTC" -> it.calculoBRLBTC
                    "ETHBTC" -> it.calculoETHBTC
                    "EURETH" -> it.calculoEURETH
                    "EURBTC" -> it.calculoEURBTC
                    "USDETH" -> it.calculoUSDETH
                    "USDBTC" -> it.calculoUSDBTC
                    "BTCETH" -> it.calculoBTCETH
                    else -> {
                        // Se não for uma das cotações fixas, buscar dinamicamente
                        it::class.members.firstOrNull { member -> member.name == moedas }
                            ?.call(it) as? MoedaCotacao
                    }
                }

                // Verifica se a cotação foi encontrada
                cotacao?.let {
                    return it.bid  // Retorna o valor da cotação
                } ?: run {
                    null  // Se não encontrar a cotação, retorna null
                }
            }
        } catch (e: Exception) {
            null  // Retorna null em caso de erro
        }
    }
}
