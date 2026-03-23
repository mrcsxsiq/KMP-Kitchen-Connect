package dev.marcos.lks.makeorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcos.lks.data.model.MenuItem
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderItem
import dev.marcos.lks.data.model.OrderStatus
import dev.marcos.lks.data.repositories.OrderHistoryRepositoryApi
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlinx.coroutines.async
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private suspend fun ViewModel.stop() {
    val job = viewModelScope.coroutineContext[Job] ?: return
    job.cancelAndJoin()
}

private class FakeOrderHistoryRepository(
    override val orders: MutableStateFlow<List<Order>> = MutableStateFlow(emptyList()),
    override val menuItems: MutableStateFlow<List<MenuItem>> = MutableStateFlow(emptyList()),
) : OrderHistoryRepositoryApi {
    var fetchMenuCalls: Int = 0
        private set

    var addOrderCalls: Int = 0
        private set

    var lastAddedOrder: Order? = null
        private set

    override suspend fun fetchHistoryOrders() = Unit

    override suspend fun fetchMenu() {
        fetchMenuCalls++
    }

    override suspend fun addOrder(order: Order) {
        addOrderCalls++
        lastAddedOrder = order
    }
}

class MakeOrderViewModelTest {
    @Test
    fun `Given fake repository when viewmodel is created then fetchMenu called once`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repo = FakeOrderHistoryRepository()

            val koinApp = startKoin {
                modules(
                    module {
                        single<OrderHistoryRepositoryApi> { repo }
                        factory { MakeOrderViewModel(get()) }
                    }
                )
            }
            val viewModel: MakeOrderViewModel = koinApp.koin.get()

            advanceUntilIdle()

            assertEquals(1, repo.fetchMenuCalls)
            viewModel.stop()
        } finally {
            stopKoin()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `Given menu items are emitted then viewmodel exposes them`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repo = FakeOrderHistoryRepository()

            val koinApp = startKoin {
                modules(
                    module {
                        single<OrderHistoryRepositoryApi> { repo }
                        factory { MakeOrderViewModel(get()) }
                    }
                )
            }
            val viewModel: MakeOrderViewModel = koinApp.koin.get()

            val received = async {
                viewModel.menuItems.first { it.isNotEmpty() }
            }

            repo.menuItems.value = listOf(
                MenuItem(name = "Smash", description = "Sem cebola", price = "R$ 25,00")
            )

            advanceUntilIdle()
            val menu = received.await()

            assertEquals(1, menu.size)
            assertEquals("Smash", menu.first().name)

            viewModel.stop()
        } finally {
            stopKoin()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `Given table and items when createOrder is called then repository receives expected order`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repo = FakeOrderHistoryRepository()
            val koinApp = startKoin {
                modules(
                    module {
                        single<OrderHistoryRepositoryApi> { repo }
                        factory { MakeOrderViewModel(get()) }
                    }
                )
            }
            val viewModel: MakeOrderViewModel = koinApp.koin.get()
            advanceUntilIdle()

            val items = listOf(
                OrderItem(quantity = 2, name = "Smash Burger Deluxe", note = "Sem Cebola")
            )

            viewModel.createOrder(table = "Mesa 12", items = items)
            advanceUntilIdle()

            assertEquals(1, repo.addOrderCalls)
            val order = assertNotNull(repo.lastAddedOrder)

            assertTrue(order.id.matches(Regex("#\\d{3}")))
            val idNumber = order.id.removePrefix("#").toInt()
            assertTrue(idNumber in 100..998)

            assertEquals("Mesa 12", order.table)
            assertEquals(items, order.items)
            assertEquals("00m", order.time)
            assertEquals(OrderStatus.WAITING, order.status)
            assertEquals("AGUARDANDO", order.timeLabel)
            assertFalse(order.isLate)

            viewModel.stop()
        } finally {
            stopKoin()
            Dispatchers.resetMain()
        }
    }
}

