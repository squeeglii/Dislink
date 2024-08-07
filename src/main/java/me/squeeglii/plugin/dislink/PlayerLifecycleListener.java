package me.squeeglii.plugin.dislink;

import me.squeeglii.plugin.dislink.config.Feature;
import me.squeeglii.plugin.dislink.exception.ExhaustedOptionsException;
import me.squeeglii.plugin.dislink.storage.DBLinks;
import me.squeeglii.plugin.dislink.storage.DBPendingLinks;
import me.squeeglii.plugin.dislink.storage.LinkResult;
import me.squeeglii.plugin.dislink.storage.LinkedAccount;
import me.squeeglii.plugin.dislink.config.Cfg;
import me.squeeglii.plugin.dislink.util.Run;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;

import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;


public class PlayerLifecycleListener implements Listener {

    public static final String FAILED_ACCOUNT_GET = error("001");

    public static final String FAILED_CODE_GENERATION_INTERNAL = error("002");
    public static final String FAILED_CODE_GENERATION_OTHER = error("003");

    public static final String TOO_MANY_CODES = (
            "%s%s%sWoah there!\n" +
            "%sA lot of people have joined recently! Please\n" +
            "%scome back in a bit when the queue has caught up."
    ).formatted(
            ChatColor.GOLD, ChatColor.UNDERLINE, ChatColor.ITALIC,
            ChatColor.YELLOW,
            ChatColor.YELLOW
    );

    public static final String LINKING_TEMPORARILY_DISABLED = (
            "%s%s%sWoah there!\n" +
            "%sNew players are not allowed to join the server for\n" +
            "%sthe time being. Please try again later."
    ).formatted(
            ChatColor.GOLD, ChatColor.UNDERLINE, ChatColor.ITALIC,
            ChatColor.YELLOW,
            ChatColor.YELLOW
    );

    public static final String LINK_WHITELISTING_DISABLED = (
           """
           %s%s%sUnable to authorise you!
           
           %sAccount linking is currently disabled, so the server is unable to check
           %sif you are authorised to join or not.
           
           %s%sWhitelisted users are still permitted to join.
           """
            ).formatted(
                    ChatColor.RED, ChatColor.UNDERLINE, ChatColor.ITALIC,
                    ChatColor.GRAY,
                    ChatColor.GRAY,
                    ChatColor.AQUA, ChatColor.UNDERLINE
            );

    public static final String LINK_CODE_GENERATION_DISABLED = (
            """
            %s%s%sWho goes there!
            
            %sYou do not have an account currently linked & linking new accounts
            %sis currently disabled.
            
            %s%sPlease try again another time.
            """
    ).formatted(
            ChatColor.RED, ChatColor.UNDERLINE, ChatColor.ITALIC,
            ChatColor.GRAY,
            ChatColor.GRAY,
            ChatColor.AQUA, ChatColor.UNDERLINE
    );

