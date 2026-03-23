package dev.marcos.lks.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.marcos.lks.*

val Orange = Color(0xFFFF8A00)
val Teal = Color(0xFF00A699)
val Mint = Color(0xFF00C2A0)
val SkyBlue = Color(0xFF0EA5E9)
val Gray = Color(0xFF4A5568)
val Red = Color(0xFFFF4D4D)
val LightGray = Color(0xFFF8FAFC)

@Composable
fun Dashboard(viewModel: DashboardViewModel = viewModel { DashboardViewModel() }) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val isRefreshing = viewModel.isRefreshing

    Box(modifier = Modifier.fillMaxSize().background(LightGray)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Header()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardColumn(
                    title = "AGUARDANDO",
                    count = "${orders.count { it.status == OrderStatus.WAITING }} Pedidos",
                    color = Orange,
                    modifier = Modifier.width(320.dp)
                ) {
                    orders.filter { it.status == OrderStatus.WAITING }.forEach { order ->
                        OrderCard(
                            order = order,
                            buttonText = "INICIAR PREPARO",
                            buttonColor = Orange,
                            buttonTextColor = Color.White,
                            onButtonClick = { viewModel.updateOrderStatus(order.id, OrderStatus.PREPARING) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                DashboardColumn(
                    title = "PREPARANDO",
                    count = "${orders.count { it.status == OrderStatus.PREPARING }} Pedidos",
                    color = Teal,
                    modifier = Modifier.width(320.dp)
                ) {
                    orders.filter { it.status == OrderStatus.PREPARING }.forEach { order ->
                        OrderCard(
                            order = order,
                            buttonText = "MARCAR COMO PRONTO",
                            buttonColor = Teal,
                            isIconEnabled = true,
                            onButtonClick = { viewModel.updateOrderStatus(order.id, OrderStatus.READY) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                DashboardColumn(
                    title = "PRONTO",
                    count = "${orders.count { it.status == OrderStatus.READY }} Pedidos",
                    color = Mint,
                    modifier = Modifier.width(320.dp)
                ) {
                    orders.filter { it.status == OrderStatus.READY }.forEach { order ->
                        OrderCard(
                            order = order,
                            buttonText = "ENTREGAR PEDIDO",
                            buttonColor = SkyBlue,
                            buttonTextColor = Color.White,
                            isIconEnabled = false,
                            showBorder = false,
                            onButtonClick = { viewModel.updateOrderStatus(order.id, OrderStatus.DELIVERED) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                DashboardColumn(
                    title = "ENTREGUE",
                    count = "HISTÓRICO DE HOJE",
                    color = Gray,
                    modifier = Modifier.width(320.dp)
                ) {
                    orders.filter { it.status == OrderStatus.DELIVERED }.forEach { order ->
                        HistoryCard(
                            id = order.id,
                            table = order.table.uppercase(),
                            time = order.time,
                            items = order.items.joinToString(", ") { "${it.quantity}x ${it.name}" }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Indicador de Polling (Círculo no canto superior direito)
        val indicatorColor by animateColorAsState(
            targetValue = if (isRefreshing) Color(0xFF22C55E) else Color(0xFFCBD5E1),
            animationSpec = tween(durationMillis = 300)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 26.dp, end = 24.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(indicatorColor)
                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
        )
    }
}

@Composable
fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "KMP Kitchen Connect",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Gray
            )
        }
    }
}

@Composable
fun DashboardColumn(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.DarkGray
                )
            }
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    count,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            content = content
        )
    }
}

@Composable
fun OrderCard(
    order: Order,
    buttonText: String,
    buttonColor: Color,
    buttonTextColor: Color = Color.White,
    isIconEnabled: Boolean = false,
    showBorder: Boolean = false,
    onButtonClick: () -> Unit
) {
    val statusColor = when {
        order.isLate -> Red
        order.status == OrderStatus.WAITING -> Orange
        order.status == OrderStatus.PREPARING -> Teal
        order.status == OrderStatus.READY -> Mint
        else -> Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("PEDIDO ${order.id}", color = Color.Gray, fontSize = 12.sp)
                    Text(order.table, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        order.time,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        order.timeLabel,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            order.items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${item.quantity}x ${item.name}",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (item.note != null) {
                        Surface(
                            color = if (item.note == "Sem Cebola") Color.White else Mint,
                            shape = RoundedCornerShape(12.dp),
                            border = if (item.note == "Sem Cebola") BorderStroke(1.dp, Color.LightGray) else null
                        ) {
                            Text(
                                item.note,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.note == "Sem Cebola") Color.Gray else Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onButtonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .then(
                        if (showBorder) Modifier.border(1.dp, buttonColor, RoundedCornerShape(12.dp))
                        else Modifier
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showBorder) Color.Transparent else buttonColor,
                    contentColor = buttonTextColor
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isIconEnabled) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(buttonText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HistoryCard(id: String, table: String, time: String, items: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$id • $table", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(time, fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(items, fontSize = 12.sp, color = Color.DarkGray, maxLines = 1)
        }
    }
}
