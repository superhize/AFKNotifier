package be.hize.afknotifier.utils

import be.hize.afknotifier.data.IslandType
import be.hize.afknotifier.data.ScoreboardData
import be.hize.afknotifier.data.TabListData
import be.hize.afknotifier.events.HypixelJoinEvent
import be.hize.afknotifier.events.IslandChangeEvent
import be.hize.afknotifier.events.ModTickEvent
import be.hize.afknotifier.events.ScoreboardUpdateEvent
import be.hize.afknotifier.events.SkyblockJoinEvent
import be.hize.afknotifier.events.TabListUpdateEvent
import be.hize.afknotifier.events.WorldChangeEvent
import be.hize.afknotifier.utils.RegexUtil.matchFirst
import be.hize.afknotifier.utils.RegexUtil.matches
import be.hize.afknotifier.utils.StringUtils.removeColor
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

object HypixelUtils {
    private val islandNamePattern = "(?:§.)*(Area|Dungeon): (?:§.)*(?<island>.*)".toPattern()
    private val guestPattern = "SKYBLOCK GUEST".toPattern()

    private var hypixelMain = false
    private var hypixelAlpha = false
    private var skyblock = false

    val onHypixel get() = (hypixelMain || hypixelAlpha) && Minecraft.getMinecraft().thePlayer != null
    val inSkyblock get() = skyblock && onHypixel
    var skyblockIsland = IslandType.UNKNOWN

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        hypixelMain = false
        hypixelAlpha = false
        skyblock = false
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldChangeEvent) {
        skyblock = false
    }

    @SubscribeEvent
    fun onTick(event: ModTickEvent) {
        if (!onHypixel) return
        val inSkyblock = checkScoreboard()

        if (inSkyblock && !skyblock) {
            SkyblockJoinEvent().postAndCatch()
        }

        if (skyblock == inSkyblock) return
        skyblock = inSkyblock
    }

    private fun checkScoreboard(): Boolean {
        val minecraft = Minecraft.getMinecraft()
        val world = minecraft.theWorld ?: return false
        val obj = world.scoreboard.getObjectiveInDisplaySlot(1) ?: return false
        val displayName = obj.displayName
        val title = displayName.removeColor()
        return title.contains("SKYBLOCK") || title.contains("SKIBLOCK")
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
        if (event.scoreboard.isEmpty()) return

        if (!onHypixel) {
            val last = event.scoreboard.last()
            hypixelMain = last == "§ewww.hypixel.net"
            hypixelAlpha = last == "§ealpha.hypixel.net"

            if (onHypixel) {
                HypixelJoinEvent().postAndCatch()
            }
        }
        if (!onHypixel) return
    }

    @SubscribeEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        checkIsland(event)
    }

    private fun checkIsland(event: TabListUpdateEvent) {
        var foundIsland = ""

        TabListData.getTabList().matchFirst(islandNamePattern) {
            foundIsland = group("island").removeColor()
        }

        val guest = guestPattern.matches(ScoreboardData.objectiveTitle.removeColor())
        val islandType = getIslandType(foundIsland, guest)
        if (skyblockIsland != islandType) {
            IslandChangeEvent(islandType, skyblockIsland).postAndCatch()
            skyblockIsland = islandType
        }
    }

    private fun getIslandType(name: String, guesting: Boolean): IslandType {
        val islandType = IslandType.getByNameOrUnknown(name)
        if (guesting) {
            if (islandType == IslandType.PRIVATE_ISLAND) return IslandType.PRIVATE_ISLAND_GUEST
            if (islandType == IslandType.GARDEN) return IslandType.GARDEN_GUEST
        }
        return islandType
    }
}
