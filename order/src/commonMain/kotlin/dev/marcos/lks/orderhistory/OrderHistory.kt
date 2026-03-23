package dev.marcos.lks.orderhistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderStatus
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistory(viewModel: OrderHistoryViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val orders = uiState.orders
    val isRefreshing = uiState.isRefreshing
    val errorMessage = uiState.errorMessage

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            val groupedOrders = orders.filter { it.status != OrderStatus.DELIVERED }.groupBy { it.table }

            when {
                errorMessage != null -> {
                    ErrorState(message = errorMessage) {
                        viewModel.refresh()
                    }
                }

                groupedOrders.isEmpty() -> {
                    EmptyState()
                }

                else -> {

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
                    ) {
                        groupedOrders.forEach { (table, tableOrders) ->
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.TableBar,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = Color(0xFF475569)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(table.uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    }
                                    Text(
                                        if (table.contains("12")) "FLUXO NORMAL" else "ALTA PRIORIDADE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (table.contains("12")) Color.Gray else Color(0xFFB45309)
                                    )
                                }
                            }
                            items(tableOrders) { order ->
                                HistoryOrderCard(order) {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Inbox,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Nenhum pedido encontrado",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569)
        )
        Text(
            "Os novos pedidos aparecerão aqui automaticamente.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFEF4444)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Ops! Algo deu errado",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569)
        )
        Text(
            message,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
        ) {
            Text("Tentar Novamente")
        }
    }
}

@Composable
fun HistoryHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Precision Galley", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A))
        }
        Icon(Icons.Default.Wifi, contentDescription = null, tint = Color(0xFF475569))
    }
}

@Composable
fun StatsSection(orders: List<Order>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1.5f).height(140.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.White)
                Column {
                    Text(
                        orders.count { it.status == OrderStatus.PREPARING }.toString().padStart(2, '0'),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text("Em Preparo", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SmallStatCard(
                icon = Icons.Default.HourglassEmpty,
                count = orders.count { it.status == OrderStatus.WAITING },
                label = "Aguardando",
                iconColor = Color(0xFFB45309),
                iconBg = Color(0xFFFFEDD5)
            )
            SmallStatCard(
                icon = Icons.Default.CheckCircle,
                count = orders.count { it.status == OrderStatus.DELIVERED },
                label = "Concluídos",
                iconColor = Color(0xFF0F766E),
                iconBg = Color(0xFFCCFBF1)
            )
        }
    }
}

@Composable
fun SmallStatCard(icon: ImageVector, count: Int, label: String, iconColor: Color, iconBg: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = iconColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(count.toString().padStart(2, '0'), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(label, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun HistoryOrderCard(order: Order, onAction: () -> Unit) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PEDIDO ${order.id}", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(order.items.firstOrNull()?.name ?: "", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (order.items.size > 1 || order.items.firstOrNull()?.note != null) {
                        Text(
                            "+ " + (order.items.drop(1).joinToString(", ") { it.name }
                                .ifEmpty { order.items.firstOrNull()?.note ?: "" }),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                Surface(
                    color = when (order.status) {
                        OrderStatus.PREPARING -> Color(0xFFCFFAFE)
                        OrderStatus.WAITING -> Color(0xFFFFEDD5)
                        OrderStatus.READY -> Color(0xFFDCFCE7)
                        else -> Color(0xFFF1F5F9)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        when (order.status) {
                            OrderStatus.PREPARING -> "EM PREPARO"
                            OrderStatus.WAITING -> "AGUARDANDO"
                            OrderStatus.READY -> "PRONTO"
                            else -> "CONCLUÍDO"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (order.status) {
                            OrderStatus.PREPARING -> Color(0xFF0891B2)
                            OrderStatus.WAITING -> Color(0xFFB45309)
                            OrderStatus.READY -> Color(0xFF166534)
                            else -> Color.Gray
                        }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (order.isLate) Color.Red else Color(0xFF0F766E)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${order.time} min",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.isLate) Color.Red else Color(0xFF1E293B)
                    )
                }
            }
        }
    }
}

