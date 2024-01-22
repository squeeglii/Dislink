package me.squeeglii.plugin.dislink.discord;

import me.squeeglii.plugin.dislink.Dislink;
import me.squeeglii.plugin.dislink.config.Cfg;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

public class DiscordManager {

    private Thread botThread = null;
    private JDA bot = null;
    private HashMap<Long, ServerAdapter> serverAdapters;


    public void start(String token) {
        this.serverAdapters = this.parseAdaptersFromConfig();

        this.botThread = new Thread(() -> {
            try {

                JDABuilder botBuilder = JDABuilder.createLight(token, Collections.emptyList())
                        .setActivity(Activity.customStatus("Telling bad jokes 24/7"));

                this.serverAdapters.values().forEach(botBuilder::addEventListeners);

                this.bot = botBuilder.build();

                this.bot.updateCommands().addCommands(
                        Commands.slash("link", "Go in-game to link your account & enter the code it gives you here.")
                                .addOption(OptionType.STRING, "code", "The link code", true, false)
                                .setGuildOnly(true), // Needs to check roles

                        Commands.slash("unlinkall", "Unlinks all accounts from your Discord account.")
                                .setGuildOnly(true) // Needs to check roles
                ).queue();

            } catch (Exception err) {
                Dislink.plugin()
                       .getLogger()
                       .throwing("Discord", "init", err);
            }
        });

        this.botThread.start();
    }

    private HashMap<Long, ServerAdapter> parseAdaptersFromConfig() {
        Optional<ConfigurationSection> optSection = Cfg.SERVER_CONFIGS.dislink();

        if(optSection.isEmpty()) {
            Dislink.plugin().getLogger().warning("No server configurations found! The bot is basically a dud.");
            return new HashMap<>();
        }

        ConfigurationSection root = optSection.get();
        Set<String> keys = root.getKeys(false);
        HashMap<Long, ServerAdapter> adapters = new HashMap<>();

        for(String guildId: keys) {
            this.parseSingleAdapter(guildId, root).ifPresent(adapter -> {
                adapters.put(adapter.getGuildId(), adapter);
            });
        }

        return adapters;
    }

    private Optional<ServerAdapter> parseSingleAdapter(String guildId, ConfigurationSection configsSection) {
        long longGuildId;

        try {
            longGuildId = Long.parseLong(guildId);
        } catch (NumberFormatException err) {
            Dislink.plugin().getLogger().warning("Non-numerical key found in discord server configurations!");
            return Optional.empty();
        }

        ConfigurationSection guildSection = configsSection.getConfigurationSection(guildId);

        if(guildSection == null) {
            Dislink.plugin().getLogger().warning("Non-config-section type found in server configs.");
            return Optional.empty();
        }

        String shortName = Cfg.DISCORD_SHORT_NAME.from(guildSection).orElse("somewhere");
        Long memberRole = Cfg.DISCORD_MEMBER_ROLE_ID.from(guildSection).orElse(null);
        String missingPermsMsg = Cfg.DISCORD_MISSING_PERMS_MESSAGE
                .from(guildSection)
                .orElse(ServerAdapter.DEFAULT_NO_LINK_PERMS_NOTICE);

        try {
            ServerAdapter adapter = new ServerAdapter(longGuildId, shortName, memberRole, missingPermsMsg);
            return Optional.of(adapter);

        } catch (Exception err) {
            Dislink.plugin().getLogger().throwing("Discord", "adapterSetup", err);
            return Optional.empty();
        }
    }


    public JDA getBot() {
        return this.bot;
    }
}
