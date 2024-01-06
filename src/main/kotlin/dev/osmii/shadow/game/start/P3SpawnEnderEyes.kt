package dev.osmii.shadow.game.start

import com.fastasyncworldedit.core.history.change.MutableBlockChange
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.function.mask.*
import com.sk89q.worldedit.function.pattern.RandomPattern
import com.sk89q.worldedit.history.change.Change
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.math.Vector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.world.block.BlockTypes
import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.GamePhase
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import kotlin.random.Random

const val ENDER_EYE_OVERWORLD_COUNT = 4
const val ENDER_EYE_STRONGHOLD_COUNT = 4
const val ENDER_EYE_NETHER_COUNT = 8
const val ENDER_EYE_NETHER_ROOF_COUNT = 12

class P3SpawnEnderEyes(private val shadow: Shadow) {
    fun spawnEnderEyes() {
        val overworld = shadow.server.worlds[0]


        // Spawn Overworld Surface Ender Eyes

        for(i in 1..ENDER_EYE_OVERWORLD_COUNT) {
            val x = Random.nextInt((-WORLD_BORDER_SIZE).toInt(), WORLD_BORDER_SIZE.toInt()) + overworld.spawnLocation.x.toInt()
            val z = Random.nextInt((-WORLD_BORDER_SIZE).toInt(), WORLD_BORDER_SIZE.toInt()) + overworld.spawnLocation.z.toInt()
            val loc = overworld.getHighestBlockAt(x,z).location // World 0 is overworld
            loc.add(0.0,1.0,0.0)
            createEnderEye(loc)
        }


        // Spawn Stronghold Ender Eyes

        val session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(overworld))

        val stoneBrickMask = BlockTypeMask(session, BlockTypes.STONE_BRICKS, BlockTypes.CRACKED_STONE_BRICKS,BlockTypes.MOSSY_STONE_BRICKS)
        val strongholdMask = MaskIntersection()


        for (i in 1..2) strongholdMask.add(OffsetMask(Masks.negate(ExistingBlockMask(session)),
            BlockVector3.at(0,i,0),-64,315)) // FAWE only has support for OffsetMask, not OffsetsMasks

        strongholdMask.add(stoneBrickMask)

        val worldBorderBoundingBox = BoundingBox(overworld.spawnLocation.x + WORLD_BORDER_SIZE, -64.0,
            overworld.spawnLocation.z + WORLD_BORDER_SIZE, overworld.spawnLocation.x - WORLD_BORDER_SIZE, 315.0,
            overworld.spawnLocation.z - WORLD_BORDER_SIZE)

        var strongholdBoundingBox : BoundingBox? = null

        for (bb in shadow.boundingBoxSet) {
            if (worldBorderBoundingBox.overlaps(worldBorderBoundingBox)) {
                strongholdBoundingBox = bb
                break
            }
        }

        strongholdBoundingBox = worldBorderBoundingBox.intersection(strongholdBoundingBox!!)

        val region = CuboidRegion(BlockVector3.at(strongholdBoundingBox.minX,strongholdBoundingBox.minY,strongholdBoundingBox.minZ),
            BlockVector3.at(strongholdBoundingBox.maxX,strongholdBoundingBox.maxY,strongholdBoundingBox.maxZ))

        val stoneBrickPattern = RandomPattern()
        stoneBrickPattern.add(BlockTypes.STONE_BRICKS!!.defaultState ,0.97)
        stoneBrickPattern.add(BlockTypes.CRACKED_STONE_BRICKS!!.defaultState ,0.02)
        stoneBrickPattern.add(BlockTypes.MOSSY_STONE_BRICKS!!.defaultState ,0.01)

        session.replaceBlocks(region,strongholdMask,stoneBrickPattern)

        val possiblePositions = ArrayList<Vector3>()

        val iter : Iterator<Change> = session.changeSet.backwardIterator()
        while(iter.hasNext()) {
            possiblePositions.add((iter.next() as MutableBlockChange).let {
                Vector3.at(it.x.toDouble(),it.y.toDouble(),it.z.toDouble())
            })
        }

        if(possiblePositions.size > ENDER_EYE_STRONGHOLD_COUNT) {
            for (i in 1..ENDER_EYE_STRONGHOLD_COUNT) {
                val chosenVector = possiblePositions[Random.nextInt(0, possiblePositions.size - 1)]
                val loc = Location(overworld, chosenVector.x, chosenVector.y + 1, chosenVector.z)
                createEnderEye(loc)
            }
        } else {
            shadow.server.broadcast(MiniMessage.miniMessage().deserialize("<red> not enough space to spawn stronghold ender eyes </red>"))
        }


        // Spawn Nether Ender Eyes


        // Spawn Nether Roof Ender Eyes

        // Finish phase
        shadow.gameState.currentPhase = GamePhase.GAME_IN_PROGRESS
    }

    private fun createEnderEye(loc : Location) : Item {
        loc.chunk.load()
        val e = loc.world.spawnEntity(loc, EntityType.DROPPED_ITEM) as Item
        e.itemStack = ItemStack(Material.ENDER_EYE,1)
        e.setWillAge(false)
        e.setCanMobPickup(false)
        e.isUnlimitedLifetime = true
        return e
    }

}