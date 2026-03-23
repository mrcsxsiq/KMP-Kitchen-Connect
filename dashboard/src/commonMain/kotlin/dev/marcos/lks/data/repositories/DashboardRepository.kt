package dev.marcos.lks.data.repositories

import dev.marcos.lks.data.datasources.remote.DashboardApi
import dev.marcos.lks.data.datasources.remote.InMemoryDatabase
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow

class DashboardRepository(
    private val api: DashboardApi
) : DashboardRepositoryApi {
    override val orders: Flow<List<Order>> = InMemoryDatabase.orders

    override suspend fun fetchDashboardOrders() {
        try {
            val response = api.getDashboardOrders()
            InMemoryDatabase.updateOrders(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        InMemoryDatabase.updateStatus(orderId, newStatus)

        try {
            api.updateOrderStatus(mapOf("id" to orderId, "status" to newStatus.name))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

