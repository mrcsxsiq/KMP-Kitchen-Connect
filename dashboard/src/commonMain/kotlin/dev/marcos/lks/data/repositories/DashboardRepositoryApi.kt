package dev.marcos.lks.data.repositories

import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface DashboardRepositoryApi {
    val orders: Flow<List<Order>>
    suspend fun fetchDashboardOrders()
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus)
}

