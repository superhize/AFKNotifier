package be.hize.afknotifier.data

import be.hize.afknotifier.events.ModTickEvent
import be.hize.afknotifier.events.TabListUpdateEvent
import be.hize.afknotifier.utils.DelayedRun
import be.hize.afknotifier.utils.HypixelUtils
import be.hize.afknotifier.utils.StringUtils.stripHypixelMessage
import com.google.common.collect.ComparisonChain
import com.google.common.collect.Ordering
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.world.WorldSettings
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.time.Duration.Companion.seconds

object TabListData {
    private var tablistCache = emptyList<String>()
    private var debugCache: List<String>? = null

    fun getTabList() = debugCache ?: tablistCache

    private val playerOrdering = Ordering.from(PlayerComparator())

    @SideOnly(Side.CLIENT)
    internal class PlayerComparator : Comparator<NetworkPlayerInfo> {

        override fun compare(o1: NetworkPlayerInfo, o2: NetworkPlayerInfo): Int {
            val team1 = o1.playerTeam
            val team2 = o2.playerTeam
            return ComparisonChain.start().compareTrueFirst(
                o1.gameType != WorldSettings.GameType.SPECTATOR,
                o2.gameType != WorldSettings.GameType.SPECTATOR,
            )
                .compare(
                    if (team1 != null) team1.registeredName else "",
                    if (team2 != null) team2.registeredName else "",
                )
                .compare(o1.gameProfile.name, o2.gameProfile.name).result()
        }
    }

    private fun readTabList(): List<String>? {
        val thePlayer = Minecraft.getMinecraft()?.thePlayer ?: return null
        val players = playerOrdering.sortedCopy(thePlayer.sendQueue.playerInfoMap)
        val result = mutableListOf<String>()
        for (info in players) {
            val name = Minecraft.getMinecraft().ingameGUI.tabList.getPlayerName(info)
            result.add(name.stripHypixelMessage())
        }
        return result.dropLast(1)
    }

    @SubscribeEvent
    fun onTick(event: ModTickEvent) {
        if (!event.isMod(2)) return
        val tabList = readTabList() ?: return
        if (tablistCache != tabList) {
            tablistCache = tabList
            TabListUpdateEvent(getTabList()).postAndCatch()
            if (!HypixelUtils.onHypixel){
                workaroundDelayedTabListUpdateAgain()
            }
        }
    }

    private fun workaroundDelayedTabListUpdateAgain() {
        DelayedRun.runDelayed(2.seconds) {
            if (HypixelUtils.onHypixel) {
                println("workaroundDelayedTabListUpdateAgain")
                TabListUpdateEvent(getTabList()).postAndCatch()
            }
        }
    }
}
