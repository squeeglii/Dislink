package me.squeeglii.plugin.dislink;

import me.squeeglii.plugin.dislink.storage.DBLinks;
import me.squeeglii.plugin.dislink.storage.DBPendingLinks;
import me.squeeglii.plugin.dislink.storage.LinkedAccountCache;
import me.squeeglii.plugin.dislink.storage.helper.ConnectionWrapper;
import me.squeeglii.plugin.dislink.storage.helper.DatabaseAccess;
import me.squeeglii.plugin.dislink.util.Cfg;
import me.squeeglii.plugin.dislink.util.Run;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class Dislink extends JavaPlugin {

    private static Dislink instance = null;

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

        DBLinks.getExistingAccountQuantityFor("test_user_not_real").whenComplete((ret, err) -> {
            if(err != null) {
                this.getLogger().info("Error trying to fetch paired account count for 'test_user_not_real' account");
                this.getLogger().throwing("Dislink", "getExistingAccountQuantityFor", err);
                return;
            }

            this.getLogger().info("'test_user_not_real' has %s accounts linked.".formatted(ret));
        });

        if(Cfg.PRUNE_PENDING_LINKS_ON_START.dislink().orElse(true)) {
            DBPendingLinks.clearPendingLinks().whenComplete((ret, err) -> {
                if(err != null)
                    this.getLogger().throwing("Dislink", "clearPendingLinks", err);

                this.getLogger().info("Cleared existing pending account links. Codes must be regenerated.");
            });
        }
    }

    @Override
    public void onDisable() {
        if(instance == this)
            instance = null;
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

    public static Dislink get() {
        return instance;
    }
}
