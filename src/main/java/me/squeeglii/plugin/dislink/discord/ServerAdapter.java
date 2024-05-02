package me.squeeglii.plugin.dislink.discord;

import me.squeeglii.plugin.dislink.Dislink;
import me.squeeglii.plugin.dislink.storage.DBLinks;
import me.squeeglii.plugin.dislink.storage.DBPendingLinks;
import me.squeeglii.plugin.dislink.config.Cfg;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ServerAdapter extends ListenerAdapter {



    public static final String DEFAULT_NO_LINK_PERMS_NOTICE = "It seems that you don't have permission to use /link - have you got the right roles?";

    private final long guildId;

    private final String shortName;
    private final Long memberRoleId;

    private final String missingLinkPermsMessage;

    private final HashMap<String, Consumer<InteractionHook>> postGdprAction;

    public ServerAdapter(long guildId, String shortName, Long memberId, String missingPermsMessage) {
        this.guildId = guildId;
        this.memberRoleId = memberId;
        this.shortName = shortName == null
                ? "unknown"
                : shortName;

        String missingPerms = missingPermsMessage == null
                ? DEFAULT_NO_LINK_PERMS_NOTICE
                : missingPermsMessage;

        this.missingLinkPermsMessage = missingPerms.replace("\\n", "\n");

        if(this.shortName.length() > 12)
            throw new IllegalArgumentException("Short-name for discord server is restricted to 12 characters in length.");

        this.postGdprAction = new HashMap<>();
    }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(this.shouldIgnore(event.getGuild()))
            return;


        switch (event.getName()) {
            case "link" -> this.handleLinkCommand(event);
            case "unlinkall" -> this.handleUnlinkAllCommand(event);
            default -> this.handleUnknownCommand(event);
        }

    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if(this.shouldIgnore(event.getGuild()))
            return;

        event.getHook().setEphemeral(true);
        event.deferEdit().setComponents().queue();

        // Admittedly, this is quite a bad way to implement this as it means the only buttons
        // supported must be for GDPR consent under /link.

        // Change this if you need more buttons !!
        String userId = event.getUser().getId();

        switch (event.getButton().getLabel().trim().toLowerCase()) {

            case "agree" -> {
                Consumer<InteractionHook> action = this.postGdprAction.remove(userId);

                if(action == null) {
                    event.getHook().editOriginal(MessageEditData.fromEmbeds(
                            this.generateGenericErrorEmbed("G001")
                    )).queue();
                    return;
                }

                action.accept(event.getHook());
            }

            case "cancel" -> {
                this.postGdprAction.remove(userId);

                // Clean up any existing data now that consent is revoked.
                DBLinks.deleteAllLinksFor(userId).whenComplete((ret, err) -> {

                    if(err != null) {
                        Dislink.plugin().getLogger().log(Level.WARNING, "Error G002 for Discord User <@%s>".formatted(userId), err);

                        event.getHook().editOriginal(MessageEditData.fromEmbeds(
                                this.generateGenericErrorEmbed("G002")
                        )).queue();
                        return;
                    }

                    event.getHook().editOriginal(MessageEditData.fromEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Alrighty then!")
                                    .setDescription("Your account has not been linked & any previously linked accounts have been unlinked.")
                                    .setFooter("/link")
                                    .setColor(new Color(200, 200, 200))
                                    .build()
                    )).queue();
                });
            }

        }
    }


    private void handleLinkCommand(SlashCommandInteractionEvent event) {
        event.getHook().setEphemeral(true);
        event.deferReply(true).queue();

        String code = event.getOption("code", "", OptionMapping::getAsString);
        Member member = event.getMember();
        String memberId = event.getUser().getId();
        boolean showConsentMenu = Cfg.OBTAIN_GDPR_CONSENT.dislink().orElse(true);

        if(!showConsentMenu) {
            this.handleLink(member, code, event.getHook());
            return;
        }

        this.postGdprAction.put(memberId, (hook) -> this.handleLink(member, code, hook));

        event.getHook().editOriginal(MessageEditData.fromEmbeds(
                new EmbedBuilder()
                        .setTitle("You & Your Data!")
                        .setDescription(
                        """
                            By running this command & clicking 'Agree' below, you're agreeing to the following:
                                
                            - Your Discord Id & your Minecraft Account Id being stored.
                            
                            - The Discord Guild that this command was run in being stored.
                            
                        """)
                        .setFooter("/link")
                        .setColor(new Color(200, 200, 200))
                        .build()
        )).setActionRow(
                Button.success("agree", "Agree"),
                Button.secondary("cancel", "Cancel")
        ).queue();
    }


    public void handleUnlinkAllCommand(SlashCommandInteractionEvent event) {
        event.getHook().setEphemeral(true);
        event.deferReply(true).queue();

        DBLinks.deleteAllLinksFor(event.getUser().getId()).whenComplete((ret, err) -> {

            if(err != null) {
                String userId = event.getUser().getId();
                Dislink.plugin().getLogger().log(Level.WARNING, "Error U001 for Discord User <@%s>".formatted(userId), err);

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

    public void handleUnknownCommand(SlashCommandInteractionEvent event) {
        event.getHook().setEphemeral(true);
        event.replyEmbeds(this.generateGenericErrorEmbed("404")).queue();

        String userId = event.getId();
        Dislink.plugin().getLogger().log(Level.WARNING, "Error 404 for Discord User <@%s> -- missing command".formatted(userId));
    }


    private void handleLink(Member member, String code, InteractionHook hook) {

        if(member == null) {
            hook.editOriginal(MessageEditData.fromEmbeds(
                    this.generateGenericErrorEmbed("D001")
            )).queue();
            return;
        }

        if(!this.canUserLink(member)) {
            hook.editOriginal(MessageEditData.fromEmbeds(
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

        if(code.trim().isEmpty()) {
            hook.editOriginal(MessageEditData.fromEmbeds(
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

            hook.editOriginal(MessageEditData.fromEmbeds(response)).queue();
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

    public boolean canUserLink(Member member) {
        if(this.memberRoleId == null)
            return true;

        Role role = Dislink.discord().getBot().getRoleById(this.memberRoleId);
        return member.getRoles().contains(role);
    }

    private boolean shouldIgnore(Guild guild) {
        if(guild == null)
            return true;

        return guild.getIdLong() != this.guildId;
    }


    @Override
    public String toString() {
        return "{ Server Id: %s, Member Role Id: %s, Short Name: %s}".formatted(this.guildId, this.memberRoleId, this.shortName);
    }
}
