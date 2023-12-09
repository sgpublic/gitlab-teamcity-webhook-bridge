package io.github.sgpublic.gtwb.plugins

import io.github.sgpublic.gtwb.bridge.teamcityWebhook
import io.github.sgpublic.gtwb.logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/{buildConfId}") {
            try {
                val buildConfId = call.parameters["buildConfId"]?.takeIf { it.isNotBlank() }
                if (buildConfId == null) {
                    call.respond(HttpStatusCode.BadRequest, "You must pass a buildConfId!")
                    return@get
                }
                if (buildConfId == "favicon.ico") {
                    call.respond(HttpStatusCode.NotFound, "404 Not Found!")
                    return@get
                }
                val bridge = teamcityWebhook(buildConfId)
                if (bridge == null) {
                    call.respond(HttpStatusCode.NotFound, "You must pass a buildConfId!")
                    return@get
                }
                bridge.forward(call)
            } catch (e: Throwable) {
                logger.error("Failed to forward webhook request!", e)
            }
        }
    }
}