    @EventHandler
    public void handleCustomWhitelist(AsyncPlayerPreLoginEvent event) {
        UUID accountId = event.getUniqueId();
        Dislink.plugin().getLogger().info("Checking if %s has a linked account.".formatted(accountId));

        // If players is on the whitelist (even if it's turned off),
        // skip all verification on account linking.
        if(this.hasWhitelistBypass(accountId)) {
            Dislink.plugin().getLogger().info("%s bypassed link check (Whitelisted)".formatted(accountId));
            LinkedAccount guestAcc = new LinkedAccount(null, accountId, "whitelist", true);
            Dislink.plugin().getLinkedAccountCache().offerAccount(guestAcc);
            return;
        }

        if(!Dislink.usingFeature(Feature.LINK_WHITELIST)) {
            Dislink.plugin().getLogger().info("%s was rejected entry (Link whitelist disabled)".formatted(accountId));
            event.disallow(Result.KICK_OTHER, LINK_WHITELISTING_DISABLED);
            return;
        }

        Optional<LinkedAccount> optLinkAccount;

        try {
            optLinkAccount = DBLinks.getLinkFor(accountId).get();

        } catch (Exception err) {
            Dislink.plugin().getLogger()
                    .throwing("PlayerLifecycleListener", "handleCustomWhitelist", err);
            event.disallow(Result.KICK_OTHER, FAILED_ACCOUNT_GET);
            return;
        }

        // Successfully logged in - just let 'em through and cache it.
        if(optLinkAccount.isPresent()) {
            LinkedAccount existingLink = optLinkAccount.get();
            Dislink.plugin().getLogger().info("%s is linked to <@%s>!".formatted(accountId, existingLink.discordId()));
            Dislink.plugin().getLinkedAccountCache().offerAccount(existingLink);
            return;
        }

        // New pairings are disabled (broken config probs)
        if(!Dislink.usingFeature(Feature.PAIR_CODE_GENERATION)) {
            Dislink.plugin().getLogger().info("%s was rejected entry (New code generation disabled)".formatted(accountId));
            event.disallow(Result.KICK_OTHER, LINK_CODE_GENERATION_DISABLED);
            return;
        }

        // Failed to log in (not linked) - create new link.
        Dislink.plugin().getLogger().info("%s is attempting to generate a pairing code...".formatted(accountId));
        String pairCode;

        if(!Cfg.ALLOW_NEW_LINKS.dislink().orElse(true)) {
            event.disallow(Result.KICK_OTHER, LINKING_TEMPORARILY_DISABLED);
            return;
        }

        try {
            pairCode = DBPendingLinks.startLinkingFor(accountId).get();

        } catch (CompletionException err) {
            Run.sync(() ->
                Dislink.plugin()
                        .getLogger()
                        .severe(err.getMessage())
            );

            String kickReason = err.getCause() instanceof ExhaustedOptionsException
                    ? TOO_MANY_CODES
                    : FAILED_CODE_GENERATION_INTERNAL;
            event.disallow(Result.KICK_OTHER, kickReason);
            return;

        } catch (Exception err) {
            Run.sync(() ->
                    Dislink.plugin()
                            .getLogger()
                            .severe(err.getMessage())
            );
            event.disallow(Result.KICK_OTHER, FAILED_CODE_GENERATION_OTHER);
            return;
        }

        String codeMessage = codeMessage(pairCode);
        event.disallow(Result.KICK_WHITELIST, codeMessage);
    }

    private boolean hasWhitelistBypass(UUID uuid) {
        Server server = Dislink.plugin().getServer();
        OfflinePlayer player = server.getOfflinePlayer(uuid);

        return server.getWhitelistedPlayers().contains(player);
    }


    private static String error(String code) {
        return """
               %s%s%s/!\\Internal Server Error/!\\
               
               %sSomething unexpected went wrong. %sContact an admin please (E#%s)
               """
        .formatted(
                ChatColor.DARK_RED, ChatColor.UNDERLINE, ChatColor.ITALIC,
                ChatColor.RED, ChatColor.UNDERLINE, code
        );
    }

    private static String codeMessage(String code) {
        Optional<String> optServer = Cfg.LINK_LOCATION.dislink();
        String server = optServer.isPresent()
                ? "%s%s%s%s%s".formatted(ChatColor.AQUA, ChatColor.UNDERLINE, optServer.get(), ChatColor.RESET, ChatColor.DARK_AQUA)
                : "the discord server";

        Optional<String> optDiscordHint = Cfg.LINK_COMPLETION_COMMAND_HINT.dislink();
        String discordHint = optDiscordHint.isPresent()
                ? "%s%s%s%s".formatted(ChatColor.RESET, ChatColor.GRAY, ChatColor.ITALIC, optDiscordHint.get())
                : "";

        return """
               %s%s%sWhere are you from?
               
               %sThis server requires you to link your Minecraft account with Discord!
               %sPlease go to %s and run %s%s/link%s%s with the code:

               %s%s%s
               
               %s
               """
        .formatted(
                ChatColor.AQUA, ChatColor.UNDERLINE, ChatColor.ITALIC,
                ChatColor.DARK_AQUA,
                ChatColor.DARK_AQUA, server, ChatColor.AQUA, ChatColor.UNDERLINE, ChatColor.RESET, ChatColor.DARK_AQUA,
                ChatColor.LIGHT_PURPLE, ChatColor.UNDERLINE, code,
                discordHint
        );
    }

}
