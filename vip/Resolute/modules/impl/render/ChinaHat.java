package vip.Resolute.modules.impl.render;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventRender3D;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ColorSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.render.ColorUtils;
import vip.Resolute.util.render.RenderUtils;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.lwjgl.util.glu.GLU;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

public class ChinaHat extends Module {
    public ColorSetting colorset = new ColorSetting("Color", new Color(2, 207, 167));
    public NumberSetting pointsProp = new NumberSetting("Points", 20, 3, 100, 1);
    public NumberSetting widthProp = new NumberSetting("Width", 5, 1, 10, 1);
    public NumberSetting repeatProp = new NumberSetting("Repeat", 200, 10, 500, 5);

    public ChinaHat() {
        super("ChinaHat", 0, "Epic hat", Category.RENDER);
        this.addSettings(colorset, pointsProp, widthProp, repeatProp);
    }

    public void onEvent(Event e) {
        if(e instanceof EventRender3D) {
            EventRender3D eventRender3D = (EventRender3D) e;

            if(mc.gameSettings.thirdPersonView != 0) {
                for(int i = 0; i < repeatProp.getValue(); i++) {
                    drawHat(mc.thePlayer, 0.001 + i * 0.004f, eventRender3D.getPartialTicks(), (int)pointsProp.getValue(), (float) widthProp.getValue(), (mc.thePlayer.isSneaking() ? 2f : 2.15f)  - i * 0.002f, colorset.getColor());
                }
            }
        }
    }

    public void drawHat(Entity entity, double radius, float partialTicks, int points, float width, float yAdd, int color) {
        glPushMatrix();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glDisable(GL_DEPTH_TEST);
        glLineWidth(width);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        glBegin(GL_LINE_STRIP);

        final double x = RenderUtils.interpolate(entity.prevPosX, entity.posX, partialTicks) - RenderManager.viewerPosX;
        final double y = RenderUtils.interpolate(entity.prevPosY + yAdd, entity.posY + yAdd, partialTicks) - RenderManager.viewerPosY;
        final double z = RenderUtils.interpolate(entity.prevPosZ, entity.posZ, partialTicks) - RenderManager.viewerPosZ;

        GL11.glColor4f(new Color(color).getRed() / 255f, new Color(color).getGreen() / 255f, new Color(color).getBlue() / 255f, 0.15f);

        for (int i = 0; i <= points; i++) {
            glVertex3d(x + radius * Math.cos(i * (Math.PI * 2) / points), y, z + radius * Math.sin(i * (Math.PI * 2) / points));
        }

        glEnd();
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }
}


