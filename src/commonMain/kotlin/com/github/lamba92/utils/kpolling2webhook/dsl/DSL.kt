package com.github.lamba92.utils.kpolling2webhook.dsl

import com.github.lamba92.utils.kpolling2webhook.jsonPolling2WebhookFlow
import com.github.lamba92.utils.kpolling2webhook.polling2WebhookFlow
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

@ExperimentalTime
inline fun <reified R, reified T> buildKPoller2WebhookFlow(action: KPoller2WebhookBuilder<R, T>.() -> Unit) =
    KPoller2WebhookBuilder<R, T>().apply(action).build()

@ExperimentalTime
inline fun <reified R, reified T> buildJsonKPoller2WebhookFlow(action: JsonKPoller2WebhookBuilder<R, T>.() -> Unit) =
    JsonKPoller2WebhookBuilder<R, T>().apply(action).build()

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
class KPoller2WebhookBuilder<R, T>(
    @KronInternalAPI var pollingRequest: HttpRequestBuilder? = null,
    @KronInternalAPI var webhookRequest: ((R) -> HttpRequestBuilder)? = null,
    @KronInternalAPI var objectTransformer: (suspend (String) -> R)? = null,
    @KronInternalAPI var saveFunction: (suspend (R) -> Unit)? = null,
    @KronInternalAPI var loadFunction: (suspend () -> R?)? = null,
    @KronInternalAPI var httpClientSetup: HttpClientConfig<*>.() -> Unit = {},
    @KronInternalAPI var checkEquality: suspend (R, R) -> Boolean = { a, b -> a == b },
    var notifyOnFirstPoll: Boolean = true,
    var interval: Duration = 1.minutes,
    private val buildAction: (KPoller2WebhookBuilder<R, T>) -> Flow<Pair<R, T>>
) {

    @OptIn(KronInternalAPI::class)
    companion object {
        inline operator fun <reified R, reified T> invoke() =
            KPoller2WebhookBuilder<R, T> {
                val tr = it.pollingRequest
                val wr = it.webhookRequest
                val ot = it.objectTransformer
                val sf = it.saveFunction
                val lf = it.loadFunction
                require(tr != null) { "target request has not been set." }
                require(wr != null) { "webhook request has not been set." }
                require(ot != null) { "target request body transformation has not been set." }
                require(sf != null) { "save function has not been set." }
                require(lf != null) { "load function has not been set." }
                polling2WebhookFlow(
                    tr, wr, ot, sf, lf,
                    it.interval,
                    it.notifyOnFirstPoll,
                    it.checkEquality,
                    HttpClient(it.httpClientSetup)
                )
            }
    }

    fun pollingRequest(action: HttpRequestBuilder.() -> Unit) {
        pollingRequest = request(action)
    }

    fun targetBodyTransform(action: suspend (String) -> R) {
        objectTransformer = action
    }

    fun saveStateFunction(action: suspend (R) -> Unit) {
        saveFunction = action
    }

    fun loadStateFunction(action: suspend () -> R?) {
        loadFunction = action
    }

    fun webhookRequest(action: (R) -> HttpRequestBuilder) {
        webhookRequest = action
    }

    fun deepEqualityCheck(action: suspend (R, R) -> Boolean) {
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
class JsonKPoller2WebhookBuilder<R, T>(
    @KronInternalAPI var pollingRequest: HttpRequestBuilder? = null,
    @KronInternalAPI var webhookRequest: ((R) -> HttpRequestBuilder)? = null,
    @KronInternalAPI var saveFunction: (suspend (R) -> Unit)? = null,
    @KronInternalAPI var loadFunction: (suspend () -> R?)? = null,
    @KronInternalAPI var httpClientSetup: HttpClientConfig<*>.() -> Unit = {},
    @KronInternalAPI var checkEquality: suspend (R, R) -> Boolean = { a, b -> a == b },
    var notifyOnFirstPoll: Boolean = true,
    var interval: Duration = 1.minutes,
    private val buildActon: (JsonKPoller2WebhookBuilder<R, T>) -> Flow<Pair<R, T>>
) {

    companion object {

        @OptIn(KronInternalAPI::class)
        inline operator fun <reified R, reified T> invoke() =
            JsonKPoller2WebhookBuilder<R, T> {
                val pr = it.pollingRequest
                val wr = it.webhookRequest
                val sf = it.saveFunction
                val lf = it.loadFunction
                require(pr != null) { "target request has not been set." }
                require(wr != null) { "webhook request has not been set." }
                require(sf != null) { "save function has not been set." }
                require(lf != null) { "load function has not been set." }
                jsonPolling2WebhookFlow(
                    pr, wr, sf, lf,
                    it.interval,
                    it.notifyOnFirstPoll,
                    it.checkEquality,
                    HttpClient(it.httpClientSetup)
                )
            }
    }

    fun pollingRequest(action: HttpRequestBuilder.() -> Unit) {
        pollingRequest = request(action)
    }

    fun webhookRequest(action: (R) -> HttpRequestBuilder) {
        webhookRequest = action
    }

    fun saveStateFunction(action: suspend (R) -> Unit) {
        saveFunction = action
    }

    fun loadStateFunction(action: suspend () -> R?) {
        loadFunction = action
    }

    fun deepEqualityCheck(action: suspend (R, R) -> Boolean) {
        checkEquality = action
    }

    fun httpClientConfiguration(action: HttpClientConfig<*>.() -> Unit) {
        httpClientSetup = action
    }

    fun build() =
        buildActon(this)

}
