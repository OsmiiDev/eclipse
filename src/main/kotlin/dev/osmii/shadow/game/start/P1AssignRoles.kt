package dev.osmii.shadow.game.start

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.GamePhase
import dev.osmii.shadow.enums.PlayableRole
import dev.osmii.shadow.util.ItemUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MerchantRecipe
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.time.Duration

class P1AssignRoles(private var shadow: Shadow) {
    fun assignRoles() {
        var players = ArrayList<Player>(shadow.server.onlinePlayers)
        players = players.filter { shadow.gameState.participationStatus.getOrDefault(it.uniqueId, false)} as ArrayList<Player>

        if (players.size < 4) {
            shadow.server.broadcast(
                MiniMessage.miniMessage().deserialize("<red>Failed to start game. Not enough participating players!</red>")
            )
            shadow.gameState.currentPhase = GamePhase.IN_BETWEEN_ROUND
            return
        }

        shadow.gameState.currentRoles.clear()

        shadow.gameState.participationStatus.forEach { (player, _) ->
            if (!shadow.gameState.participationStatus[player]!!) {
                shadow.gameState.currentRoles[player] = PlayableRole.SPECTATOR
            }
        }

        // Choose shadow(s)
        // Curve equation, should fit the following:
        // 0-3: 0, 4-6: 1, 7-12: 2, 13+: 3
        val shadowCount = when (players.size) {
            in 0..3 -> 0
            in 4..6 -> 1
            in 7..11 -> 2
            else -> 3
        }
        for (i in 0..<shadowCount) {
            val player = players.random()
            shadow.gameState.currentRoles[player.uniqueId] = PlayableRole.SHADOW
            players.remove(player)
        }

        // Choose sheriff(s)
        val sheriffCount = when (players.size) {
            in 0..10 -> 1
            else -> 2
        }
        for (i in 0..<sheriffCount) {
            val player = players.random()
            shadow.gameState.currentRoles[player.uniqueId] = PlayableRole.SHERIFF
            players.remove(player)
        }

        // Remaining players are villagers
        players.forEach { player ->
            shadow.gameState.currentRoles[player.uniqueId] = PlayableRole.VILLAGER
        }

        // Copy without .clone()
        shadow.gameState.currentRoles.forEach { (uuid, role) ->
            shadow.gameState.originalRoles[uuid] = role
        }

        // Send roles to players
        shadow.gameState.currentRoles.forEach { (uuid, role) ->
            val player: Player? = shadow.server.getPlayer(uuid)
            if (player == null) {
                shadow.logger.warning("Player $uuid is null!")
                return@forEach
            }
            if (role == PlayableRole.SPECTATOR) player.sendMessage(MiniMessage.miniMessage().deserialize("<gray><i>You are spectating this game.</i></gray>"))
            if (role == PlayableRole.SHADOW) player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You are a shadow. Protect the dragon, kill all villagers, and stay hidden.</red>"))
            if (role == PlayableRole.SHERIFF) player.sendMessage(MiniMessage.miniMessage().deserialize("<gold>You are a sheriff. Use your bow to find and kill shadows.</gold>"))
            if (role == PlayableRole.VILLAGER) player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You are a villager. Kill the dragon, or find the shadows, and stay alive.</green>"))

            if (role == PlayableRole.SHADOW) player.showTitle(
                Title.title(
                    MiniMessage.miniMessage().deserialize("<red>You are a Shadow.</red>"),
                    MiniMessage.miniMessage().deserialize("<red>Protect the dragon. Kill the villagers.</red>"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofSeconds(500))
                )
            )
            if (role == PlayableRole.SHERIFF) player.showTitle(
                Title.title(
                    MiniMessage.miniMessage().deserialize("<gold>You are a Sheriff.</gold>"),
                    MiniMessage.miniMessage().deserialize("<gold>Find and kill the shadows.</gold>"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofSeconds(500))
                )
            )
            if (role == PlayableRole.VILLAGER) player.showTitle(
                Title.title(
                    MiniMessage.miniMessage().deserialize("<green>You are a Villager.</green>"),
                    MiniMessage.miniMessage().deserialize("<green>Kill the dragon and stay alive.</green>"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofSeconds(500))
                )
            )

            // Set gamemodes
            if(role != PlayableRole.SPECTATOR) player.gameMode = GameMode.SURVIVAL
            if(role == PlayableRole.SPECTATOR) player.gameMode = GameMode.SPECTATOR

            // /shadowchat tip
            if(role == PlayableRole.SHADOW) {
                player.sendMessage(
                    Component.text("The shadows are: ")
                        .color(NamedTextColor.RED)
                        .append(
                            Component.join(
                                JoinConfiguration.separator(Component.text(", ").color(NamedTextColor.RED)),
                                shadow.gameState.currentRoles.filter { (_, role) -> role == PlayableRole.SHADOW }.keys.map { uuid ->
                                    Component.text(shadow.server.getPlayer(uuid)?.name!!)
                                        .color(NamedTextColor.GOLD)
                                }
                            )
                        )
                )

                player.sendMessage(
                    MiniMessage.miniMessage().deserialize("<red><i>You can use <gold>/sc <message></gold> to talk to other shadows!</i></red>")
                )
            }
        }

        P2GiveItems(shadow).giveItems()
    }
}