package dev.osmii.shadow.events.custom

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.GamePhase
import dev.osmii.shadow.enums.PlayableFaction
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.network.protocol.Packet
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class HandleDayNight(val shadow: Shadow) {

    val glowingUpdatedFor: ArrayList<Pair<Int, Int>> = ArrayList()

    fun register() {
        val world: World? = Bukkit.getWorld("world")
        Bukkit.getScheduler().runTaskTimer(shadow, Runnable {
            if (shadow.gameState.currentPhase != GamePhase.GAME_IN_PROGRESS) return@Runnable

            if (world?.time in 12452L..12532L) {
                glowingUpdatedFor.clear()
                shadow.server.onlinePlayers.forEach { p ->
                    if (shadow.gameState.currentRoles[p.uniqueId]!!.roleFaction == PlayableFaction.SHADOW) {
                        Audience.audience(p).sendMessage(
                            MiniMessage.miniMessage()
                                .deserialize("<green>Darkness approaches. Your powers grow.</green>")
                        )
                    }
                    if (shadow.gameState.currentRoles[p.uniqueId]!!.roleFaction == PlayableFaction.VILLAGE ||
                        shadow.gameState.currentRoles[p.uniqueId]!!.roleFaction == PlayableFaction.NEUTRAL
                    ) {
                        Audience.audience(p).sendActionBar(
                            MiniMessage.miniMessage().deserialize("<red>Darkness approaches. It is now nighttime</red>")
                        )
                    }
                }
            }
            if (world?.time in 0L..80L) {
                shadow.server.onlinePlayers.forEach { p ->
                    Audience.audience(p).sendActionBar(
                        MiniMessage.miniMessage().deserialize("<green>The sky clears.</green>")
                    )
                }
            }
            if (world?.time!! > 12452L) {
                world.time += 9
                shadow.server.onlinePlayers.forEach { p ->
                    p.isGlowing = false
                    if (shadow.gameState.currentRoles[p.uniqueId]!!.roleFaction == PlayableFaction.VILLAGE) {
                        p.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 40, 1, false, false))
                        p.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 40, 0, false, false))
                        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 40, 0, false, false))
                    }
                    if (shadow.gameState.currentRoles[p.uniqueId]!!.roleFaction == PlayableFaction.SHADOW) {
                        shadow.server.onlinePlayers.forEach inner@ { other ->
                            // If glowing is already updated for this player, don't update it again
                            val alreadyUpdated = glowingUpdatedFor.any { pair ->
                                pair.first == other.entityId && pair.second == p.entityId
                            }
                            if (alreadyUpdated) return@inner

                            val container = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)
                            val watcher = WrappedDataWatcher()
                            watcher.entity = other
                            watcher.setObject(0, WrappedDataWatcher.Registry.get(java.lang.Byte::class.java), 0x40.toByte(), true)
                            container.integers.write(0, other.entityId) // The entity ID
                            container.watchableCollectionModifier.write(0, watcher.watchableObjects) // The data watcher
                            shadow.protocolManager!!.sendServerPacket(p, container)
                            glowingUpdatedFor.add(Pair(other.entityId, p.entityId))
                        }
                    }
                }
            } else {
                shadow.server.onlinePlayers.forEach { p ->
                    p.isGlowing = true
                }
            }
        }, 0, 1L)
    }
}