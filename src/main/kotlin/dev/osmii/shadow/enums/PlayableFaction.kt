package dev.osmii.shadow.enums

import org.bukkit.ChatColor

enum class PlayableFaction(val color: ChatColor) {
    VILLAGE(ChatColor.GREEN),
    SHADOW(ChatColor.RED),
    NEUTRAL(ChatColor.GRAY),
    SPECTATOR(ChatColor.GRAY)

}
