package dev.marcos.lks.data.repositories

import dev.marcos.lks.data.datasources.remote.OrderApi
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderItem
import dev.marcos.lks.data.model.OrderStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OrderRepository(
    private val api: OrderApi
) {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: Flow<List<Order>> = _orders.asStateFlow()

    suspend fun fetchData() {
        try {
            val response = api.getOrders()
            _orders.value = response
        } catch (e: Exception) {
            e.printStackTrace()
            if (_orders.value.isEmpty()) {
                _orders.value = listOf(
                    Order(
                        id = "#882",
                        table = "Mesa 12",
                        items = listOf(OrderItem(2, "Smash Burger Deluxe", "Sem Cebola")),
                        time = "18m",
                        status = OrderStatus.WAITING,
                        timeLabel = "ATRASADO",
                        isLate = true
                    ),
                    Order(
                        id = "#884",
                        table = "Mesa 04",
                        items = listOf(OrderItem(1, "Salada Caesar Especial")),
                        time = "04m",
                        status = OrderStatus.WAITING,
                        timeLabel = "AGUARDANDO"
                    )
                )
            }
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        delay(300)
        _orders.update { currentOrders ->
            currentOrders.map {
                if (it.id == orderId) it.copy(status = newStatus) else it
            }
        }
    }

    suspend fun addOrder(order: Order) {
        try {
            api.addOrder(order)
            fetchData()
        } catch (e: Exception) {
            e.printStackTrace()
            _orders.update { it + order }
        }
    }
}

