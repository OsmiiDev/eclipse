package dev.osmii.shadow.enums

import org.bukkit.ChatColor

enum class PlayableSubfaction(
    val parentFaction: PlayableFaction,
    val color: ChatColor
) {
    VILLAGE_INVESTIGATIVE(PlayableFaction.VILLAGE, ChatColor.GREEN),
    VILLAGE_PROTECTIVE(PlayableFaction.VILLAGE, ChatColor.DARK_AQUA),
    VILLAGE_KILLING(PlayableFaction.VILLAGE, ChatColor.GOLD),
    VILLAGE_SUPPORT(PlayableFaction.VILLAGE, ChatColor.LIGHT_PURPLE),
    VILLAGE(PlayableFaction.VILLAGE, ChatColor.GREEN),

    SHADOW_KILLING(PlayableFaction.SHADOW, ChatColor.RED),
    SHADOW_DECEPTION(PlayableFaction.SHADOW, ChatColor.RED),

    SPECTATOR(PlayableFaction.SPECTATOR, ChatColor.GRAY)
}