package dev.marcos.lks.data.datasources.remote

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import dev.marcos.lks.data.model.Order

interface DashboardApi {
    @GET("dashboard-orders")
    suspend fun getDashboardOrders(): List<Order>

    @POST("update-status")
    suspend fun updateOrderStatus(@Body body: Map<String, String>)
}

