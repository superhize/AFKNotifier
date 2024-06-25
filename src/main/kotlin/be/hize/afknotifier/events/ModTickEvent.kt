package be.hize.afknotifier.events

class ModTickEvent(
    private val tick: Int,
) : ModEvent() {
    fun isMod(i: Int) = tick % i == 0
}