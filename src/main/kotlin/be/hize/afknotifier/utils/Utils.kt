package be.hize.afknotifier.utils

import java.util.Timer
import java.util.TimerTask
import kotlin.time.Duration

object Utils {
    fun runDelayed(
        duration: Duration,
        runnable: () -> Unit,
    ) {
        Timer().schedule(
            object : TimerTask() {
                override fun run() {
                    runnable()
                }
            },
            duration.inWholeMilliseconds,
        )
    }
}
