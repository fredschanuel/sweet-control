package com.example.sweetcontrol.ui.components

import java.util.Locale

fun formatData(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}