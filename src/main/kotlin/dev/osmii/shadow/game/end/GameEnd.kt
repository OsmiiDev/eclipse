package dev.osmii.shadow.game.end

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.*
import net.md_5.bungee.api.ChatMessageType
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.sqrt

class GameEnd(var shadow: Shadow) {

    private var timerTask = AtomicReference<BukkitTask>()
    private var antiStallTask = AtomicReference<BukkitTask>()
    private var damagerTask = AtomicReference<BukkitTask>()

    fun checkGameEnd() {
        // Check that the game is actually over
        val villagersAlive = AtomicReference(false)
        val shadowsAlive = AtomicReference(false)
        shadow.gameState.currentRoles.forEach { (uuid, role) ->
            if (role.roleFaction == PlayableFaction.SHADOW) shadowsAlive.set(true)
            if (role.roleFaction == PlayableFaction.VILLAGE) villagersAlive.set(true)
        }
        if (villagersAlive.get() && shadowsAlive.get()) return
        if (GamePhase.GAME_IN_PROGRESS != shadow.gameState.currentPhase) return

        val result = when {
            !villagersAlive.get() && !shadowsAlive.get() -> GameResult.DRAW
            !villagersAlive.get() -> GameResult.SHADOW_WINS
            !shadowsAlive.get() -> GameResult.VILLAGE_WINS
            else -> GameResult.DRAW
        }

        shadow.gameState.currentPhase = GamePhase.IN_BETWEEN_ROUND

        when (result) {
            GameResult.SHADOW_WINS -> {
                shadow.server.broadcastMessage("${ChatColor.RED}The shadows have won!")
                shadow.server.onlinePlayers.forEach { p ->
                    p.sendTitle("${ChatColor.RED}Shadows Win", "", 10, 70, 20)
                }

                shadow.gameState.originalRoles.forEach { (uuid, role) ->
                    if (role.roleFaction == PlayableFaction.SHADOW) {
                        if (shadow.server.getPlayer(uuid) == null) return@forEach
                        shadow.gameState.currentWinners.add(shadow.server.getPlayer(uuid)!!)
                    }
                }
            }

            GameResult.VILLAGE_WINS -> {
                shadow.server.broadcastMessage("${ChatColor.GREEN}The village has won!")

                shadow.server.onlinePlayers.forEach { p ->
                    p.sendTitle("${ChatColor.GREEN}Villagers Win", "", 10, 70, 20)
                }

                shadow.gameState.originalRoles.forEach { (uuid, role) ->
                    if (role.roleFaction == PlayableFaction.VILLAGE) {
                        if (shadow.server.getPlayer(uuid) == null) return@forEach
                        shadow.gameState.currentWinners.add(shadow.server.getPlayer(uuid)!!)
                    }
                }
            }

            GameResult.DRAW -> {
                shadow.server.broadcastMessage("${ChatColor.GRAY}The game has ended in a draw!")
                shadow.server.onlinePlayers.forEach { p ->
                    p.sendTitle("${ChatColor.GRAY}Match Draw", "", 10, 70, 20)
                }
            }
        }

        shadow.server.onlinePlayers.forEach { p ->
            p.sendMessage("${ChatColor.BLUE}Game winners: ${
                shadow.gameState.currentWinners.joinTo(
                    StringBuilder(),
                    "${ChatColor.BLUE}, "
                ) { "${ChatColor.GOLD}${it.name}" }
            }")
        }
    }

