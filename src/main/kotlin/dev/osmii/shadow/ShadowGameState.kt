package dev.osmii.shadow

import dev.osmii.shadow.enums.GamePhase
import dev.osmii.shadow.enums.PlayableRole
import dev.osmii.shadow.game.abilities.QueuedAction
import dev.osmii.shadow.game.rolelist.Rolelist
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ShadowGameState {
    var currentPhase: GamePhase = GamePhase.NONE

    var participationStatus = HashMap<UUID, Boolean>()

    var originalRolelist: Rolelist = Rolelist()
    var originalRoles: HashMap<UUID, PlayableRole> = HashMap<UUID, PlayableRole>()
    var currentRoles: HashMap<UUID, PlayableRole> = HashMap<UUID, PlayableRole>()
    var currentWinners: HashSet<Player> = HashSet<Player>()

    var queuedAbilityActions: HashMap<Player, ArrayList<QueuedAction>> = HashMap<Player, ArrayList<QueuedAction>>()
    var queuedAbilityMenus: HashMap<Player, LinkedList<Inventory>> = HashMap<Player, LinkedList<Inventory>>()
}