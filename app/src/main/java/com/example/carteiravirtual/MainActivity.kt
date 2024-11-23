package com.example.carteiravirtual

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)

        val tvSaldo: TextView = findViewById(R.id.tvSaldo)
        val btnDepositar: Button = findViewById(R.id.btnDepositar)
        val btnListarRecursos: Button = findViewById(R.id.btnListarRecursos)
        val btnConverter: Button = findViewById(R.id.btnConverter)

        atualizarSaldo(tvSaldo)

        btnDepositar.setOnClickListener {
            DepositarActivity.start(this)
        }

        btnListarRecursos.setOnClickListener {
            ListarRecursosActivity.start(this)
        }

        btnConverter.setOnClickListener {
            ConverterRecursosActivity.start(this)
        }
    }

    private fun atualizarSaldo(tvSaldo: TextView) {
        val saldoReais = dbHelper.buscarSaldo("BRL")
        tvSaldo.text = "Saldo em R$: %.2f".format(saldoReais)
    }

    override fun onResume() {
        super.onResume()
        atualizarSaldo(findViewById(R.id.tvSaldo))
    }


}