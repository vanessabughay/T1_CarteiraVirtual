package com.example.carteiravirtual

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DepositarActivity : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_depositar)

        dbHelper = DBHelper(this)

        val etValor: EditText = findViewById(R.id.etValor)
        val btnConfirmar: Button = findViewById(R.id.btnConfirmar)

        btnConfirmar.setOnClickListener {
            val valor = etValor.text.toString().toDoubleOrNull()

            if (valor == null || valor <= 0) {
                Toast.makeText(this, "Digite um valor válido para depositar!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Atualiza o saldo do usuário no banco
            val saldoAtual = dbHelper.buscarSaldo("BRL")
            val novoSaldo = saldoAtual + valor
            dbHelper.salvarSaldo("BRL", novoSaldo)

            // Retorna o saldo atualizado para a MainActivity
            val resultIntent = Intent().apply {
                putExtra("novoSaldo", novoSaldo)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, DepositarActivity::class.java)
            context.startActivity(intent)
        }
    }
}
