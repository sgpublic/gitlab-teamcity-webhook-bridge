package io.github.sgpublic.gtwb.bridge

import io.github.sgpublic.gtwb.logger
import io.github.sgpublic.gtwb.utils.XmlGlobal
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import io.ktor.utils.io.*

var TeamCityBasicAuth: BasicAuthCredentials? = null
var TeamCityBearerAuth: BearerTokens? = null

class TeamcityDelegate(
    private val id: Int
) {
    @OptIn(InternalAPI::class)
    suspend fun forward(call: ApplicationCall) {
        val requestHeader = call.request.headers
        val resp = TeamcityWebhook.commitHookNotification(
            locator = "id:$id",
            UserAgent = requestHeader.getAll(HttpHeaders.UserAgent),
            XGitlabEvent = requestHeader.getAll("X-Gitlab-Event"),
            XGitlabWebhookUUID = requestHeader.getAll("X-Gitlab-Webhook-UUID"),
            XGitlabInstance = requestHeader.getAll("X-Gitlab-Instance"),
            XGitlabEventUUID = requestHeader.getAll("X-Gitlab-Event-UUID"),
            XGitlabToken = requestHeader.getAll("X-Gitlab-Token"),
            call.receiveText(),
        )
        resp.headers.forEach { key, values ->
            for (value in values) {
                try {
                    call.response.headers.append(key, value)
                } catch (e: Throwable) {
                    logger.debug("Failed to add header: $key")
                }
            }
        }
        call.respondBytesWriter(
            contentType = resp.contentType(),
            status = resp.status,
            contentLength = resp.contentLength(),
        ) {
            resp.content.run {
                copyAndClose(this@respondBytesWriter)
                close()
            }
        }
        logger.info("request forward success.")
    }
}

suspend fun teamcityWebhook(buildConfId: String): TeamcityDelegate? {
    val resp = try {
        TeamcityBridge.vcsRootInstances("buildType:$buildConfId").bodyAsText()
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
