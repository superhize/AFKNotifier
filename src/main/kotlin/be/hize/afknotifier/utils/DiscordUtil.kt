package be.hize.afknotifier.utils

import be.hize.afknotifier.AFKNotifier
import be.hize.afknotifier.config.core.ConfigManager.gson
import net.minecraft.client.Minecraft
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object DiscordUtil {
    private val logger = Logger("discord_webhook")
    private val config get() = AFKNotifier.feature.main

    @JvmStatic
    fun sendTestMessage() {
        sendMessage("This is a test. (It worked)")
    }

    private fun sendMessage(str: String) {
        val url = config.webhook
        val client = OkHttpClient()
        val msg = mapOf("content" to str)
        val json = gson.toJson(msg)

        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                logger.log("Error sending message to webhook. Code: ${response.code}")
                logger.log("Message: ${response.body?.string()}")
            } else {
                logger.log("Message sent to webhook.")
            }
        }
    }

    fun sendAfkWarning(users: String?) {
        val username = Minecraft.getMinecraft().session.username
        val usersList = users ?: "Invalid users tag list."
        val message = """
            $usersList
            ${config.messageToSend.replace("%%user%%", username)}
        """.trimIndent()
        sendMessage(message)
    }
}
