package net.adamcowell14.skpillars.utils

import net.adamcowell14.skpillars.PlayerStats
import net.adamcowell14.skpillars.SKPillars
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization
import java.io.File
import java.util.*

class StatsHelper(private val plugin: SKPillars) {
    private val statsFile = File(plugin.dataFolder, "player_stats.yml")
    private val statsData: YamlConfiguration
    private val playerStats = mutableMapOf<UUID, PlayerStats>()

    init {
        // Регистрация сериализуемого класса
        ConfigurationSerialization.registerClass(PlayerStats::class.java)

        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        if (!statsFile.exists()) {
            statsFile.createNewFile()
        }

        statsData = YamlConfiguration.loadConfiguration(statsFile)
        loadAllStats()
    }

    fun getPlayerStats(uuid: UUID): PlayerStats {
        return playerStats.getOrPut(uuid) {
            PlayerStats(
                playerName = "",
                uuid = uuid.toString(),
                wins = 0,
                playedGames = 0,
                itemsGet = 0,
                eventsGet = 0
            )
        }
    }

    fun savePlayerStats(uuid: UUID) {
        val stats = playerStats[uuid] ?: return
        statsData.set("stats.$uuid", stats)
        statsData.save(statsFile)
    }

    fun saveAllStats() {
        playerStats.forEach { (uuid, _) ->
            savePlayerStats(uuid)
        }
    }

    private fun loadAllStats() {
        val statsSection = statsData.getConfigurationSection("stats") ?: return

        for (uuidString in statsSection.getKeys(false)) {
            try {
                val uuid = UUID.fromString(uuidString)
                val stats = statsData.getObject("stats.$uuidString", PlayerStats::class.java) ?: continue
                playerStats[uuid] = stats
            } catch (e: Exception) {
                plugin.logger.warning("Failed to load stats for $uuidString: ${e.message}")
            }
        }
    }
}