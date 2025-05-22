package com.example.sweetcontrol.ui.producao

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sweetcontrol.ui.components.formatData
import com.example.sweetcontrol.models.Producao
import com.google.firebase.database.DatabaseReference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaProducao(database: DatabaseReference, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var quantidade by remember { mutableStateOf("") }
    val tipos = listOf("Cascão 500g", "Cascão 1kg", "Mole 500g")
    var tipoSelecionado by remember { mutableStateOf(tipos[0]) }
    var expanded by remember { mutableStateOf(false) }
    var producoes by remember { mutableStateOf(listOf<Producao>()) }
    var showHistory by remember { mutableStateOf(false) }

    val totalProduzido = producoes.sumOf { it.quantidade }

    LaunchedEffect(Unit) {
        database.child("producoes").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val lista = mutableListOf<Producao>()
                for (item in snapshot.children) {
                    val id = item.key ?: continue
                    val qtd = item.child("quantidade").getValue(Int::class.java) ?: 0
                    val tp = item.child("tipo").getValue(String::class.java) ?: ""
                    val ts = item.child("timestamp").getValue(Long::class.java) ?: 0L
                    lista.add(Producao(id, qtd, tp, ts))
                }
                producoes = lista.sortedByDescending { it.timestamp }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(context, "Erro ao carregar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (showHistory) "Histórico de Produção" else "Nova Produção",
                style = MaterialTheme.typography.titleLarge
            )

            IconButton(onClick = { showHistory = !showHistory }) {
                Icon(
                    if (showHistory) Icons.Default.Add else Icons.Default.Info,
                    contentDescription = if (showHistory) "Nova produção" else "Histórico"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showHistory) {
            Column {
                Text(
                    text = "Total Produzido: $totalProduzido unidades",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (producoes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhuma produção registrada")
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(producoes) { producao ->
                            var expandedItem by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = { expandedItem = !expandedItem }
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(producao.tipo, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "${producao.quantidade} un",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }

                                    Text(
                                        formatData(producao.timestamp),
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    if (expandedItem) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Quantidade")
                                                Text("${producao.quantidade}")
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Tipo")
                                                Text(producao.tipo)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text("Tipo de Goiabada")
            Box {
                OutlinedTextField(
                    value = tipoSelecionado,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Selecionar tipo")
                        }
                    }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    tipos.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                tipoSelecionado = tipo
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = quantidade,
                onValueChange = { quantidade = it },
                label = { Text("Quantidade produzida") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val qtd = quantidade.toIntOrNull()
                if (qtd != null) {
                    val id = database.child("producoes").push().key ?: return@Button
                    val dados = mapOf(
                        "quantidade" to qtd,
                        "tipo" to tipoSelecionado,
                        "timestamp" to System.currentTimeMillis()
                    )
                    database.child("producoes").child(id).setValue(dados)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Produção salva!", Toast.LENGTH_SHORT).show()
                            quantidade = ""
                        }
                } else {
                    Toast.makeText(context, "Digite um número válido", Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Salvar Produção")
            }
        }
    }
}