package dev.marcos.lks.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderStatus
import dev.marcos.lks.data.repositories.DashboardRepositoryApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val orders: List<Order> = emptyList(),
    val isRefreshing: Boolean = false
)

class DashboardViewModel(
    private val repository: DashboardRepositoryApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeOrders()
        startPolling()
    }

    private fun observeOrders() {
        viewModelScope.launch {
            repository.orders.collect { orders ->
                _uiState.update { it.copy(orders = orders) }
            }
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                refresh()
                delay(5000)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            repository.fetchDashboardOrders()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }
}

