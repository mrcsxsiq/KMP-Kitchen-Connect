package dev.marcos.lks

import dev.marcos.lks.data.repositories.DashboardRepository
import dev.marcos.lks.dashboard.DashboardViewModel
import dev.marcos.lks.makeorder.MakeOrderViewModel
import dev.marcos.lks.data.repositories.OrderHistoryRepository
import dev.marcos.lks.orderhistory.OrderHistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { DashboardRepository() }
    single { OrderHistoryRepository() }
    
    viewModel { DashboardViewModel(get()) }
    viewModel { MakeOrderViewModel(get()) }
    viewModel { OrderHistoryViewModel(get()) }
}
