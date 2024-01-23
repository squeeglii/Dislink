package me.squeeglii.plugin.dislink.config;

import me.squeeglii.plugin.dislink.Dislink;
import me.squeeglii.plugin.dislink.data.Key;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ConfigChecks {


    /**
     * Requires a Dislink plugin instance to be assigned - it checks all
     * config values to ensure that the plugin can run properly.
     * Some test cases result in warnings but return as true, as long as the plugin
     * can operate, even if not optimally.
     * @return Returns true if all checks have passed.
     */
    public static Set<Feature> testForWorkingFeatures() {
        Set<Feature> enabledFeatures = new HashSet<>();

        if(pairingCodes())
            enabledFeatures.add(Feature.PAIR_CODE_GENERATION);

        if(remoteDatabase())
            enabledFeatures.add(Feature.REMOTE_DATABASE);

        if(inGame())
            enabledFeatures.add(Feature.GAME_INTEGRATION);

        if(discord() && pairingCompletion())
            enabledFeatures.add(Feature.DISCORD_BOT);

        // todo: add an offline database or check for other ways the plugin can run.
        if(enabledFeatures.contains(Feature.REMOTE_DATABASE))
            enabledFeatures.add(Feature.CORE);

        return Collections.unmodifiableSet(enabledFeatures);
    }


    public static boolean remoteDatabase() {
        return true;
    }

    public static boolean discord() {
        String token = Cfg.DISCORD_TOKEN.dislink().orElse("");

        if(token.isBlank()) {
            warn(Cfg.DISCORD_TOKEN, "It must not be blank! See https://discordpy.readthedocs.io/en/stable/discord.html");
            return false;
        }

        return true;
    }

    public static boolean inGame() {
        return true;
    }

    public static boolean pairingCodes() {
        return true;
    }

    public static boolean pairingCompletion() {
        return true;
    }

    private static void error(Key<?> key, String message) {
        String formatted = "Config issue at '%s' -- %s - aborting".formatted(key.name(), message);
        Dislink.plugin().getLogger().severe(formatted);
    }

    private static void warn(Key<?> key, String message) {
        String formatted = "Config problem at '%s' -- %s - the plugin may behave strangely until this is fixed".formatted(
                key.name(), message
        );

        Dislink.plugin().getLogger().warning(formatted);
    }
}
