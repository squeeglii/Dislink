package me.squeeglii.plugin.dislink.data;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Optional;

public class StringGetter extends ConfigGetter<String> {

    public StringGetter(String key) {
        super(key);
    }

    @Override
    public Optional<String> from(FileConfiguration config) {
        if(!config.contains(this.get()))
            return Optional.empty();

        String val = config.getString(this.get());

        if(val == null || val.isEmpty())
            return Optional.empty();

        return Optional.of(val);
    }
}
