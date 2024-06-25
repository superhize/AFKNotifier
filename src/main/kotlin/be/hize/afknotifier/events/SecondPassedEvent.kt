package be.hize.afknotifier.events

class SecondPassedEvent(private val totalSeconds: Int) : ModEvent() {
    fun repeatSeconds(i: Int) = totalSeconds % i == 0
}
