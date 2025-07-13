package net.adamcowell14.skpillars

import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

data class PlayerData(
    var lose: Boolean = false,
    var spec: Boolean = false
)

class Arena(val name: String, private val plugin: SKPillars) {
    val players = mutableMapOf<String, PlayerData>()
    var started = false
    var waiting = true

    private val minPlayersToStart = plugin.config.getInt("game.$name.settings.min-players")
    private val maxPlayers = plugin.config.getInt("game.$name.settings.max-players")
    private val timerNormal = 20 * plugin.config.getInt("game.$name.settings.wait-min")
    private val timerMaxPlayers = 20 * plugin.config.getInt("game.$name.settings.wait-max")

    private var countdownTask: BukkitTask? = null
    private var rewardTask: BukkitTask? = null
    val remainingTime = AtomicInteger(0)
    private val random = Random(System.currentTimeMillis())
    val rewardInterval get() = plugin.config.getInt("game.$name.settings.reward_interval", 10) * 20
    private val defaultEventChance get() = plugin.config.getInt("game.$name.settings.event_chance", 10)

    fun addPlayer(playerName: String) {
        players[playerName] = PlayerData()
        broadcastMessageComponent(plugin.messageHelper.parse("player-join", Bukkit.getPlayer(playerName)))
        checkStartConditions()
    }

    fun removePlayer(playerName: String) {
        players.remove(playerName)
        broadcastMessageComponent(plugin.messageHelper.parse("player-leave", Bukkit.getPlayer(playerName)))
        checkStartConditions()
    }

    fun stopArena() {
        cancelTasks()
        started = false
        waiting = true
    }

    private fun checkStartConditions() {
        if (started) return

        val currentPlayers = players.size

        if (currentPlayers == maxPlayers) {
            startCountdown(timerMaxPlayers)
        } else {
            startCountdown(timerNormal)
        }
    }

