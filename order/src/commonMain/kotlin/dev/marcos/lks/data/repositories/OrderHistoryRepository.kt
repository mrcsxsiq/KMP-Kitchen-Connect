package dev.marcos.lks.data.repositories

import dev.marcos.lks.data.datasources.remote.InMemoryDatabase
import dev.marcos.lks.data.datasources.remote.OrderHistoryApi
import dev.marcos.lks.data.model.MenuItem
import dev.marcos.lks.data.model.Order
import kotlinx.coroutines.flow.Flow

class OrderHistoryRepository(
    private val api: OrderHistoryApi,
    private val inMemoryDatabase: InMemoryDatabase,
) : OrderHistoryRepositoryApi {
    override val orders: Flow<List<Order>> = inMemoryDatabase.orders
    override val menuItems: Flow<List<MenuItem>> = inMemoryDatabase.menuItems

    override suspend fun fetchHistoryOrders() {
        try {
            val response = api.getHistoryOrders()
            inMemoryDatabase.updateOrders(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun fetchMenu() {
        try {
            val response = api.getMenu()
            inMemoryDatabase.updateMenuItems(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun addOrder(order: Order) {
        inMemoryDatabase.addOrder(order)
        try {
            api.addOrder(order)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

