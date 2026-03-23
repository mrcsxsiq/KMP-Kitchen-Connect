package dev.marcos.lks

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class DashboardRepository {
    val orders: Flow<List<Order>> = InMemoryDatabase.orders

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun fetchDashboardOrders() {
        try {
            val response: List<Order> = client.get("http://$host:8080/dashboard-orders").body()
            InMemoryDatabase.updateOrders(response)
        } catch (e: Exception) {
            e.printStackTrace()
            // Em caso de erro, o InMemoryDatabase mantém o estado atual ou pode ser inicializado
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        // Atualiza localmente no banco em memória imediatamente
        InMemoryDatabase.updateStatus(orderId, newStatus)

        try {
            client.post("http://$host:8080/update-status") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("id" to orderId, "status" to newStatus.name))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
