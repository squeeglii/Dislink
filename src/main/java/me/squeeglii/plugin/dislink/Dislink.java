package me.squeeglii.plugin.dislink;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Dislink extends JavaPlugin {

    private static Dislink instance;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();

        int maxAccounts = Cfg.MAX_ACCOUNT_LIMIT.dislink().orElseThrow();

        this.getLogger().info("%s".formatted(maxAccounts));
    }

    @Override
    public void onDisable() {
        if(instance == this)
            instance = null;
    }


    public static Dislink get() {
        return instance;
    }
}
