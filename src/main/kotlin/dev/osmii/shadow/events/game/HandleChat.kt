package dev.osmii.shadow.events.game

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.GamePhase
import dev.osmii.shadow.enums.PlayableRole
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import kotlin.math.ceil

class HandleChat (private var shadow: Shadow) : Listener {

    private var lastChat: HashMap<Player, Double>  = HashMap<Player, Double>();

    @EventHandler
    fun onChat(e: AsyncChatEvent) {
        e.renderer().render(
            e.player,
            Component.text(e.player.name)
                .append(Component.text(" » "))
                .color(NamedTextColor.WHITE),
            Component.text(e.message)
                .color(NamedTextColor.GRAY),
            Audience.audience(e.player)
        )
        if(shadow.gameState.currentPhase != GamePhase.GAME_IN_PROGRESS) return
        if(e.player.isOp) return

        // If player is spectator, cancel
        if (shadow.gameState.currentRoles[e.player.uniqueId] == PlayableRole.SPECTATOR) {
            e.isCancelled = true
            e.player.sendMessage("${ChatColor.RED}You cannot send messages while dead.")
            return
        }

        // If less than 30 seconds since last chat, cancel
        if (lastChat[e.player] != null && System.currentTimeMillis().toDouble() - lastChat.getOrDefault(e.player, 0.toDouble()) < 30000) {
            e.isCancelled = true
            e.player.sendMessage("${ChatColor.RED}You must wait "
                    + "${
                ceil((30000 - (System.currentTimeMillis() - lastChat.getOrDefault(e.player, 0).toDouble()).toDouble()) / 1000).toInt()
                    }"
                    + " more seconds before sending another message.")
            return
        }

        // If player is not on full health, cancel
        if (e.player.health < 19.50) {
            e.isCancelled = true
            e.player.sendMessage("${ChatColor.RED}You must be on full health to send a message.")
            return
        }

        lastChat[e.player] = System.currentTimeMillis().toDouble()
    }
}