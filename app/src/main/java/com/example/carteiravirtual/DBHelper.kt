package com.example.carteiravirtual

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// A classe DBHelper é responsável pela manipulação do banco de dados SQLite
class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "CarteiraVirtualDB.db"
        const val TABLE_NAME = "recursos" // Tabela que armazenará os saldos das moedas
    }

    // Método chamado quando o banco de dados é criado pela primeira vez
    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                valor REAL NOT NULL
            );
        """
        db.execSQL(createTableQuery)
    }

    // Método chamado quando a versão do banco de dados é atualizada
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Método para buscar o saldo de uma moeda específica
    fun buscarSaldo(nome: String): Double {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf("valor"),
            "nome = ?",
            arrayOf(nome),
            null,
            null,
            null
        )

        var saldo = 0.0
        if (cursor.moveToFirst()) {
            saldo = cursor.getDouble(cursor.getColumnIndexOrThrow("valor"))
        }
        cursor.close()
        db.close()
        return saldo
    }

    // Método para salvar ou atualizar o saldo de uma moeda
    fun salvarSaldo(nome: String, valor: Double) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("nome", nome)
            put("valor", valor)
        }

        // Verifica se já existe o saldo para a moeda. Se sim, atualiza, senão, insere um novo valor
        val rowsUpdated = db.update(TABLE_NAME, contentValues, "nome = ?", arrayOf(nome))
        if (rowsUpdated == 0) {
            db.insert(TABLE_NAME, null, contentValues)
        }
        db.close()
    }

    // Método para buscar todos os recursos (moedas e seus saldos)
    fun getAllResources(): List<Recurso> {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf("nome", "valor"), null, null, null, null, null)

        val recursos = mutableListOf<Recurso>()
        while (cursor.moveToNext()) {
            val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
            val valor = cursor.getDouble(cursor.getColumnIndexOrThrow("valor"))
            recursos.add(Recurso(nome, valor))
        }
        cursor.close()
        db.close()
        return recursos
    }

    // Método para inicializar as moedas padrões, caso necessário
    fun inicializarMoedas() {
        val moedas = listOf("BRL", "USD", "EUR", "BTC", "ETH")
        val valoresIniciais = mapOf(
            "BRL" to 0.0,
            "USD" to 0.0,
            "EUR" to 0.0,
            "BTC" to 0.0,
            "ETH" to 0.0
        )

        val db = writableDatabase
        for (moeda in moedas) {
            val contentValues = ContentValues().apply {
                put("nome", moeda)
                put("valor", valoresIniciais[moeda] ?: 0.0)
            }
            db.insert(TABLE_NAME, null, contentValues)
        }
        db.close()
    }

    fun clearDatabase() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME")
        db.close()
    }

}

// Classe de dados para armazenar as informações de cada recurso (moeda)
data class Recurso(val nome: String, val valor: Double)
