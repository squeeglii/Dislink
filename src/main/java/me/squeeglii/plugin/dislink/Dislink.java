package me.squeeglii.plugin.dislink;

import me.squeeglii.plugin.dislink.discord.DiscordManager;
import me.squeeglii.plugin.dislink.storage.DBPendingLinks;
import me.squeeglii.plugin.dislink.storage.LinkedAccountCache;
import me.squeeglii.plugin.dislink.storage.helper.ConnectionWrapper;
import me.squeeglii.plugin.dislink.storage.helper.DatabaseAccess;
import me.squeeglii.plugin.dislink.util.Cfg;
import me.squeeglii.plugin.dislink.util.Run;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Optional;

public final class Dislink extends JavaPlugin {

    private static Dislink instance = null;
    private static DiscordManager discordInstance = null;

    private Run threadWatcher;
    private LinkedAccountCache linkedAccountCache;

    private DatabaseAccess databaseCredentials;


    @Override
    public void onEnable() {
        instance = this;
        this.threadWatcher = new Run();
        this.linkedAccountCache = new LinkedAccountCache();

        // Must be called before loading DB config
        // as it adds the necessary fields.
        this.saveDefaultConfig();

        this.databaseCredentials = DatabaseAccess.fromConfig();

        this.event(new PlayerLifecycleListener())
            .event(this.linkedAccountCache);


        try {
            this.initDiscord();
            this.postInitTasks();

        } catch (Exception err) {
            this.getLogger().throwing("Dislink", "init", err);
        }
    }

    @Override
    public void onDisable() {
        if(instance == this)
            instance = null;
    }


    public void initDiscord() {
        discordInstance = new DiscordManager();

        Optional<String> optToken = Cfg.DISCORD_TOKEN.dislink();

        if(optToken.isEmpty()) {
            discordInstance = null;
            this.getLogger().warning("Missing bot token! Discord bot is disabled !");
            return;
        }

        Dislink.discord().start(optToken.get());
    }

    private void postInitTasks() {
        if(Cfg.PRUNE_PENDING_LINKS_ON_START.dislink().orElse(true)) {
            DBPendingLinks.clearPendingLinks().whenComplete((ret, err) -> {
                if(err != null)
                    this.getLogger().throwing("Dislink", "clearPendingLinks", err);

                this.getLogger().info("Cleared existing pending account links. Codes must be regenerated.");
            });
        }
    }


    private Dislink event(Listener listener) {
        this.getServer().getPluginManager().registerEvents(listener, this);
        return this;
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


    public static Dislink plugin() {
        return instance;
    }

    public static DiscordManager discord() {
        return discordInstance;
    }
}
