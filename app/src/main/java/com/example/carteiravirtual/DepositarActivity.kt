package com.example.carteiravirtual

import android.app.AlertDialog
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

        val btnVoltar1: Button = findViewById(R.id.btnVoltar1)
        btnVoltar1.setOnClickListener {
            finish()
        }

        btnConfirmar.setOnClickListener {
            val numValor = obterDoubleDoInput(etValor)
            if (numValor != null) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Depósito")
                builder.setMessage("Valor depositado: ${String.format("%.2f", numValor)}")
                builder.setPositiveButton("OK") { dialog, which ->
                    val valor = numValor.toString().toDoubleOrNull()

                    if (valor == null || valor <= 0) {
                        Toast.makeText(this, "Digite um valor válido para depositar!", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton // Retorna do setPositiveButton
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
                val dialog = builder.create()
                dialog.show()

            } else {
                Toast.makeText(this, "Entrada inválida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, DepositarActivity::class.java)
            context.startActivity(intent)
        }
    }

    private fun obterDoubleDoInput(editText: EditText): Double? {
        val texto = editText.text.toString().trim()
        return try {
            if (texto.isNotEmpty()) {
                // Substitui vírgula por ponto para conversão
                val textoNormalizado = texto.replace(',', '.')

                // Verifica se há mais de 2 casas decimais
                if (textoNormalizado.contains('.')) {
                    val partes = textoNormalizado.split('.')
                    if (partes.size > 1 && partes[1].length > 2) {
                        throw IllegalArgumentException("Número com mais de 2 casas decimais")
                    }
                }

                // Converte para Double
                textoNormalizado.toDouble()
            } else {
                null // Retorna null se o campo estiver vazio
            }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, "Por favor, insira no máximo 2 casas decimais", Toast.LENGTH_SHORT).show()
            null
        } catch (e: NumberFormatException) {
            null // Retorna null se a entrada for inválida
        }
    }

}
