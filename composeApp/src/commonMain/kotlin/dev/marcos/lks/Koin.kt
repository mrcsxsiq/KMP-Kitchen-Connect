package dev.marcos.lks

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}

