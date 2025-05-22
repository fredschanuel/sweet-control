package com.example.sweetcontrol.ui.materia

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
import androidx.compose.material3.Divider
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
import com.example.sweetcontrol.models.MateriaPrima
import com.example.sweetcontrol.ui.components.convertToReal
import com.google.firebase.database.DatabaseReference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaMateriaPrima(database: DatabaseReference, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tipos = listOf("Goiaba", "Açúcar", "Embalagem", "Adesivo", "Bobina de Plástico", "Durex", "Potinhos")
    var tipoSelecionado by remember { mutableStateOf(tipos[0]) }
    var quantidade by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var entradas by remember { mutableStateOf(listOf<MateriaPrima>()) }
    var showHistory by remember { mutableStateOf(false) }

    val valorTotalGasto = entradas.sumOf { it.quantidade * it.preco }

    LaunchedEffect(Unit) {
        database.child("materia_prima").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val lista = mutableListOf<MateriaPrima>()
                for (item in snapshot.children) {
                    val id = item.key ?: continue
                    val qtd = item.child("quantidade").getValue(Int::class.java) ?: 0
                    val tp = item.child("tipo").getValue(String::class.java) ?: ""
                    val pr = item.child("preco").getValue(Double::class.java) ?: 0.0
                    val ts = item.child("timestamp").getValue(Long::class.java) ?: 0L
                    lista.add(MateriaPrima(id, tp, qtd, pr, ts))
                }
                entradas = lista.sortedByDescending { it.timestamp }
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
                if (showHistory) "Histórico de Matéria-Prima" else "Nova Entrada",
                style = MaterialTheme.typography.titleLarge
            )

            IconButton(onClick = { showHistory = !showHistory }) {
                Icon(
                    if (showHistory) Icons.Default.Add else Icons.Default.Info,
                    contentDescription = if (showHistory) "Nova entrada" else "Histórico"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showHistory) {
            Column {
                Text(
                    text = "Total Gasto: ${valorTotalGasto.convertToReal()}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(entradas) { entrada ->
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
                                    Text(entrada.tipo, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        (entrada.quantidade * entrada.preco).convertToReal(),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                Text(
                                    formatData(entrada.timestamp),
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
                                            Text("${entrada.quantidade}")
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Preço Unitário")
                                            Text(entrada.preco.convertToReal())
                                        }
                                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Total")
                                            Text((entrada.quantidade * entrada.preco).convertToReal())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text("Tipo de Matéria-prima")
            Box {
                OutlinedTextField(
                    value = tipoSelecionado,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
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
                label = { Text("Quantidade") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = preco,
                onValueChange = { preco = it },
                label = { Text("Preço Unitário") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("R$ 0,00") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val totalEntrada = remember(quantidade, preco) {
                val qtd = quantidade.toIntOrNull() ?: 0
                val prc = preco.toDoubleOrNull() ?: 0.0
                qtd * prc
            }

            Text(
                text = "Total: ${totalEntrada.convertToReal()}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val qtd = quantidade.toIntOrNull()
                val prc = preco.toDoubleOrNull()

                if (qtd != null && prc != null) {
                    val id = database.child("materia_prima").push().key ?: return@Button
                    val dados = mapOf(
                        "tipo" to tipoSelecionado,
                        "quantidade" to qtd,
                        "preco" to prc,
                        "timestamp" to System.currentTimeMillis()
                    )
                    database.child("materia_prima").child(id).setValue(dados)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Entrada salva!", Toast.LENGTH_SHORT).show()
                            quantidade = ""
                            preco = ""
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Erro ao salvar: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Digite valores válidos", Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Salvar Entrada")
            }
        }
    }
}