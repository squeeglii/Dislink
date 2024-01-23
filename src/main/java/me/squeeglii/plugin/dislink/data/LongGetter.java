package me.squeeglii.plugin.dislink.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

public class LongGetter extends ConfigGetter<Long> {

    public LongGetter(String key) {
        super(key);
    }

    @Override
    public Optional<Long> from(ConfigurationSection config) {
        if(!config.contains(this.name()))
            return Optional.empty();

        long val = config.getLong(this.name());

        return Optional.of(val);
    }
}
