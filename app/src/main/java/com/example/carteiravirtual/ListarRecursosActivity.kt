package com.example.carteiravirtual

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class ListarRecursosActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var layoutRecursos: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_recursos)

        dbHelper = DBHelper(this)
        layoutRecursos = findViewById(R.id.layoutRecursos)

        listarRecursos()

        val btnVoltar2: Button = findViewById(R.id.btnVoltar2)
        btnVoltar2.setOnClickListener {
            finish()
        }
    }

    private val moedaNomes = mapOf(
        "BRL" to "BRL - Real Brasileiro: R$",
        "USD" to "USD - Dólar Americano: US$",
        "EUR" to "EUR - Euro: €",
        "BTC" to "BTC - Bitcoin: ",
        "ETH" to "ETH - Ethereum: "
    )

    // Metodo para listar todos os recursos financeiros
    private fun listarRecursos() {
        val recursos = dbHelper.getAllResources()

        // Para cada recurso, cria-se um TextView dinamicamente
        for (recurso in recursos) {
            val textView = TextView(this)
            val nomeMoedaCompleto = formatarNomeMoeda(recurso.nome)
            val saldoFormatado = formatarSaldo(recurso.nome, recurso.valor)
            textView.text = "$nomeMoedaCompleto $saldoFormatado"
            layoutRecursos.addView(textView)  // Adiciona o TextView ao LinearLayout
        }
    }
    // Metodo para formatar o nome da moeda
    private fun formatarNomeMoeda(codigoMoeda: String): String {
        val nome = moedaNomes[codigoMoeda] ?: "Moeda Desconhecida"
        return "$nome"
    }

    // Metodo para formatar o saldo com base na moeda
    private fun formatarSaldo(moeda: String, valor: Double): String {
        val casasDecimais = when (moeda) {
            "BRL", "USD", "EUR" -> 2
            "ETH", "BTC" -> 8
            else -> 2 // Padrão: 2 casas decimais
        }

        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }

        val pattern = "#,##0.${"0".repeat(casasDecimais)}"
        val decimalFormat = DecimalFormat(pattern, symbols)

        return decimalFormat.format(valor)
    }

}
