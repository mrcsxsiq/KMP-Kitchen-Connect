package dev.marcos.lks.makeorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcos.lks.data.model.MenuItem
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderItem
import dev.marcos.lks.data.model.OrderStatus
import dev.marcos.lks.data.repositories.OrderHistoryRepository
import dev.marcos.lks.data.repositories.OrderHistoryRepositoryApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class MakeOrderViewModel(
    private val repository: OrderHistoryRepositoryApi = OrderHistoryRepository()
) : ViewModel() {

    val menuItems: StateFlow<List<MenuItem>> = repository.menuItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        fetchMenu()
    }

    private fun fetchMenu() {
        viewModelScope.launch {
            repository.fetchMenu()
        }
    }

    fun createOrder(table: String, items: List<OrderItem>) {
        viewModelScope.launch {
            val newOrder = Order(
                id = "#${Random.nextInt(100, 999)}",
                table = table,
                items = items,
                time = "00m",
                status = OrderStatus.WAITING,
                timeLabel = "AGUARDANDO",
                isLate = false
            )
            repository.addOrder(newOrder)
        }
    }
}

