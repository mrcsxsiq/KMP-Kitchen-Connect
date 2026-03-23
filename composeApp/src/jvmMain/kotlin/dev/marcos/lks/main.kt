package dev.marcos.lks

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.marcos.lks.dashboard.DashboardScreen
import dev.marcos.lks.data.model.MenuItem
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderItem
import dev.marcos.lks.data.model.OrderStatus
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.awt.Toolkit

val serverOrders = MutableStateFlow<List<Order>>(listOf(
    Order(
        "#882",
        "Mesa 01",
        listOf(OrderItem(2, "Smash Burger Deluxe", "Sem Cebola")),
        "18m",
        OrderStatus.WAITING,
        "ATRASADO",
        true
    ),
    Order("#884", "Mesa 01", listOf(OrderItem(1, "Salada Caesar Especial")), "04m", OrderStatus.WAITING, "AGUARDANDO"),
    Order("#879", "Mesa 01", listOf(OrderItem(1, "Salmão Grelhado", "AO PONTO")), "09m", OrderStatus.PREPARING, "COZINHANDO"),
    Order("#870", "Mesa 01", listOf(OrderItem(1, "Bife de Ancho")), "12:45", OrderStatus.DELIVERED, "CONCLUÍDO")
))

val serverMenu = listOf(
    MenuItem("Smash Burger Deluxe", "Blend especial, queijo, bacon e molho", "R$ 32,90"),
    MenuItem("Pizza Margherita", "Molho de tomate, mussarela e manjericão", "R$ 45,00"),
    MenuItem("Salada Caesar", "Alface, croutons, frango e molho especial", "R$ 28,50"),
    MenuItem("Batata Trufada", "Batatas fritas com azeite de trufas", "R$ 22,00"),
    MenuItem("Coca-Cola Zero", "Lata 350ml bem gelada", "R$ 6,00")
)

fun main() {
    initKoin()

    val scope = CoroutineScope(Dispatchers.Default)

    scope.launch {
        embeddedServer(Netty, port = 8080) {
            install(ContentNegotiation) {
                json()
            }
            install(CORS) {
                anyHost()
                allowMethod(HttpMethod.Options)
                allowMethod(HttpMethod.Post)
                allowHeader(HttpHeaders.ContentType)
            }
            routing {
                get("/menu") {
                    call.respond(serverMenu)
                }

                get("/dashboard-orders") {
                    val activeOrders = serverOrders.value
                    call.respond(activeOrders)
                }

                get("/history-orders") {
                    call.respond(serverOrders.value)
                }

                post("/orders") {
                    try {
                        val newOrder = call.receive<Order>()
                        serverOrders.update { it + newOrder }
                        call.respond(HttpStatusCode.Created, newOrder)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Erro ao criar pedido")
                    }
                }

                post("/update-status") {
                    try {
                        val params = call.receive<Map<String, String>>()
                        val id = params["id"]
                        val statusName = params["status"]
                        
                        if (id != null && statusName != null) {
                            val newStatus = OrderStatus.valueOf(statusName)
                            serverOrders.update { list ->
                                list.map { if (it.id == id) it.copy(status = newStatus) else it }
                            }
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.BadRequest)
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Erro ao atualizar status")
                    }
                }
            }
        }.start(wait = true)
    }

    application {
        val screenSize = Toolkit.getDefaultToolkit().screenSize

        val mainWindowState = rememberWindowState(
            placement = WindowPlacement.Maximized,
            position = WindowPosition(Alignment.Center),
            size = DpSize(width = screenSize.width.dp, height = screenSize.height.dp)
        )

        Window(
            onCloseRequest = ::exitApplication,
            title = "KMP Kitchen Connect",
            state = mainWindowState
        ) {
            DashboardScreen()
        }
    }
}
