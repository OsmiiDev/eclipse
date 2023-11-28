package dev.osmii.shadow.enums

enum class GamePhase(private val phase: Int) {
    NONE(0),
    IN_BETWEEN_ROUND(1),
    LOCATION_SELECTED(2),
    INITIAL_COUNTDOWN(3),
    ROLES_ASSIGNED(4),
    GAME_IN_PROGRESS(5)
}
