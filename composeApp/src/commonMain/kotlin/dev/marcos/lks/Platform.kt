package dev.marcos.lks

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform