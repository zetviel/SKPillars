package net.adamcowell14.skpillars

import net.adamcowell14.skpillars.commands.*
import net.adamcowell14.skpillars.listeners.*
import net.adamcowell14.skpillars.utils.*
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class SKPillars : JavaPlugin() {
    lateinit var messageHelper: MessageHelper
    lateinit var messagesConfig: YamlConfiguration
    lateinit var worldEditHelper: WorldEditHelper
    lateinit var schematicManager: SchematicManager
    lateinit var scoreboardHelper: ScoreboardHelper
    lateinit var customItems: CustomItems
    lateinit var statsHelper: StatsHelper


    val activeGames = mutableMapOf<String, Arena>()
    var cooldownGame = false
    private val schematicsDir by lazy { File(dataFolder, "schematics") }

    override fun onEnable() {
        setupConfigs()
        initManagers()
        registerCommands()
        registerEvents()
    }

    private fun setupConfigs() {
        saveDefaultConfig()
        saveResource("messages.yml", false)
        schematicsDir.mkdirs()

        messagesConfig = YamlConfiguration.loadConfiguration(File(dataFolder, "messages.yml"))
    }

    private fun initManagers() {
        statsHelper = StatsHelper(this)
        messageHelper = MessageHelper(this)
        worldEditHelper = WorldEditHelper(this)
        schematicManager = SchematicManager(this)
        scoreboardHelper = ScoreboardHelper(this, messageHelper)
        customItems = CustomItems(this).also { it.init() }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PlaceholderHelper(this).register()
        }
    }

    private fun registerCommands() {
        getCommand("pillars")?.run {
            setExecutor(PillarsCommand(this@SKPillars))
            tabCompleter = PillarsCommand(this@SKPillars)
        }

        getCommand("psetup")?.run {
            setExecutor(SetupCommand(this@SKPillars))
            tabCompleter = SetupCommand(this@SKPillars)
        }

        getCommand("t")?.run {
            setExecutor(TestCommand(this@SKPillars))
            tabCompleter = TestCommand(this@SKPillars)
        }
    }

    private fun registerEvents() {
        val pm = server.pluginManager
        pm.registerEvents(LeaveZone(this), this)
        pm.registerEvents(BlockEvents(this), this)
        pm.registerEvents(PlayerEvents(this), this)
        pm.registerEvents(DisableCommand(this), this)
        pm.registerEvents(CustomItemsEvents(this), this)
    }

    fun getArenaNames(): List<String> = config.getConfigurationSection("game")?.getKeys(false)?.toList() ?: emptyList()
    fun getActiveGames(): List<String> = getArenaNames()

    fun getIdsSpawn(arenaName: String): List<String> {
        val spawnList = config.getList("game.$arenaName.spawn_location") ?: return listOf("0")
        return List(spawnList.size) { it.toString() }
    }

    fun reloadPlugin() {
        setupConfigs()
        reloadConfig()
        messageHelper = MessageHelper(this)
        customItems.init()
    }

    override fun onDisable() {
        activeGames.clear()
    }
}