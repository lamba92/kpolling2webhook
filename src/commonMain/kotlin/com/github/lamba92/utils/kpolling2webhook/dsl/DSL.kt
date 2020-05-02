package com.github.lamba92.utils.kpolling2webhook.dsl

import com.github.lamba92.utils.kpolling2webhook.jsonPolling2WebhookFlow
import com.github.lamba92.utils.kpolling2webhook.polling2WebhookFlow
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

@ExperimentalTime
inline fun <reified RESPONSE> buildKPoller2WebhookFlow(action: KPoller2WebhookBuilder<RESPONSE>.() -> Unit) =
    KPoller2WebhookBuilder<RESPONSE>().apply(action).build()

@ExperimentalTime
inline fun <reified RESPONSE> buildJsonKPoller2WebhookFlow(action: JsonKPoller2WebhookBuilder<RESPONSE>.() -> Unit) =
    JsonKPoller2WebhookBuilder<RESPONSE>().apply(action).build()

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is internal and should not be used. It could be removed or changed without notice."
)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.CONSTRUCTOR
)
annotation class KronInternalAPI

@OptIn(KronInternalAPI::class)
@ExperimentalTime
class KPoller2WebhookBuilder<RESPONSE>(
    @KronInternalAPI var request: HttpRequestBuilder? = null,
    @KronInternalAPI var objectTransformer: (suspend (String) -> RESPONSE)? = null,
    @KronInternalAPI var saveFunction: (suspend (RESPONSE) -> Unit)? = null,
    @KronInternalAPI var loadFunction: (suspend () -> RESPONSE?)? = null,
    @KronInternalAPI var httpClientSetup: HttpClientConfig<*>.() -> Unit = {},
    @KronInternalAPI var checkEquality: suspend (RESPONSE, RESPONSE) -> Boolean = { a, b -> a == b },
    var webhookUrl: Url? = null,
    var notifyOnFirstPoll: Boolean = true,
    var interval: Duration = 1.minutes,
    private val buildAction: (KPoller2WebhookBuilder<RESPONSE>) -> Flow<RESPONSE>
) {

    @OptIn(KronInternalAPI::class)
    companion object {
        inline operator fun <reified R> invoke() =
            KPoller2WebhookBuilder<R> {
                val r = it.request
                val ot = it.objectTransformer
                val wh = it.webhookUrl
                val sf = it.saveFunction
                val lf = it.loadFunction
                require(r != null) { "target request has not been set." }
                require(ot != null) { "target request body transformation has not been set." }
                require(wh != null) { "webhook url has not been set." }
                require(sf != null) { "save function has not been set." }
                require(lf != null) { "load function has not been set." }
                polling2WebhookFlow(
                    r, ot, wh, sf, lf,
                    it.interval,
                    it.notifyOnFirstPoll,
                    it.checkEquality,
                    HttpClient(it.httpClientSetup)
                )
            }
    }

    fun targetRequest(action: HttpRequestBuilder.() -> Unit) {
        request = request(action)
    }

    fun targetBodyTransform(action: suspend (String) -> RESPONSE) {
        objectTransformer = action
    }

    fun saveStateFunction(action: suspend (RESPONSE) -> Unit) {
        saveFunction = action
    }

    fun loadStateFunction(action: suspend () -> RESPONSE?) {
        loadFunction = action
    }

    fun deepEqualityCheck(action: suspend (RESPONSE, RESPONSE) -> Boolean) {
        checkEquality = action
    }

    fun httpClientConfiguration(action: HttpClientConfig<*>.() -> Unit) {
        httpClientSetup = action
    }

    fun build() =
        buildAction(this)
}

@ExperimentalTime
@OptIn(KronInternalAPI::class)
class JsonKPoller2WebhookBuilder<RESPONSE>(
    @KronInternalAPI var request: HttpRequestBuilder? = null,
    @KronInternalAPI var saveFunction: (suspend (RESPONSE) -> Unit)? = null,
    @KronInternalAPI var loadFunction: (suspend () -> RESPONSE?)? = null,
    @KronInternalAPI var httpClientSetup: HttpClientConfig<*>.() -> Unit = {},
    @KronInternalAPI var checkEquality: suspend (RESPONSE, RESPONSE) -> Boolean = { a, b -> a == b },
    var webhookUrl: Url? = null,
    var notifyOnFirstPoll: Boolean = true,
    var interval: Duration = 1.minutes,
    private val buildActon: (JsonKPoller2WebhookBuilder<RESPONSE>) -> Flow<RESPONSE>
) {

    companion object {

        @OptIn(KronInternalAPI::class)
        inline operator fun <reified RESPONSE> invoke() =
            JsonKPoller2WebhookBuilder<RESPONSE> {
                val r = it.request
                val wh = it.webhookUrl
                val sf = it.saveFunction
                val lf = it.loadFunction
                require(r != null) { "target request has not been set." }
                require(wh != null) { "webhook url has not been set." }
                require(sf != null) { "save function has not been set." }
                require(lf != null) { "load function has not been set." }
                jsonPolling2WebhookFlow(
                    r, wh, sf, lf,
                    it.interval,
                    it.notifyOnFirstPoll,
                    it.checkEquality,
                    HttpClient(it.httpClientSetup)
                )
            }
    }

    fun targetRequest(action: HttpRequestBuilder.() -> Unit) {
        request = request(action)
    }

    fun saveStateFunction(action: suspend (RESPONSE) -> Unit) {
        saveFunction = action
    }

    fun loadStateFunction(action: suspend () -> RESPONSE?) {
        loadFunction = action
    }

    fun deepEqualityCheck(action: suspend (RESPONSE, RESPONSE) -> Boolean) {
        checkEquality = action
    }

    fun httpClientConfiguration(action: HttpClientConfig<*>.() -> Unit) {
        httpClientSetup = action
    }

    fun build() =
        buildActon(this)

}
