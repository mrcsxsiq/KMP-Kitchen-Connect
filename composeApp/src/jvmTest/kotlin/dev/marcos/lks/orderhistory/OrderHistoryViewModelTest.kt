package dev.marcos.lks.orderhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.marcos.lks.data.model.MenuItem
import dev.marcos.lks.data.model.Order
import dev.marcos.lks.data.repositories.OrderHistoryRepositoryApi
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

class OrderHistoryViewModelTest {
    @Test
    fun `Given successful fetch when refresh completes then isRefreshing false and errorMessage is null`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            // Dado: um repositório fake que retorna sucesso.
            val repo = FakeOrderHistoryRepository(fetchHistoryOrdersImpl = {})

            // Quando: instanciamos o ViewModel.
            val viewModel = OrderHistoryViewModel(repository = repo)

            runCurrent()

            // Então: isRefreshing deve terminar em false e errorMessage deve ficar null.
            assertFalse(viewModel.isRefreshing)
            assertNull(viewModel.errorMessage)

            viewModel.stop()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `Given failing fetch when refresh completes then errorMessage is set and isRefreshing false`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            // Dado: um repositório fake que falha ao carregar o histórico.
            val repo = FakeOrderHistoryRepository(fetchHistoryOrdersImpl = {
                throw RuntimeException("boom")
            })

            // Quando: instanciamos o ViewModel.
            val viewModel = OrderHistoryViewModel(repository = repo)

            runCurrent()

            // Então: errorMessage deve ser setado e isRefreshing deve terminar em false.
            assertFalse(viewModel.isRefreshing)
            assertEquals(
                "Falha ao carregar o histórico. Verifique sua conexão.",
                viewModel.errorMessage
            )

            viewModel.stop()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `Given blocked fetch when refresh in flight then isRefreshing true and then false`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            // Dado: um repositório fake que bloqueia o refresh para testarmos o estado "em voo".
            val started = kotlinx.coroutines.CompletableDeferred<Unit>()
            val finish = kotlinx.coroutines.CompletableDeferred<Unit>()

            val repo = FakeOrderHistoryRepository(fetchHistoryOrdersImpl = {
                started.complete(Unit)
                finish.await()
            })

            // Quando: instanciamos o ViewModel.
            val viewModel = OrderHistoryViewModel(repository = repo)

            started.await()

            // Então: isRefreshing deve estar true enquanto o repositório ainda está bloqueado.
            assertTrue(viewModel.isRefreshing)

            // Quando: liberamos o repositório e processamos as coroutines pendentes.
            finish.complete(Unit)
            runCurrent()

            // Então: isRefreshing deve voltar para false e errorMessage deve ser null.
            assertFalse(viewModel.isRefreshing)
            assertNull(viewModel.errorMessage)

            viewModel.stop()
        } finally {
            Dispatchers.resetMain()
        }
    }
}

