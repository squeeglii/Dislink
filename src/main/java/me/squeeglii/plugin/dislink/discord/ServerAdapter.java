package me.squeeglii.plugin.dislink.discord;

import me.squeeglii.plugin.dislink.Dislink;
import me.squeeglii.plugin.dislink.storage.DBLinks;
import me.squeeglii.plugin.dislink.storage.DBPendingLinks;
import me.squeeglii.plugin.dislink.util.Cfg;
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

        String missingPerms = missingPermsMessage == null
                ? DEFAULT_NO_LINK_PERMS_NOTICE
                : missingPermsMessage;

        this.missingLinkPermsMessage = missingPerms.replace("\\n", "\n");

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

        switch (event.getName()) {
            case "link" -> this.handleLinkCommand(event);
            case "unlinkall" -> this.handleUnlinkAllCommand(event);
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

        DBPendingLinks.tryCompleteLink(userId, code, this.shortName).whenComplete((ret, err) -> {
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
                case ACCOUNT_CAP_REACHED ->  new EmbedBuilder()
                        .setTitle("You've hit the paired account limit.")
                        .setDescription(
                                """
                                You're limited to %s Minecraft accounts per Discord account.

                                To pair more Minecraft accounts, you can either:
                                - Unpair all your current accounts with /unlinkall and pair new ones
                                - Contact server management to get an exemption.
                              
                                """.formatted(Cfg.MAX_ACCOUNT_LIMIT.dislink().orElse(1)))
                        .setFooter("/link")
                        .setColor(new Color(200, 200, 10))
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


    public void handleUnlinkAllCommand(SlashCommandInteractionEvent event) {
        event.getHook().setEphemeral(true);
        event.deferReply(true).queue();

        DBLinks.deleteAllLinksFor(event.getUser().getId()).whenComplete((ret, err) -> {

            if(err != null) {
                err.printStackTrace();
                event.getHook().editOriginal(MessageEditData.fromEmbeds(
                        this.generateGenericErrorEmbed("U001"))
                ).queue();
                return;
            }

            event.getHook().editOriginal(MessageEditData.fromEmbeds(
                    new EmbedBuilder()
                            .setTitle("Completed!")
                            .setDescription("Unlinked all Minecraft accounts associated with your Discord account.")
                            .setFooter("/unlinkall")
                            .setColor(new Color(150, 45, 170))
                            .build()
            )).queue();
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
