package com.example.sweetcontrol.ui.vendas

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
import com.example.sweetcontrol.models.ItemVenda
import com.example.sweetcontrol.models.Venda
import com.example.sweetcontrol.ui.components.convertToReal
import com.google.firebase.database.DatabaseReference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaVendas(database: DatabaseReference, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var cliente by remember { mutableStateOf("") }
    var produtoSelecionado by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("") }
    var valorUnitario by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var itensVenda by remember { mutableStateOf<List<ItemVenda>>(emptyList()) }
    var vendas by remember { mutableStateOf<List<Venda>>(emptyList()) }
    var showHistory by remember { mutableStateOf(false) } // << NOVO: Controla exibição do histórico

    val produtos = listOf("Cascão 500g", "Cascão 1kg", "Mole 500g", "Mole 1kg")

    LaunchedEffect(Unit) {
        database.child("vendas").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val lista = mutableListOf<Venda>()
                for (item in snapshot.children) {
                    val id = item.key ?: continue
                    val cli = item.child("cliente").getValue(String::class.java) ?: ""
                    val total = item.child("valorTotal").getValue(Double::class.java) ?: 0.0
                    val data = item.child("data").getValue(Long::class.java) ?: 0L

                    val itens = mutableListOf<ItemVenda>()
                    for (itemVenda in item.child("itens").children) {
                        val prod = itemVenda.child("produto").getValue(String::class.java) ?: ""
                        val qtd = itemVenda.child("quantidade").getValue(Int::class.java) ?: 0
                        val valor = itemVenda.child("valorUnitario").getValue(Double::class.java) ?: 0.0
                        itens.add(ItemVenda(prod, qtd, valor))
                    }

                    lista.add(Venda(id, cli, itens, data, total))
                }
                vendas = lista.sortedByDescending { it.data } // Ordena por data (mais recente primeiro)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(context, "Erro ao carregar vendas: ${error.message}", Toast.LENGTH_SHORT).show()
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
                if (showHistory) "Histórico de Vendas" else "Nova Venda",
                style = MaterialTheme.typography.titleLarge
            )

            IconButton(onClick = { showHistory = !showHistory }) {
                Icon(
                    if (showHistory) Icons.Default.Add else Icons.Default.Info,
                    contentDescription = if (showHistory) "Nova venda" else "Histórico"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showHistory) {
            if (vendas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma venda registrada")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(vendas) { venda ->
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
                                    Text(venda.cliente, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        venda.valorTotal.convertToReal(),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                Text(
                                    formatData(venda.data),
                                    style = MaterialTheme.typography.bodySmall
                                )

                                if (expandedItem) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Column {
                                        venda.itens.forEach { item ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("${item.quantidade}x ${item.produto}")
                                                Text((item.quantidade * item.valorUnitario).convertToReal())
                                            }
                                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = cliente,
                onValueChange = { cliente = it },
                label = { Text("Cliente") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box {
                OutlinedTextField(
                    value = produtoSelecionado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Produto") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Selecionar produto")
                        }
                    }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    produtos.forEach { produto ->
                        DropdownMenuItem(
                            text = { Text(produto) },
                            onClick = {
                                produtoSelecionado = produto
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { quantidade = it },
                    label = { Text("Quantidade") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = valorUnitario,
                    onValueChange = { valorUnitario = it },
                    label = { Text("Valor Unitário") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("R$ 0,00") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val qtd = quantidade.toIntOrNull()
                    val cleanValue = valorUnitario.replace(Regex("[^0-9]"), "")
                    val valor = if (cleanValue.isNotEmpty()) cleanValue.toDouble() / 100 else 0.0

                    if (qtd != null && valor > 0 && produtoSelecionado.isNotEmpty()) {
                        itensVenda = itensVenda + ItemVenda(produtoSelecionado, qtd, valor)
                        quantidade = ""
                        valorUnitario = ""
                        produtoSelecionado = ""
                    } else {
                        Toast.makeText(context, "Preencha todos os campos corretamente", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adicionar Item")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Itens da Venda", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(itensVenda) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(item.produto)
                            Text("${item.quantidade} x ${item.valorUnitario.convertToReal()}")
                        }
                        Text((item.quantidade * item.valorUnitario).convertToReal())
                    }
                    Divider()
                }
            }

            val totalVenda = itensVenda.sumOf { it.quantidade * it.valorUnitario }
            Text(
                text = "Total: ${totalVenda.convertToReal()}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (cliente.isNotEmpty() && itensVenda.isNotEmpty()) {
                        val id = database.child("vendas").push().key ?: return@Button
                        val venda = Venda(
                            id = id,
                            cliente = cliente,
                            itens = itensVenda,
                            valorTotal = totalVenda
                        )

                        database.child("vendas").child(id).setValue(venda)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Venda registrada!", Toast.LENGTH_SHORT).show()
                                cliente = ""
                                itensVenda = emptyList()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Erro ao registrar venda", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Adicione itens e informe o cliente", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finalizar Venda")
            }
        }
    }
}