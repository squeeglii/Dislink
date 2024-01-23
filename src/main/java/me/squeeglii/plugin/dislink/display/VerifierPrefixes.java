package me.squeeglii.plugin.dislink.display;

import me.squeeglii.plugin.dislink.Dislink;
import me.squeeglii.plugin.dislink.config.Cfg;
import me.squeeglii.plugin.dislink.config.Feature;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

public class VerifierPrefixes {
    
    private final HashMap<String, String> prefixes;
    
    public VerifierPrefixes() {
        this.prefixes = new HashMap<>();
    }
    
    
    public Optional<String> getPrefixForVerifier(String linkVerifier) {
        String prefixStr = this.prefixes.get(linkVerifier.toLowerCase());
        return Optional.ofNullable(prefixStr);
    }

    public void setPrefixForVerifier(String linkVerifier, String prefix) {
        if(prefix == null) {
            this.resetPrefixForVerifier(linkVerifier);
            return;
        }

        this.prefixes.put(linkVerifier.toLowerCase(), prefix);
    }

    public void resetPrefixForVerifier(String linkVerifier) {
        this.prefixes.remove(linkVerifier.toLowerCase());
    }

    public void loadFromConfig() {
        if(!Dislink.usingFeature(Feature.GAME_INTEGRATION)) {
            Dislink.plugin().getLogger().warning("In-Game prefix support is disabled as the config is invalid.");
            return;
        }

        Optional<ConfigurationSection> optSection = Cfg.VERIFIER_SHORT_NAME_PREFIXES.dislink();

        if(optSection.isEmpty())
            return;

        ConfigurationSection section = optSection.get();
        Set<String> verifiers = section.getKeys(false);

        for(String verifier: verifiers) {
            if(!section.isString(verifier)) {
                Dislink.plugin()
                        .getLogger()
                        .warning("Verifier Prefix for '%s' is not a string within the config.".formatted(verifier));
                continue;
            }

            String prefix = section.getString(verifier);

            this.setPrefixForVerifier(verifier.toLowerCase(), prefix);
        }
    }

}
