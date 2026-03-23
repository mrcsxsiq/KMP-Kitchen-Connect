package dev.marcos.lks

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Banco de dados em memória para centralizar o estado dos pedidos no CommonMain.
 * Isso permite que diferentes repositórios compartilhem a mesma fonte de verdade.
 */
object InMemoryDatabase {
    val orders = MutableStateFlow<List<Order>>(emptyList())

    fun updateOrders(newOrders: List<Order>) {
        orders.value = newOrders
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
