package dev.marcos.lks.makeorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcos.lks.data.model.MenuItem
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderItem
import dev.marcos.lks.data.model.OrderStatus
import dev.marcos.lks.data.repositories.OrderHistoryRepositoryApi
import dev.marcos.lks.util.formatDetailsForUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class OrderSubmitSnackEvent {
    Success,
    Failure,
}

data class MakeOrderUiState(
    val menuItems: List<MenuItem> = emptyList(),
    val isLoadingMenu: Boolean = true,
    val menuErrorTitle: String? = null,
    val menuErrorDetails: String? = null,
)

class MakeOrderViewModel(
    private val repository: OrderHistoryRepositoryApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(MakeOrderUiState())
    val uiState: StateFlow<MakeOrderUiState> = _uiState.asStateFlow()

    private val _orderSubmitSnackEvents = MutableSharedFlow<OrderSubmitSnackEvent>(extraBufferCapacity = 1)
    val orderSubmitSnackEvents: SharedFlow<OrderSubmitSnackEvent> = _orderSubmitSnackEvents.asSharedFlow()

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

    fun retryLoadMenu() {
        fetchMenu()
    }

    private fun fetchMenu() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingMenu = true,
                    menuErrorTitle = null,
                    menuErrorDetails = null,
                )
            }
            try {
                repository.fetchMenu()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        menuErrorTitle = "Não foi possível carregar o cardápio",
                        menuErrorDetails = e.formatDetailsForUser(),
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoadingMenu = false) }
            }
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
            try {
                repository.addOrder(newOrder)
                _orderSubmitSnackEvents.tryEmit(OrderSubmitSnackEvent.Success)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                _orderSubmitSnackEvents.tryEmit(OrderSubmitSnackEvent.Failure)
            }
        }
    }
}

