package net.adamcowell14.skpillars.commands

import net.adamcowell14.skpillars.SKPillars
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class TestCommand(private val plugin: SKPillars) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        when (args[0].lowercase()) {
            "show" -> {
                if (sender !is Player) {
                    return false
                }

                if (args.size != 2) {
                    sender.player?.sendMessage("args no")
                    return false
                }

                sender.player?.let { plugin.scoreboardHelper.showScoreboard(it, args[1]) }

                return true
            }
            "remove" -> {
                if (sender !is Player) {
                    return false
                }

                sender.player?.let { plugin.scoreboardHelper.removeScoreboard(it) }
                return true
            }
            "giveitem" -> {
                if (sender !is Player) {
                    return false
                }

                sender.inventory.setItem(0, plugin.customItems.leaveBed)
                sender.inventory.setItem(4, plugin.customItems.statisticHead)
                sender.inventory.setItem(8, plugin.customItems.statisticHead)

                return true
            }
            "removeitems" -> {
                if (sender !is Player) {
                    return false
                }

                val itemToRemove = listOf(plugin.customItems.statisticHead, plugin.customItems.leaveBed)

                for (i in 0 until sender.inventory.size) {
                    val item = sender.inventory.getItem(i) ?: continue

                    for (itemc in itemToRemove) {
                        if (item.isSimilar(itemc)) {
                            sender.inventory.setItem(i, null)
                        }
                    }
                }

                return true
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf("show", "remove", "giveitem", "removeitems").filter { it.startsWith(args[0], ignoreCase = true) }
            else -> emptyList()
        }
    }
}