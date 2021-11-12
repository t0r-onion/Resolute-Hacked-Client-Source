package vip.Resolute.modules.impl.render;

import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ColorSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.ui.click.drop.ClickGui;
import vip.Resolute.ui.click.skeet.SkeetUI;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class ClickGUI extends Module {

    public static boolean isClosing;
    private final Minecraft mc = Minecraft.getMinecraft();

    public static ModeSetting mode = new ModeSetting("Mode", "Radium", "Radium", "Dropdown");

    public static BooleanSetting rainbow = new BooleanSetting("Rainbow", false, () -> mode.is("Radium"));
    public static ColorSetting color = new ColorSetting("Color", new Color(112, 0 ,207), () -> mode.is("Dropdown"));

    public ClickGUI() {
        super("ClickGUI", Keyboard.KEY_RSHIFT, "Module GUI", Category.RENDER);

        this.addSettings(mode, rainbow, color);
    }

    public void onEnable() {
        isClosing = false;
        if(mode.is("Radium"))
            SkeetUI.init();
        if(mode.is("Dropdown"))
            mc.displayGuiScreen(new ClickGui());
        toggled = false;
    }
}
