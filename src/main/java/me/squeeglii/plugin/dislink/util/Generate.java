package me.squeeglii.plugin.dislink.util;

import me.squeeglii.plugin.dislink.Dislink;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Generate {

    private static final Random randomSource = new SecureRandom();

    /**
     * Uses config defined values to generate a new linking code.
     * See config entries 'pairing-code-blocks' & 'pairing-word-list'
     */
    public static String newLinkCode() {
        if(Dislink.get() == null)
            throw new IllegalStateException("Tried to generate a new link code without Dislink being loaded.");

        Optional<List<String>> optPool = Cfg.PAIRING_WORDS.dislink();

        if(optPool.isEmpty())
            throw new IllegalStateException("Misconfigured Pairing !! - You have no word list set in your config!");

        List<String> pool = optPool.get();

        if(pool.isEmpty())
            throw new IllegalStateException("Misconfigured Pairing !! - You have no words in your word list!");

        int blocks = Cfg.PAIRING_BLOCKS.dislink().orElse(3);
        String[] strings = new String[blocks];

        for(int i = 0; i < strings.length; i++) {
            int nextIndex = randomSource.nextInt(pool.size());
            strings[i] = pool.get(nextIndex);
        }

        return String.join("-", strings);
    }

}
