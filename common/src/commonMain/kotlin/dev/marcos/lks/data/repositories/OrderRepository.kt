package dev.marcos.lks.data.repositories

import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderItem
import dev.marcos.lks.data.model.OrderStatus
import dev.marcos.lks.host
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

class OrderRepository {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: Flow<List<Order>> = _orders.asStateFlow()

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    suspend fun fetchData() {
        try {
            val response: List<Order> = client.get("http://${host}:8080/orders").body()
            _orders.value = response
        } catch (e: Exception) {
            e.printStackTrace()
            if (_orders.value.isEmpty()) {
                _orders.value = listOf(
                    Order(
                        id = "#882",
                        table = "Mesa 12",
                        items = listOf(OrderItem(2, "Smash Burger Deluxe", "Sem Cebola")),
                        time = "18m",
                        status = OrderStatus.WAITING,
                        timeLabel = "ATRASADO",
                        isLate = true
                    ),
                    Order(
                        id = "#884",
                        table = "Mesa 04",
                        items = listOf(OrderItem(1, "Salada Caesar Especial")),
                        time = "04m",
                        status = OrderStatus.WAITING,
                        timeLabel = "AGUARDANDO"
                    )
                )
            }
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        delay(300)
        _orders.update { currentOrders ->
            currentOrders.map {
                if (it.id == orderId) it.copy(status = newStatus) else it
            }
        }
    }

    suspend fun addOrder(order: Order) {
        try {
            client.post("http://${host}:8080/orders") {
                contentType(ContentType.Application.Json)
                setBody(order)
            }
            fetchData()
        } catch (e: Exception) {
            e.printStackTrace()
            _orders.update { it + order }
        }
    }
}

