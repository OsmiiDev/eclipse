package dev.osmii.shadow.enums

import net.kyori.adventure.text.format.NamedTextColor

enum class PlayableRole(
    val roleFaction: PlayableFaction,
    val roleSubfaction: PlayableSubfaction,
    val roleName: String,
    val roleColor: NamedTextColor
) {
    VILLAGER(PlayableFaction.VILLAGE, PlayableSubfaction.VILLAGE, "Villager", NamedTextColor.GREEN),
    SHERIFF(PlayableFaction.VILLAGE, PlayableSubfaction.VILLAGE_KILLING, "Sheriff", NamedTextColor.GOLD),
    SHADOW(PlayableFaction.SHADOW, PlayableSubfaction.SHADOW_KILLING, "Shadow", NamedTextColor.RED),
    SPECTATOR(PlayableFaction.SPECTATOR, PlayableSubfaction.SPECTATOR, "Spectator", NamedTextColor.GRAY)

}
