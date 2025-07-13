package net.adamcowell14.skpillars

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.*

@SerializableAs("PlayerStats")
data class PlayerStats(
    val playerName: String,
    val uuid: String,
    var wins: Int = 0,
    var playedGames: Int = 0,
    var itemsGet: Int = 0,
    var eventsGet: Int = 0,
) : ConfigurationSerializable {

    override fun serialize(): Map<String, Any> {
        return mapOf(
            "playerName" to playerName,
            "uuid" to uuid,
            "wins" to wins,
            "playedGames" to playedGames,
            "itemsGet" to itemsGet,
            "eventsGet" to eventsGet
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(map: Map<String, Any>): PlayerStats {
            return PlayerStats(
                playerName = map["playerName"] as String,
                uuid = map["uuid"] as String,
                wins = map["wins"] as? Int ?: 0,
                playedGames = map["playedGames"] as? Int ?: 0,
                itemsGet = map["itemsGet"] as? Int ?: 0,
                eventsGet = map["eventsGet"] as? Int ?: 0,
            )
        }
    }
}