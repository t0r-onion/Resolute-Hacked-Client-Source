package vip.Resolute.util.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class StringUtils {

    private StringUtils() {}

    public static String upperSnakeCaseToPascal(String s) {
        return s.charAt(0) + s.substring(1).toLowerCase();
    }

    public static boolean isTeamMate(EntityLivingBase entity) {
        String entName = entity.getDisplayName().getFormattedText();
        String playerName = Minecraft.getMinecraft().thePlayer.getDisplayName().getFormattedText();
        if (entName.length() < 2 || playerName.length() < 2) return false;
        if (!entName.startsWith("\247") || !playerName.startsWith("\247")) return false;
        return entName.charAt(1) == playerName.charAt(1);
    }

    public static Object castNumber(final String newValueText, final Object currentValue) {
        if (newValueText.contains(".")) {
            if (newValueText.toLowerCase().contains("f")) {
                return Float.parseFloat(newValueText);
            }
            return Double.parseDouble(newValueText);
        }
        else {
            if (isNumeric(newValueText)) {
                return Integer.parseInt(newValueText);
            }
            return newValueText;
        }
    }

    public static boolean isNumeric(final String text) {
        try {
            Integer.parseInt(text);
            return true;
        }
        catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static String getTrimmedClipboardContents() {
        String data = null;
        try {
            data = (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException ignored) {
        }

        if (data != null) {
            data = data.trim();

            if (data.indexOf('\n') != -1)
                data = data.replace("\n", "");
        }

        return data;
    }

}
