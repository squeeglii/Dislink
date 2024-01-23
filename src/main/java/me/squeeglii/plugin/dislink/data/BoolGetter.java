package me.squeeglii.plugin.dislink.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

public class BoolGetter extends ConfigGetter<Boolean> {

    public BoolGetter(String key) {
        super(key);
    }

    @Override
    public Optional<Boolean> from(ConfigurationSection config) {
        if(!config.contains(this.name()))
            return Optional.empty();

        boolean val = config.getBoolean(this.name());

        return Optional.of(val);
    }
}
