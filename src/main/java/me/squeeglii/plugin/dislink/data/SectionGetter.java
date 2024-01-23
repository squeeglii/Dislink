package me.squeeglii.plugin.dislink.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

public class SectionGetter extends ConfigGetter<ConfigurationSection> {

    public SectionGetter(String key) {
        super(key);
    }

    @Override
    public Optional<ConfigurationSection> from(ConfigurationSection config) {
        if(!config.contains(this.name()))
            return Optional.empty();

        ConfigurationSection val = config.getConfigurationSection(this.name());

        if(val == null)
            return Optional.empty();

        return Optional.of(val);
    }
}
