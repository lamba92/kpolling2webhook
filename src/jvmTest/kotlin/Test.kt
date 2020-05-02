import com.github.lamba92.utils.kpolling2webhook.dsl.KronInternalAPI
import com.github.lamba92.utils.kpolling2webhook.dsl.buildKPoller2WebhookFlow
import io.ktor.client.request.url
import io.ktor.http.Url
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

class Test {

    var map: Map<String, String>? = null

    @ExperimentalStdlibApi
    @OptIn(ExperimentalTime::class, KronInternalAPI::class)
//    @Test
    fun testtt() = runBlocking {
        val flow = buildKPoller2WebhookFlow<Map<String, String>> {
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
    }

}
