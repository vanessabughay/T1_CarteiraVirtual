package com.example.carteiravirtual

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.full.memberProperties


class ConverterRecursosActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter_recursos)

        dbHelper = DBHelper(this)

        val etOrigem: EditText = findViewById(R.id.etOrigem)
        val etDestino: EditText = findViewById(R.id.etDestino)
        val etValor: EditText = findViewById(R.id.etValor)
        val btnConverter: Button = findViewById(R.id.btnConverter)
        val tvResultado: TextView = findViewById(R.id.tvResultado)

        btnConverter.setOnClickListener {
            val origem = etOrigem.text.toString()
            val destino = etDestino.text.toString()
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

                            tvResultado.text = "Valor convertido: %.2f $destino".format(valorConvertido)
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
            val moedas = "$origem-$destino"  // Exemplo: "BRL-USD"
            val resposta = ApiClient.awesomeAPI.getCotacao(moedas)

            // A resposta pode ser aninhada, então é necessário acessar o valor correto.
            resposta?.let {
                val cotacao = it.BRLUSD?.bid
                return cotacao
            //    val cotacao = it.cotações[moedas]
            //    return cotacao?.bid
            }
        } catch (e: Exception) {
            null  // Retorna null em caso de erro
        }
    }

}






