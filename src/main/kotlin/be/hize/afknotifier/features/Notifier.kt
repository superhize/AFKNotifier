package be.hize.afknotifier.features

import be.hize.afknotifier.AFKNotifier
import be.hize.afknotifier.data.ScoreboardData
import be.hize.afknotifier.events.ChatEvent
import be.hize.afknotifier.events.IslandChangeEvent
import be.hize.afknotifier.events.SecondPassedEvent
import be.hize.afknotifier.events.SkyblockJoinEvent
import be.hize.afknotifier.utils.DelayedRun
import be.hize.afknotifier.utils.DiscordUtil
import be.hize.afknotifier.utils.HypixelUtils
import be.hize.afknotifier.utils.Logger
import be.hize.afknotifier.utils.RegexUtil.matchFirst
import be.hize.afknotifier.utils.RegexUtil.matchMatcher
import be.hize.afknotifier.utils.SimpleTimeMark
import be.hize.afknotifier.utils.TimeUtil.format
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent
import kotlin.time.Duration.Companion.minutes
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

    private var lobbyRestarting = false
    private var restartReason = ""
    private var restartingIn = ""

    //private var hoppityCall = false

    private val restartingPattern = "§cServer closing: (?<minutes>\\d+):(?<seconds>\\d+) ?§8.*".toPattern()
    private val rebootReasonPattern = "§c\\[Important] §r§eThis server will restart soon: §r§b(?<reason>.*)".toPattern()
    private val hoppityCallPattern = "§e✆ §r§aHoppity.*".toPattern()

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (!config.enabled) return
        if (HypixelUtils.inSkyblock) return

        ScoreboardData.sidebarLinesFormatted.matchFirst(restartingPattern) {
            val minutes = group("minutes").toInt().minutes
            val seconds = group("seconds").toInt().seconds
            val totalTime = minutes + seconds
            if (totalTime > 2.minutes && totalTime.inWholeSeconds % 30 != 0L) return
            lobbyRestarting = true
            restartingIn = totalTime.format()
        }

        if (messageSent) return
        if (!check) return
        if (lastIslandChange.passedSince() < 2.seconds) return

        tryNumber++
        logger.log("You are not in skyblock!! §7(Try $tryNumber of ${config.retryValue})")

        if (tryNumber >= config.retryValue) {
            DiscordUtil.sendAfkWarning(config.lobbyMessage)
            logger.log("Sent message to discord.")
            tryNumber = 0
            messageSent = true
            check = false
        }
    }

    @SubscribeEvent
    fun onChat(event: ChatEvent) {
        if (!isEnabled()) return
        rebootReasonPattern.matchMatcher(event.message) {
            lobbyRestarting = true
            restartReason = group("reason")

            if (config.messageWhenRestart) {
                val text = buildString {
                    append("Server is restarting")
                    if (restartingIn.isNotEmpty()) {
                        append(" in $restartingIn")
                    }
                    append(". Reason: $restartReason")
                }
                DiscordUtil.sendAfkWarning(text)
            }
        }
    }

    @SubscribeEvent
    fun onHoppityCall(event: ChatEvent) {
        if (!isEnabled()) return
        hoppityCallPattern.matchMatcher(event.message) {
            //hoppityCall = true
                logger.log("Detected Hoppity Call")
            if (config.hoppityCall) {
                DiscordUtil.sendAfkWarning("Hoppity has called %%user%%")
                logger.log("Sending Hoppity Call Notification")
            }
        }
    }

    @SubscribeEvent
    fun onSkyblockJoin(event: SkyblockJoinEvent) {
        messageSent = false
        check = true
        restartReason = ""
    }

    @SubscribeEvent
    fun onDisconnect(event: ClientDisconnectionFromServerEvent) {
        if (!config.onDisconnect) return
        disconnected = true
        connected = false
        AFKNotifier.coroutineScope.launch {
            DelayedRun.runDelayed(5.seconds) {
                if (!connected) {
                    DiscordUtil.sendAfkWarning("%%user%% has disconnected.")
                    logger.log("You have disconnected, sent the message to discord.")
                }
            }
        }

    }

    @SubscribeEvent
    fun onConnect(event: ClientConnectedToServerEvent) {
        if (!config.onDisconnect) return
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
            lobbyRestarting = false
        }

        if (islandMessageSent) return
        if (!isCheck) return

        val old = event.oldIsland
        if (old == config.islandType) {
            if (config.onlyOnLobbyRestart && !lobbyRestarting) return
            DiscordUtil.sendAfkWarning(
                config.islandLeaveMessage.replace("%%island%%", old.displayName.uppercase())
            )
            logger.log("Private island leave message sent.")
            islandMessageSent = true
            isCheck = false
            lobbyRestarting = false
        }
    }

    fun sendFakeMessage(args: Array<String>) {
        val message = args.joinToString(" ").replace("&&", "§")
        Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(message))
        ChatEvent(message, ChatComponentText(message)).postAndCatch()
    }

    private fun isEnabled() = Minecraft.getMinecraft().theWorld != null
}
