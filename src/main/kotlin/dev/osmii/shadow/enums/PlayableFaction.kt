package dev.osmii.shadow.enums

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material

enum class PlayableFaction(val color: NamedTextColor, val icon: Material) {
    VILLAGE(NamedTextColor.GREEN, Material.EMERALD),
    SHADOW(NamedTextColor.RED, Material.NETHERITE_AXE),
    NEUTRAL(NamedTextColor.GRAY, Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE),
    SPECTATOR(NamedTextColor.GRAY, Material.BARRIER)

}
