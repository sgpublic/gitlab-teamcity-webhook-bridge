package io.github.sgpublic.gtwb.bridge

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.github.sgpublic.gtwb.logger as mainLogger

var TeamCityHost: String? = null
private val HttpClient: HttpClient by lazy {
    HttpClient(CIO) {
        install(Auth) {
            basic {
                credentials {
                    TeamCityAuth
                }
            }
        }
        install(Logging) {
            val headerSet = setOf(
                HttpHeaders.Authorization,
                "X-Gitlab-Token",
            )
            logger = object : Logger {
                override fun log(message: String) {
                    mainLogger.info(message)
                }
            }
            sanitizeHeader { header ->
                headerSet.contains(header)
            }
        }
    }
}
val TeamcityBridge: ITeamcityBridge by lazy {
    return@lazy Ktorfit.Builder()
        .converterFactories(ContentConverterFactory)
        .baseUrl(TeamCityHost!!)
        .httpClient(HttpClient)
        .build()
        .create()
}

interface ITeamcityBridge {
    @GET("app/rest/vcs-root-instances")
    suspend fun vcsRootInstances(
        @Query("locator") locator: String
    ): String
}

object ContentConverterFactory: Converter.Factory {
    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit
    ): Converter.SuspendResponseConverter<HttpResponse, *> {
        return ContentConverter(typeData, ktorfit)
    }

    class ContentConverter(
        private val typeData: TypeData,
        private val ktorfit: Ktorfit,
    ): Converter.SuspendResponseConverter<HttpResponse, Any> {
        @Suppress("OVERRIDE_DEPRECATION")
        override suspend fun convert(response: HttpResponse): Any {
            return response.bodyAsText()
        }
    }
}