    fun checkAntiStall() {
        // Check that one villager and one shadow are alive
        val villagersAlive =
            shadow.gameState.currentRoles.filter { (_, role) -> role.roleFaction == PlayableFaction.VILLAGE }.size
        val shadowsAlive =
            shadow.gameState.currentRoles.filter { (_, role) -> role.roleFaction == PlayableFaction.SHADOW }.size

        if (villagersAlive == 1 && shadowsAlive == 1) {
            // Send anti-stall notification
            shadow.server.onlinePlayers.forEach { p ->
                p.sendMessage("${ChatColor.RED}There are only 2 players left. In ten minutes, anyone not in the end will begin taking damage.")
            }

            // If last villager is a sheriff, remove their ability to kill
            shadow.gameState.currentRoles.forEach { (uuid, role) ->
                if (role.roleFaction == PlayableFaction.VILLAGE && role == PlayableRole.SHERIFF) {
                    shadow.server.getPlayer(uuid)
                        ?.sendMessage("${ChatColor.RED}You are the last villager alive. You will no longer have your instant kill ability.")

                    // Clear sheriff bow from inventory
                    shadow.server.getPlayer(uuid)?.inventory?.iterator()?.forEach { item ->
                        if (item != null && item.hasItemMeta() && item.itemMeta!!.persistentDataContainer.has(
                                Namespace.CUSTOM_ID,
                                PersistentDataType.STRING
                            )
                        ) {
                            if (item.itemMeta!!.persistentDataContainer.get(
                                    Namespace.CUSTOM_ID,
                                    PersistentDataType.STRING
                                ) == "sheriff-bow"
                            ) {
                                shadow.server.getPlayer(uuid)?.inventory?.remove(item)
                            }
                        }
                    }
                }
            }

            val minutes = AtomicInteger(10)
            val seconds = AtomicInteger(0)

            // Send timer action bar message
            timerTask.set(Bukkit.getScheduler().runTaskTimer(shadow, Runnable {
                // If game is no longer in progress, cancel task
                if (shadow.gameState.currentPhase != GamePhase.GAME_IN_PROGRESS) {
                    timerTask.get().cancel()
                    antiStallTask.get().cancel()
                    return@Runnable
                }

                val color: ChatColor = when {
                    seconds.get() % 2 == 0 -> ChatColor.RED
                    else -> ChatColor.GOLD
                }
                shadow.server.onlinePlayers.forEach { p ->
                    p.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR, *arrayOf(
                            net.md_5.bungee.api.chat.TextComponent(
                                "${color}${minutes}:${
                                    if (seconds.get() < 10) "0${seconds.get()}" else seconds.get()
                                }"
                            ),
                        )
                    )
                }

                if (seconds.get() == 0) {
                    minutes.getAndDecrement()
                    seconds.set(59)
                } else {
                    seconds.getAndDecrement()
                }
            }, 0, 20))

            antiStallTask.set(Bukkit.getScheduler().runTaskLater(shadow, Runnable {
                shadow.logger.info("Anti-stall I triggered")
                if (shadow.gameState.currentPhase != GamePhase.GAME_IN_PROGRESS) return@Runnable
                timerTask.get()?.cancel()
                antiStallPhase1()
            }, minutes.get() * 60 * 20L + seconds.get() * 20L))
        }
    }

    private fun antiStallPhase1() {
        val alternatingColor = AtomicReference(false)
        val triggers = AtomicReference(0)
        damagerTask.set(Bukkit.getScheduler().runTaskTimer(shadow, Runnable {
            alternatingColor.set(!alternatingColor.get())
            triggers.set(triggers.get() + 1)

            // If game is no longer in progress, cancel task
            if (shadow.gameState.currentPhase != GamePhase.GAME_IN_PROGRESS) {
                damagerTask.get().cancel()
                return@Runnable
            }

            shadow.server.onlinePlayers.forEach { p ->
                val color: ChatColor = when {
                    alternatingColor.get() -> ChatColor.RED
                    else -> ChatColor.GOLD
                }
                if (p.world.name == "world_the_end" || shadow.gameState.currentRoles[p.uniqueId]!!.roleFaction == PlayableFaction.SPECTATOR) return@forEach

                p.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR, *arrayOf(
                        net.md_5.bungee.api.chat.TextComponent("${color}You are now taking damage. Get to the end!"),
                    )
                )

                if (alternatingColor.get()) {
                    p.damage(sqrt(triggers.get().toDouble()) * 0.5)
                }
            }
        }, 0, 20))
    }
}