package vip.Resolute.util.misc;

import vip.Resolute.Resolute;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class Logger {
    public static String prefixText = Resolute.fullname;
    public static String prefixColor = "9";
    public static String textColor = "f";

    public static void ingameInfo(String msg) {
        if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null) {
            prefixColor = "9";
            textColor = "6";
            prefixText = Resolute.name + " - " + "\247" + textColor + "Info" + "\2479";
            StringBuilder tempMsg = new StringBuilder();

            for (String line : msg.split("\n")) {
                tempMsg.append(line).append("\2477");
            }

            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\247" + prefixColor + "[" + prefixText + "]\247" + textColor + " " + tempMsg.toString()));
        }
    }

    public static void ingameError(String msg) {
        if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null) {
            textColor = "c";
            prefixText = Resolute.name + " - " + "\247" + textColor + "Error" + "\2479";
            StringBuilder tempMsg = new StringBuilder();

            for (String line : msg.split("\n")) {
                tempMsg.append(line).append("\2477");
            }

            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\247" + prefixColor + "[" + prefixText + "]\247" + textColor + " " + tempMsg.toString()));
        }
    }

    public static void ingameWarn(String msg) {
        if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null) {
            textColor = "e";
            prefixText = Resolute.name + " - " + "\247" + textColor + "Warning" + "\2479";
            StringBuilder tempMsg = new StringBuilder();

            for (String line : msg.split("\n")) {
                tempMsg.append(line).append("\2477");
            }

            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\247" + prefixColor + "[" + prefixText + "]\247" + textColor + " " + tempMsg.toString()));
        }
    }

}
