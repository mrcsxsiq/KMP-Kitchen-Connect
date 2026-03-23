package dev.marcos.lks.order

import dev.marcos.lks.Order
import dev.marcos.lks.host
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

class OrderHistoryRepository {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: Flow<List<Order>> = _orders.asStateFlow()

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun fetchHistoryOrders() {
        try {
            val response: List<Order> = client.get("http://${host}:8080/history-orders").body()
            _orders.value = response
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addOrder(order: Order) {
        _orders.update { it + order }
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
