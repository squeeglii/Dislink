package me.squeeglii.plugin.dislink;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPIConfig;
import me.squeeglii.plugin.dislink.command.ConfiguredCommand;
import me.squeeglii.plugin.dislink.command.WhoIsCommand;
import me.squeeglii.plugin.dislink.config.ConfigChecks;
import me.squeeglii.plugin.dislink.config.Feature;
import me.squeeglii.plugin.dislink.discord.DiscordManager;
import me.squeeglii.plugin.dislink.display.VerifierPrefixes;
import me.squeeglii.plugin.dislink.storage.DBLinks;
import me.squeeglii.plugin.dislink.storage.DBPendingLinks;
import me.squeeglii.plugin.dislink.storage.LinkedAccountCache;
import me.squeeglii.plugin.dislink.storage.helper.ConnectionWrapper;
import me.squeeglii.plugin.dislink.storage.helper.DatabaseAccess;
import me.squeeglii.plugin.dislink.config.Cfg;
import me.squeeglii.plugin.dislink.util.Run;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public final class Dislink extends JavaPlugin {

    private static Dislink instance = null;
    private static int enableCount = 0;
    private static DiscordManager discordInstance = null;

    private final List<ConfiguredCommand> commands = new LinkedList<>();

    private Set<Feature> lastEnabledFlags = new HashSet<>();

    private Run threadWatcher;
    private LinkedAccountCache linkedAccountCache;
    private VerifierPrefixes verifierPrefixes;

    private DatabaseAccess databaseCredentials;



    @Override
    public void onLoad() {
        instance = this;

        this.saveDefaultConfig();

        CommandAPIConfig<?> cmdApiCfg = new CommandAPIBukkitConfig(this);
        CommandAPI.onLoad(cmdApiCfg);

        this.registerCommand(new WhoIsCommand());
    }

    @Override
    public void onEnable() {
        instance = this;
        enableCount++;

        this.threadWatcher = new Run();
        this.verifierPrefixes = new VerifierPrefixes();
        this.linkedAccountCache = new LinkedAccountCache();

        // Must be called before loading DB config
        // as it adds the necessary fields.
        this.saveDefaultConfig();

        this.lastEnabledFlags = ConfigChecks.testForWorkingFeatures();

        if(!this.lastEnabledFlags.contains(Feature.CORE)) {
            this.getLogger().severe("Failed enough config checks to stop the plugin from working. Please review the log and fix any.");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        try {

            // todo: here would be a good place to replace just storing the credentials, with
            //       configuring a database interface w/ login included (aka, add local file storage support?)
            if(this.lastEnabledFlags.contains(Feature.REMOTE_DATABASE)) {
                this.databaseCredentials = DatabaseAccess.fromConfig();
            }

            DBLinks.createTables().get();
            DBPendingLinks.createTables().get();

        } catch (CancellationException | InterruptedException err) {
            this.getLogger().severe("Database validation was terminated too early! Aborting.");
            this.getPluginLoader().disablePlugin(this);
            return;

        } catch (IllegalStateException | ExecutionException err) {
            this.getLogger().severe("Error while validating the database: %s".formatted(err.getMessage()));
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        this.event(new PlayerLifecycleListener())
            .event(this.linkedAccountCache);

        try {
            CommandAPI.onEnable();
            this.requestDiscordInit();
            this.postInitTasks();

        } catch (Exception err) {
            this.getLogger().throwing("Dislink", "init", err);
            this.getPluginLoader().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if(instance == this)
            instance = null;

        for(ConfiguredCommand command: this.commands)
            CommandAPI.unregister(command.getId());

        this.commands.clear();
        this.getServer().getServicesManager().unregisterAll(this);
    }


    public void requestDiscordInit() {

        if(!this.lastEnabledFlags.contains(Feature.DISCORD_BOT)) {
            this.getLogger().warning("Skipped enabling the discord bot as it's improperly configured. Check logs for errors.");
            return;
        }

        this.getLogger().info("Launching the Discord Bot component.");

        discordInstance = new DiscordManager();

        Optional<String> optToken = Cfg.DISCORD_TOKEN.dislink();

        // sanity check - can probably be removed.
        if(optToken.isEmpty()) {
            discordInstance = null;
            this.getLogger().warning("Missing bot token! Discord bot is disabled !");
            return;
        }

        Dislink.discord().start(optToken.get());
        this.getLogger().info("Launched Discord Bot!");
    }

    private void postInitTasks() {
        if(Cfg.PRUNE_PENDING_LINKS_ON_START.dislink().orElse(true)) {
            DBPendingLinks.clearPendingLinks().whenComplete((ret, err) -> {
                if(err != null)
                    this.getLogger().throwing("Dislink", "clearPendingLinks", err);

                this.getLogger().info("Cleared existing pending account links. Codes must be regenerated.");
            });
        }

        this.service(LinkedAccountCache.class, this.linkedAccountCache)
            .service(VerifierPrefixes.class, this.verifierPrefixes);

        this.verifierPrefixes.loadFromConfig();
    }


    private Dislink event(Listener listener) {
        this.getServer().getPluginManager().registerEvents(listener, this);
        return this;
    }

    private <T> Dislink service(Class<T> serviceClass, T impl) {
        this.getServer().getServicesManager().register(serviceClass, impl, this, ServicePriority.Normal);
        return this;
    }

    private void registerCommand(ConfiguredCommand command) {
        this.commands.add(command);
        command.buildCommand().register();
    }


    public Run getThreadWatcher() {
        return this.threadWatcher;
    }

    public LinkedAccountCache getLinkedAccountCache() {
        return this.linkedAccountCache;
    }

    public DatabaseAccess getDbCredentials() {
        return this.databaseCredentials;
    }

    public ConnectionWrapper getDbConnection() throws SQLException {
        return ConnectionWrapper.fromAccess(this.databaseCredentials);
    }

    public Set<Feature> getLastEnabledFlags() {
        return this.lastEnabledFlags;
    }

    public static Dislink plugin() {
        return instance;
    }

    public static DiscordManager discord() {
        return discordInstance;
    }

    public static boolean usingFeature(Feature feature) {
        return Dislink.plugin().lastEnabledFlags.contains(feature);
    }

    public static int getEnableCount() {
        return enableCount;
    }
}
