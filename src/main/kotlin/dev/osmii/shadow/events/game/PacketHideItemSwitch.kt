package dev.osmii.shadow.events.game

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.AbstractStructure
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import dev.osmii.shadow.Shadow
import org.bukkit.inventory.ItemStack

class PacketHideItemSwitch(val shadow: Shadow) : PacketAdapter(
    shadow,
    ListenerPriority.NORMAL,
    PacketType.Play.Server.ENTITY_EQUIPMENT
) {
    override fun onPacketSending(event: PacketEvent) {
        if (event.packetType != PacketType.Play.Server.ENTITY_EQUIPMENT) return
        if(event.player.entityId == event.packet.integers.read(0)) return
        val p = event.packet

        val entityId = p.integers.read(0)
        val equipment = p.slotStackPairLists.read(0)
        val itemSlot = equipment[0].first
        val item = equipment[0].second

        if(itemSlot != EnumWrappers.ItemSlot.MAINHAND) return
    }
}