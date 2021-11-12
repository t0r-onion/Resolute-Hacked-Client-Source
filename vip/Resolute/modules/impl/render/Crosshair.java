package vip.Resolute.modules.impl.render;

import net.minecraft.client.entity.EntityPlayerSP;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventCrosshair;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventRender2D;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ColorSetting;
import vip.Resolute.settings.impl.NumberSetting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import vip.Resolute.util.movement.MovementUtils;
import vip.Resolute.util.render.RenderUtils;

import java.awt.*;

public class Crosshair extends Module {

    public NumberSetting gapProp = new NumberSetting("Gap", 1.0, 0.0, 10.0, 0.5);
    public NumberSetting lengthProp = new NumberSetting("Length", 3.0, 0.0, 10.0, 0.5);
    public NumberSetting widthProp = new NumberSetting("Width", 1.0, 0.0, 5.0, 0.5);
    public BooleanSetting tShapeProp = new BooleanSetting("T Shape", false);
    public BooleanSetting dotProp = new BooleanSetting("Dot", false);
    public BooleanSetting dynamicProp = new BooleanSetting("Dynamic", true);
    public BooleanSetting outlineProp = new BooleanSetting("Outline", true);
    public NumberSetting outlineWidthProp = new NumberSetting("Outline Width", 0.5, outlineProp::isEnabled, 0.5, 5.0, 0.5);
    public ColorSetting colorProp = new ColorSetting("Color", new Color(1668818));

    private static double lastDist;
    private static double prevLastDist;
    private static double baseMoveSpeed;

    public Crosshair() {
        super("Crosshair", 0, "Renders a custom crosshair", Category.RENDER);
        this.addSettings(gapProp, lengthProp, widthProp, tShapeProp, dynamicProp, outlineProp, outlineWidthProp, colorProp);
    }

    public void onEvent(Event e) {
        final double width;
        final double halfWidth;
        double gap;
        final double length;
        final int color;
        final double outlineWidth;
        final boolean outline;
        final boolean tShape;
        final ScaledResolution lr;
        final double middleX;
        final double middleY;

        if(e instanceof EventCrosshair) {
            e.setCancelled(true);
        }

        if(e instanceof EventMotion) {
            EventMotion event = (EventMotion) e;

            if (e.isPre()) {
                baseMoveSpeed = MovementUtils.getBaseMoveSpeed();
                final EntityPlayerSP player = mc.thePlayer;
                final double xDif = player.posX - player.lastTickPosX;
                final double zDif = player.posZ - player.lastTickPosZ;
                prevLastDist = lastDist;
                lastDist = StrictMath.sqrt(xDif * xDif + zDif * zDif);
            }
        }

        if(e instanceof EventRender2D) {
            EventRender2D event = (EventRender2D) e;

            width = this.widthProp.getValue();
            halfWidth = width / 2.0;
            gap = this.gapProp.getValue();
            if (this.dynamicProp.isEnabled()) {
                gap *= Math.max(mc.thePlayer.isSneaking() ? 0.5 : 1.0, RenderUtils.interpolate(getPrevLastDist(), getLastDist(), event.getPartialTicks()) * 10.0);
            }
            length = this.lengthProp.getValue();
            color = this.colorProp.getColor();
            outlineWidth = this.outlineWidthProp.getValue();
            outline = this.outlineProp.isEnabled();
            tShape = this.tShapeProp.isEnabled();
            lr = new ScaledResolution(mc);
            middleX = lr.getScaledWidth() / 2.0;
            middleY = lr.getScaledHeight() / 2.0;
            if (outline) {
                Gui.drawRect(middleX - gap - length - outlineWidth, middleY - halfWidth - outlineWidth, middleX - gap + outlineWidth, middleY + halfWidth + outlineWidth, -1778384896);
                Gui.drawRect(middleX + gap - outlineWidth, middleY - halfWidth - outlineWidth, middleX + gap + length + outlineWidth, middleY + halfWidth + outlineWidth, -1778384896);
                Gui.drawRect(middleX - halfWidth - outlineWidth, middleY + gap - outlineWidth, middleX + halfWidth + outlineWidth, middleY + gap + length + outlineWidth, -1778384896);
                if (!tShape) {
                    Gui.drawRect(middleX - halfWidth - outlineWidth, middleY - gap - length - outlineWidth, middleX + halfWidth + outlineWidth, middleY - gap + outlineWidth, -1778384896);
                }
            }
            Gui.drawRect(middleX - gap - length, middleY - halfWidth, middleX - gap, middleY + halfWidth, color);
            Gui.drawRect(middleX + gap, middleY - halfWidth, middleX + gap + length, middleY + halfWidth, color);
            Gui.drawRect(middleX - halfWidth, middleY + gap, middleX + halfWidth, middleY + gap + length, color);
            if (!tShape) {
                Gui.drawRect(middleX - halfWidth, middleY - gap - length, middleX + halfWidth, middleY - gap, color);
            }
        }
    }

    public static double getPrevLastDist() {
        return prevLastDist;
    }

    public static double getLastDist() {
        return lastDist;
    }

    public static double getBaseMoveSpeed() {
        return baseMoveSpeed;
    }
}
