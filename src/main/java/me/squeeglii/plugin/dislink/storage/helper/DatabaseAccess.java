package me.squeeglii.plugin.dislink.storage.helper;

import me.squeeglii.plugin.dislink.Dislink;
import me.squeeglii.plugin.dislink.config.Cfg;

import java.util.Optional;

public record DatabaseAccess(String address, String schema, String username, String password) {

    public static DatabaseAccess fromConfig() {
        if (Dislink.plugin() == null)
            throw new IllegalStateException("Tried to load database config without an initialised plugin.");

        Optional<String> addr = Cfg.DB_ADDRESS.dislink();
        Optional<String> schema = Cfg.DB_SCHEMA.dislink();
        Optional<String> username = Cfg.DB_USERNAME.dislink();
        Optional<String> password = Cfg.DB_PASSWORD.dislink();

        boolean anyInvalidDetails = addr.isEmpty() ||
                                    schema.isEmpty() ||
                                    username.isEmpty() ||
                                    password.isEmpty();

        if (anyInvalidDetails)
            throw new IllegalStateException("Database configuration is incomplete. Please check all fields are filled in the config.");

        return new DatabaseAccess(addr.get(), schema.get(), username.get(), password.get());
    }

}
