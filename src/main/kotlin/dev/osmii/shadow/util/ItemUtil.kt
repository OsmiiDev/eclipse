package dev.osmii.shadow.util

object ItemUtil {
    fun forbidden(
        drop: Boolean=true,
        use: Boolean=true,
        move: Boolean=true,
        moveContainer: Boolean=true): ByteArray {
        val forbidden = ByteArray(3)
        forbidden[0] = if (drop) 1.toByte() else 0.toByte()
        forbidden[1] = if (use) 1.toByte() else 0.toByte()
        forbidden[2] = if (move) 1.toByte() else 0.toByte()
        forbidden[3] = if (moveContainer) 1.toByte() else 0.toByte()
        return forbidden
    }
}
