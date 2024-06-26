package be.hize.afknotifier.features

import be.hize.afknotifier.AFKNotifier
import be.hize.afknotifier.events.IslandChangeEvent
import be.hize.afknotifier.events.SecondPassedEvent
import be.hize.afknotifier.events.SkyblockJoinEvent
import be.hize.afknotifier.utils.DelayedRun
import be.hize.afknotifier.utils.DiscordUtil
import be.hize.afknotifier.utils.HypixelUtils
import be.hize.afknotifier.utils.Logger
import be.hize.afknotifier.utils.SimpleTimeMark
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent
import kotlin.time.Duration.Companion.seconds

object Notifier {

    private val config get() = AFKNotifier.feature.main
    private val logger = Logger("notifier")
    private var tryNumber = 0
    private var messageSent = false
    private var islandMessageSent = false
    private var check = false
    private var isCheck = false
    private var connected = false
    private var disconnected = false
    private var lastIslandChange = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (!config.enabled) return
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
    fun onDisconnect(event: ClientDisconnectionFromServerEvent) {
        if (!config.onDisconnect) return
        disconnected = true
        connected = false
        AFKNotifier.coroutineScope.launch {
            DelayedRun.runDelayed(5.seconds) {
                println(connected)
                if (!connected) {
                    val users = validateUserList(config.userTagList.get())
                    DiscordUtil.sendAfkWarning("%%user%% has disconnected.", users)
                    logger.log("You have disconnected, sent the message to discord.")
                }
            }
        }

    }

    @SubscribeEvent
    fun onConnect(event: ClientConnectedToServerEvent) {
        if (!config.onDisconnect) return
        println("connect")
        connected = true
        disconnected = false
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (!isEnabled()) return
        if (!config.onIslandChange) return
        lastIslandChange = SimpleTimeMark.now()
        tryNumber = 0

        if (event.newIsland == config.islandType) {
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

    private fun isEnabled() = Minecraft.getMinecraft().theWorld != null
}
