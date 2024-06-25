package be.hize.afknotifier.events

import be.hize.afknotifier.data.IslandType

class IslandChangeEvent(val newIsland: IslandType, val oldIsland: IslandType) : ModEvent()
