package dev.osmii.shadow.enums

import dev.osmii.shadow.Shadow
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey

class Namespace(val namespace: NamespacedKey) {
    companion object {
        val FORBIDDEN = NamespacedKey(Bukkit.getPluginManager().getPlugin("Shadow") as Shadow, "forbidden")
        val CUSTOM_ID = NamespacedKey(Bukkit.getPluginManager().getPlugin("Shadow") as Shadow, "custom-id")
    }
}