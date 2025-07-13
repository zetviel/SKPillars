package net.adamcowell14.skpillars.utils

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.adamcowell14.skpillars.SKPillars
import org.bukkit.OfflinePlayer
import org.jetbrains.annotations.NotNull

class PlaceholderHelper(private val plugin: SKPillars) : PlaceholderExpansion() {

    @NotNull
    override fun getAuthor(): String {
        return "adamcowell14"
    }

    @NotNull
    override fun getIdentifier(): String {
        return "pillars"
    }

    @NotNull
    override fun getVersion(): String {
        return plugin.description.version
    }

    override fun persist(): Boolean {
        return true
    }

    override fun onRequest(player: OfflinePlayer?, @NotNull params: String): String? {
        if (params.lowercase() == "player_wins") {
            return player?.uniqueId?.let { plugin.statsHelper.getPlayerStats(it).wins.toString() }
        }

        if (params.lowercase() == "player_games") {
            return player?.uniqueId?.let { plugin.statsHelper.getPlayerStats(it).playedGames.toString() }
        }

        if (params.lowercase() == "player_items") {
            return player?.uniqueId?.let { plugin.statsHelper.getPlayerStats(it).itemsGet.toString() }
        }

        if (params.lowercase() == "player_events") {
            return player?.uniqueId?.let { plugin.statsHelper.getPlayerStats(it).eventsGet.toString() }
        }

        if (plugin.activeGames[params.lowercase().split("_")[0]] != null) {
            val arenaCurrent = plugin.activeGames[params.lowercase().split("_")[0]]
            if (params.lowercase() == "${arenaCurrent?.name}_remainingtime") {
                return arenaCurrent?.remainingTime.toString()
            }

            if (params.lowercase() == "${arenaCurrent?.name}_rewardinterval") {
                return arenaCurrent?.rewardInterval.toString()
            }

        }

        return null
    }
}