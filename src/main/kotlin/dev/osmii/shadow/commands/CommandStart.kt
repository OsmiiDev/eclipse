package dev.osmii.shadow.commands

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.game.start.P0InitialCountdown
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandStart (var shadow: Shadow) : CommandExecutor {
    override fun onCommand(commandSender: CommandSender, command: Command, s: String, args: Array<String>): Boolean {
        val player = commandSender as Player

        P0InitialCountdown(shadow).startCountdown()
        return false
    }
}
