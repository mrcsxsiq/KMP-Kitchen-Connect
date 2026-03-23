package dev.marcos.lks.makeorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcos.lks.data.model.MenuItem
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderItem
import dev.marcos.lks.data.model.OrderStatus
import dev.marcos.lks.data.repositories.OrderHistoryRepositoryApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class MakeOrderUiState(
    val menuItems: List<MenuItem> = emptyList()
)

class MakeOrderViewModel(
    private val repository: OrderHistoryRepositoryApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(MakeOrderUiState())
    val uiState: StateFlow<MakeOrderUiState> = _uiState.asStateFlow()

    init {
        observeMenuItems()
        fetchMenu()
    }

    private fun observeMenuItems() {
        viewModelScope.launch {
            repository.menuItems.collect { items ->
                _uiState.update { it.copy(menuItems = items) }
            }
        }
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

