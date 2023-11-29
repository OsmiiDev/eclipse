package dev.osmii.shadow

import com.comphenix.protocol.ProtocolLib
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import dev.osmii.shadow.commands.*
import dev.osmii.shadow.enums.GamePhase
import dev.osmii.shadow.enums.PlayableRole
import dev.osmii.shadow.events.*
import dev.osmii.shadow.events.custom.HandleAddRole
import dev.osmii.shadow.events.custom.HandleDayNight
import dev.osmii.shadow.events.custom.HandleParticipationToggle
import dev.osmii.shadow.events.custom.abilities.HandleOpenAbilityMenu
import dev.osmii.shadow.events.custom.abilities.shadow.HandleShadowGuessSheriff
import dev.osmii.shadow.events.custom.abilities.sheriff.HandleSheriffBow
import dev.osmii.shadow.events.game.*
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Team
import java.util.*
import java.util.logging.Logger
import kotlin.collections.HashMap

class Shadow : JavaPlugin() {
    var gameState: ShadowGameState = ShadowGameState()
    var pm: ProtocolManager? = null

    override fun onEnable() {
        pm = ProtocolLibrary.getProtocolManager()

        Companion.logger = logger
        Companion.logger!!.info("Enabling Shadow")

        // Register events
        HandleDayNight(this).register()

        Bukkit.getPluginManager().registerEvents(HandleItemInteractionRestrict(this), this)
        Bukkit.getPluginManager().registerEvents(HandleChat(this), this)
        Bukkit.getPluginManager().registerEvents(HandleDeath(this), this)
        Bukkit.getPluginManager().registerEvents(HandleJoinLeave(this), this)
        Bukkit.getPluginManager().registerEvents(HandleMoveRestrict(this), this)

        Bukkit.getPluginManager().registerEvents(HandleSheriffBow(this), this)

        Bukkit.getPluginManager().registerEvents(HandleParticipationToggle(this), this)
        Bukkit.getPluginManager().registerEvents(HandleAddRole(this), this)
        Bukkit.getPluginManager().registerEvents(HandleOpenAbilityMenu(this), this)

        Bukkit.getPluginManager().registerEvents(HandleShadowGuessSheriff(this), this)

        pm!!.addPacketListener(PacketHideItemSwitch(this))

        // Register commands
        getCommand("\$roles")!!.setExecutor(CommandRoles(this))
        getCommand("\$location")!!.setExecutor(CommandLocation(this))
        getCommand("\$start")!!.setExecutor(CommandStart(this))
        getCommand("\$cancel")!!.setExecutor(CommandCancel(this))
        getCommand("shadowchat")!!.setExecutor(CommandShadowChat(this))

        // Create player team
        var team = Bukkit.getScoreboardManager()?.mainScoreboard?.getTeam("players")
        if (team == null) {
            team = Bukkit.getScoreboardManager()?.mainScoreboard?.registerNewTeam("players")
        }

        team?.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)
    }

    companion object {
        var logger: Logger? = null
        val instance: Shadow
            get() = Bukkit.getPluginManager().getPlugin("Shadow") as Shadow
    }
}
