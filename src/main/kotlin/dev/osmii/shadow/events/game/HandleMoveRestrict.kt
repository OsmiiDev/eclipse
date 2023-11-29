package dev.osmii.shadow.events.game

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.GamePhase
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerMoveEvent

class HandleMoveRestrict(var shadow: Shadow) : Listener {
    // Handle interaction restrictions during the phase between location selection and game start
    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if (e.player.isOp) return
        if (e.from.x == e.to.x && e.from.y == e.to.y && e.from.z == e.to.z) return
        if (shadow.gameState.currentPhase == GamePhase.LOCATION_SELECTED) e.isCancelled = true
    }

    @EventHandler
    fun onCombat(e: EntityDamageByEntityEvent) {
        if (e.entity.isOp || e.damager.isOp) return
        if (shadow.gameState.currentPhase == GamePhase.LOCATION_SELECTED) e.isCancelled = true
    }
}
