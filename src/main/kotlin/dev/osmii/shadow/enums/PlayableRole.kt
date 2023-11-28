package dev.osmii.shadow.enums

import org.bukkit.ChatColor

enum class PlayableRole(
    val roleFaction: PlayableFaction,
    val roleSubfaction: PlayableSubfaction,
    val roleName: String,
    val roleColor: ChatColor
) {
    VILLAGER(PlayableFaction.VILLAGE, PlayableSubfaction.VILLAGE, "Villager", ChatColor.GREEN),
    SHERIFF(PlayableFaction.VILLAGE, PlayableSubfaction.VILLAGE_KILLING, "Sheriff", ChatColor.GOLD),
    SHADOW(PlayableFaction.SHADOW, PlayableSubfaction.SHADOW_KILLING, "Shadow", ChatColor.RED),
    SPECTATOR(PlayableFaction.SPECTATOR, PlayableSubfaction.SPECTATOR, "Spectator", ChatColor.GRAY)

}
