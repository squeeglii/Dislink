package me.squeeglii.plugin.dislink.data;

import me.squeeglii.plugin.dislink.Dislink;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Optional;

// This can be used in other projects via from(...) but dislink()
// is there for this plugin's convenience.
public abstract class ConfigGetter<T> extends Key<T> {
    public ConfigGetter(String key) {
        super(key);
    }

    public abstract Optional<T> from(FileConfiguration config);

    public final Optional<T> dislink() {
        return this.from(Dislink.get().getConfig());
    }

}
