package me.squeeglii.plugin.dislink.discord;

import me.squeeglii.plugin.dislink.Dislink;
import me.squeeglii.plugin.dislink.storage.DBPendingLinks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ServerAdapter extends ListenerAdapter {

    public static final String DEFAULT_NO_LINK_PERMS_NOTICE = "It seems that you don't have permission to use /link - have you got the right roles?";

    private final long guildId;

    private final String shortName;
    private final Long memberRoleId;
    private final Long adminRoleId;

    private final String missingLinkPermsMessage;

    public ServerAdapter(long guildId, String shortName, Long memberId, Long adminId, String missingPermsMessage) {
        this.guildId = guildId;
        this.memberRoleId = memberId;
        this.adminRoleId = adminId;
        this.shortName = shortName == null
                ? "unknown"
                : shortName;
        this.missingLinkPermsMessage = missingPermsMessage == null
                ? DEFAULT_NO_LINK_PERMS_NOTICE
                : missingPermsMessage;

        if(this.shortName.length() > 12)
            throw new IllegalArgumentException("Short-name for discord server is restricted to 12 characters in length.");
    }


    public boolean canUserLink(Member member) {
        if(this.memberRoleId == null)
            return true;

        Role role = Dislink.discord().getBot().getRoleById(this.memberRoleId);
        return member.getRoles().contains(role);
    }

    public boolean isUserAdmin(Member member) {
        if(this.adminRoleId == null)
            return false;

        Role role = Dislink.discord().getBot().getRoleById(this.adminRoleId);
        return member.getRoles().contains(role);
    }

    private boolean shouldIgnore(Guild guild) {
        if(guild == null)
            return true;

        return guild.getIdLong() != this.guildId;
    }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(this.shouldIgnore(event.getGuild()))
            return;

        switch (event.getCommandId()) {
            case "link" -> this.handleLinkCommand(event);
            case "unlink" -> {

            }
        }

    }

    private void handleLinkCommand(SlashCommandInteractionEvent event) {
        event.getHook().setEphemeral(true);
        event.deferReply(true).queue();

        Member member = event.getMember();

        if(member == null) {
            event.getHook().editOriginal(MessageEditData.fromEmbeds(
                    this.generateGenericErrorEmbed("D001")
            )).queue();
            return;
        }

        if(!this.canUserLink(member)) {
            event.getHook().editOriginal(MessageEditData.fromEmbeds(
                    new EmbedBuilder()
                            .setTitle("Uh Oh!")
                            .setDescription(this.missingLinkPermsMessage)
                            .setFooter("/link")
                            .setColor(new Color(200, 45, 10))
                            .build()
            )).queue();
            return;
        }

        String userId = member.getId();
        String code = event.getOption("code", "", OptionMapping::getAsString);

        if(code.trim().isEmpty()) {
            event.getHook().editOriginal(MessageEditData.fromEmbeds(
                    new EmbedBuilder()
                            .setTitle("Uh Oh!")
                            .setDescription("You didn't enter a valid code. Please try again.")
                            .setFooter("/link")
                            .setColor(new Color(200, 45, 10))
                            .build()
            )).queue();
            return;
        }

        DBPendingLinks.tryLink(userId, code, this.shortName).whenComplete((ret, err) -> {
            MessageEmbed response = switch (ret) {
                case INTERNAL_ERROR -> this.generateGenericErrorEmbed("D002");
                case NO_LINKS_PENDING -> new EmbedBuilder()
                        .setTitle("We have a problem!")
                        .setDescription("It doesn't seem that you have any account links pending. Try joining the server to generate a code.")
                        .setFooter("/link")
                        .setColor(new Color(200, 45, 10))
                        .build();
                case INVALID_CODE ->  new EmbedBuilder()
                        .setTitle("Uh Oh!")
                        .setDescription("The code '%s' was not correct. Please double-check it and try again.".formatted(code))
                        .setFooter("/link")
                        .setColor(new Color(200, 45, 10))
                        .build();
                case SUCCESS -> new EmbedBuilder()
                        .setTitle("Welcome!")
                        .setDescription("You've successfully linked your account! Re-join the Minecraft server and you should be able to play!")
                        .setFooter("/link")
                        .setColor(new Color(150, 45, 170))
                        .build();
            };

            event.getHook().editOriginal(MessageEditData.fromEmbeds(response)).queue();
        });
    }


    private MessageEmbed generateGenericErrorEmbed(String errorCode) {
        return new EmbedBuilder()
                .setTitle("Uh Oh!")
                .setDescription("Something went wrong while processing your command. (%s)".formatted(errorCode))
                .setFooter("/link")
                .setColor(new Color(200, 45, 10))
                .build();
    }


    public long getGuildId() {
        return this.guildId;
    }
}
