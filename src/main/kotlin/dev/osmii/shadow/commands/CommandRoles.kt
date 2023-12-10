package dev.osmii.shadow.commands

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.Namespace
import dev.osmii.shadow.enums.PlayableFaction
import dev.osmii.shadow.enums.PlayableRole
import dev.osmii.shadow.enums.PlayableSubfaction
import dev.osmii.shadow.game.rolelist.RolelistGUI
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class CommandRoles(val shadow: Shadow) : CommandExecutor {
    override fun onCommand(commandSender: CommandSender, command: Command, s: String, args: Array<String>): Boolean {
        val player = commandSender as Player

        if (args.isEmpty()) {
            RolelistGUI(shadow).showRoleBook(player)
            return false
        }

        when (args[0]) {
            "add" -> {
                if (args.size < 2) {
                    RolelistGUI(shadow).showAddRoleInventory(player)
                    return false
                }
            }
            "removeall" -> {
                shadow.gameState.originalRolelist.roles.clear()
                RolelistGUI(shadow).showRoleBook(player)
                return false
            }
            "remove" -> {
                if (args.size < 3) return false

                val page = args[2].toInt()
                val index = args[3].toInt()
                val id = page * 14 + index
                // Ensure that the ID to remove actually exists
                if(index == -1 || id > shadow.gameState.originalRolelist.roles.size || id < 0) return false
                shadow.gameState.originalRolelist.roles.removeAt(id)
                RolelistGUI(shadow).showRoleBook(player)
                return false
            }
        }

        return false
    }


}
