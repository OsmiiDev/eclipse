package dev.osmii.shadow.game.start

import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.function.mask.BlockTypeMask
import com.sk89q.worldedit.function.pattern.StateApplyingPattern
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.world.block.BlockTypes
import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.GamePhase
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import kotlin.math.cos
import kotlin.math.sin

const val WORLD_BORDER_SIZE = 62.5

class L1SelectLocation(private val shadow: Shadow) {
    private fun checkForStronghold(center : Location) : Boolean { // checks if there are more than 12 end portal frames in the area
        val session : EditSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(center.world))

        val worldBorderBoundingBox = BoundingBox(center.x + WORLD_BORDER_SIZE, -64.0,
            center.z + WORLD_BORDER_SIZE, center.x - WORLD_BORDER_SIZE, 315.0,
            center.z - WORLD_BORDER_SIZE)

        var strongholdBoundingBox : BoundingBox? = null

        for (bb in shadow.boundingBoxSet) {
            if (worldBorderBoundingBox.overlaps(bb)) {
                strongholdBoundingBox = bb
                break
            }
        }

        if(strongholdBoundingBox == null) return false

        strongholdBoundingBox = worldBorderBoundingBox.intersection(strongholdBoundingBox)

        val region = CuboidRegion(BlockVector3.at(strongholdBoundingBox.minX,strongholdBoundingBox.minY,strongholdBoundingBox.minZ),
            BlockVector3.at(strongholdBoundingBox.maxX,strongholdBoundingBox.maxY,strongholdBoundingBox.maxZ))

        return session.countBlocks(region,
            BlockTypes.END_PORTAL_FRAME!!.allStates.map { it.toBaseBlock() }.toSet()) >= 12
    }

    fun selectLocation(location: Location) {
        val overworld = shadow.server.worlds[0]
        val nether = shadow.server.worlds[1]

        if(location.world != overworld) {
            shadow.server.broadcast(
                MiniMessage.miniMessage().deserialize(
                    "<red>Failed to start game. Please start the game in the default overworld</red>"
                )
            )
        }

        if(!checkForStronghold(location)) {
            shadow.server.broadcast(
                MiniMessage.miniMessage().deserialize(
                    "<red>Failed to start game. No Portal Room within Area</red>"
                )
            )
            shadow.gameState.currentPhase = GamePhase.IN_BETWEEN_ROUND
            return
        }

        val session : EditSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(overworld))

        val worldBorderBoundingBox = BoundingBox(location.x + WORLD_BORDER_SIZE, -64.0,
            location.z + WORLD_BORDER_SIZE, location.x - WORLD_BORDER_SIZE, 315.0,
            location.z - WORLD_BORDER_SIZE)

        var strongholdBoundingBox : BoundingBox? = null

        for (bb in shadow.boundingBoxSet) {
            if (worldBorderBoundingBox.overlaps(bb)) {
                strongholdBoundingBox = bb
                break
            }
        }

        strongholdBoundingBox = worldBorderBoundingBox.intersection(strongholdBoundingBox!!)

        val region = CuboidRegion(BlockVector3.at(strongholdBoundingBox.minX,strongholdBoundingBox.minY,strongholdBoundingBox.minZ),
            BlockVector3.at(strongholdBoundingBox.maxX,strongholdBoundingBox.maxY,strongholdBoundingBox.maxZ))

        val eyeState = HashMap<String,String>()
        eyeState["eye"] = "false"
        session.replaceBlocks(region,BlockTypeMask(session,BlockTypes.END_PORTAL_FRAME), StateApplyingPattern(session,eyeState))
        session.close()

        overworld!!.worldBorder.center = location
        overworld.worldBorder.size = WORLD_BORDER_SIZE*2
        overworld.setSpawnLocation(location)

        val netherLocation = Location(nether,location.x/8,0.0,location.z/8)

        nether!!.worldBorder.center = netherLocation
        nether.worldBorder.size = WORLD_BORDER_SIZE*2

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