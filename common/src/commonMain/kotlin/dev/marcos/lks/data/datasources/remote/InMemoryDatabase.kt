package dev.marcos.lks.data.datasources.remote

import dev.marcos.lks.data.model.MenuItem
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

object InMemoryDatabase {
    val orders = MutableStateFlow<List<Order>>(emptyList())
    val menuItems = MutableStateFlow<List<MenuItem>>(emptyList())

    fun updateOrders(newOrders: List<Order>) {
        orders.value = newOrders
    }

    fun updateMenuItems(items: List<MenuItem>) {
        menuItems.value = items
    }

    fun addOrder(order: Order) {
        orders.update { it + order }
    }

    fun updateStatus(orderId: String, newStatus: OrderStatus) {
        orders.update { currentOrders ->
            currentOrders.map {
                if (it.id == orderId) it.copy(status = newStatus) else it
            }
        }
    }
}

