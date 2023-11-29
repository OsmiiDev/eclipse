package dev.osmii.shadow.game.rolelist

enum class RolelistInvalidReason {
    VALID,

    NOT_ENOUGH_ROLES,
    TOO_MANY_ROLES,

    ONLY_ONE_FACTION,

    UNIQUE_CONFLICT,
    INVALID_ROLE,
}