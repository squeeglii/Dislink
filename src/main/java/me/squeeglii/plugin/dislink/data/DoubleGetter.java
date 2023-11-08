package me.squeeglii.plugin.dislink.data;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Optional;

public class DoubleGetter extends ConfigGetter<Double> {

    public DoubleGetter(String key) {
        super(key);
    }

    @Override
    public Optional<Double> from(FileConfiguration config) {
        if(config.contains(this.get()))
            return Optional.empty();

        double val = config.getDouble(this.get());

        return Optional.of(val);
    }
}
