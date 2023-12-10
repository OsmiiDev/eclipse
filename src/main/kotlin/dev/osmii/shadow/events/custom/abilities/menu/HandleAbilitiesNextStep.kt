package dev.osmii.shadow.events.custom.abilities.menu

import dev.osmii.shadow.Shadow
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class HandleAbilitiesNextStep(val shadow: Shadow): Listener {
        @EventHandler
        fun onNextStep(e: InventoryClickEvent) {
        }
}