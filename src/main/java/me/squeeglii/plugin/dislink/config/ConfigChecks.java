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

        if(gameIntegration())
            enabledFeatures.add(Feature.GAME_INTEGRATION);

        if(discord())
            enabledFeatures.add(Feature.DISCORD_BOT);

        // Always run the check for error logging.
        boolean isLinkingWhitelist = linkingWhitelist();

        // todo: add an offline database or check for other ways the plugin can run.
        if(enabledFeatures.contains(Feature.REMOTE_DATABASE)) {

            if(isLinkingWhitelist)
                enabledFeatures.add(Feature.LINK_WHITELIST);

            enabledFeatures.add(Feature.CORE);
        }

        return Collections.unmodifiableSet(enabledFeatures);
    }


    public static boolean remoteDatabase() {
        String addr = Cfg.DB_ADDRESS.dislink().orElse("");
        String schema = Cfg.DB_SCHEMA.dislink().orElse("");
        String username = Cfg.DB_USERNAME.dislink().orElse("");
        String password = Cfg.DB_PASSWORD.dislink().orElse("");

        if(addr.isBlank()) return errorBlankString(Cfg.DB_ADDRESS);
        if(schema.isBlank()) return errorBlankString(Cfg.DB_SCHEMA);
        if(username.isBlank()) return errorBlankString(Cfg.DB_USERNAME);
        if(password.isBlank()) return errorBlankString(Cfg.DB_PASSWORD);

        //todo: pattern matching?

        return true;
    }

    public static boolean discord() {
        String token = Cfg.DISCORD_TOKEN.dislink().orElse("");

        if(token.isBlank()) {
            error(Cfg.DISCORD_TOKEN, "It must not be blank! See https://discordpy.readthedocs.io/en/stable/discord.html");
            return false;
        }

        //TODO: Check the subsections. If one is invalid, invalidate them all.

        return true;
    }

    public static boolean gameIntegration() {
        //TODO check prefixes.
        return true;
    }

    public static boolean linkingWhitelist() {

        return true;
    }

    public static boolean pairingCodes() {
        int generationAttempts = Cfg.PAIRING_GENERATION_ATTEMPTS.dislink().orElse(3);

        return true;
    }

    private static boolean error(Key<?> key, String message) {
        String formatted = "Config issue at '%s' -- %s - aborting".formatted(key.name(), message);
        Dislink.plugin().getLogger().severe(formatted);
        return false;
    }

    private static boolean errorBlankString(Key<?> key) {
        return error(key, "It must not be blank!");
    }

    private static void warn(Key<?> key, String message) {
        String formatted = "Config problem at '%s' -- %s - the plugin may behave strangely until this is fixed".formatted(
                key.name(), message
        );

        Dislink.plugin().getLogger().warning(formatted);
    }
}
