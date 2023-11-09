package me.squeeglii.plugin.dislink.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Optional;

public class SectionGetter extends ConfigGetter<ConfigurationSection> {

    public SectionGetter(String key) {
        super(key);
    }

    @Override
    public Optional<ConfigurationSection> from(ConfigurationSection config) {
        if(!config.contains(this.get()))
            return Optional.empty();

        ConfigurationSection val = config.getConfigurationSection(this.get());

        if(val == null)
            return Optional.empty();

        return Optional.of(val);
    }
}
