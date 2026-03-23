package dev.marcos.lks

import dev.marcos.lks.data.datasources.remote.DashboardApi
import dev.marcos.lks.data.datasources.remote.InMemoryDatabase
import dev.marcos.lks.data.datasources.remote.OrderHistoryApi
import dev.marcos.lks.data.repositories.DashboardRepository
import dev.marcos.lks.data.repositories.DashboardRepositoryApi
import dev.marcos.lks.dashboard.DashboardViewModel
import dev.marcos.lks.makeorder.MakeOrderViewModel
import dev.marcos.lks.data.repositories.OrderHistoryRepository
import dev.marcos.lks.data.repositories.OrderHistoryRepositoryApi
import dev.marcos.lks.orderhistory.OrderHistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { InMemoryDatabase() }
    single<DashboardRepositoryApi> { DashboardRepository(get(), get()) }
    single<OrderHistoryRepositoryApi> { OrderHistoryRepository(get<OrderHistoryApi>(), get()) }
    
    viewModel { DashboardViewModel(get()) }
    viewModel { MakeOrderViewModel(get()) }
    viewModel { OrderHistoryViewModel(get()) }
}
