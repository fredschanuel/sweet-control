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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sweetcontrol.ui.components.convertToReal
import com.example.sweetcontrol.ui.components.GraficoLinha
import com.example.sweetcontrol.ui.components.formatData
import com.example.sweetcontrol.ui.components.SummaryCard
import com.example.sweetcontrol.models.Producao
import com.google.firebase.database.DatabaseReference
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDashboard(
    modifier: Modifier = Modifier,
    onNavigateToProduction: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToMaterials: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf("Hoje") }
    var expanded by remember { mutableStateOf(false) }

    val dashboardData by viewModel.dashboardData.observeAsState(DashboardData())
    val recentProductions by viewModel.recentProductions.observeAsState(emptyList())
    //val chartData by viewModel.chartData.observeAsState(ChartData())

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
                                viewModel.updatePeriod(period)
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
                value = "${dashboardData.totalProduction} un",
                onClick = onNavigateToProduction
            )
            SummaryCard(
                icon = Icons.Default.ShoppingCart,
                title = "Vendas",
                value = dashboardData.totalSales.convertToReal(),
                onClick = onNavigateToSales
            )
            SummaryCard(
                icon = Icons.Default.Build,
                title = "Matéria-Prima",
                value = dashboardData.totalMaterials.convertToReal(),
                onClick = onNavigateToMaterials
            )
            SummaryCard(
                icon = Icons.Default.ThumbUp,
                title = "Lucro",
                value = (dashboardData.totalSales - dashboardData.totalMaterials).convertToReal()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        //Text("Produção vs Vendas (últimos 7 dias)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        //GraficoLinha(
            //productionData = chartData.productionData,
            //salesData = chartData.salesData,
            //modifier = Modifier
                //.height(150.dp)
                //.fillMaxWidth()
                //.padding(vertical = 8.dp)
        //)

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