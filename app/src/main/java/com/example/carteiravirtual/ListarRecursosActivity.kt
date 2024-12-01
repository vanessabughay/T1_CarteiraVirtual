package com.example.carteiravirtual

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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

    // Método para listar todos os recursos financeiros
    private fun listarRecursos() {
        val recursos = dbHelper.getAllResources() // Supondo que você tenha um método que retorna todos os recursos

        // Para cada recurso, cria-se um TextView dinamicamente
        for (recurso in recursos) {
            val textView = TextView(this)
            textView.text = "Moeda: ${recurso.nome} - Saldo: ${recurso.valor}"
            layoutRecursos.addView(textView)  // Adiciona o TextView ao LinearLayout
        }
    }
}
