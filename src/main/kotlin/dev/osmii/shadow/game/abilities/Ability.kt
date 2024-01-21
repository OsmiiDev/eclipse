package dev.osmii.shadow.game.abilities

import dev.osmii.shadow.Shadow
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface Ability {
    val item: ItemStack

    fun apply(player: Player, shadow: Shadow)
}