package dev.marcos.lks.orderhistory

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.repositories.OrderHistoryRepository
import dev.marcos.lks.data.repositories.OrderHistoryRepositoryApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderHistoryViewModel(
    private val repository: OrderHistoryRepositoryApi = OrderHistoryRepository()
) : ViewModel() {

    var isRefreshing by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val orders: StateFlow<List<Order>> = repository.orders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    init {
        startPolling()
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
            isRefreshing = true
            errorMessage = null
            try {
                repository.fetchHistoryOrders()
            } catch (e: Exception) {
                errorMessage = "Falha ao carregar o histórico. Verifique sua conexão."
            } finally {
                isRefreshing = false
            }
        }
    }
}
