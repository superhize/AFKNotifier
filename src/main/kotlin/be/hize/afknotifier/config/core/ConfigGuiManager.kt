package be.hize.afknotifier.config.core

import be.hize.afknotifier.AFKNotifier
import io.github.notenoughupdates.moulconfig.gui.GuiScreenElementWrapper
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor

object ConfigGuiManager {
    val editor by lazy { MoulConfigEditor(ConfigManager.processor) }

    fun openConfigGui(search: String? = null) {
        if (search != null) {
            editor.search(search)
        }
        AFKNotifier.screenToOpen = GuiScreenElementWrapper(editor)
    }
}
