package dev.osmii.shadow

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import dev.osmii.shadow.commands.*
import dev.osmii.shadow.events.CollectStrongholdGeneration
import dev.osmii.shadow.events.HandleItemInteractionRestrict
import dev.osmii.shadow.events.custom.HandleAddRole
import dev.osmii.shadow.events.custom.HandleDayNight
import dev.osmii.shadow.events.custom.HandleParticipationToggle
import dev.osmii.shadow.events.custom.abilities.item.sheriff.HandleSheriffBow
import dev.osmii.shadow.events.custom.abilities.menu.HandleAbilities
import dev.osmii.shadow.events.game.*
import dev.osmii.shadow.util.BoundingBoxComparator
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Team
import org.bukkit.util.BoundingBox
import java.util.concurrent.ConcurrentSkipListSet
import java.util.logging.Logger

class Shadow : JavaPlugin() {
    var gameState: ShadowGameState = ShadowGameState()
    var protocolManager: ProtocolManager? = null
    val boundingBoxSet: ConcurrentSkipListSet<BoundingBox> = ConcurrentSkipListSet(BoundingBoxComparator())

    override fun onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager()

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
        Bukkit.getPluginManager().registerEvents(HandleAbilities(this), this)

        Bukkit.getPluginManager().registerEvents(CollectStrongholdGeneration(this), this)

        protocolManager!!.addPacketListener(PacketHideItemSwitch(this))

        // Register commands
        getCommand("\$roles")!!.setExecutor(CommandRoles(this))
        getCommand("\$location")!!.setExecutor(CommandLocation(this))
        getCommand("\$start")!!.setExecutor(CommandStart(this))
        getCommand("\$cancel")!!.setExecutor(CommandCancel(this))
        getCommand("shadowchat")!!.setExecutor(CommandShadowChat(this))

        // Create player team
        var team = Bukkit.getScoreboardManager().mainScoreboard.getTeam("players")
        if (team == null) {
            team = Bukkit.getScoreboardManager().mainScoreboard.registerNewTeam("players")
        }

        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)
    }

    companion object {
        var logger: Logger? = null
    }
}
