package be.hize.afknotifier.data

import be.hize.afknotifier.config.core.ConfigGuiManager
import be.hize.afknotifier.features.update.AutoUpdate
import net.minecraft.command.ICommandSender
import net.minecraftforge.client.ClientCommandHandler

object Commands {
    private val openConfig: (Array<String>) -> Unit = {
        if (it.isNotEmpty()) {
            ConfigGuiManager.openConfigGui(it.joinToString(" "))
        } else {
            ConfigGuiManager.openConfigGui()
        }
    }

    fun init() {
        registerCommand("afk", openConfig)
        registerCommand("afknotifier", openConfig)
        registerCommand("afkn", openConfig)
        registerCommand("afkupdate") { AutoUpdate.onCommand() }

        registerCommand("afkcopyerror") { CopyErrorCommand.command(it) }
    }

    private fun registerCommand(
        name: String,
        function: (Array<String>) -> Unit,
    ) {
        ClientCommandHandler.instance.registerCommand(SimpleCommand(name, createCommand(function)))
    }

    private fun createCommand(function: (Array<String>) -> Unit) =
        object : SimpleCommand.ProcessCommandRunnable() {
            override fun processCommand(
                sender: ICommandSender?,
                args: Array<String>?,
            ) {
                if (args != null) function(args.asList().toTypedArray())
            }
        }
}