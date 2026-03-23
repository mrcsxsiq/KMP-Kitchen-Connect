package dev.marcos.lks.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderStatus
import dev.marcos.lks.data.repositories.DashboardRepositoryApi
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private suspend fun ViewModel.stop() {
    val job = viewModelScope.coroutineContext[Job] ?: return
    job.cancelAndJoin()
}

private class FakeDashboardRepository(
    override val orders: MutableStateFlow<List<Order>> = MutableStateFlow(emptyList()),
    private val fetchDashboardOrdersImpl: suspend () -> Unit,
) : DashboardRepositoryApi {
    var fetchCalls: Int = 0
        private set

    val updateCalls: MutableList<Pair<String, OrderStatus>> = mutableListOf()

    override suspend fun fetchDashboardOrders() {
        fetchCalls++
        fetchDashboardOrdersImpl()
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        updateCalls.add(orderId to newStatus)
    }
}

class DashboardViewModelTest {
    @Test
    fun `Given blocked refresh when viewmodel starts then isRefreshing true and then false`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repoBlock = kotlinx.coroutines.CompletableDeferred<Unit>()
            val repoCanFinish = kotlinx.coroutines.CompletableDeferred<Unit>()

            val repo = FakeDashboardRepository(
                fetchDashboardOrdersImpl = {
                    repoBlock.complete(Unit)
                    repoCanFinish.await()
                }
            )

            val koinApp = startKoin {
                modules(
                    module {
                        single<DashboardRepositoryApi> { repo }
                        factory { DashboardViewModel(get()) }
                    }
                )
            }
            val viewModel: DashboardViewModel = koinApp.koin.get()

            repoBlock.await()
            assertTrue(viewModel.uiState.value.isRefreshing)

            repoCanFinish.complete(Unit)
            runCurrent()

            assertFalse(viewModel.uiState.value.isRefreshing)
            assertEquals(1, repo.fetchCalls)

            viewModel.stop()
        } finally {
            stopKoin()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `Given polling when time advances by five seconds then refresh is executed again`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repo = FakeDashboardRepository(fetchDashboardOrdersImpl = {})

            val koinApp = startKoin {
                modules(
                    module {
                        single<DashboardRepositoryApi> { repo }
                        factory { DashboardViewModel(get()) }
                    }
                )
            }
            val viewModel: DashboardViewModel = koinApp.koin.get()

            runCurrent()
            assertEquals(1, repo.fetchCalls)

            advanceTimeBy(5_000)
            runCurrent()

            assertEquals(2, repo.fetchCalls)

            viewModel.stop()
        } finally {
            stopKoin()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `Given update order status when updateOrderStatus is called then repository is updated`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repo = FakeDashboardRepository(fetchDashboardOrdersImpl = {})

            val koinApp = startKoin {
                modules(
                    module {
                        single<DashboardRepositoryApi> { repo }
                        factory { DashboardViewModel(get()) }
                    }
                )
            }
            val viewModel: DashboardViewModel = koinApp.koin.get()

            runCurrent()

            viewModel.updateOrderStatus(orderId = "#1", newStatus = OrderStatus.READY)
            runCurrent()

            assertEquals(listOf("#1" to OrderStatus.READY), repo.updateCalls)

            viewModel.stop()
        } finally {
            stopKoin()
            Dispatchers.resetMain()
        }
    }
}