    private fun startCountdown(duration: Int) {
        cancelCountdown()
        remainingTime.set(duration)

        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            var timeLeft = remainingTime.decrementAndGet()

            if (timeLeft % 20 == 0 && players.size >= minPlayersToStart) {
                broadcastActionBarComponent(plugin.messageHelper.parse("player-cooldown", null, mapOf("time-left" to (timeLeft / 20).toString())))
            }

            if (players.size < minPlayersToStart) {
                broadcastActionBarComponent(plugin.messageHelper.parse("error-player-min"))
                waiting = true
                timeLeft = timerNormal
                return@Runnable
            }

            if (timeLeft <= 0) {
                startGame()
                cancelCountdown()
            }
        }, 1L, 1L)
    }

    private fun startGame() {
        if (started) return
        started = true
        waiting = false
        broadcastMessageComponent(plugin.messageHelper.parse("game-start", null, mapOf("name" to name)))

        players.keys.forEach { playerName ->
            val player = Bukkit.getPlayer(playerName)
            player?.let {
                plugin.statsHelper.getPlayerStats(it.uniqueId).playedGames++
                plugin.statsHelper.savePlayerStats(it.uniqueId)
            }
            if (player != null) {
                plugin.scoreboardHelper.showScoreboard(player, "game-scoreboard")
            }
        }

        val world = plugin.config.getString("world_all")?.let { Bukkit.getWorld(it) }
        world?.difficulty = Difficulty.HARD
        world?.worldBorder?.setSize(
            plugin.config.getDouble("game.$name.worldborder.to"),
            plugin.config.getLong("game.$name.worldborder.time")
        )

        val spawns = plugin.config.getStringList("game.$name.spawn_location")
        if (spawns.size < players.size) {
            plugin.logger.warning("ne xvataet to4ek dla spawna igrokov $name!")
            return
        }

        players.keys.forEachIndexed { index, playerName ->
            val player = Bukkit.getPlayer(playerName) ?: return@forEachIndexed
            if (index < spawns.size) {
                val spawn = spawns[index].split(" ")
                if (spawn.size >= 3) {
                    player.teleport(Location(world, spawn[0].toDouble(), spawn[1].toDouble(), spawn[2].toDouble()))
                    player.gameMode = GameMode.SURVIVAL

                    player.health = 20.0
                    player.foodLevel = 20
                    player.inventory.clear()
                }
            }
        }

        startRewardSystem()
    }

    private fun startRewardSystem() {
        rewardTask = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val activePlayers = players.filter { !it.value.lose && !it.value.spec }

            when {
                activePlayers.size <= 1 -> declareWinner(activePlayers.keys.first())
                else -> executeRandomAction()
            }
        }, rewardInterval.toLong(), rewardInterval.toLong())
    }

    private fun executeRandomAction() {
        if (random.nextInt(100) < defaultEventChance) {
            val eventsSection = plugin.config.getConfigurationSection("game.$name.events") ?: return
            val events = eventsSection.getKeys(false)

            if (events.isEmpty()) return

            val totalWeight = events.sumOf { event ->
                eventsSection.getInt("$event.chance", 10)
            }

            if (totalWeight <= 0) return

            var randomValue = random.nextInt(totalWeight)
            for (event in events) {
                val chance = eventsSection.getInt("$event.chance", 10)
                if (randomValue < chance) {
                    executeEvent(event)
                    return
                }
                randomValue -= chance
            }
        } else {
            giveRandomItems()
        }
    }

    private fun executeEvent(eventName: String) {
        val commands = plugin.config.getStringList("game.$name.events.$eventName.commands")
        broadcastActionBarComponent(plugin.messageHelper.parse("game-event", null, mapOf("eventName" to eventName)))

        // Обновляем статистику полученных событий
        players.keys.forEach { playerName ->
            val player = Bukkit.getPlayer(playerName)
            player?.let {
                plugin.statsHelper.getPlayerStats(it.uniqueId).eventsGet++
                plugin.statsHelper.savePlayerStats(it.uniqueId)
            }
        }

        commands.forEach { cmd ->
            players.keys.forEach { player ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player))
            }
        }
    }
    private fun giveRandomItems() {
        val itemsSection = plugin.config.getConfigurationSection("game.$name.items") ?: return

        when (itemsSection.getString("type", "whitelist")?.lowercase()) {
            "whitelist" -> giveWhitelistItems()
            "blacklist" -> giveBlacklistItems()
            else -> plugin.logger.warning("Неизвестный тип выдачи предметов. Используется whitelist")
        }
    }

    private fun giveWhitelistItems() {
        val itemsSection = plugin.config.getConfigurationSection("game.$name.items") ?: return
        val items = itemsSection.getKeys(false).filter { it != "type" }

        if (items.isEmpty()) return

        val weightedItems = mutableListOf<Pair<String, Int>>()
        var totalWeight = 0

        for (itemKey in items) {
            val weight = itemsSection.getInt("$itemKey.chance", 10)
            weightedItems.add(itemKey to weight)
            totalWeight += weight
        }

        if (totalWeight <= 0) return

        players.forEach { (name, data) ->
            if (data.lose || data.spec) return@forEach

            val player = Bukkit.getPlayer(name) ?: return@forEach
            val randomValue = random.nextInt(totalWeight)
            var currentWeight = 0

            for ((itemKey, weight) in weightedItems) {
                currentWeight += weight
                if (randomValue < currentWeight) {
                    val item = plugin.config.getItemStack("game.$name.items.$itemKey") ?: return@forEach
                    giveItemWithMessage(player, item)
                    break
                }
            }
        }
    }

    private fun giveBlacklistItems() {
        val blacklisted = getBlacklistedMaterials()
        val materials = Material.entries
            .filter { it.isItem && !it.isAir && !blacklisted.contains(it) }

        if (materials.isEmpty()) return

        players.forEach { (name, data) ->
            if (data.lose || data.spec) return@forEach

            val player = Bukkit.getPlayer(name) ?: return@forEach
            val item = ItemStack(materials.random(random))
            giveItemWithMessage(player, item)
        }
    }

    private fun getBlacklistedMaterials(): Set<Material> {
        return plugin.config.getConfigurationSection("game.$name.items")
            ?.getKeys(false)
            ?.filter { it != "type" }
            ?.mapNotNull { plugin.config.getItemStack("game.$name.items.$it")?.type }
            ?.toSet() ?: emptySet()
    }

    private fun giveItemWithMessage(player: Player, item: ItemStack) {
        if (player.inventory.firstEmpty() != -1) {
            player.inventory.addItem(item.clone())
        } else {
            player.world.dropItem(player.location, item.clone())
        }

        // Обновляем статистику полученных предметов
        plugin.statsHelper.getPlayerStats(player.uniqueId).itemsGet++
        plugin.statsHelper.savePlayerStats(player.uniqueId)

        val name = item.itemMeta?.displayName() ?: item.type.translationKey()
            .split(".").last().replace("_", " ").replaceFirstChar { it.uppercase() }

        player.sendActionBar(plugin.messageHelper.parse("game-item",player, mapOf("nameItem" to name.toString())))
    }

    fun declareWinner(winnerName: String) {
        rewardTask?.cancel()
        rewardTask = null


        if (players[winnerName]?.lose == false) {
            broadcastMessageComponent(plugin.messageHelper.parse("game-win", Bukkit.getPlayer(winnerName), mapOf("player" to winnerName)))

            // Обновляем статистику побед для победителя
            val winner = Bukkit.getPlayer(winnerName)
            winner?.let {
                plugin.statsHelper.getPlayerStats(it.uniqueId).wins++
                plugin.statsHelper.savePlayerStats(it.uniqueId)
            }

            if (winner != null) {
                plugin.scoreboardHelper.removeScoreboard(winner)
            }

            plugin.config.getStringList("game.$name.winner_commands").forEach { cmd ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player1}", winnerName))
            }
        }

        val world = plugin.config.getString("world_all")?.let { Bukkit.getWorld(it) }
        val pos1 = plugin.config.getString("game.$name.settings.chunk-delete.1pos")?.split(" ")!!
        val pos2 = plugin.config.getString("game.$name.settings.chunk-delete.2pos")?.split(" ")!!
        val player = Bukkit.getPlayer(winnerName)
        player?.allowFlight = true

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            stopArena()
            player?.allowFlight = false

            plugin.config.getString("game.$name.settings.back-command")?.let { player?.performCommand(it)
                ?: plugin.logger.warning("idk") }

            player?.clearActivePotionEffects()
            player?.gameMode = GameMode.SURVIVAL
            player?.inventory?.clear()

            for (plr in players) {
                if (plr.value.spec) {
                    val plr2 = Bukkit.getPlayer(plr.key)

                    plugin.config.getString("game.$name.settings.back-command")?.let { plr2?.performCommand(it)
                        ?: plugin.logger.warning("idk") }
                    plr2?.clearActivePotionEffects()
                    plr2?.gameMode = GameMode.SURVIVAL

                    if (plr2 != null) {
                        plugin.scoreboardHelper.removeScoreboard(plr2)
                    }
                }
            }

            plugin.activeGames.remove(name)

            if (world != null) {
                plugin.cooldownGame = true
                plugin.worldEditHelper.clearChunks(world,
                    pos1[0].toInt(),
                    pos1[1].toInt(),
                    pos2[0].toInt(),
                    pos2[1].toInt()
                )
            }
        }, 20 * 5)
    }

    private fun cancelTasks() {
        countdownTask?.cancel()
        rewardTask?.cancel()
        countdownTask = null
        rewardTask = null
    }

    private fun broadcastMessageComponent(message: Component) {
        players.keys.forEach { Bukkit.getPlayer(it)?.sendMessage(message) }
    }

    private fun broadcastActionBarComponent(message: Component) {
        players.keys.forEach { Bukkit.getPlayer(it)?.sendActionBar(message) }
    }

    private fun cancelCountdown() {
        countdownTask?.cancel()
        countdownTask = null
    }
}