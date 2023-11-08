package me.squeeglii.plugin.dislink.data;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Optional;

public class BoolGetter extends ConfigGetter<Boolean> {

    public BoolGetter(String key) {
        super(key);
    }

    @Override
    public Optional<Boolean> from(FileConfiguration config) {
        if(!config.contains(this.get()))
            return Optional.empty();

        boolean val = config.getBoolean(this.get());

        return Optional.of(val);
    }
}
