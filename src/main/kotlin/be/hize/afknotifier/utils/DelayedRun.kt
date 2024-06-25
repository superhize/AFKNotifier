package be.hize.afknotifier.utils

import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration

object DelayedRun {

    private val futureTasks = ConcurrentLinkedQueue<Pair<() -> Any, SimpleTimeMark>>()

    fun runDelayed(duration: Duration, run: () -> Unit): SimpleTimeMark {
        val time = SimpleTimeMark.now() + duration
        futureTasks.add(Pair(run, time))
        return time
    }
}
