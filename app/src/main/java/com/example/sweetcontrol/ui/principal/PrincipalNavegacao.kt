package com.example.sweetcontrol.ui.principal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sweetcontrol.ui.dash.TelaDashboard
import com.example.sweetcontrol.ui.materia.TelaMateriaPrima
import com.example.sweetcontrol.ui.producao.TelaProducao
import com.example.sweetcontrol.ui.vendas.TelaVendas
import com.google.firebase.database.DatabaseReference

@Composable
fun PrincipalNavegacao(
    database: DatabaseReference,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            TelaDashboard(
                //database = database,
                onNavigateToProduction = { navController.navigate("producao") },
                onNavigateToSales = { navController.navigate("vendas") },
                onNavigateToMaterials = { navController.navigate("materiaprima") }
            )
        }
        composable("producao") {
            TelaProducao(database)
        }
        composable("vendas") {
            TelaVendas(database)
        }
        composable("materiaprima") {
            TelaMateriaPrima(database)
        }
    }
}