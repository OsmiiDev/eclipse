package dev.osmii.shadow.events.custom

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.GamePhase
import dev.osmii.shadow.enums.PlayableFaction
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class HandleDayNight(var shadow: Shadow) {

    fun register() {
        val world: World? = Bukkit.getWorld("world")
        Bukkit.getScheduler().runTaskTimer(shadow, Runnable {
            if (shadow.gameState.currentPhase != GamePhase.GAME_IN_PROGRESS) return@Runnable

            if (world?.time in 12452L..12532L) {
                shadow.server.onlinePlayers.forEach { p ->
                    if (shadow.gameState.currentRoles[p.uniqueId]!!.roleFaction == PlayableFaction.SHADOW) {
                        p.spigot().sendMessage(
                            net.md_5.bungee.api.ChatMessageType.ACTION_BAR, *arrayOf(
                                net.md_5.bungee.api.chat.TextComponent("${ChatColor.GREEN}Darkness approaches. Your powers grow."),
                            )
                        )
                    }
                    if (shadow.gameState.currentRoles[p.uniqueId]!!.roleFaction == PlayableFaction.VILLAGE ||
                        shadow.gameState.currentRoles[p.uniqueId]!!.roleFaction == PlayableFaction.NEUTRAL
                    ) {
                        p.spigot().sendMessage(
                            net.md_5.bungee.api.ChatMessageType.ACTION_BAR, *arrayOf(
                                net.md_5.bungee.api.chat.TextComponent("${ChatColor.RED}Darkness approaches. It is now nighttime."),
                            )
                        )
                    }
                }
            }
            if (world?.time in 0L..80L) {
                shadow.server.onlinePlayers.forEach { p ->
                    p.spigot().sendMessage(
                        net.md_5.bungee.api.ChatMessageType.ACTION_BAR, *arrayOf(
                            net.md_5.bungee.api.chat.TextComponent("${ChatColor.GREEN}The sky clears."),
                        )
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
                        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 40, 1, false, false))
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