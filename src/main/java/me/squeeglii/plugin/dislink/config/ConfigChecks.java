package me.squeeglii.plugin.dislink.config;

public class ConfigChecks {


    /**
     * Requires a Dislink plugin instance to be assigned - it checks all
     * config values to ensure that the plugin can run properly.
     * Some test cases result in warnings but return as true, as long as the plugin
     * can operate, even if not optimally.
     * @return Returns true if all checks have passed.
     */
    public static boolean runAll() {
        return database() &&
               bot() &&
               inGame() &&
               pairing();
    }


    public static boolean database() {
        return true;
    }

    public static boolean bot() {
        return true;
    }

    public static boolean inGame() {
        return true;
    }

    public static boolean pairing() {
        return pairingCodes() && pairingGeneral();
    }

    public static boolean pairingCodes() {
        return true;
    }

    public static boolean pairingGeneral() {
        return true;
    }
}
