package dev.marcos.lks

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
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
            // Usa o host dinâmico baseado na plataforma (127.0.0.1 ou 10.0.2.2)
            val response: List<Order> = client.get("http://$host:8080/orders").body()
            _orders.value = response
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback para dados locais caso o servidor esteja offline ou em modo standalone
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
            // Tenta enviar o pedido para o servidor via POST
            client.post("http://$host:8080/orders") {
                contentType(ContentType.Application.Json)
                setBody(order)
            }
            // Se o envio deu certo, atualiza a lista chamando o fetch
            fetchData()
        } catch (e: Exception) {
            e.printStackTrace()
            // Se falhar (modo offline), adiciona apenas localmente
            _orders.update { it + order }
        }
    }
}
