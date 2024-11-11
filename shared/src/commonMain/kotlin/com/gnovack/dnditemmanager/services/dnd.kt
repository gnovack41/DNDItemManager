package com.gnovack.dnditemmanager.services

import com.gnovack.dnditemmanager.BuildKonfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

expect val platformEngine: HttpClientEngine

class DNDApiClient(engine: HttpClientEngine = platformEngine, baseHost: String = BuildKonfig.BASE_API_HOST) {
    private val client = HttpClient(engine) {
        expectSuccess = true

        defaultRequest {
            url {
                host = baseHost
                protocol = URLProtocol.byName[BuildKonfig.BASE_API_PROTOCOL]!!
                port = BuildKonfig.BASE_API_PORT.toInt()
            }
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getItems(limit: Int? = null, source: String? = null, rarity: String? = null): List<Item> {
        return client.get {
            url {
                appendPathSegments("items")

                limit?.let { parameters.append("limit", limit.toString())}
                source?.let { parameters.append("source", it)}
                rarity?.let { parameters.append("rarity", it)}
            }
        }.body()
    }

    suspend fun getItem(id: String): Item {
        return client.get {
            url {
                appendPathSegments("items", id)
            }
        }.body()
    }

    suspend fun getSources(): List<String> {
        return client.get {
            url {
                appendPathSegments("items", "sources")
            }
        }.body()
    }

    suspend fun getRarities(): List<String> {
        return client.get {
            url {
                appendPathSegments("items", "rarities")
            }
        }.body()
    }
}

@Serializable
data class Item(
    val id: String,
    val name: String? = null,
    val rarity: String? = null,
    val source: String? = null,
    val description: String? = null,
)

