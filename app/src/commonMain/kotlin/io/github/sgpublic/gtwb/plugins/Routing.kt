package io.github.sgpublic.gtwb.plugins

import io.github.sgpublic.gtwb.bridge.teamcityWebhook
import io.github.sgpublic.gtwb.logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        post("/{buildConfId}") request@{
            try {
                val buildConfId = call.parameters["buildConfId"]?.takeIf { it.isNotBlank() }
                if (buildConfId == null) {
                    call.respond(HttpStatusCode.BadRequest, "You must pass a buildConfId!")
                    return@request
                }
                if (buildConfId == "favicon.ico") {
                    call.respond(HttpStatusCode.NotFound, "404 Not Found!")
                    return@request
                }
                val bridge = teamcityWebhook(buildConfId)
                if (bridge == null) {
                    call.respond(HttpStatusCode.BadGateway, "Failed to get info by buildConfId!")
                    return@request
                }
                bridge.forward(call)
            } catch (e: Throwable) {
                logger.error("Failed to forward webhook request!", e)
                call.respond(HttpStatusCode.InternalServerError, "Server internal error")
            }
        }
    }
}
