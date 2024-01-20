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
import org.bukkit.entity.ItemDisplay
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
        val nether = shadow.server.worlds[1]


        // Spawn Overworld Surface Ender Eyes

        for(i in 1..ENDER_EYE_OVERWORLD_COUNT) {
            val x = Random.nextInt((-WORLD_BORDER_SIZE).toInt(), WORLD_BORDER_SIZE.toInt()) + overworld.spawnLocation.x.toInt()
            val z = Random.nextInt((-WORLD_BORDER_SIZE).toInt(), WORLD_BORDER_SIZE.toInt()) + overworld.spawnLocation.z.toInt()
            val loc = overworld.getHighestBlockAt(x,z).location // World 0 is overworld
            loc.add(0.0,1.0,0.0)
            createEnderEye(loc)
            //shadow.logger.info("overworld ender eye spawned at: $loc")
        }


        // Spawn Stronghold Ender Eyes

        /* We replace the target spaces for the ender eyes with structure blocks,
         * then replace them back (with a similar pattern).
         * We then get the blocks changed and pick a few on which to spawn ender eyes.
         */

        val session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(overworld))


        /* This mask searches for this pattern
         *  ________
         * |        |
         * |   Air  |
         * |________|
         *  ________
         * |        |
         * |   Air  |
         * |________|
         *  ________
         * | Stone  |
         * | Bricks |
         * |________|
         * NOTE: Stone bricks or equivalents like cracked & mossy stone bricks.
         */

        val strongholdMask = MaskIntersection()

        // Creates a mask that checks for this
        strongholdMask.add(OffsetMask(Masks.negate(ExistingBlockMask(session)),
            BlockVector3.at(0,1,0),-64,315)) // FAWE only has support for OffsetMask, not OffsetsMasks
        strongholdMask.add(OffsetMask(Masks.negate(ExistingBlockMask(session)),
            BlockVector3.at(0,2,0),-64,315))

        val stoneBrickMask = BlockTypeMask(session, BlockTypes.STONE_BRICKS, BlockTypes.CRACKED_STONE_BRICKS,BlockTypes.MOSSY_STONE_BRICKS)
        strongholdMask.add(stoneBrickMask)

        val worldBorderBoundingBox = BoundingBox(overworld.spawnLocation.x + WORLD_BORDER_SIZE, -64.0,
            overworld.spawnLocation.z + WORLD_BORDER_SIZE, overworld.spawnLocation.x - WORLD_BORDER_SIZE, 315.0,
            overworld.spawnLocation.z - WORLD_BORDER_SIZE)

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

        session.replaceBlocks(region,strongholdMask, BlockTypes.STRUCTURE_BLOCK!!.defaultState)

        session.close()

        val session2 = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(overworld))

        val stoneBrickPattern = RandomPattern()
        stoneBrickPattern.add(BlockTypes.STONE_BRICKS!!.defaultState ,0.97)
        stoneBrickPattern.add(BlockTypes.CRACKED_STONE_BRICKS!!.defaultState ,0.02)
        stoneBrickPattern.add(BlockTypes.MOSSY_STONE_BRICKS!!.defaultState ,0.01)

        session2.replaceBlocks(region, BlockTypes.STRUCTURE_BLOCK!!.allStates.map { it.toBaseBlock() }.toSet(),
            stoneBrickPattern)

        session2.close()

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
                //shadow.logger.info("stronghold ender eye spawned at: $loc")
            }
        } else {
            shadow.server.broadcast(MiniMessage.miniMessage().deserialize("<red> not enough space to spawn stronghold ender eyes </red>"))
        }


        // Spawn Nether Ender Eyes
        val minY = 32
        val maxY = 110

        for (i in 1..ENDER_EYE_NETHER_COUNT) {
            var eyePosition : Location?

            do {
                val x = Random.nextInt(
                    (overworld.spawnLocation.x/8 - WORLD_BORDER_SIZE).toInt(),
                    (overworld.spawnLocation.x/8 + WORLD_BORDER_SIZE).toInt()
                )
                val z = Random.nextInt(
                    (overworld.spawnLocation.z/8 - WORLD_BORDER_SIZE).toInt(),
                    (overworld.spawnLocation.z/8 + WORLD_BORDER_SIZE).toInt()
                )
                val y = Random.nextInt(minY,maxY)

                eyePosition = Location(nether, x.toDouble(), y.toDouble(), z.toDouble())

                if(!eyePosition.block.isEmpty) {
                    eyePosition = null
                    continue
                }

                while (eyePosition.block.isEmpty) {
                    eyePosition.add(0.0, -1.0, 0.0)
                }

            } while(eyePosition == null || eyePosition.block.type == Material.LAVA || eyePosition.block.type == Material.FIRE)

            eyePosition.add(0.0,1.0,0.0)

            createEnderEye(eyePosition)
            //shadow.logger.info("nether ender eye spawned at: $eyePosition")
        }


        // Spawn Nether Roof Ender Eyes

        for(i in 1..ENDER_EYE_NETHER_ROOF_COUNT) {
            val x = Random.nextInt((-WORLD_BORDER_SIZE).toInt(), WORLD_BORDER_SIZE.toInt()) + overworld.spawnLocation.x.toInt()/8
            val z = Random.nextInt((-WORLD_BORDER_SIZE).toInt(), WORLD_BORDER_SIZE.toInt()) + overworld.spawnLocation.z.toInt()/8
            val loc = Location(nether, x.toDouble(), 128.0, z.toDouble())

            createEnderEye(loc)
            //shadow.logger.info("nether roof ender eye spawned at: $loc")
        }

        // Finish phase
        shadow.gameState.currentPhase = GamePhase.GAME_IN_PROGRESS
        P4FinalInitializations(shadow).init()
    }

    private fun createEnderEye(loc : Location) : Item {
        loc.chunk.load()
        val e = loc.world.spawnEntity(loc, EntityType.DROPPED_ITEM) as Item
        e.itemStack = ItemStack(Material.ENDER_EYE,1)
        e.setWillAge(false)
        e.setCanMobPickup(false)
        e.isInvulnerable = true
        e.isUnlimitedLifetime = true

        val display = loc.world.spawnEntity(loc.add(0.5,1.0,0.5), EntityType.ITEM_DISPLAY) as ItemDisplay
        display.itemStack = e.itemStack
        display.displayHeight = 3.0F
        display.displayWidth = 3.0F

        return e
    }

}