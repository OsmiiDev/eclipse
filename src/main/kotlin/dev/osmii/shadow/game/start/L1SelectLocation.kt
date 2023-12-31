package dev.osmii.shadow.game.start

import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.function.mask.BlockTypeMask
import com.sk89q.worldedit.function.pattern.StateApplyingPattern
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.world.block.BlockTypes
import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.GamePhase
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.math.cos
import kotlin.math.sin

const val WORLD_BORDER_SIZE = 135.0

class L1SelectLocation(private val shadow: Shadow) {
    fun checkForStronghold(center : Location) : Boolean { // checks if there are more than 12 end portal frames in the area
        val session : EditSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(center.world))
        val region : Region = CuboidRegion(BlockVector3.at(center.x + WORLD_BORDER_SIZE,-64.0,center.z + WORLD_BORDER_SIZE),
            BlockVector3.at(center.x - WORLD_BORDER_SIZE,64.0,center.z - WORLD_BORDER_SIZE))

        return session.countBlocks(region,
            BlockTypes.END_PORTAL_FRAME!!.allStates.map { it.toBaseBlock() }.toSet()) >= 12
    }

    fun selectLocation(location: Location) {
        val world = location.world
        if(!checkForStronghold(location)) {
            shadow.server.broadcast(
                MiniMessage.miniMessage().deserialize(
                    "<red>Failed to start game. No Portal Room within Area</red>"
                )
            )
            shadow.gameState.currentPhase = GamePhase.IN_BETWEEN_ROUND
            return
        }

        val session : EditSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.world))
        val region : Region = CuboidRegion(BlockVector3.at(location.x + WORLD_BORDER_SIZE,-64.0,location.z + WORLD_BORDER_SIZE),
            BlockVector3.at(location.x - WORLD_BORDER_SIZE,64.0,location.z - WORLD_BORDER_SIZE))

        val eyeState = HashMap<String,String>()
        eyeState["eye"] = "false"
        session.replaceBlocks(region,BlockTypeMask(session,BlockTypes.END_PORTAL_FRAME), StateApplyingPattern(session,eyeState))
        session.close()

        world!!.worldBorder.center = location
        world.worldBorder.size = WORLD_BORDER_SIZE
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