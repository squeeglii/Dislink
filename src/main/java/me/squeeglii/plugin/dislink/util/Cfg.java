package me.squeeglii.plugin.dislink.util;

import me.squeeglii.plugin.dislink.data.BoolGetter;
import me.squeeglii.plugin.dislink.data.IntGetter;
import me.squeeglii.plugin.dislink.data.StringGetter;
import me.squeeglii.plugin.dislink.data.StringListGetter;

public class Cfg {

    // Database Details
    public static final StringGetter DB_ADDRESS = new StringGetter("database-address");
    public static final StringGetter DB_SCHEMA = new StringGetter("database-schema");
    public static final StringGetter DB_USERNAME = new StringGetter("database-username");
    public static final StringGetter DB_PASSWORD = new StringGetter("database-password");

    // General Pairing
    public static final IntGetter MAX_ACCOUNT_LIMIT = new IntGetter("paired-account-limit");
    public static final BoolGetter PRUNE_PENDING_LINKS_ON_START = new BoolGetter("prune-pending-links-on-restart");

    // Pairing Code Generation
    public static final IntGetter PAIRING_BLOCKS = new IntGetter("pairing-code-blocks");
    public static final StringListGetter PAIRING_WORDS = new StringListGetter("pairing-word-list");


}
