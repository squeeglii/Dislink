package me.squeeglii.plugin.dislink;

import me.squeeglii.plugin.dislink.util.Cfg;
import me.squeeglii.plugin.dislink.util.Generate;
import org.bukkit.plugin.java.JavaPlugin;

public final class Dislink extends JavaPlugin {

    private static Dislink instance = null;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();


        this.getServer().getPluginManager()
                .registerEvents(new PlayerLifecycleListener(), this);

        int maxAccounts = Cfg.MAX_ACCOUNT_LIMIT.dislink().orElseThrow();

        for(int i = 0; i < 10; i++) {
            this.getLogger().info(Generate.newLinkCode());
        }
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
