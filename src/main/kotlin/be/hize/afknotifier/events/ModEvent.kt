package be.hize.afknotifier.events

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event

open class ModEvent : Event() {
    private val eventName by lazy {
        this::class.simpleName
    }

    fun postAndCatch(): Boolean =
        runCatching {
            postWithoutCatch()
        }.onFailure {
        }.getOrDefault(isCanceled)

    private fun postWithoutCatch() = MinecraftForge.EVENT_BUS.post(this)
}