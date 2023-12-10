package io.github.sgpublic.gtwb

import io.github.sgpublic.gtwb.bridge.TeamCityBasicAuth
import io.github.sgpublic.gtwb.bridge.TeamCityBearerAuth
import io.github.sgpublic.gtwb.bridge.TeamCityHost
import io.github.sgpublic.gtwb.plugins.configureRouting
import io.github.sgpublic.gtwb.utils.GtwbScope
import io.github.sgpublic.gtwb.utils.getenv
import io.ktor.client.plugins.auth.providers.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.logging.*

val logger = KtorSimpleLogger("main")

fun realMain(args: Array<String>) {
    var port = 8080
    var host = "0.0.0.0"
    var teamcityHost: String? = null
    var teamcityUser: String? = null
    var teamcityToken: String? = null
    for (arg in args) {
        if (arg.startsWith("--port=")) {
            port = arg.substring(7).toIntOrNull()
                ?: throw IllegalArgumentException("Unknown arg: $arg")
        } else if (arg.startsWith("--host=")) {
            host = arg.substring(7)
        } else if (arg.startsWith("--teamcity-user=")) {
            teamcityUser = arg.substring(16)
        } else if (arg.startsWith("--teamcity-token=")) {
            teamcityToken = arg.substring(17)
        }
    }

    getenv("GTWB_PORT")?.toIntOrNull()?.let {
        port = it
    }
    getenv("GTWB_HOST")?.let {
        host = it
    }

    getenv("GTWB_TEAMCITY_HOST")?.let {
        teamcityHost = it
    }
    if (teamcityHost == null) {
        throw IllegalArgumentException("You must set GTWB_TEAMCITY_HOST or --teamcity-host=xxx to run bridge!")
    }
    TeamCityHost = teamcityHost

    getenv("GTWB_TEAMCITY_USER")?.let {
        teamcityUser = it
    }
    getenv("GTWB_TEAMCITY_TOKEN")?.let {
        teamcityToken = it
    }
    if (teamcityUser == null) {
        throw IllegalArgumentException("You must set GTWB_TEAMCITY_USER or --teamcity-user=xxx to run bridge!")
    }
    if (teamcityToken == null) {
        throw IllegalArgumentException("You must set GTWB_TEAMCITY_TOKEN or --teamcity-token=xxx to run bridge!")
    }
    logger.info("Connect to Teamcity ($TeamCityHost) with user: $teamcityUser")
    TeamCityBasicAuth = BasicAuthCredentials(teamcityUser!!, teamcityToken!!)
    TeamCityBearerAuth = BearerTokens(TeamCityBasicAuth?.password!!, "")

    GtwbScope.embeddedServer(
        factory = CIO,
        port = port,
        host = host,
        module = Application::module,
    ).start(wait = true)
}

fun postExit(e: Throwable?) {
    if (e != null) {
        logger.error("bridge exit unexpected!", e)
    } else {
        logger.info("bridge exit.")
    }
}

expect fun runWithCancelable(args: Array<String>)

fun main(args: Array<String>) {
    runWithCancelable(args)
}

fun Application.module() {
    configureRouting()
}
