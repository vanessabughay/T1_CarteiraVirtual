package com.example.carteiravirtual

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "FinTechApp.db"
        const val TABLE_BALANCES = "balances"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createBalancesTable = """
            CREATE TABLE $TABLE_BALANCES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                moeda TEXT NOT NULL,
                quantidade REAL NOT NULL
            )
        """
        db.execSQL(createBalancesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BALANCES")
        onCreate(db)
    }

    fun salvarSaldo(moeda: String, quantidade: Double) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("moeda", moeda)
            put("quantidade", quantidade)
        }
        db.insertWithOnConflict(TABLE_BALANCES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun buscarSaldo(moeda: String): Double {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_BALANCES,
            arrayOf("quantidade"),
            "moeda = ?",
            arrayOf(moeda),
            null,
            null,
            null
        )
        val saldo = if (cursor.moveToFirst()) cursor.getDouble(cursor.getColumnIndexOrThrow("quantidade")) else 0.0
        cursor.close()
        db.close()
        return saldo
    }

    fun buscarTodosSaldos(): List<Pair<String, Double>> {
        val db = readableDatabase
        val cursor = db.query(TABLE_BALANCES, arrayOf("moeda", "quantidade"), null, null, null, null, null)

        val saldos = mutableListOf<Pair<String, Double>>()
        while (cursor.moveToNext()) {
            val moeda = cursor.getString(cursor.getColumnIndexOrThrow("moeda"))
            val quantidade = cursor.getDouble(cursor.getColumnIndexOrThrow("quantidade"))
            saldos.add(moeda to quantidade)
        }
        cursor.close()
        db.close()
        return saldos
    }
}