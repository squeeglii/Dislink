package me.squeeglii.plugin.dislink.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import me.squeeglii.plugin.dislink.Dislink;
import me.squeeglii.plugin.dislink.storage.DBLinks;
import me.squeeglii.plugin.dislink.storage.LinkedAccount;
import me.squeeglii.plugin.dislink.util.OptionalFuture;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.Optional;
import java.util.UUID;

public class WhoIsCommand extends ConfiguredCommand {

    private static final String FEEDBACK_PREFIX = "%sD %s| %s".formatted(
            ChatColor.LIGHT_PURPLE, ChatColor.DARK_GRAY, ChatColor.RESET
    );

    private static final ChatColor HIGHLIGHT = ChatColor.AQUA;

    private static final String FORMATTED_TRUE = "%sTrue%s".formatted(ChatColor.GREEN, ChatColor.RESET);
    private static final String FORMATTED_FALSE = "%sFalse%s".formatted(ChatColor.DARK_RED, ChatColor.RESET);

    public WhoIsCommand() {
        super("whois");
    }

    @Override
    public CommandAPICommand buildCommand() {
        return new CommandAPICommand(this.getId())
                .withPermission("dislink.admin.whois")
                .withArguments(new OfflinePlayerArgument("player"))
                .executes((executor, args) -> {

                    OfflinePlayer offlinePlayer = (OfflinePlayer) args.get("player");

                    if(offlinePlayer == null) {
                        executor.sendMessage("%sUnable to find that player! Have they joined before?".formatted(ChatColor.RED));
                        return;
                    }

                    executor.sendMessage("%s%sfetching link details...".formatted(ChatColor.GRAY, ChatColor.ITALIC));

                    UUID accountId = offlinePlayer.getUniqueId();

                    OptionalFuture<LinkedAccount> profileFetchTask = offlinePlayer.isOnline()
                            ? this.getOnlinePlayerLink(accountId) // Utilise cache
                            : this.getOfflinePlayerLink(accountId); // Won't be cached, manually grab.

                    profileFetchTask.whenComplete((optProfile, err) -> {

                        if(err != null) {
                            executor.sendMessage("%sSomething went wrong while fetching their link details. Try again later.".formatted(ChatColor.RED));
                            err.printStackTrace();
                            return;
                        }

                        StringBuilder messageBuilder = new StringBuilder();
                        messageBuilder.append(FEEDBACK_PREFIX)
                                      .append("Profile for %s'%s'%s:\n".formatted(HIGHLIGHT, offlinePlayer.getName(), ChatColor.RESET));

                        this.formatBooleanValue(messageBuilder, "Is Online", offlinePlayer.isOnline())
                            .formatBooleanValue(messageBuilder, "Is Linked", optProfile.isPresent());

                        if(optProfile.isEmpty())
                            return;

                        LinkedAccount profile = optProfile.get();
                        this.formatStringValue(messageBuilder, "Origin (Where did they link?)", profile.verifier())
                            .formatStringValue(messageBuilder, "Discord Id", profile.discordId())
                            .formatStringValue(messageBuilder, "Minecraft Id", profile.minecraftId().toString())
                            .formatBooleanValue(messageBuilder, "Has Whitelist Bypass", profile.isGuest());

                        executor.sendMessage(messageBuilder.toString());
                    });

                });
    }

    private OptionalFuture<LinkedAccount> getOfflinePlayerLink(UUID accountId) {
        return DBLinks.getLinkFor(accountId);
    }

    private OptionalFuture<LinkedAccount> getOnlinePlayerLink(UUID accountId) {
        OptionalFuture<LinkedAccount> future = new OptionalFuture<>();
        Optional<LinkedAccount> optProfile = Dislink.plugin().getLinkedAccountCache().getAccount(accountId);
        future.complete(optProfile);
        return future;
    }

    private WhoIsCommand formatBooleanValue(StringBuilder builder, String key, boolean value) {
        builder.append("  %s? - %s\n".formatted(
                key,
                value ? FORMATTED_TRUE : FORMATTED_FALSE
        ));

        return this;
    }

    private WhoIsCommand formatStringValue(StringBuilder builder, String key, String value) {
        builder.append("  %s - %s%s%s\n".formatted(
                key,
                HIGHLIGHT, value, ChatColor.RESET
        ));

        return this;
    }

}
