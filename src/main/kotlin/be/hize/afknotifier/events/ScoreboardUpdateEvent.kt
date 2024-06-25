package be.hize.afknotifier.events

class ScoreboardUpdateEvent(
    val scoreboard: List<String>,
) : ModEvent()