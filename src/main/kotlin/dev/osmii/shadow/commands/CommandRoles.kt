package dev.osmii.shadow.commands

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.game.start.L0ResetStates
import dev.osmii.shadow.game.start.L1SelectLocation
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandRoles(var shadow: Shadow) : CommandExecutor {
    override fun onCommand(commandSender: CommandSender, command: Command, s: String, args: Array<String>): Boolean {
        val player = commandSender as Player


        return false
    }
}
