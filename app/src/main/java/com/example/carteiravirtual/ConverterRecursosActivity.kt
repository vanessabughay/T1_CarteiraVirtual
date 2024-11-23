package com.example.carteiravirtual

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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
            val origem = etOrigem.text.toString().uppercase() // Garante o formato correto, ex: "BRL"
            val destino = etDestino.text.toString().uppercase()
            val valor = etValor.text.toString().toDoubleOrNull()

            if (valor == null || valor <= 0) {
                tvResultado.text = "Digite um valor válido!"
                return@setOnClickListener
            }

            val saldoOrigem = dbHelper.buscarSaldo(origem)
            if (saldoOrigem < valor) {
                tvResultado.text = "Saldo insuficiente em $origem"
                return@setOnClickListener
            }

            // Simulação de conversão. Substitua com chamada à API AwesomeAPI
            val taxaConversao = obterTaxaConversao(origem, destino)
            if (taxaConversao == null) {
                tvResultado.text = "Erro ao obter a taxa de conversão."
                return@setOnClickListener
            }

            val valorConvertido = valor * taxaConversao
            dbHelper.salvarSaldo(origem, saldoOrigem - valor)

            val saldoDestino = dbHelper.buscarSaldo(destino)
            dbHelper.salvarSaldo(destino, saldoDestino + valorConvertido)

            tvResultado.text = "Convertido: %.2f $destino".format(valorConvertido)
        }
    }

    private fun obterTaxaConversao(origem: String, destino: String): Double? {
        // Simulação. Aqui você faria uma chamada para a API AwesomeAPI para obter a taxa real.
        // Exemplo: Retorna 5.0 como taxa fixa. Substituir por lógica da API.
        return if (origem != destino) 5.0 else 1.0
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ConverterRecursosActivity::class.java)
            context.startActivity(intent)
        }
    }
}