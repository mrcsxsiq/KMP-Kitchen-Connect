package dev.marcos.lks.orderhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.repositories.OrderHistoryRepositoryApi
import dev.marcos.lks.util.formatDetailsForUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderHistoryUiState(
    val orders: List<Order> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val errorDetails: String? = null,
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
                try {
                    performHistoryRefresh()
                } catch (e: CancellationException) {
                    throw e
                }
                delay(5000)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                performHistoryRefresh()
            } catch (_: CancellationException) {
                // ignorar cancelamento (ex.: saída da tela)
            }
        }
    }

    private suspend fun performHistoryRefresh() {
        _uiState.update {
            it.copy(
                isRefreshing = true,
                errorMessage = null,
                errorDetails = null,
            )
        }
        try {
            repository.fetchHistoryOrders()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            _uiState.update {
                it.copy(
                    errorMessage = "Falha ao carregar o histórico.",
                    errorDetails = e.formatDetailsForUser(),
                )
            }
        } finally {
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}

