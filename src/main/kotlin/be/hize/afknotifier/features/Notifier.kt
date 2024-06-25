package be.hize.afknotifier.features

import be.hize.afknotifier.AFKNotifier
import be.hize.afknotifier.data.IslandType
import be.hize.afknotifier.events.IslandChangeEvent
import be.hize.afknotifier.events.SecondPassedEvent
import be.hize.afknotifier.events.SkyblockJoinEvent
import be.hize.afknotifier.utils.DiscordUtil
import be.hize.afknotifier.utils.HypixelUtils
import be.hize.afknotifier.utils.Logger
import be.hize.afknotifier.utils.SimpleTimeMark
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

object Notifier {

    private val config get() = AFKNotifier.feature.main
    private val logger = Logger("notifier")
    private var tryNumber = 0
    private var messageSent = false
    private var islandMessageSent = false
    private var check = false
    private var isCheck = false
    private var lastIslandChange = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (HypixelUtils.inSkyblock) return
        if (messageSent) return
        if (!check) return
        if (lastIslandChange.passedSince() < 2.seconds) return

        tryNumber++
        logger.log("You are not in skyblock!! §7(Try $tryNumber of ${config.retryValue})")

        if (tryNumber >= config.retryValue) {
            val users = validateUserList(config.userTagList.get())
            DiscordUtil.sendAfkWarning(config.lobbyMessage, users)
            logger.log("Sent message to discord.")
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

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (!isEnabled()) return

        lastIslandChange = SimpleTimeMark.now()
        tryNumber = 0

        if (event.newIsland == IslandType.PRIVATE_ISLAND) {
            isCheck = true
            islandMessageSent = false
        }

        if (!config.onIslandChange) return
        if (islandMessageSent) return
        if (!isCheck) return

        val old = event.oldIsland
        if (old == config.islandType) {
            val users = validateUserList(config.userTagList.get())
            DiscordUtil.sendAfkWarning(config.islandLeaveMessage, users)
            logger.log("Private island leave message sent.")
            islandMessageSent = true
            isCheck = false
        }
    }

    private fun validateUserList(users: String): String? {
        val list = users.split(",")
        if (list.any { !it.matches("\\d+".toRegex()) }) return null
        return list.joinToString(" ") { user -> "<@$user>" }
    }

    private fun isEnabled() = Minecraft.getMinecraft().theWorld != null && config.enabled
}
