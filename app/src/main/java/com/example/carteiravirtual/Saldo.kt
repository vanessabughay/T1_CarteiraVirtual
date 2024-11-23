package com.example.carteiravirtual

// @Entity(tablename = "saldo")
data class Saldo(
    @PrimaryKey val moeda: String,
    val valor: Double
)
