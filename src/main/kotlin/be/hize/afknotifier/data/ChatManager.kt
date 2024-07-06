package be.hize.afknotifier.data

import be.hize.afknotifier.events.ChatEvent
import be.hize.afknotifier.utils.StringUtils.stripHypixelMessage
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class ChatManager {

    @SubscribeEvent(receiveCanceled = true)
    fun onChatReceive(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return

        val original = event.message
        val message = original.formattedText.stripHypixelMessage()

        if (message.startsWith("§f{\"server\":\"")) return

        ChatEvent(message, original).postAndCatch()
    }

}
