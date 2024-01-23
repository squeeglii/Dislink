package me.squeeglii.plugin.dislink.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

public class DoubleGetter extends ConfigGetter<Double> {

    public DoubleGetter(String key) {
        super(key);
    }

    @Override
    public Optional<Double> from(ConfigurationSection config) {
        if(!config.contains(this.name()))
            return Optional.empty();

        double val = config.getDouble(this.name());

        return Optional.of(val);
    }
}
