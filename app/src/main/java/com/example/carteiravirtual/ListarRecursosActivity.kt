package com.example.carteiravirtual

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ListarRecursosActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_recursos)

        dbHelper = DBHelper(this)

        val tvRecursos: TextView = findViewById(R.id.tvRecursos)
        val saldos = dbHelper.buscarTodosSaldos()

        tvRecursos.text = saldos.joinToString("\n") { "${it.first}: ${it.second}" }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ListarRecursosActivity::class.java)
            context.startActivity(intent)
        }
    }
}