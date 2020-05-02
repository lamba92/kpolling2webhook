package com.github.lamba92.utils.kpolling2webhook.dsl

import com.github.lamba92.utils.kpolling2webhook.jsonPolling2WebhookFlow
import com.github.lamba92.utils.kpolling2webhook.polling2WebhookFlow
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

@OptIn(KronInternalAPI::class)
@ExperimentalTime
inline fun <reified R, reified T> buildKPoller2WebhookFlow(action: KPoller2WebhookBuilder<R, T>.() -> Unit) =
    KPoller2WebhookBuilder<R, T>().apply(action).build()

@OptIn(KronInternalAPI::class)
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

@KronInternalAPI
@ExperimentalTime
abstract class AbstractPoller2WebhookBuilder<R, T> constructor(
    @KronInternalAPI var pollingRequest: HttpRequestBuilder? = null,
    @KronInternalAPI var webhookRequest: ((R) -> HttpRequestBuilder)? = null,
    @KronInternalAPI var saveFunction: (suspend (R) -> Unit)? = null,
    @KronInternalAPI var loadFunction: (suspend () -> R?)? = null,
    @KronInternalAPI var httpClientSetup: HttpClientConfig<*>.() -> Unit = {},
    @KronInternalAPI var checkEquality: suspend (R, R) -> Boolean = { a, b -> a == b },
    var notifyOnFirstPoll: Boolean = true,
    var interval: Duration = 1.minutes
) {

    data class VerifiedParams<R>(
        val pollingRequest: HttpRequestBuilder,
        val webhookRequest: (R) -> HttpRequestBuilder,
        val saveFunction: suspend (R) -> Unit,
        val loadFunction: suspend () -> R?,
    )

    @KronInternalAPI
    companion object {
        fun <R, T> verifyDataFunction(builder: AbstractPoller2WebhookBuilder<R, T>): VerifiedParams<R> {
            val pr = builder.pollingRequest
            val wr = builder.webhookRequest
            val sf = builder.saveFunction
            val lf = builder.loadFunction
            require(pr != null) { "target request has not been set." }
            require(wr != null) { "webhook request has not been set." }
            require(sf != null) { "save function has not been set." }
            require(lf != null) { "load function has not been set." }
            return VerifiedParams(pr, wr, sf, lf)
        }
    }

    fun pollingRequest(action: HttpRequestBuilder.() -> Unit) {
        pollingRequest = request(action)
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
}

@KronInternalAPI
@ExperimentalTime
class KPoller2WebhookBuilder<R, T>(
    @KronInternalAPI var objectTransformer: (suspend (String) -> R)? = null,
    private val buildAction: (KPoller2WebhookBuilder<R, T>) -> Flow<Pair<R, T>>
) : AbstractPoller2WebhookBuilder<R, T>() {

    @KronInternalAPI
    companion object {
        inline operator fun <reified R, reified T> invoke() =
            KPoller2WebhookBuilder<R, T> {
                val (pr, wr, sf, lf) =
                    verifyDataFunction(it)
                val ot = it.objectTransformer
                require(ot != null) { "target request body transformation has not been set." }
                polling2WebhookFlow(
                    pr, wr, ot, sf, lf,
                    it.interval,
                    it.notifyOnFirstPoll,
                    it.checkEquality,
                    HttpClient(it.httpClientSetup)
                )
            }
    }

    fun pollingRequestBodyTransform(action: suspend (String) -> R) {
        objectTransformer = action
    }

    fun build() =
        buildAction(this)
}

@ExperimentalTime
@KronInternalAPI
class JsonKPoller2WebhookBuilder<R, T>(
    private val buildActon: (JsonKPoller2WebhookBuilder<R, T>) -> Flow<Pair<R, T>>
) : AbstractPoller2WebhookBuilder<R, T>() {

    companion object {

        @KronInternalAPI
        inline operator fun <reified R, reified T> invoke() =
            JsonKPoller2WebhookBuilder<R, T> {
                val (pr, wr, sf, lf) =
                    verifyDataFunction(it)
                jsonPolling2WebhookFlow(
                    pr, wr, sf, lf,
                    it.interval,
                    it.notifyOnFirstPoll,
                    it.checkEquality,
                    HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer()
                        }
                        it.httpClientSetup(this)
                    }
                )
            }
    }

    fun build() =
        buildActon(this)

}
