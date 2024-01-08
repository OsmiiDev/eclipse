package dev.osmii.shadow.game.abilities

import dev.osmii.shadow.Shadow
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class GuessSheriff : Ability {
    override val item: ItemStack = ItemStack(Material.NETHERITE_SWORD)
    init {
        val meta = item.itemMeta
        meta.displayName(Component.text("Guess the Sheriff").color(TextColor.color(192,0,0)))
        item.itemMeta = meta
    }
    override fun apply(player: Player, shadow: Shadow) {
        player.sendMessage("Activated Guess Sheriff Ability")
    }
}