package dev.marcos.lks.data.repositories

import dev.marcos.lks.data.datasources.remote.DashboardApi
import dev.marcos.lks.data.datasources.remote.InMemoryDatabase
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow

class DashboardRepository(
    private val api: DashboardApi,
    private val inMemoryDatabase: InMemoryDatabase,
) : DashboardRepositoryApi {
    override val orders: Flow<List<Order>> = inMemoryDatabase.orders

    override suspend fun fetchDashboardOrders() {
        try {
            val response = api.getDashboardOrders()
            inMemoryDatabase.updateOrders(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        inMemoryDatabase.updateStatus(orderId, newStatus)

        try {
            api.updateOrderStatus(mapOf("id" to orderId, "status" to newStatus.name))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

