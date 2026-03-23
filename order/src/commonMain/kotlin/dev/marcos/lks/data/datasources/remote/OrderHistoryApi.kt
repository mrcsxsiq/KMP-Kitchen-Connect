package dev.marcos.lks.data.datasources.remote

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import dev.marcos.lks.data.model.MenuItem
import dev.marcos.lks.data.model.Order

interface OrderHistoryApi {
    @GET("history-orders")
    suspend fun getHistoryOrders(): List<Order>

    @GET("menu")
    suspend fun getMenu(): List<MenuItem>

    @POST("orders")
    suspend fun addOrder(@Body order: Order)
}

