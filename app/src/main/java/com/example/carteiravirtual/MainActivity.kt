package com.example.carteiravirtual

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var tvSaldo: TextView

    // Definição das constantes para o request code
    private val REQUEST_CODE_DEPOSITO = 1
    private val REQUEST_CODE_CONVERSAO = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)
        tvSaldo = findViewById(R.id.tvSaldo)

        // Exibe o saldo atual
        exibirSaldo()

        // Botão para realizar o depósito
        findViewById<Button>(R.id.btnDepositar).setOnClickListener {
            val intent = Intent(this, DepositarActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_DEPOSITO)
        }

        // Botão para listar recursos
        findViewById<Button>(R.id.btnListarRecursos).setOnClickListener {
            val intent = Intent(this, ListarRecursosActivity::class.java)
            startActivity(intent)
        }

        // Botão para realizar a conversão
        findViewById<Button>(R.id.btnComprar).setOnClickListener {
            val intent = Intent(this, ConverterRecursosActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CONVERSAO)
        }
    }

    private fun exibirSaldo() {
        val saldo = dbHelper.buscarSaldo("BRL") // "BRL" é o Real
        tvSaldo.text = "Saldo em R$: ${formatarSaldo(saldo)}"
    }

    // Captura o resultado da atividade de depósito ou conversão
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val novoSaldo = data.getDoubleExtra("novoSaldo", 0.0)

            // Atualiza o saldo exibido na MainActivity
            tvSaldo.text = "Saldo em R$: ${formatarSaldo(novoSaldo)}"
        }
    }
    // Método para formatar o saldo em reais
    private fun formatarSaldo(valor: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val decimalFormat = DecimalFormat("#,##0.00", symbols)
        return decimalFormat.format(valor)
    }

}
