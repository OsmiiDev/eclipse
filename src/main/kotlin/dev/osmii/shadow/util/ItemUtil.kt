package dev.osmii.shadow.util

import dev.osmii.shadow.enums.Namespace
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object ItemUtil {
    fun forbidden(
        drop: Boolean = true,
        use: Boolean = true,
        move: Boolean = true,

        moveContainer: Boolean = true
    ): ByteArray {
        val forbidden = ByteArray(5)
        forbidden[0] = if (drop) 1.toByte() else 0.toByte()
        forbidden[1] = if (use) 1.toByte() else 0.toByte()
        forbidden[2] = if (move) 1.toByte() else 0.toByte()
        forbidden[3] = if (moveContainer) 1.toByte() else 0.toByte()
        return forbidden
    }

    fun customIdIs(item: ItemStack, customId: String): Boolean {
        if(item.itemMeta == null) return false
        return item.itemMeta?.persistentDataContainer?.getOrDefault(Namespace.CUSTOM_ID, PersistentDataType.STRING, "").equals(customId)
    }

    fun customKeyIs(namespace: NamespacedKey, item: ItemStack, customId: String): Boolean {
        if(item.itemMeta == null) return false
        return item.itemMeta?.persistentDataContainer?.getOrDefault(namespace, PersistentDataType.STRING, "").equals(customId)
    }
}
