package com.github.lamba92.utils.kpolling2webhook

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.request
import io.ktor.http.Url
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

@ExperimentalTime
inline fun <reified R> polling2WebhookFlow(
    targetRequest: HttpRequestBuilder,
    noinline objectTransformer: suspend (String) -> R,
    webhookUrl: Url,
    noinline saveFunction: suspend (R) -> Unit,
    noinline loadFunction: suspend () -> R?,
    interval: Duration = 1.minutes,
    notifyOnFirstPoll: Boolean = true,
    noinline checkEquality: suspend (R, R) -> Boolean = { a, b -> a == b },
    httpClient: HttpClient = HttpClient(),
) = flow {
    while (coroutineContext.isActive) {
        emit(httpClient.request<String>(targetRequest))
        delay(interval)
    }
}
    .map(objectTransformer)
    .map { it to loadFunction() }
    .filter { (currentState, previousState) ->
        previousState == null || checkEquality(previousState, currentState).not()
    }
    .onEach { (currentState, _) -> saveFunction(currentState) }
    .filter { (_, previousState) ->
        previousState == null && notifyOnFirstPoll || previousState != null
    }
    .map { (currentState, _) ->
        httpClient.get<Unit>(webhookUrl)
        currentState
    }

@ExperimentalTime
inline fun <reified R> jsonPolling2WebhookFlow(
    targetRequest: HttpRequestBuilder,
    webhookUrl: Url,
    noinline saveFunction: suspend (R) -> Unit,
    noinline loadFunction: suspend () -> R?,
    interval: Duration = 1.minutes,
    notifyOnFirstPoll: Boolean = true,
    noinline checkEquality: suspend (R, R) -> Boolean = { a, b -> a == b },
    httpClient: HttpClient = HttpClient { install(JsonFeature) { serializer = KotlinxSerializer() } },
) = flow {
    while (coroutineContext.isActive) {
        emit(httpClient.request<R>(targetRequest) to loadFunction())
        delay(interval)
    }
}
    .filter { (currentState, previousState) ->
        previousState == null || checkEquality(previousState, currentState).not()
    }
    .onEach { (currentState, _) -> saveFunction(currentState) }
    .filter { (_, previousState) ->
        previousState == null && notifyOnFirstPoll || previousState != null
    }
    .map { (currentState, _) ->
        httpClient.get<Unit>(webhookUrl)
        currentState
    }
