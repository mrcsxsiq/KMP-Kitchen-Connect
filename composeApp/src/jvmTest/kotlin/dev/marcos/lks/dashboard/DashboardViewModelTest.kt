package dev.marcos.lks.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.model.OrderStatus
import dev.marcos.lks.data.repositories.DashboardRepositoryApi
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
            // Dado: uma chamada ao repositório bloqueada para conseguirmos inspecionar "in flight".
            val repoBlock = kotlinx.coroutines.CompletableDeferred<Unit>()
            val repoCanFinish = kotlinx.coroutines.CompletableDeferred<Unit>()

            val repo = FakeDashboardRepository(
                fetchDashboardOrdersImpl = {
                    repoBlock.complete(Unit)
                    repoCanFinish.await()
                }
            )

            // Quando: instanciamos o ViewModel.
            val viewModel = DashboardViewModel(repository = repo)

            // Então: o flag deve ficar true enquanto o refresh está em andamento.
            repoBlock.await()
            assertTrue(viewModel.isRefreshing)

            // Quando: liberamos o repositório e processamos as coroutines pendentes.
            repoCanFinish.complete(Unit)
            runCurrent()

            // Então: o flag deve voltar para false.
            assertFalse(viewModel.isRefreshing)
            assertEquals(1, repo.fetchCalls)

            viewModel.stop()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `Given polling when time advances by five seconds then refresh is executed again`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            // Dado: um repositório fake que conta quantas vezes o polling chama refresh.
            val repo = FakeDashboardRepository(fetchDashboardOrdersImpl = {})

            // Quando: instanciamos o ViewModel (ele inicia o polling no init).
            val viewModel = DashboardViewModel(repository = repo)

            // Então: a primeira execução do polling deve acontecer.
            runCurrent() // run first refresh started by polling loop
            assertEquals(1, repo.fetchCalls)

            // Quando: avançamos 5s para disparar o próximo loop.
            advanceTimeBy(5_000)
            runCurrent() // run second refresh after delay

            // Então: deve ter executado novamente.
            assertEquals(2, repo.fetchCalls)

            viewModel.stop()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `Given update order status when updateOrderStatus is called then repository is updated`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            // Dado: um repositório fake.
            val repo = FakeDashboardRepository(fetchDashboardOrdersImpl = {})

            // Quando: instanciamos o ViewModel e deixamos o init começar/terminar.
            val viewModel = DashboardViewModel(repository = repo)

            runCurrent() // allow init polling refresh to start/end (no time advance)

            // Quando: chamamos updateOrderStatus no ViewModel.
            viewModel.updateOrderStatus(orderId = "#1", newStatus = OrderStatus.READY)
            runCurrent()

            // Então: o repositório deve ter recebido a atualização.
            assertEquals(listOf("#1" to OrderStatus.READY), repo.updateCalls)

            viewModel.stop()
        } finally {
            Dispatchers.resetMain()
        }
    }
}

