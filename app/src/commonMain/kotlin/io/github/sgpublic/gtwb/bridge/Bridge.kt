package io.github.sgpublic.gtwb.bridge

import de.jensklingenberg.ktorfit.converter.KtorfitResult
import io.github.sgpublic.gtwb.logger
import io.github.sgpublic.gtwb.utils.XmlGlobal
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.statement.*
import io.ktor.server.application.*

var TeamCityAuth: BasicAuthCredentials? = null

class TeamcityDelegate(
    private val id: Int
) {
    fun forward(call: ApplicationCall) {

    }
}

suspend fun teamcityWebhook(buildConfId: String): TeamcityDelegate? {
    val resp = try {
        TeamcityBridge.vcsRootInstances("buildType=$buildConfId")
    } catch (e: Throwable) {
        logger.error("No buildType of name \"$buildConfId\"", e)
        return null
    }
    val instances = XmlGlobal.decodeFromString(
        VcsRootInstances.serializer(), resp
    )
    if (instances.count != instances.content.size) {
        throw IllegalStateException("Unknown response of vcsRootInstances selector!")
    }
    return TeamcityDelegate(id = instances.content[0].id)
}
