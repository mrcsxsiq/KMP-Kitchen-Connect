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
        val response = api.getHistoryOrders()
        inMemoryDatabase.updateOrders(response)
    }

    override suspend fun fetchMenu() {
        val response = api.getMenu()
        inMemoryDatabase.updateMenuItems(response)
    }

    override suspend fun addOrder(order: Order) {
        inMemoryDatabase.addOrder(order)
        api.addOrder(order)
    }
}

