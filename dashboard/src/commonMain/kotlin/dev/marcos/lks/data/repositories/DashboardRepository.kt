package dev.marcos.lks.data.repositories

import dev.marcos.lks.data.datasources.remote.InMemoryDatabase
import dev.marcos.lks.data.model.Order
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
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class DashboardRepository : DashboardRepositoryApi {
    override val orders: Flow<List<Order>> = InMemoryDatabase.orders

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun fetchDashboardOrders() {
        try {
            val response: List<Order> = client.get("http://${host}:8080/dashboard-orders").body()
            InMemoryDatabase.updateOrders(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        InMemoryDatabase.updateStatus(orderId, newStatus)

        try {
            client.post("http://${host}:8080/update-status") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("id" to orderId, "status" to newStatus.name))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

