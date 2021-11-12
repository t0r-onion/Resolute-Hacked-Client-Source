package vip.Resolute.util.world;

import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public class ServerUtils {
    private static final Map<String, Long> serverIpPingCache = new HashMap<>();
    public static Minecraft mc = Minecraft.getMinecraft();
    private static final String HYPIXEL = "hypixel.net";


    private ServerUtils() {
    }

    public static void update(String ip, long ping) {
        serverIpPingCache.put(ip, ping);
    }

    public static long getPingToServer(String ip) {
        return serverIpPingCache.get(ip);
    }

    public static boolean isOnServer(String ip) {
        if (mc.isSingleplayer())
            return false;

        return getCurrentServerIP().endsWith(ip);
    }

    public static String getCurrentServerIP() {
        if (mc.isSingleplayer())
            return "Singleplayer";

        return mc.getCurrentServerData().serverIP;
    }

    public static boolean isOnHypixel() {
        return isOnServer(HYPIXEL);
    }

    public static long getPingToCurrentServer() {
        if (mc.isSingleplayer())
            return 0;

        return getPingToServer(getCurrentServerIP());
    }
}
