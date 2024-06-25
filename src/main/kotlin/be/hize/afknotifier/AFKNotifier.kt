package be.hize.afknotifier

import be.hize.afknotifier.AFKNotifier.Companion.MODID
import be.hize.afknotifier.config.Features
import be.hize.afknotifier.config.core.ConfigManager
import be.hize.afknotifier.data.Commands
import be.hize.afknotifier.data.FixedRateTimerManager
import be.hize.afknotifier.data.MinecraftData
import be.hize.afknotifier.data.ScoreboardData
import be.hize.afknotifier.events.ModTickEvent
import be.hize.afknotifier.features.Notifier
import be.hize.afknotifier.utils.HypixelUtils
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(
    modid = MODID,
    clientSideOnly = true,
    useMetadata = true,
    guiFactory = "be.hize.afknotifier.config.core.ConfigGuiForgeInterop",
)
class AFKNotifier {
    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent?) {
        loadModule(this)

        loadModule(HypixelUtils)
        loadModule(MinecraftData())
        loadModule(ScoreboardData)
        loadModule(Notifier)
        loadModule(FixedRateTimerManager)

        Commands.init()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent?) {
        configManager = ConfigManager
        configManager.firstLoad()
        Runtime.getRuntime().addShutdownHook(
            Thread {
                configManager
                    .saveConfig("shutdown-hook")
            },
        )

        // loadModule(AutoUpdate)
    }

    private fun loadModule(obj: Any) {
        modules.add(obj)
        MinecraftForge.EVENT_BUS.register(obj)
    }

    @SubscribeEvent
    fun onTick(event: ModTickEvent) {
        if (screenToOpen != null) {
            Minecraft.getMinecraft().displayGuiScreen(screenToOpen)
            screenToOpen = null
        }
    }

    companion object {
        const val MODID = "AFKNotifier"

        @JvmStatic
        val version: String get() = Loader.instance().indexedModList[MODID]!!.version

        @JvmStatic
        val feature: Features get() = configManager.features

        lateinit var configManager: ConfigManager

        private val logger: Logger = LogManager.getLogger("AFKNotifier")

        fun consoleLog(message: String) {
            logger
                .log(Level.INFO, message)
        }

        private val modules: MutableList<Any> = ArrayList()
        private val globalJob: Job = Job(null)

        val coroutineScope =
            CoroutineScope(
                CoroutineName("AFKNotifier") + SupervisorJob(globalJob),
            )

        var screenToOpen: GuiScreen? = null
    }
}
