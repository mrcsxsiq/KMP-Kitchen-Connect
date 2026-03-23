package dev.marcos.lks

import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus {
    WAITING, PREPARING, READY, DELIVERED
}

@Serializable
data class Order(
    val id: String,
    val table: String,
    val items: List<OrderItem>,
    val time: String,
    val status: OrderStatus,
    val timeLabel: String,
    val isLate: Boolean = false
)

@Serializable
data class OrderItem(
    val quantity: Int,
    val name: String,
    val note: String? = null
)
