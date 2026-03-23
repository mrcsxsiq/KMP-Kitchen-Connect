package dev.marcos.lks.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcos.lks.Order
import dev.marcos.lks.OrderItem
import dev.marcos.lks.OrderStatus
import kotlinx.coroutines.launch
import kotlin.random.Random

class MakeOrderViewModel(
    private val repository: OrderHistoryRepository = OrderHistoryRepository()
) : ViewModel() {

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
