package vip.Resolute.util.font;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FontUtil {
    public static volatile int completed;
    public static MinecraftFontRenderer clientfont, clientmedium, clientsmall, summer, sf, oxide, robo, robo22, roboSmall, icons, icons2, icons3, verdana10, neverlose, tahoma, tahomaSmall, tahomaVerySmall, c16, c22, moon, rain, iconfont, light;
    private static Font clientfont_, clientmedium_, clientsmall_, summer_, sf_, oxide_, robo_, robo22_, roboSmall_, icons_, icons2_, icons3_, verdana10_, neverlose_, tahoma_, tahomaSmall_, tahomaVerySmall_, c16_, c22_, moon_, iconfont_;

    private static Font getFont(Map<String, Font> locationMap, String location, int size) {
        Font font = null;

        try {
            if (locationMap.containsKey(location)) {
                font = locationMap.get(location).deriveFont(Font.PLAIN, size);
            } else {
                InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(location)).getInputStream();
                font = Font.createFont(0, is);
                locationMap.put(location, font);
                font = font.deriveFont(Font.PLAIN, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, +10);
        }

        return font;
    }

    public static boolean hasLoaded() {
        return completed >= 3;
    }

    public static void bootstrap() {
        new Thread(() ->
        {
            Map<String, Font> locationMap = new HashMap<>();
            clientfont_ = getFont(locationMap, "resolute/font.ttf", 21);
            clientmedium_ = getFont(locationMap, "resolute/font.ttf", 20);
            clientsmall_ = getFont(locationMap, "resolute/font.ttf", 16);
            summer_ = getFont(locationMap, "resolute/SF.ttf", 23);
            sf_ = getFont(locationMap, "resolute/SF.ttf", 20);
            oxide_ = getFont(locationMap, "resolute/oxide.ttf", 42);
            robo_ = getFont(locationMap, "resolute/Roboto-Regular.ttf", 20);
            robo22_ = getFont(locationMap, "resolute/Roboto-Regular.ttf", 21);
            roboSmall_ = getFont(locationMap, "resolute/Roboto-Regular.ttf", 18);
            icons_ = getFont(locationMap, "resolute/icons.ttf", 40);
            icons2_ =getFont(locationMap, "resolute/Icon-Font.ttf", 40);
            icons3_ = getFont(locationMap, "resolute/icons2.ttf", 40);
            verdana10_ = getFont(locationMap, "resolute/Verdana.ttf", 15);
            neverlose_ = getFont(locationMap, "resolute/museosans-900.ttf", 20);
            tahoma_ = getFont(locationMap, "resolute/tahoma.ttf", 20);
            tahomaSmall_ = getFont(locationMap, "resolute/tahoma.ttf", 15);
            tahomaVerySmall_ = getFont(locationMap, "resolute/tahoma.ttf", 10);
            c16_ = getFont(locationMap, "resolute/ali.ttf", 16);
            c22_ = getFont(locationMap, "resolute/ali.ttf", 22);
            moon_ = getFont(locationMap, "resolute/Moon.ttf", 20);
            iconfont_ = getFont(locationMap, "resolute/Icon-Font.ttf", 40);
            completed++;
        }).start();
        new Thread(() ->
        {
            Map<String, Font> locationMap = new HashMap<>();
            completed++;
        }).start();
        new Thread(() ->
        {
            Map<String, Font> locationMap = new HashMap<>();
            completed++;
        }).start();

        while (!hasLoaded()) {
            try {
                //noinspection BusyWait
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        clientfont = new MinecraftFontRenderer(clientfont_, true, true);
        clientmedium = new MinecraftFontRenderer(clientmedium_, true, true);
        clientsmall = new MinecraftFontRenderer(clientsmall_, true, true);
        summer = new MinecraftFontRenderer(summer_, true, true);
        sf = new MinecraftFontRenderer(sf_, true, true);
        oxide = new MinecraftFontRenderer(oxide_, true, true);
        robo = new MinecraftFontRenderer(robo_, true, true);
        robo22 = new MinecraftFontRenderer(robo22_, true, true);
        roboSmall = new MinecraftFontRenderer(roboSmall_, true, true);
        icons = new MinecraftFontRenderer(icons_, true, true);
        icons2 = new MinecraftFontRenderer(icons2_, true, true);
        icons3 = new MinecraftFontRenderer(icons3_, true, true);
        verdana10 = new MinecraftFontRenderer(verdana10_, true, true);
        neverlose = new MinecraftFontRenderer(neverlose_, true, true);
        tahoma = new MinecraftFontRenderer(tahoma_, true, true);
        tahomaSmall = new MinecraftFontRenderer(tahomaSmall_, true, true);
        tahomaVerySmall = new MinecraftFontRenderer(tahomaVerySmall_, true, true);
        c16 = new MinecraftFontRenderer(c16_, true, true);
        c22 = new MinecraftFontRenderer(c22_, true, true);
        moon = new MinecraftFontRenderer(moon_, true, true);
        iconfont = new MinecraftFontRenderer(iconfont_, true, true);
    }
}
