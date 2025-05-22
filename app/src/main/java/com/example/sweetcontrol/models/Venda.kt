package com.example.sweetcontrol.models

data class Venda(
    val id: String = "",
    val cliente: String = "",
    val itens: List<ItemVenda> = emptyList(),
    val data: Long = System.currentTimeMillis(),
    val valorTotal: Double = 0.0
)