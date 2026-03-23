package dev.marcos.lks.util

fun Throwable.formatDetailsForUser(maxStackLines: Int = 16): String = buildString {
    var t: Throwable? = this@formatDetailsForUser
    var first = true
    while (t != null) {
        if (!first) appendLine()
        append(t::class.simpleName ?: "Exception")
        append(": ")
        appendLine(t.message ?: "(sem mensagem)")
        first = false
        t = t.cause
    }
    appendLine()
    appendLine("— Stack (início) —")
    append(stackTraceToString().lineSequence().take(maxStackLines).joinToString("\n"))
}
