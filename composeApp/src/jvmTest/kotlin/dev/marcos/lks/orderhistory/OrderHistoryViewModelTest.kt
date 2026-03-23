package dev.marcos.lks.orderhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcos.lks.data.model.MenuItem
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.repositories.OrderHistoryRepositoryApi
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi

private suspend fun ViewModel.stop() {
    val job = viewModelScope.coroutineContext[Job] ?: return
    job.cancelAndJoin()
}

private class FakeOrderHistoryRepository(
    override val orders: MutableStateFlow<List<Order>> = MutableStateFlow(emptyList()),
    override val menuItems: MutableStateFlow<List<MenuItem>> = MutableStateFlow(emptyList()),
    private val fetchHistoryOrdersImpl: suspend () -> Unit = {},
) : OrderHistoryRepositoryApi {
    var fetchHistoryCalls: Int = 0
        private set

    var fetchMenuCalls: Int = 0
        private set

    override suspend fun fetchHistoryOrders() {
        fetchHistoryCalls++
        fetchHistoryOrdersImpl()
    }

    override suspend fun fetchMenu() {
        fetchMenuCalls++
    }

    override suspend fun addOrder(order: Order) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class OrderHistoryViewModelTest {
    @Test
    fun `Given successful fetch when refresh completes then isRefreshing false and errorMessage is null`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repo = FakeOrderHistoryRepository(fetchHistoryOrdersImpl = {})

            val koinApp = startKoin {
                modules(
                    module {
                        single<OrderHistoryRepositoryApi> { repo }
                        factory { OrderHistoryViewModel(get()) }
                    }
                )
            }
            val viewModel: OrderHistoryViewModel = koinApp.koin.get()

            runCurrent()

            assertFalse(viewModel.uiState.value.isRefreshing)
            assertNull(viewModel.uiState.value.errorMessage)

            viewModel.stop()
        } finally {
            stopKoin()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `Given failing fetch when refresh completes then errorMessage is set and isRefreshing false`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repo = FakeOrderHistoryRepository(fetchHistoryOrdersImpl = {
                throw RuntimeException("boom")
            })

            val koinApp = startKoin {
                modules(
                    module {
                        single<OrderHistoryRepositoryApi> { repo }
                        factory { OrderHistoryViewModel(get()) }
                    }
                )
            }
            val viewModel: OrderHistoryViewModel = koinApp.koin.get()

            runCurrent()

            assertFalse(viewModel.uiState.value.isRefreshing)
            assertEquals(
                "Falha ao carregar o histórico. Verifique sua conexão.",
                viewModel.uiState.value.errorMessage
            )

            viewModel.stop()
        } finally {
            stopKoin()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `Given blocked fetch when refresh in flight then isRefreshing true and then false`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val started = kotlinx.coroutines.CompletableDeferred<Unit>()
            val finish = kotlinx.coroutines.CompletableDeferred<Unit>()

            val repo = FakeOrderHistoryRepository(fetchHistoryOrdersImpl = {
                started.complete(Unit)
                finish.await()
            })

            val koinApp = startKoin {
                modules(
                    module {
                        single<OrderHistoryRepositoryApi> { repo }
                        factory { OrderHistoryViewModel(get()) }
                    }
                )
            }
            val viewModel: OrderHistoryViewModel = koinApp.koin.get()

            started.await()

            assertTrue(viewModel.uiState.value.isRefreshing)

            finish.complete(Unit)
            runCurrent()

            assertFalse(viewModel.uiState.value.isRefreshing)
            assertNull(viewModel.uiState.value.errorMessage)

            viewModel.stop()
        } finally {
            stopKoin()
            Dispatchers.resetMain()
        }
    }
}

