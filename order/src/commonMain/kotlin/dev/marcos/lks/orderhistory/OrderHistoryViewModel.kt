package dev.marcos.lks.orderhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.repositories.OrderHistoryRepositoryApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderHistoryUiState(
    val orders: List<Order> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class OrderHistoryViewModel(
    private val repository: OrderHistoryRepositoryApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

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
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            try {
                repository.fetchHistoryOrders()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Falha ao carregar o histórico. Verifique sua conexão.") }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }
}

