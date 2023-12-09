package io.github.sgpublic.gtwb

import io.github.sgpublic.gtwb.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main(args: Array<String>) {
    var port = 8080
    for (arg in args) {
        if (arg.startsWith("--port=")) {

        }
    }
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
}
