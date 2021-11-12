package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ColorSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.render.Colors;
import vip.Resolute.util.render.RenderUtils;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Chams extends Module {
    public static ModeSetting mode = new ModeSetting("Mode", "Blend", "Blend");

    public static BooleanSetting hurtEffect = new BooleanSetting("Hurt Effect", true);

    public static ColorSetting hurtColor = new ColorSetting("Hurt Color", new Color(255, 0, 0), hurtEffect::isEnabled);
    public static NumberSetting hurtAlpha = new NumberSetting("Hurt Alpha", 1.0, hurtEffect::isEnabled, 0.0, 1.0, 0.1);

    public static BooleanSetting handsProp = new BooleanSetting("Hands", true);
    public static ColorSetting handsColor = new ColorSetting("Hands Color", new Color(16007990), handsProp::isEnabled);
    public static NumberSetting handsAlpha = new NumberSetting("Hands Alpha", 0.3D, handsProp::isEnabled, 0.1D, 1.0D, 0.1D);

    public static BooleanSetting visibleFlat = new BooleanSetting("Visible Flat", true);
    public static BooleanSetting occludedFlat = new BooleanSetting("Occluded Flat", true);

    public static ModeSetting visibleColorMode = new ModeSetting("Visible Color", "Static", "Static", "Rainbow", "Pulsing");
    public static ModeSetting occludedColorMode = new ModeSetting("Occluded Color", "Static", "Static", "Rainbow", "Pulsing");

    public static NumberSetting visibleAlpha = new NumberSetting("Visible Alpha", 1.0, 0.0, 1.0, 0.1);
    public static NumberSetting occludedAlpha = new NumberSetting("Obstructed Alpha", 0.4, 0.0, 1.0, 0.1);

    public static ColorSetting visibleColor = new ColorSetting("Visible", new Color(0, 255, 133));
    public static ColorSetting obstructedColor = new ColorSetting("Obstructed", new Color(255, 191, 226));

    public static ColorSetting secondVisibleColor = new ColorSetting("Second Visible", new Color(16007990), () -> visibleColorMode.is("Pulsing"));
    public static ColorSetting secondObstructedColor = new ColorSetting("Second Obstructed", new Color(65350), () -> visibleColorMode.is("Pulsing"));

    public static boolean enabled = false;

    public Chams() {
        super("Chams", 0, "Renders entities behind walls", Category.RENDER);
        this.addSettings(mode, hurtEffect, hurtColor, hurtAlpha, handsProp, handsColor, handsAlpha, visibleFlat, occludedFlat, visibleColorMode, occludedColorMode, visibleAlpha, occludedAlpha, visibleColor, obstructedColor, secondVisibleColor, secondObstructedColor);
    }

    public void onEvent(Event e) {
        this.setSuffix("Blend");
    }

    public void onEnable() {
        enabled = true;
    }

    public void onDisable() {
        enabled = false;
    }

    public static void preRenderOccluded(final int occludedColor, final boolean occludedFlat) {
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        if (occludedFlat) {
            GL11.glDisable(2896);
        }
        GL11.glEnable(32823);
        GL11.glPolygonOffset(0.0f, -1000000.0f);
        OpenGlHelper.setLightmapTextureCoords(1, 240.0f, 240.0f);
        GL11.glDepthMask(false);
        RenderUtils.color(occludedColor);
    }

    public static void preRenderVisible(final int visibleColor, final boolean visibleFlat, final boolean occludedFlat) {
        GL11.glDepthMask(true);
        if (occludedFlat && !visibleFlat) {
            GL11.glEnable(2896);
        }
        else if (!occludedFlat && visibleFlat) {
            GL11.glDisable(2896);
        }
        RenderUtils.color(visibleColor);
        GL11.glDisable(32823);
    }

    public static void postRender(final boolean visibleFlat) {
        if (visibleFlat) {
            GL11.glEnable(2896);
        }
        GL11.glEnable(3553);
        GL11.glDisable(3042);
    }

    public static boolean isValid(final EntityLivingBase entity) {
        return !entity.isInvisible() && entity.isEntityAlive() && entity instanceof EntityPlayer;
    }
}
