package me.squeeglii.plugin.dislink.util;

import me.squeeglii.plugin.dislink.data.IntGetter;
import me.squeeglii.plugin.dislink.data.StringListGetter;

public class Cfg {

    public static final IntGetter MAX_ACCOUNT_LIMIT = new IntGetter("paired-account-limit");

    public static final IntGetter PAIRING_BLOCKS = new IntGetter("pairing-code-blocks");
    public static final StringListGetter PAIRING_WORDS = new StringListGetter("pairing-word-list");


}
