package be.hize.afknotifier.utils

import io.github.notenoughupdates.moulconfig.internal.KeybindHelper
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

object KeyboardUtil {
    fun isControlKeyDown() = isKeyHeld(Keyboard.KEY_LCONTROL) || isKeyHeld(Keyboard.KEY_RCONTROL)

    private fun isKeyHeld(keyCode: Int): Boolean {
        if (keyCode == 0) return false
        return if (keyCode < 0) {
            Mouse.isButtonDown(keyCode + 100)
        } else {
            KeybindHelper.isKeyDown(keyCode)
        }
    }
}
