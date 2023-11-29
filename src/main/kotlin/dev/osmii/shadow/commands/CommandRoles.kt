package dev.osmii.shadow.commands

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.Namespace
import dev.osmii.shadow.enums.PlayableFaction
import dev.osmii.shadow.enums.PlayableRole
import dev.osmii.shadow.enums.PlayableSubfaction
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
            showRoleBook(player)
            return false
        }

        when (args[0]) {
            "add" -> {
                if (args.size < 2) {
                    showAddRoleInventory(player)
                    return false
                }
            }
            "removeall" -> {
                shadow.gameState.originalRolelist.roles.clear()
                return false
            }
        }

        return false
    }

    private fun showAddRoleInventory(player: Player) {
        val inv = Bukkit.createInventory(null, 54, Component.text("Add Role").color(NamedTextColor.BLUE))

        PlayableRole.entries.forEach { role ->
            if (role == PlayableRole.SPECTATOR) return@forEach
            val item = ItemStack(role.roleIcon)
            item.itemMeta = item.itemMeta.apply {
                displayName(Component.text(role.roleName).color(role.roleColor))
                persistentDataContainer.set(Namespace.CUSTOM_ID, PersistentDataType.STRING, "role-select-add-role")
                persistentDataContainer.set(
                    Namespace.ROLE_SELECT_ADD_ROLE,
                    PersistentDataType.STRING,
                    "role-${role.name}"
                )
                addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS, ItemFlag.HIDE_ATTRIBUTES)
            }
            inv.addItem(item)
        }

        PlayableSubfaction.entries.forEach { role ->
            if (role == PlayableSubfaction.SPECTATOR) return@forEach
            val item = ItemStack(role.factionIcon)

            val lower = role.name.lowercase(Locale.getDefault()).replace("_", " ")
            val word1 = lower.split(" ").first()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val word2 = lower.split(" ").last()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val name = "$word1 $word2"
            item.itemMeta = item.itemMeta.apply {
                displayName(Component.text(name).color(role.color).decoration(TextDecoration.ITALIC, false))
                persistentDataContainer.set(Namespace.CUSTOM_ID, PersistentDataType.STRING, "role-select-add-role")
                persistentDataContainer.set(
                    Namespace.ROLE_SELECT_ADD_ROLE,
                    PersistentDataType.STRING,
                    "subfaction-${role.name}"
                )
                addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS, ItemFlag.HIDE_ATTRIBUTES)
            }
            inv.addItem(item)
        }

        PlayableFaction.entries.forEach { role ->
            if (role == PlayableFaction.SPECTATOR) return@forEach
            val item = ItemStack(role.icon)

            val name = role.name.lowercase(Locale.getDefault()).replace("_", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            item.itemMeta = item.itemMeta.apply {
                displayName(
                    Component.text("Random ").color(NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(name).color(role.color).decoration(TextDecoration.ITALIC, false))
                )
                persistentDataContainer.set(Namespace.CUSTOM_ID, PersistentDataType.STRING, "role-select-add-role")
                persistentDataContainer.set(
                    Namespace.ROLE_SELECT_ADD_ROLE,
                    PersistentDataType.STRING,
                    "faction-${role.name}"
                )
                addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS, ItemFlag.HIDE_ATTRIBUTES)
            }
            inv.addItem(item)
        }

        val special = ItemStack(Material.DIAMOND)
        special.itemMeta = special.itemMeta.apply {
            displayName(
                MiniMessage.miniMessage().deserialize("<!i><gray>Any</gray> <blue>Special</blue></!i>")
            )
            persistentDataContainer.set(Namespace.CUSTOM_ID, PersistentDataType.STRING, "role-select-add-role")
            persistentDataContainer.set(Namespace.ROLE_SELECT_ADD_ROLE, PersistentDataType.STRING, "special")
        }

        val basic = ItemStack(Material.IRON_NUGGET)
        basic.itemMeta = basic.itemMeta.apply {
            displayName(
                MiniMessage.miniMessage().deserialize("<!i><gray>Any</gray> <blue>Basic</blue></!i>")
            )
            persistentDataContainer.set(Namespace.CUSTOM_ID, PersistentDataType.STRING, "role-select-add-role")
            persistentDataContainer.set(Namespace.ROLE_SELECT_ADD_ROLE, PersistentDataType.STRING, "basic")
        }

        val all = ItemStack(Material.NETHER_STAR)
        all.itemMeta = all.itemMeta.apply {
            displayName(
                MiniMessage.miniMessage().deserialize("<!i><gray>Any</gray></!i>")
            )
            persistentDataContainer.set(Namespace.CUSTOM_ID, PersistentDataType.STRING, "role-select-add-role")
            persistentDataContainer.set(Namespace.ROLE_SELECT_ADD_ROLE, PersistentDataType.STRING, "all")
        }

        inv.addItem(special)
        inv.addItem(basic)
        inv.addItem(all)

        player.openInventory(inv)
    }

    fun showRoleBook(player: Player) {
        var book = Book.book(
            Component.text("Roles").color(NamedTextColor.BLUE),
            Component.text("Shadow").color(NamedTextColor.GRAY),
        )

        // Split into pages of 14 roles
        val pages = shadow.gameState.originalRolelist.getSelectors().chunked(14)
        val pageList = ArrayList<Component>()
        pages.forEachIndexed { _, page ->
            val pageComponent = Component.join(
                JoinConfiguration.separator(Component.newline()),
                page.map { role -> role.selectorText }
            )
            pageList.add(pageComponent)
        }

        pageList.add(
            MiniMessage.miniMessage().deserialize("<dark_green>[+] Add role</dark_green>")
                .hoverEvent(Component.text("Click to add a role").color(NamedTextColor.GRAY))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/\$roles add"))
                .append(Component.newline())
                .append(
                    MiniMessage.miniMessage().deserialize("<red>[-] Clear All</red>")
                        .hoverEvent(Component.text("Click to clear the role list").color(NamedTextColor.GRAY))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/\$roles removeall"))
                )
        )

        book = book.pages(pageList)

        player.openBook(book)
    }
}
