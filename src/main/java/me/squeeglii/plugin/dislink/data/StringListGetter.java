package me.squeeglii.plugin.dislink.data;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Optional;

public class StringListGetter extends ConfigGetter<List<String>> {

    public StringListGetter(String key) {
        super(key);
    }

    @Override
    public Optional<List<String>> from(FileConfiguration config) {
        if(!config.contains(this.get()))
            return Optional.empty();

        List<String> val = config.getStringList(this.get());

        return Optional.of(val);
    }

}
