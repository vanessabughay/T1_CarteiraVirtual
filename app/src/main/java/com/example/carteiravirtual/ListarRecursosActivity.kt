package com.example.carteiravirtual

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

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

        val btnLimparBanco: Button = findViewById(R.id.btnLimparBanco)
        btnLimparBanco.setOnClickListener {
            limparBanco()
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
            textView.textSize = 16f
            textView.setTextColor(resources.getColor(R.color.text_color))
            layoutRecursos.addView(textView)  // Adiciona o TextView ao LinearLayout
        }
    }
    /*
    // Metodo para limpar o banco
    private fun limparBanco() {
        dbHelper.clearDatabase()
        listarRecursos()
        Toast.makeText(this, "Banco de dados limpo com sucesso!", Toast.LENGTH_SHORT).show()
    }
    */
    private fun limparBanco() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmação")
        builder.setMessage("Tem certeza que deseja limpar o banco de dados?")

        builder.setPositiveButton("OK") { dialog, which ->
            dbHelper.clearDatabase()
            listarRecursos()
            Toast.makeText(this, "Banco de dados limpo com sucesso!", Toast.LENGTH_SHORT).show()
            // retornar a tela inicial
            val intent = Intent(this, MainActivity::class.java) // Substitua MainActivity pela sua tela inicial
            startActivity(intent)
            finish() // Fecha a Activity atual
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
            // Não faz nada, apenas fecha o diálogo
        }

        val dialog = builder.create()
        dialog.show()

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
