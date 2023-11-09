package me.squeeglii.plugin.dislink.discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ServerAdapter extends ListenerAdapter {

    public static final String DEFAULT_NO_LINK_PERMS_NOTICE = "It seems that you don't have permission to use /link - have you got the right roles?";

    private final long guildId;

    private final String shortName;
    private final Long memberId;
    private final Long adminId;

    private final String missingLinkPermsMessage;

    public ServerAdapter(long guildId, String shortName, Long memberId, Long adminId, String missingPermsMessage) {
        this.guildId = guildId;
        this.memberId = memberId;
        this.adminId = adminId;
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
        if(this.memberId == null)
            return true;
    }

    public boolean isUserAdmin(Member member) {
        if(this.adminId == null)
            return false;


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

        super.onSlashCommandInteraction(event);
    }


    public long getGuildId() {
        return this.guildId;
    }
}
