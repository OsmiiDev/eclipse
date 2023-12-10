package dev.osmii.shadow.game.abilities

import java.util.UUID

class QueuedAction {
    private var uuid: UUID

    constructor(uuid: UUID) {
        this.uuid = uuid
    }

    constructor() {
        this.uuid = UUID.randomUUID()
    }

    fun readUUID(): UUID {
        return uuid
    }
}