package me.squeeglii.plugin.dislink.config;

import me.squeeglii.plugin.dislink.Dislink;
import me.squeeglii.plugin.dislink.data.Key;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

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
        int errorCount = 0;
        String addr = Cfg.DB_ADDRESS.dislink().orElse("");
        String schema = Cfg.DB_SCHEMA.dislink().orElse("");
        String username = Cfg.DB_USERNAME.dislink().orElse("");
        String password = Cfg.DB_PASSWORD.dislink().orElse("");

        if(addr.isBlank()) errorCount += errorBlank(Cfg.DB_ADDRESS);
        if(schema.isBlank()) errorCount += errorBlank(Cfg.DB_SCHEMA);
        if(username.isBlank()) errorCount += errorBlank(Cfg.DB_USERNAME);
        if(password.isBlank()) errorCount += errorBlank(Cfg.DB_PASSWORD);

        //todo: pattern matching?

        return errorCount == 0;
    }

    public static boolean discord() {
        int errorCount = 0;
        String token = Cfg.DISCORD_TOKEN.dislink().orElse("");
        Optional<ConfigurationSection> optSection = Cfg.DISCORD_SERVER_CONFIGS.dislink();

        if(token.isBlank())
            errorCount += error(Cfg.DISCORD_TOKEN, "It must not be blank! See https://discordpy.readthedocs.io/en/stable/discord.html");

        if(optSection.isEmpty()) {
            errorCount += error(Cfg.DISCORD_SERVER_CONFIGS, "At least 1 server config must be defined for the bot to work.");

        } else {
            ConfigurationSection section = optSection.get();
            Set<String> entries = section.getKeys(false);

            if(entries.isEmpty())
                errorCount += error(Cfg.DISCORD_SERVER_CONFIGS, "At least 1 server config must be defined for the bot to work.");

            for(String key: entries) {
                errorCount += checkServerConfigEntry(section, key);
            }
        }

        return errorCount == 0;
    }

    public static boolean gameIntegration() {
        int errorCount = 0;

        //TODO check prefixes.

        return errorCount == 0;
    }

    public static boolean linkingWhitelist() {
        int errorCount = 0;

        return errorCount == 0;
    }

    public static boolean pairingCodes() {
        int errorCount = 0;

        int generationAttempts = Cfg.PAIRING_GENERATION_ATTEMPTS.dislink().orElse(3);
        Optional<Integer> codeBlocks = Cfg.PAIRING_BLOCKS.dislink();
        Optional<List<String>> codeWords = Cfg.PAIRING_WORDS.dislink();

        if(generationAttempts < 1)
            errorCount += errorLessThanOne(Cfg.PAIRING_GENERATION_ATTEMPTS);

        if(generationAttempts > 100)
            warn(Cfg.PAIRING_GENERATION_ATTEMPTS, "A large number of generation attempts could cause lag. Try reducing this to less than 100.");

        if(codeBlocks.isEmpty()) {
            errorCount += errorBlank(Cfg.PAIRING_BLOCKS);

        } else {
            int val = codeBlocks.get();

            if(val < 1)
                errorCount += errorLessThanOne(Cfg.PAIRING_BLOCKS);

            if(val > 8)
                warn(Cfg.PAIRING_BLOCKS, "Block counts of higher than 8 are ill-advised. Codes have a max length of 128 characters and this risks exceeding it + they're less memorable.");
        }

        if(codeWords.isEmpty()) {
            errorCount += errorBlank(Cfg.PAIRING_WORDS);

        } else {
            List<String> words = codeWords.get();

            if(words.isEmpty())
                errorCount += errorBlank(Cfg.PAIRING_WORDS);

            for(int i = 0; i < words.size(); i++) {
                String word = words.get(i);

                if(word.isBlank()) {
                    errorCount += error(
                            Cfg.PAIRING_WORDS,
                            "Words within the list cannot be blank. (See word #%s)".formatted(i)
                    );
                    continue;
                }

                if(word.length() > 16)
                    warn(Cfg.PAIRING_WORDS,
                        "(#%s: '%s')Words should aim to be shorter than 16 letters. They're less memorable.".formatted(
                                i, word
                        )
                    );
            }
        }

        return errorCount == 0;
    }


    private static int checkServerConfigEntry(ConfigurationSection parent, String key) {
        int errorCount = 0;

        if(!parent.isConfigurationSection(key)) {
            errorCount += error(
                    Cfg.DISCORD_SERVER_CONFIGS,
                    "Invalid server config entry at '%s' - it's not a section.".formatted(key)
            );
            return errorCount;
        }

        ConfigurationSection serverConfig = parent.getConfigurationSection(key);

        if(serverConfig == null)
            throw new IllegalStateException("Server config is null, even though it has passed a type check");

        Optional<String> shortName = Cfg.DISCORD_SHORT_NAME.from(serverConfig);

        if(shortName.isEmpty()) {
            warn(Cfg.DISCORD_SHORT_NAME, "It's advised that you set a short-name so that the link-location can be easily identified.");

        } else {
            String name = shortName.get();

            if(name.isBlank())
                errorCount += error(Cfg.DISCORD_SHORT_NAME, "It must not be blank (Server Config @ '%s')".formatted(key));

            if(name.length() > 11)
                errorCount += error(Cfg.DISCORD_SHORT_NAME, "It must be shorter than 12 letters long. (Server Config @ '%s')".formatted(key));
        }

        return errorCount;
    }


    private static int error(Key<?> key, String message) {
        String formatted = "Config issue at '%s' -- %s - aborting".formatted(key.name(), message);
        Dislink.plugin().getLogger().severe(formatted);

        // returned for counting errors - just looks a bit cleaner.
        // errors are counted rather than true/false returns so that every config issue can be flagged
        // in one go.
        return 1;
    }

    private static int errorBlank(Key<?> key) {
        return error(key, "It must not be blank!");
    }

    private static int errorLessThanOne(Key<?> key) {
        return error(key, "It must be greater or equal to one!");
    }

    private static void warn(Key<?> key, String message) {
        String formatted = "Config problem at '%s' -- %s - the plugin may behave strangely until this is fixed".formatted(
                key.name(), message
        );

        Dislink.plugin().getLogger().warning(formatted);
    }
}
