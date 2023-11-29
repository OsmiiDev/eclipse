package dev.osmii.shadow.events.custom.abilities

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.util.ItemUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class HandleOpenAbilityMenu(val shadow: Shadow): Listener {

    @EventHandler
    fun onOpenAbilityMenu(e: PlayerInteractEvent) {
        if(e.item == null || !ItemUtil.customIdIs(e.item!!, "ability-select")) return
        e.player.sendMessage("Ability menu opened!")
    }
}