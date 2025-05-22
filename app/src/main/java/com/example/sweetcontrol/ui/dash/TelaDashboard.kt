package com.example.sweetcontrol.ui.dash

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sweetcontrol.ui.components.convertToReal
import com.example.sweetcontrol.ui.components.GraficoLinha
import com.example.sweetcontrol.ui.components.formatData
import com.example.sweetcontrol.ui.components.SummaryCard
import com.example.sweetcontrol.models.Producao
import com.google.firebase.database.DatabaseReference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDashboard(
    database: DatabaseReference,
    modifier: Modifier = Modifier,
    onNavigateToProduction: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToMaterials: () -> Unit
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf("Hoje") }
    var expanded by remember { mutableStateOf(false) }

    val totalProduction by remember { mutableStateOf(125) }
    val totalSales by remember { mutableStateOf(1200.0) }
    val totalMaterials by remember { mutableStateOf(580.0) }
    val profit by remember { mutableStateOf(totalSales - totalMaterials) }

    val recentProductions = remember {
        listOf(
            Producao(quantidade = 20, tipo = "Cascão 500g", timestamp = System.currentTimeMillis()),
            Producao(quantidade = 15, tipo = "Mole 1kg", timestamp = System.currentTimeMillis() - 86400000)
        )
    }

    Column(modifier = modifier.padding(16.dp)) {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dashboard", style = MaterialTheme.typography.titleLarge)

            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(selectedPeriod)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Selecionar período")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("Hoje", "Semana", "Mês").forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period) },
                            onClick = {
                                selectedPeriod = period
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                icon = Icons.Default.ThumbUp,
                title = "Produção",
                value = "$totalProduction un",
                onClick = onNavigateToProduction
            )
            SummaryCard(
                icon = Icons.Default.ShoppingCart,
                title = "Vendas",
                value = totalSales.convertToReal(),
                onClick = onNavigateToSales
            )
            SummaryCard(
                icon = Icons.Default.Build,
                title = "Matéria-Prima",
                value = totalMaterials.convertToReal(),
                onClick = onNavigateToMaterials
            )
            SummaryCard(
                icon = Icons.Default.ThumbUp,
                title = "Lucro",
                value = profit.convertToReal()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Produção vs Vendas (últimos 7 dias)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        GraficoLinha(
            productionData = listOf(20, 25, 30, 25, 20, 15, 10),
            salesData = listOf(15, 20, 25, 30, 25, 20, 15),
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Últimas Produções", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            recentProductions.forEach { producao ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${producao.quantidade}x ${producao.tipo}")
                    Text(formatData(producao.timestamp), style = MaterialTheme.typography.bodySmall)
                }
                Divider()
            }
        }
    }
}




