package com.gnovack.dnditemmanager.services

import com.gnovack.dnditemmanager.BuildKonfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

expect val platformEngine: HttpClientEngine

class DNDApiClient(engine: HttpClientEngine = platformEngine, baseHostOverride: String? = null) {
    @OptIn(ExperimentalSerializationApi::class)
    private val client = HttpClient(engine) {
        expectSuccess = true

        defaultRequest {
            contentType(ContentType.Application.Json)

            url {
                if (baseHostOverride == null){
                    host = BuildKonfig.BASE_API_HOST
                    protocol = URLProtocol.byName[BuildKonfig.BASE_API_PROTOCOL]!!
                    port = BuildKonfig.BASE_API_PORT.toInt()
                } else {
                    host = baseHostOverride.split("/").last()
                    protocol = URLProtocol.HTTPS
                }
            }
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                namingStrategy = JsonNamingStrategy.SnakeCase
            })
        }
    }

    suspend fun getItems(limit: Int? = null, search: String? = null, source: String? = null, rarity: String? = null): List<Item> {
        return client.get {
            url {
                appendPathSegments("items")

                limit?.let { parameters.append("limit", limit.toString())}

                search?.let { parameters.append("search", it)}
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

    suspend fun importCharacter(characterId: String): Character {
        @Serializable
        data class RequestBody(val characterDndbeyondId: String)

        return client.post {
            url {
                appendPathSegments("characters", "dndbeyond_import")
            }

            setBody(RequestBody(characterId))
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
    val imageUrl: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Item

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

@Serializable
data class Character(
    var id: String? = null,
    val avatarUrl: String? = null,
    val name: String,
    val race: String,
    val dndClass: String,
    val level: Int,
    val strengthScore: Int? = null,
    val dexterityScore: Int? = null,
    val constitutionScore: Int? = null,
    val intelligenceScore: Int? = null,
    val wisdomScore: Int? = null,
    val charismaScore: Int? = null,
    var inventory: List<Item> = listOf(),
) {
    @Transient
    val nameField = FormField(name) {
        value -> value.isNotBlank()
    }
    @Transient
    val raceField = FormField(race) {
        value -> value.isNotBlank()
    }
    @Transient
    val classField = FormField(dndClass) {
        value -> value.isNotBlank()
    }
    @Transient
    val levelField = FormField(level) {
        value -> value in 1..20
    }
    @Transient
    val isValid = listOf(
        nameField,
        raceField,
        classField,
        levelField,
    ).all { it.isValid }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Character

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}