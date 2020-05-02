import com.github.lamba92.utils.kpolling2webhook.dsl.KronInternalAPI
import com.github.lamba92.utils.kpolling2webhook.dsl.buildKPoller2WebhookFlow
import io.ktor.client.request.request
import io.ktor.client.request.url
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

class Test {

    var map: Map<String, String>? = null

    @ExperimentalStdlibApi
    @OptIn(ExperimentalTime::class, KronInternalAPI::class)
//    @Test
    fun testtt() = runBlocking {
        val flow = buildKPoller2WebhookFlow<Map<String, String>, String> {
            pollingRequest {
                url("http://worldtimeapi.org/api/ip/93.66.61.215.txt")
            }
            pollingRequestBodyTransform {
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
    }

}
