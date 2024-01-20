package dev.osmii.shadow.events.game

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.GamePhase
import dev.osmii.shadow.enums.PlayableRole
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class HandleJoinLeave(private val shadow: Shadow) : Listener {
    // Handle players joining and leaving the server during the game
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        Bukkit.getScoreboardManager().mainScoreboard.getTeam("players")?.addEntry(e.player.name)

        if (shadow.gameState.currentPhase == GamePhase.IN_BETWEEN_ROUND || shadow.gameState.currentPhase == GamePhase.NONE) return
        if (shadow.gameState.currentRoles.containsKey(e.player.uniqueId)) return

        val p = e.player
        shadow.gameState.currentRoles[p.uniqueId] = PlayableRole.SPECTATOR
        p.gameMode = GameMode.SPECTATOR
    }

}
