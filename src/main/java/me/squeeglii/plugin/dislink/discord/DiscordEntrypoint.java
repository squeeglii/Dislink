package me.squeeglii.plugin.dislink.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Collections;

public class DiscordEntrypoint {

    private void start(String token) {
        JDABuilder botBuilder = JDABuilder.createLight(token, Collections.emptyList())
                .setActivity(Activity.customStatus("Bot I MINE !"))
                .addEventListeners(new DiscordListener());

        JDA bot = botBuilder.build();

        bot.updateCommands().addCommands(
                Commands.slash("link", "Go in-game to link your account & enter the code it gives you here.")
                        .setGuildOnly(true) // Needs to check roles
        );
    }

}
