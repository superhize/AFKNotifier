package be.hize.afknotifier.utils

import be.hize.afknotifier.data.CopyErrorCommand
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration

object DelayedRun {

    private val tasks = mutableListOf<Pair<() -> Any, SimpleTimeMark>>()
    private val futureTasks = ConcurrentLinkedQueue<Pair<() -> Any, SimpleTimeMark>>()

    fun runDelayed(duration: Duration, run: () -> Unit): SimpleTimeMark {
        val time = SimpleTimeMark.now() + duration
        futureTasks.add(Pair(run, time))
        return time
    }

    fun checkRuns() {
        tasks.removeIf { (runnable, time) ->
            val inPast = time.isInPast()
            if (inPast) {
                try {
                    runnable()
                } catch (e: Exception) {
                    CopyErrorCommand.logError(e, "DelayedRun task crashed while executing")
                }
            }
            inPast
        }
        futureTasks.drainTo(tasks)
    }

    inline fun <reified E, reified L : MutableCollection<E>>
        Queue<E>.drainTo(list: L): L {
        while (true)
            list.add(this.poll() ?: break)
        return list
    }
}
