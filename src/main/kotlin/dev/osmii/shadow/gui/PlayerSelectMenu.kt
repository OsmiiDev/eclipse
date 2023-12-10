package dev.osmii.shadow.gui

import dev.osmii.shadow.enums.CID
import dev.osmii.shadow.enums.Namespace
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType

class PlayerSelectMenu {
    fun buildMenu(
        confirmButtonText: List<String>,
        confirmButtonTextPlayer: List<String>,
        page: Int,
        filter: (Player) -> Boolean = { true }
    ): Inventory {
        val menu = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize("<gray>Select Player</gray>"))

        val players: List<Player> = Bukkit.getOnlinePlayers().filter(filter).chunked(21)[page]
        val items: List<ItemStack> = players.map { player ->
            val item = ItemStack(Material.PLAYER_HEAD)
            item.itemMeta = (item.itemMeta as SkullMeta).apply {
                this.owningPlayer = player
                this.displayName(MiniMessage.miniMessage().deserialize("<gold>${player.name}</gold>"))
                this.persistentDataContainer.set(
                    Namespace.CUSTOM_ID,
                    PersistentDataType.STRING,
                    CID.ABILITY_SELECT_PLAYER,
                )
                this.persistentDataContainer.set(
                    Namespace.ABILITY_SELECT_PLAYER,
                    PersistentDataType.STRING,
                    player.uniqueId.toString(),
                )
            }
            item
        }

        for (i in 0..53) {
            val useless = ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            useless.itemMeta = useless.itemMeta.apply {
                this?.displayName(MiniMessage.miniMessage().deserialize("<gray> </gray>"))
            }
            menu.setItem(i, useless)
        }

        items.forEachIndexed { index, item ->
            menu.setItem((index / 7) * 9 + index % 7 + 10, item)
        }

        if (page > 0) {
            val prev = ItemStack(Material.ARROW)
            prev.itemMeta = prev.itemMeta.apply {
                this?.displayName(MiniMessage.miniMessage().deserialize("<gray>Previous Page</gray>"))
                this?.persistentDataContainer?.set(
                    Namespace.CUSTOM_ID,
                    PersistentDataType.STRING,
                    CID.ABILITY_SELECT_PLAYER_PREV,
                )
                this?.persistentDataContainer?.set(
                    Namespace.ABILITY_SELECT_PLAYER_PAGE,
                    PersistentDataType.INTEGER,
                    page - 1,
                )
            }
            menu.setItem(48, prev)
        }
        if (page < Bukkit.getOnlinePlayers().filter(filter).chunked(21).size - 1) {
            val next = ItemStack(Material.ARROW)
            next.itemMeta = next.itemMeta.apply {
                this?.displayName(MiniMessage.miniMessage().deserialize("<gray>Next Page</gray>"))
                this?.persistentDataContainer?.set(
                    Namespace.CUSTOM_ID,
                    PersistentDataType.STRING,
                    CID.ABILITY_SELECT_PLAYER_NEXT,
                )
                this?.persistentDataContainer?.set(
                    Namespace.ABILITY_SELECT_PLAYER_PAGE,
                    PersistentDataType.INTEGER,
                    page + 1,
                )
            }
            menu.setItem(50, next)
        }

        val next = ItemStack(Material.GREEN_TERRACOTTA)
        next.itemMeta = next.itemMeta.apply {
            this?.displayName(MiniMessage.miniMessage().deserialize("<green>Confirm</green>"))
            this.lore(confirmButtonText.map {
                MiniMessage.miniMessage().deserialize(it)
            })
            this?.persistentDataContainer?.set(
                Namespace.CUSTOM_ID,
                PersistentDataType.STRING,
                CID.ABILITY_SELECT_PLAYER_CONFIRM,
            )
            this?.persistentDataContainer?.set(
                Namespace.ABILITY_SELECT_INTERNAL_MINIMESSAGE,
                PersistentDataType.STRING,
                confirmButtonTextPlayer.joinToString("\n"),
            )

        }

        return menu
    }

    fun setSelected(inventory: Inventory, player: Player) {
        inventory.forEach { item ->
            if (item?.itemMeta?.persistentDataContainer?.get(
                    Namespace.ABILITY_SELECT_PLAYER,
                    PersistentDataType.STRING,
                ) == player.uniqueId.toString()
            ) {
                item.displayName().color(NamedTextColor.BLUE)
            }
        }

        inventory.setItem(53, ItemStack(Material.PLAYER_HEAD).apply {
            itemMeta = (itemMeta as SkullMeta).apply {
                this.owningPlayer = player
                this.displayName(MiniMessage.miniMessage().deserialize("<gold>${player.name}</gold>"))
                this.persistentDataContainer.set(
                    Namespace.CUSTOM_ID,
                    PersistentDataType.STRING,
                    CID.ABILITY_SELECT_PLAYER,
                )
                this.persistentDataContainer.set(
                    Namespace.ABILITY_SELECT_PLAYER,
                    PersistentDataType.STRING,
                    player.uniqueId.toString(),
                )
            }
        })

        inventory.getItem(49)?.itemMeta = inventory.getItem(49)?.itemMeta.apply {
            this?.displayName(MiniMessage.miniMessage().deserialize("<red>Cancel</red>"))
            this?.persistentDataContainer?.set(
                Namespace.CUSTOM_ID,
                PersistentDataType.STRING,
                CID.ABILITY_SELECT_PLAYER_CONFIRM,
            )

            val message = this?.persistentDataContainer?.get(
                Namespace.ABILITY_SELECT_INTERNAL_MINIMESSAGE,
                PersistentDataType.STRING,
            ) ?: return@apply

            this.lore(message.split("\n").map {
                val s = it
                    .trim()
                    .replace("<", "&lt;").replace(">", "&gt;")
                    .replace("#{player}", player.name)
                MiniMessage.miniMessage().deserialize(s)
            })
        }
    }
}