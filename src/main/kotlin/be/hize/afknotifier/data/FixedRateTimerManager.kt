package be.hize.afknotifier.data

import be.hize.afknotifier.events.SecondPassedEvent
import net.minecraft.client.Minecraft
import kotlin.concurrent.fixedRateTimer

object FixedRateTimerManager {
    private var totalSeconds = 0

    init {
        fixedRateTimer(name = "afknotifier-fixed-rate-timer-manager", period = 1000L) {
            Minecraft.getMinecraft().addScheduledTask {
                SecondPassedEvent(totalSeconds).postAndCatch()
                totalSeconds++
            }
        }
    }
}
