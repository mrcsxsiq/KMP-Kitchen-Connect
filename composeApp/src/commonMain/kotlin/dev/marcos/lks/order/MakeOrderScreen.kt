package dev.marcos.lks.order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Restaurant
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
import dev.marcos.lks.MenuItem
import dev.marcos.lks.OrderItem

@Composable
fun MakeOrderScreen(viewModel: MakeOrderViewModel = viewModel { MakeOrderViewModel() }) {
    val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()
    
    var itemToOrder by remember { mutableStateOf<MenuItem?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog && itemToOrder != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar Pedido") },
            text = { Text("Deseja realizar o pedido de: ${itemToOrder?.name} para a Mesa 01?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createOrder(
                            table = "Mesa 01",
                            items = listOf(OrderItem(1, itemToOrder!!.name))
                        )
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
                ) {
                    Text("Sim, Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Não, Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Text(
            "Cardápio Digital",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Text(
            "Selecione um item para realizar o pedido",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (menuItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0EA5E9))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(menuItems) { item ->
                    MenuCard(item) {
                        itemToOrder = item
                        showDialog = true
                    }
                }
            }
        }
    }
}

@Composable
fun MenuCard(item: MenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFF0EA5E9))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Text(item.description, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
                Text(item.price, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0EA5E9))
            }

            IconButton(onClick = onClick) {
                Icon(Icons.Default.Add, contentDescription = "Pedir", tint = Color(0xFF0EA5E9))
            }
        }
    }
}
