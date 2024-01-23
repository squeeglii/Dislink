package me.squeeglii.plugin.dislink.config;

/**
 * Used to indicate what systems should be enabled based
 * on a config check.
 */
public enum Feature {

    DISCORD_BOT,
    PAIR_CODE_GENERATION,
    REMOTE_DATABASE, // todo: add support for an SQLite file if a valid remote database cannot be found.
    GAME_INTEGRATION,
    CORE

}
