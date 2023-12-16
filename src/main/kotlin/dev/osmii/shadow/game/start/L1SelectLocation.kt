package dev.osmii.shadow.game.start

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.GamePhase
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class L1SelectLocation(private val shadow: Shadow) {
    fun selectLocation(location: Location) {
        val world = Objects.requireNonNull(shadow.server.getWorld("world"))
        world!!.worldBorder.center = location
        world.worldBorder.size = 135.0
        world.setSpawnLocation(location)

        if(shadow.server.onlinePlayers.size < shadow.gameState.originalRolelist.roles.size) {
            shadow.server.broadcast(
                MiniMessage.miniMessage().deserialize(
                    "<red>Failed to start game. Not enough online players!</red> <gold>(${shadow.server.onlinePlayers.size}/${shadow.gameState.originalRolelist.roles.size})</gold>"
                )
            )
            shadow.gameState.currentPhase = GamePhase.IN_BETWEEN_ROUND
            return
        }


        if(shadow.server.onlinePlayers.size > 1) {
            // Radius (Should spread players out evenly 3 blocks apart)
            val offsetRadius = 3 / (2 * sin(Math.PI / shadow.server.onlinePlayers.size))
            for (i in shadow.server.onlinePlayers.indices) {
                val p = shadow.server.onlinePlayers.toTypedArray()[i] as Player
                val offsetDeg = 360.0 / shadow.server.onlinePlayers.size
                val offsetRad = offsetDeg * Math.PI / 180
                val x = offsetRadius * cos(offsetRad * i)
                val z = offsetRadius * sin(offsetRad * i)
                // Find solid ground and teleport player there
                p.teleport(location.add(x, 0.0, z))
                p.teleport(p.location.world!!.getHighestBlockAt(p.location).location.add(0.0, 1.0, 0.0))
            }
        } // Player spreading algorithm breaks with only 1 player

        // Finish phase
        shadow.gameState.currentPhase = GamePhase.LOCATION_SELECTED
    }
}