package com.example.sweetcontrol.ui.dash

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import com.example.sweetcontrol.models.ItemVenda
import com.example.sweetcontrol.models.Producao
import java.lang.reflect.Array.set
import java.util.Calendar
import java.util.Date

class DashboardViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()

    private val _dashboardData = MutableLiveData(DashboardData())
    val dashboardData: LiveData<DashboardData> = _dashboardData

    private val _recentProductions = MutableLiveData<List<Producao>>(emptyList())
    val recentProductions: LiveData<List<Producao>> = _recentProductions

    private var currentPeriod = "Hoje"

    init {
        loadAllData()
    }

    fun updatePeriod(period: String) {
        currentPeriod = period
        loadAllData()
    }

    private fun loadAllData() {
        val (startTime, endTime) = when (currentPeriod) {
            "Hoje" -> getTodayRange()
            "Semana" -> getWeekRange()
            "Mês" -> getMonthRange()
            else -> getTodayRange()
        }

        loadProductionData(startTime, endTime)
        loadSalesData(startTime, endTime)
        loadMaterialsData(startTime, endTime)
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    private fun getWeekRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    private fun getMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    private fun loadProductionData(startTime: Long, endTime: Long) {
        database.getReference("producoes")
            .orderByChild("timestamp")
            .startAt(startTime.toDouble())
            .endAt(endTime.toDouble())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productions = mutableListOf<Producao>()
                    var total = 0

                    for (child in snapshot.children) {
                        val producao = child.getValue(Producao::class.java)
                        producao?.let {
                            productions.add(it)
                            total += it.quantidade
                        }
                    }

                    _recentProductions.value = productions.sortedByDescending { it.timestamp }
                    _dashboardData.value = _dashboardData.value?.copy(totalProduction = total)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DashboardVM", "Erro ao carregar produção: ${error.message}")
                }
            })
    }

    private fun loadSalesData(startTime: Long, endTime: Long) {
        Log.d("SalesDebug", "Iniciando busca de vendas...")
        Log.d("SalesDebug", "Período: ${Date(startTime)} até ${Date(endTime)}")

        database.getReference("vendas")
            .orderByChild("data")
            .startAt(startTime.toDouble())
            .endAt(endTime.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("SalesDebug", "Total de vendas encontradas: ${snapshot.childrenCount}")

                    var total = 0.0
                    var vendasProcessadas = 0

                    snapshot.children.forEach { venda ->
                        Log.d("SalesDebug", "\nAnalisando venda: ${venda.key}")

                        val valorTotalDirect = venda.child("valorTotal").getValue(Double::class.java)
                        if (valorTotalDirect != null) {
                            total += valorTotalDirect
                            vendasProcessadas++
                            Log.d("SalesDebug", "Valor direto: $valorTotalDirect")
                            return@forEach
                        }

                        venda.child("items").children.forEach { item ->
                            Log.d("SalesDebug", "Item encontrado: ${item.key}")

                            val valorTotalItem = item.child("valorTotal").getValue(Double::class.java)
                            if (valorTotalItem != null) {
                                total += valorTotalItem
                                vendasProcessadas++
                                Log.d("SalesDebug", "Valor do item: $valorTotalItem")
                                return@forEach
                            }

                            val qtd = item.child("quantidade").getValue(Int::class.java) ?: 0
                            val unitario = item.child("valorUnitario").getValue(Double::class.java) ?: 0.0
                            val subtotal = qtd * unitario
                            total += subtotal
                            vendasProcessadas++
                            Log.d("SalesDebug", "Calculado: $qtd x $unitario = $subtotal")
                        }
                    }

                    Log.d("SalesDebug", "TOTAL FINAL: $total (de $vendasProcessadas vendas processadas)")
                    _dashboardData.value = _dashboardData.value?.copy(totalSales = total)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("SalesError", "ERRO: ${error.message}")
                }
            })
    }

    private fun loadMaterialsData(startTime: Long, endTime: Long) {
        database.getReference("materia_prima")
            .orderByChild("timestamp")
            .startAt(startTime.toDouble())
            .endAt(endTime.toDouble())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var total = 0.0

                    for (child in snapshot.children) {
                        val preco = child.child("preco").getValue(Double::class.java)
                        val quantidade = child.child("quantidade").getValue(Int::class.java)

                        if (preco != null && quantidade != null) {
                            total += preco * quantidade
                        }
                    }

                    _dashboardData.value = _dashboardData.value?.copy(totalMaterials = total)
                    Log.d("DashboardVM", "Total materiais-prima calculado: $total")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DashboardVM", "Erro ao carregar materiais: ${error.message}")
                }
            })
    }
}

data class DashboardData(
    val totalProduction: Int = 0,
    val totalSales: Double = 0.0,
    val totalMaterials: Double = 0.0
)

data class ChartData(
    val productionData: List<Int> = List(7) { 0 },
    val salesData: List<Int> = List(7) { 0 }
)