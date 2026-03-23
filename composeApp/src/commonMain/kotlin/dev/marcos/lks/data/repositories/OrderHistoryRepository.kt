package dev.marcos.lks.data.repositories

import dev.marcos.lks.data.datasources.remote.InMemoryDatabase
import dev.marcos.lks.data.model.MenuItem
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.host
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class OrderHistoryRepository {
    val orders: Flow<List<Order>> = InMemoryDatabase.orders
    val menuItems: Flow<List<MenuItem>> = InMemoryDatabase.menuItems

    private val client = HttpClient {
        HttpClientConfig.install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun fetchHistoryOrders() {
        try {
            val response: List<Order> = client.get("http://${host}:8080/history-orders").body()
            InMemoryDatabase.updateOrders(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchMenu() {
        try {
            val response: List<MenuItem> = client.get("http://${host}:8080/menu").body()
            InMemoryDatabase.updateMenuItems(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addOrder(order: Order) {
        InMemoryDatabase.addOrder(order)
        try {
            client.post("http://${host}:8080/orders") {
                contentType(ContentType.Application.Json)
                setBody(order)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}