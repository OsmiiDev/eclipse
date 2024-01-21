package dev.osmii.shadow.events

import dev.osmii.shadow.Shadow
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.AsyncStructureSpawnEvent
import org.bukkit.generator.structure.StructureType

class CollectStrongholdGeneration(val shadow: Shadow) : Listener {
    @EventHandler
    fun naturalStrongholdGeneration(e : AsyncStructureSpawnEvent) {
        if(e.structure.structureType == StructureType.STRONGHOLD) shadow.boundingBoxSet.add(e.boundingBox)
        shadow.logger.info(shadow.boundingBoxSet.toString())

    }
}