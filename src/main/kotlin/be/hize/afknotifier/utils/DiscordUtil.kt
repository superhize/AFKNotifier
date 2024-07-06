package be.hize.afknotifier.utils

import be.hize.afknotifier.AFKNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicNameValuePair
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

object DiscordUtil {
    private val logger = Logger("discord_webhook")
    private val config get() = AFKNotifier.feature.main
    private val builder: HttpClientBuilder =
        HttpClients.custom().setUserAgent("AFKNotifier/${AFKNotifier.version}")
            .setDefaultHeaders(
                mutableListOf(
                    BasicHeader("Pragma", "no-cache"),
                    BasicHeader("Cache-Control", "no-cache"),
                ),
            )
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .build(),
            )
            .useSystemProperties()


    @JvmStatic
    fun sendTestMessage() {
        sendDiscordMessage("This is a test. (It worked)", true)
    }

    private fun sendDiscordMessage(content: String, isTest: Boolean = false): CompletableFuture<*> {
        return CompletableFuture.supplyAsync {
            val client = builder.build()
            try {
                val post = HttpPost(config.webhook)
                val params = ArrayList<BasicNameValuePair>()
                params.add(BasicNameValuePair("content", content))
                post.entity = UrlEncodedFormEntity(params, StandardCharsets.UTF_8)
                val response = client.execute(post)
                val entity = response.entity
                if (entity == null) {
                    logger.log("Message sent to webhook.")
                    if (isTest) {
                        showPlayerMessage {
                            text("Message sent!")
                        }
                    }
                    response.close()
                    return@supplyAsync
                } else {
                    logger.log("Something wrong, but i don't know what..")
                    if (isTest) {
                        showPlayerMessage {
                            text("Something wrong, but i don't know what..")
                        }
                    }
                    response.close()
                    return@supplyAsync
                }
            } catch (ex: Exception) {
                logger.log("An error occurred while trying to send the message.")
                logger.log("Error: ${ex.message}")
                if (isTest) {
                    showPlayerMessage(MessageMode.ERROR) {
                        text("Error sending the message, please check your log for more details.")
                    }
                }
                return@supplyAsync
            } finally {
                client.close()
            }
        }
    }

    fun sendAfkWarning(msg: String) {
        AFKNotifier.coroutineScope.launch(Dispatchers.IO) {
            val users = validateUserList(config.userTagList.get())
            val username = Minecraft.getMinecraft().session.username
            val usersList = users ?: "Invalid users tag list."
            val message = """
            $usersList
            ${msg.replace("%%user%%", username)}
            """.trimIndent()
            sendDiscordMessage(message)
        }
    }

    private fun validateUserList(users: String): String? {
        val list = users.split(",")
        if (list.any { !it.matches("\\d+".toRegex()) }) return null
        return list.joinToString(" ") { user -> "<@$user>" }
    }
}
