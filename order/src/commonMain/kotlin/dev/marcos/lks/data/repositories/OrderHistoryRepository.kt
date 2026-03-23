package dev.marcos.lks.data.repositories

import dev.marcos.lks.data.datasources.remote.InMemoryDatabase
import dev.marcos.lks.data.datasources.remote.OrderHistoryApi
import dev.marcos.lks.data.model.MenuItem
import dev.marcos.lks.data.model.Order
import kotlinx.coroutines.flow.Flow

class OrderHistoryRepository(
    private val api: OrderHistoryApi
) : OrderHistoryRepositoryApi {
    override val orders: Flow<List<Order>> = InMemoryDatabase.orders
    override val menuItems: Flow<List<MenuItem>> = InMemoryDatabase.menuItems

    override suspend fun fetchHistoryOrders() {
        try {
            val response = api.getHistoryOrders()
            InMemoryDatabase.updateOrders(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun fetchMenu() {
        try {
            val response = api.getMenu()
            InMemoryDatabase.updateMenuItems(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun addOrder(order: Order) {
        InMemoryDatabase.addOrder(order)
        try {
            api.addOrder(order)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

