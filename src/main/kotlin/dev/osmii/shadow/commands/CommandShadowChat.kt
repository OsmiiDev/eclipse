package dev.osmii.shadow.commands

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.GamePhase
import dev.osmii.shadow.enums.PlayableRole
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandShadowChat(var shadow: Shadow) : CommandExecutor {
    override fun onCommand(commandSender: CommandSender, command: Command, s: String, args: Array<String>): Boolean {
        val player = commandSender as Player

        if (shadow.gameState.currentPhase != GamePhase.GAME_IN_PROGRESS) {
            player.sendMessage(
                MiniMessage.miniMessage().deserialize("<red>You can't use this command right now.</red>")
            )
            return false
        }
        if (shadow.gameState.currentRoles[player.uniqueId] != PlayableRole.SHADOW) {
            player.sendMessage(
                MiniMessage.miniMessage().deserialize("<red>You must be a shadow to use this command.</red>")
            )
            return false
        }

        val message = args.joinToString(" ")
        shadow.server.onlinePlayers.forEach { p ->
            if (shadow.gameState.currentRoles[p.uniqueId] == PlayableRole.SHADOW) {
                p.sendMessage(
                    MiniMessage.miniMessage()
                        .deserialize("<red>[Shadow Chat] </red><white>${player.name} » </white><gray>$message</gray>")
                )
            }
        }
        return false
    }
}