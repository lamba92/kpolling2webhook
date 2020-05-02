# KPolling2Webhook
A Kotlin/Multiplatform library to poll an endpoint, check for changes and notify a webhook.

Inspired by [balsick/cron-webhook](https://github.com/balsick/cron-webhook) ❤️

# Usage

```kotlin
buildKPoller2WebhookFlow<Map<String, String>> {
            targetRequest {
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
            webhookUrl = Url("https://webhook.site/48e18e0c-297d-4661-a7a9-c51d42905d93")
            interval = 5.seconds
        }
        flow.collect { println(it) }
```
A builder for JSON target body is available as well: `buildJsonKPoller2WebhookFlow()`
