package com.example.sweetcontrol.models

data class MateriaPrima(
    val id: String = "",
    val tipo: String = "",
    val quantidade: Int = 0,
    val preco: Double = 0.0,
    val timestamp: Long = 0L
)