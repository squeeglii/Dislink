package me.squeeglii.plugin.dislink.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

public class IntGetter extends ConfigGetter<Integer> {

    public IntGetter(String key) {
        super(key);
    }

    @Override
    public Optional<Integer> from(ConfigurationSection config) {
        if(!config.contains(this.name()))
            return Optional.empty();

        int val = config.getInt(this.name());

        return Optional.of(val);
    }
}
