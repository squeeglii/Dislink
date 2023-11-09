package me.squeeglii.plugin.dislink;

import me.squeeglii.plugin.dislink.storage.DBPendingLinks;
import me.squeeglii.plugin.dislink.storage.helper.ConnectionWrapper;
import me.squeeglii.plugin.dislink.storage.helper.DatabaseAccess;
import me.squeeglii.plugin.dislink.util.Cfg;
import me.squeeglii.plugin.dislink.util.Run;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class Dislink extends JavaPlugin {

    private static Dislink instance = null;

    private Run threadWatcher;

    private DatabaseAccess databaseCredentials;


    @Override
    public void onEnable() {
        instance = this;
        this.threadWatcher = new Run();

        // Must be called before loading DB config
        // as it adds the necessary fields.
        this.saveDefaultConfig();

        this.databaseCredentials = DatabaseAccess.fromConfig();


        this.getServer().getPluginManager()
                .registerEvents(new PlayerLifecycleListener(), this);

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

    public Run getThreadWatcher() {
        return this.threadWatcher;
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
