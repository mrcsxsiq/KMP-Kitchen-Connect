package dev.marcos.lks.order

import dev.marcos.lks.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class OrderHistoryRepository {
    val orders: Flow<List<Order>> = InMemoryDatabase.orders
    val menuItems: Flow<List<MenuItem>> = InMemoryDatabase.menuItems

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun fetchHistoryOrders() {
        try {
            val response: List<Order> = client.get("http://$host:8080/history-orders").body()
            InMemoryDatabase.updateOrders(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchMenu() {
        try {
            val response: List<MenuItem> = client.get("http://$host:8080/menu").body()
            InMemoryDatabase.updateMenuItems(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addOrder(order: Order) {
        InMemoryDatabase.addOrder(order)
        try {
            client.post("http://$host:8080/orders") {
                contentType(ContentType.Application.Json)
                setBody(order)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
