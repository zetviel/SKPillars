package net.adamcowell14.skpillars.utils

import io.papermc.paper.scoreboard.numbers.NumberFormat
import net.adamcowell14.skpillars.SKPillars
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard

class ScoreboardHelper(private val plugin: SKPillars, private val messageHelper: MessageHelper) {

    private val legacySerializer = LegacyComponentSerializer.legacySection()
    private val playerScoreboards = mutableMapOf<Player, Scoreboard>()

    fun showScoreboard(player: Player, scb_name: String) {
        try {
            val scoreboard = playerScoreboards[player] ?: Bukkit.getScoreboardManager().newScoreboard
            var headerText = plugin.messagesConfig.getString(scb_name + ".scoreboard-head")
            val lines = plugin.messagesConfig.getStringList(scb_name + ".scoreboard")

            if (headerText == null) {
                headerText = ""
            }

            playerScoreboards[player] = scoreboard
            player.scoreboard = scoreboard

            scoreboard.objectives.forEach { it.unregister() }
            scoreboard.teams.forEach { it.unregister() }

            // Заголовок
            val header = messageHelper.parse(headerText, player)

            val objective = scoreboard.registerNewObjective(
                "main_${System.currentTimeMillis()}",
                Criteria.DUMMY,
                header
            )
            objective.numberFormat(NumberFormat.blank())
            objective.displaySlot = DisplaySlot.SIDEBAR

            lines.forEachIndexed { index, lineText ->
                val lineComponent = messageHelper.parse(lineText)

                // Используем невидимый символ + индекс для уникальности
                val invisibleEntry = "§${index}§r"

                objective.getScore(invisibleEntry).score = lines.size - index - 1

                // Настраиваем team для отображения текста
                setupTeam(scoreboard, invisibleEntry, lineComponent)
            }

        } catch (e: Exception) {
            plugin.logger.severe("Scoreboard error for ${player.name}: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupTeam(scoreboard: Scoreboard, entry: String, displayName: Component) {
        val teamName = "team_${entry.hashCode()}"

        var team = scoreboard.getTeam(teamName)
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName)
        }

        team.prefix(displayName)
        team.addEntry(entry)
    }

    fun updateScoreboard(player: Player, scb_name: String) {
        if (playerScoreboards.containsKey(player)) {
            showScoreboard(player, scb_name)
        }
    }

    fun removeScoreboard(player: Player) {
        playerScoreboards.remove(player)
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
    }
}