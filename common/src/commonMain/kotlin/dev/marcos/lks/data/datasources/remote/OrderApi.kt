package dev.marcos.lks.data.datasources.remote

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import dev.marcos.lks.data.model.Order

interface OrderApi {
    @GET("orders")
    suspend fun getOrders(): List<Order>

    @POST("orders")
    suspend fun addOrder(@Body order: Order)
}

