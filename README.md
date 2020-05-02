# KPolling2Webhook [![Build Status](https://travis-ci.org/lamba92/kpolling2webhook.svg?branch=master)](https://travis-ci.org/lamba92/kpolling2webhook) [ ![Download](https://api.bintray.com/packages/lamba92/com.github.lamba92/kpolling2webhook/images/download.svg) ](https://bintray.com/lamba92/com.github.lamba92/kpolling2webhook/_latestVersion)

A Kotlin/Multiplatform library to poll an endpoint, check for changes and notify a webhook.

Platforms available:
 - Windows x64
 - Linux x64
 - MacOS x64
 - JVM
 - JS
 
Inspired by [balsick/cron-webhook](https://github.com/balsick/cron-webhook) ❤️

# Usage

```kotlin
val flow = buildKPoller2WebhookFlow<Map<String, String>, String> {
    pollingRequest {
        url("http://worldtimeapi.org/api/ip/93.66.61.215.txt")
    }
    targetBodyTransform {
        buildMap {
            it.split("\n")
                .map { it.split(":") }
                .forEach { (k, v) -> put(k, v.removePrefix(" ")) }
        }
    }
    loadStateFunction { map }
    saveStateFunction { map = it }
    webhookRequest {
        request {
            url("https://webhook.site/48e18e0c-297d-4661-a7a9-c51d42905d93")
            body = it.toString()
        }
    }
    interval = 5.seconds
}
flow.collect { (currentState, webhookResponse) ->
    println("STATE: $currentState")
    println("WH RESPONSE BODY: $webhookResponse")
}
```
A builder for JSON target body is available as well: `buildJsonKPoller2WebhookFlow()`

# Import [ ![Download](https://api.bintray.com/packages/lamba92/com.github.lamba92/kpolling2webhook/images/download.svg) ](https://bintray.com/lamba92/com.github.lamba92/kpolling2webhook/_latestVersion)
If you have Gradle metadata enabled, just import the main module and gradle will sort out the rest.
```kotlin
repositories {
    maven("https://dl.bintray.com/lamba92/com.github.lamba92")
}
// ...
val commonMain by getting {
    dependencies {
        implementation("com.github.lamba92:kpolling2webhook:{latest_version}")
    }
}
```
Otherwise, have a look at the available artifacts here.
