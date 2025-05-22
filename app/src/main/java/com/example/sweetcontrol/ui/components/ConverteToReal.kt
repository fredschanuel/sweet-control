package com.example.sweetcontrol.ui.components

import java.text.NumberFormat
import java.util.Locale

fun Double.convertToReal(): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(this)
}