package dev.marcos.lks

import androidx.compose.ui.window.ComposeUIViewController
import dev.marcos.lks.order.OrderScreen

fun MainViewController() = ComposeUIViewController { OrderScreen() }