package be.hize.afknotifier.features

import be.hize.afknotifier.AFKNotifier
import be.hize.afknotifier.events.SecondPassedEvent
import be.hize.afknotifier.events.SkyblockJoinEvent
import be.hize.afknotifier.utils.DiscordUtil
import be.hize.afknotifier.utils.HypixelUtils
import be.hize.afknotifier.utils.MessageMode
import be.hize.afknotifier.utils.showPlayerMessage
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Notifier {

    private val config get() = AFKNotifier.feature.main
    private var tryNumber = 0
    private var messageSent = false
    private var check = false

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (HypixelUtils.inSkyblock) return
        if (messageSent) return
        if (!check) return

        tryNumber++
        showPlayerMessage(MessageMode.ERROR) {
            text("You are not in skyblock!! §7(Try $tryNumber of ${config.retryValue})")
        }
        if (tryNumber >= config.retryValue) {
            val users = validateUserList(config.userTagList.get())
            DiscordUtil.sendAfkWarning(users)
            tryNumber = 0
            messageSent = true
            check = false
        }
    }

    @SubscribeEvent
    fun onSkyblockJoin(event: SkyblockJoinEvent) {
        messageSent = false
        check = true
    }

    private fun validateUserList(users: String): String? {
        val list = users.split(",")
        if (list.any { !it.matches("\\d+".toRegex()) }) return null
        return list.joinToString(" ") { user -> "<@$user>" }
    }

    private fun isEnabled() = Minecraft.getMinecraft().theWorld != null && config.enabled
}
