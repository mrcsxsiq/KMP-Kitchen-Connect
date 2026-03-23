package dev.marcos.lks

import de.jensklingenberg.ktorfit.Ktorfit
import dev.marcos.lks.data.datasources.remote.DashboardApi
import dev.marcos.lks.data.datasources.remote.OrderApi
import dev.marcos.lks.data.datasources.remote.OrderHistoryApi
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

enum class HttpClientType {
    AUTHENTICATED,
    NON_AUTHENTICATED
}

class HttpClientFactory {
    fun getClient(isAuthenticated: Boolean): Ktorfit {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        val httpClient = HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }

        return Ktorfit.Builder()
            .httpClient(httpClient)
            .baseUrl("http://${host}:8080/")
            .build()
    }
}

val networkModule = module {
    single { HttpClientFactory() }

    single<Ktorfit>(named(HttpClientType.AUTHENTICATED.name)) { getHttpClient(isAuthenticated = true) }
    single<Ktorfit>(named(HttpClientType.NON_AUTHENTICATED.name)) { getHttpClient(isAuthenticated = false) }

    factory<DashboardApi> { getNonAuthenticatedClient().create<DashboardApi>() }
    factory<OrderHistoryApi> { getNonAuthenticatedClient().create<OrderHistoryApi>() }
    factory<OrderApi> { getNonAuthenticatedClient().create<OrderApi>() }
}

private fun Scope.getHttpClient(isAuthenticated: Boolean): Ktorfit =
    get<HttpClientFactory>().getClient(isAuthenticated)

private fun Scope.getAuthenticatedClient(): Ktorfit =
    get<Ktorfit>(named(HttpClientType.AUTHENTICATED.name))

private fun Scope.getNonAuthenticatedClient(): Ktorfit =
    get<Ktorfit>(named(HttpClientType.NON_AUTHENTICATED.name))

