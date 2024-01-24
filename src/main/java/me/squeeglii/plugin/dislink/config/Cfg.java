package me.squeeglii.plugin.dislink.config;

import me.squeeglii.plugin.dislink.data.*;

public class Cfg {

    // Database Details
    public static final StringGetter DB_PREFIX = new StringGetter("database-prefix");
    public static final StringGetter DB_ADDRESS = new StringGetter("database-address");
    public static final StringGetter DB_SCHEMA = new StringGetter("database-schema");
    public static final StringGetter DB_USERNAME = new StringGetter("database-username");
    public static final StringGetter DB_PASSWORD = new StringGetter("database-password");

    // Discord Bot Config
    public static final StringGetter DISCORD_TOKEN = new StringGetter("bot-token");
    public static final SectionGetter DISCORD_SERVER_CONFIGS = new SectionGetter("server-configs");
    public static final BoolGetter OBTAIN_GDPR_CONSENT = new BoolGetter("show-gdpr-consent"); // Move this out of discord if other integrations are added.

    //// Sub-section of SERVER_CONFIGS
    public static final StringGetter DISCORD_SHORT_NAME = new StringGetter("short-name");
    public static final LongGetter DISCORD_MEMBER_ROLE_ID = new LongGetter("member-role-id");
    public static final StringGetter DISCORD_MISSING_PERMS_MESSAGE = new StringGetter("missing-member-role-message");

    // In-Game Config
    public static final SectionGetter VERIFIER_SHORT_NAME_PREFIXES = new SectionGetter("short-name-prefixes");

    // General Pairing - Affects in-game behaviour before player has logged in (the link screen + whitelist)
    public static final IntGetter MAX_ACCOUNT_LIMIT = new IntGetter("paired-account-limit");
    public static final BoolGetter PRUNE_PENDING_LINKS_ON_START = new BoolGetter("prune-pending-links-on-restart");
    public static final StringGetter LINK_LOCATION = new StringGetter("link-location"); // used in kick message to hint where to complete the link.
    public static final StringGetter LINK_COMPLETION_COMMAND_HINT = new StringGetter("link-hint");


    // Pairing Code Generation
    public static final IntGetter PAIRING_BLOCKS = new IntGetter("pairing-code-blocks");
    public static final IntGetter PAIRING_GENERATION_ATTEMPTS = new IntGetter("paring-code-generation-attempts");
    public static final StringListGetter PAIRING_WORDS = new StringListGetter("pairing-word-list");

}
