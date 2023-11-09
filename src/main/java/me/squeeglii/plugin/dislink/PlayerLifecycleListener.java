package me.squeeglii.plugin.dislink;

import me.squeeglii.plugin.dislink.exception.ExhaustedOptionsException;
import me.squeeglii.plugin.dislink.storage.DBLinks;
import me.squeeglii.plugin.dislink.storage.DBPendingLinks;
import me.squeeglii.plugin.dislink.storage.LinkedAccount;
import me.squeeglii.plugin.dislink.util.Cfg;
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


    @EventHandler
    public void handleCustomWhitelist(AsyncPlayerPreLoginEvent event) {
        UUID accountId = event.getUniqueId();
        Dislink.get().getLogger().info("Checking if %s has a linked account.".formatted(accountId));

        // If players is on the whitelist (even if it's turned off),
        // skip all verification on account linking.
        if(this.hasWhitelistBypass(accountId)) {
            Dislink.get().getLogger().info("%s bypassed link check (Whitelisted)".formatted(accountId));
            LinkedAccount guestAcc = new LinkedAccount(null, accountId, "whitelist", true);
            Dislink.get().getLinkedAccountCache().offerAccount(guestAcc);
            return;
        }

        Optional<LinkedAccount> optLinkAccount;

        try {
            optLinkAccount = DBLinks.getLinkFor(accountId).get();

        } catch (Exception err) {
            Dislink.get().getLogger()
                    .throwing("PlayerLifecycleListener", "handleCustomWhitelist", err);
            event.disallow(Result.KICK_OTHER, FAILED_ACCOUNT_GET);
            return;
        }

        // Successfully logged in - just let 'em through and cache it.
        if(optLinkAccount.isPresent()) {
            LinkedAccount existingLink = optLinkAccount.get();
            Dislink.get().getLogger().info("%s is linked to <@%s>!".formatted(accountId, existingLink.discordId()));
            Dislink.get().getLinkedAccountCache().offerAccount(existingLink);
            return;
        }



        // Failed to log in (not linked) - create new link.
        Dislink.get().getLogger().info("%s is attempting to generate a pairing code...".formatted(accountId));
        String pairCode;

        try {
            pairCode = DBPendingLinks.startLinkingFor(accountId).get();

        } catch (CompletionException err) {
            Dislink.get()
                    .getLogger()
                    .throwing("PlayerLifecycleListener", "handleCustomWhitelist", err.getCause());

            String kickReason = err.getCause() instanceof ExhaustedOptionsException
                    ? TOO_MANY_CODES
                    : FAILED_CODE_GENERATION_INTERNAL;
            event.disallow(Result.KICK_OTHER, kickReason);
            return;

        } catch (Exception err) {
            Dislink.get()
                   .getLogger()
                   .throwing("PlayerLifecycleListener", "handleCustomWhitelist", err);
            event.disallow(Result.KICK_OTHER, FAILED_CODE_GENERATION_OTHER);
            return;
        }

        String codeMessage = codeMessage(pairCode);
        event.disallow(Result.KICK_WHITELIST, codeMessage);
    }

    private boolean hasWhitelistBypass(UUID uuid) {
        Server server = Dislink.get().getServer();

        if(!server.hasWhitelist())
            return false;

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
        String server = Cfg.LINK_SERVER.dislink().orElse("discord");

        return """
               %s%s%sWhere are you from?
               
               %sThis server requires you to link your Minecraft account with Discord!
               %sPlease go to the %s%s%s%s%s server and run %s%s/link%s%s with the code:

               %s%s%s
               """
        .formatted(
                ChatColor.AQUA, ChatColor.UNDERLINE, ChatColor.ITALIC,
                ChatColor.DARK_AQUA,
                ChatColor.DARK_AQUA, ChatColor.AQUA, ChatColor.UNDERLINE, server, ChatColor.RESET, ChatColor.DARK_AQUA,
                                     ChatColor.AQUA, ChatColor.UNDERLINE, ChatColor.RESET, ChatColor.DARK_AQUA,
                ChatColor.LIGHT_PURPLE, ChatColor.UNDERLINE, code
        );
    }

}
