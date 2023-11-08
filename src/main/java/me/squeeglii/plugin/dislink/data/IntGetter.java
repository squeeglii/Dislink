package me.squeeglii.plugin.dislink.data;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Optional;

public class IntGetter extends ConfigGetter<Integer> {

    public IntGetter(String key) {
        super(key);
    }

    @Override
    public Optional<Integer> from(FileConfiguration config) {
        if(!config.contains(this.get()))
            return Optional.empty();

        int val = config.getInt(this.get());

        return Optional.of(val);
    }
}
