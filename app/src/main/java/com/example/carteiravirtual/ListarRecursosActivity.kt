package com.example.carteiravirtual

import android.os.Bundle
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
    }

    // Metodo para listar todos os recursos financeiros
    private fun listarRecursos() {
        val recursos = dbHelper.getAllResources() // Supondo que você tenha um método que retorna todos os recursos

        // Para cada recurso, cria-se um TextView dinamicamente
        for (recurso in recursos) {
            val textView = TextView(this)
            val saldoFormatado = formatarSaldo(recurso.nome, recurso.valor)
            textView.text = "Moeda: ${recurso.nome} - Saldo: $saldoFormatado"
            layoutRecursos.addView(textView)  // Adiciona o TextView ao LinearLayout
        }
    }

    // Método para formatar o saldo com base na moeda
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
