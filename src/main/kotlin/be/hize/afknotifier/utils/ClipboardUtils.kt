package be.hize.afknotifier.utils

import be.hize.afknotifier.AFKNotifier
import be.hize.afknotifier.data.CopyErrorCommand
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import kotlin.time.Duration.Companion.milliseconds

object ClipboardUtils {
    private var lastClipboardAccessTime = SimpleTimeMark.farPast()

    private fun canAccessClipboard(): Boolean {
        val result = lastClipboardAccessTime.passedSince() > 10.milliseconds
        if (result) {
            lastClipboardAccessTime = SimpleTimeMark.now()
        }
        return result
    }

    private suspend fun getClipboard(): Clipboard? {
        val deferred = CompletableDeferred<Clipboard?>()
        if (canAccessClipboard()) {
            deferred.complete(Toolkit.getDefaultToolkit().systemClipboard)
        } else {
            Utils.runDelayed(5.milliseconds) {
                AFKNotifier.coroutineScope.launch {
                    deferred.complete(getClipboard())
                }
            }
        }
        return deferred.await()
    }

    fun copyToClipboard(
        text: String,
        step: Int = 0,
    ) {
        AFKNotifier.coroutineScope.launch {
            try {
                getClipboard()?.setContents(StringSelection(text), null)
            } catch (e: Exception) {
                if (step == 3) {
                    CopyErrorCommand.logError(e, "Error while trying to access the clipboard.")
                } else {
                    copyToClipboard(text, step + 1)
                }
            }
        }
    }
}