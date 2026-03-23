package dev.marcos.lks.data.repositories

import dev.marcos.lks.data.model.MenuItem
import dev.marcos.lks.data.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderHistoryRepositoryApi {
    val orders: Flow<List<Order>>
    val menuItems: Flow<List<MenuItem>>

    suspend fun fetchHistoryOrders()
    suspend fun fetchMenu()
    suspend fun addOrder(order: Order)
}

