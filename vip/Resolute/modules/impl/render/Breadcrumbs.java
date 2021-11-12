package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventRender3D;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ColorSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.render.RenderUtils;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Breadcrumbs extends Module {
    private final List<Vec3> breadcrumbs = new ArrayList<>();

    public NumberSetting size = new NumberSetting("Size", 1, 1, 4, 1);
    public NumberSetting length = new NumberSetting("Length", 200, 100, 1000, 10);
    public ColorSetting color = new ColorSetting("Color", new Color(255, 255, 255));

    public Breadcrumbs() {
        super("Breadcrumbs", 0, "Renders a line where you've been", Category.RENDER);
        this.addSettings(length, color);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        breadcrumbs.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        breadcrumbs.clear();
    }

    public void onEvent(Event e) {
        if(e instanceof EventRender3D) {
            EventRender3D event = (EventRender3D) e;

            RenderManager manager = mc.getRenderManager();
            final double rx = manager.renderPosX;
            final double ry = manager.renderPosY;
            final double rz = manager.renderPosZ;

            if(breadcrumbs.size() >= length.getValue()) {
                breadcrumbs.remove(0);
            }

            double x = RenderUtils.interpolate(mc.thePlayer.prevPosX, mc.thePlayer.posX, event.getPartialTicks());
            double y = RenderUtils.interpolate(mc.thePlayer.prevPosY, mc.thePlayer.posY, event.getPartialTicks());
            double z = RenderUtils.interpolate(mc.thePlayer.prevPosZ, mc.thePlayer.posZ, event.getPartialTicks());

            breadcrumbs.add(new Vec3(x, y, z));

            glTranslated(-rx, -ry, -rz);
            glDisable(GL_TEXTURE_2D);
            glDisable(GL_DEPTH_TEST);
            RenderUtils.enableBlending();
            glEnable(GL_LINE_SMOOTH);
            glDepthMask(false);
            glLineWidth((float) size.getValue());
            RenderUtils.color(this.color.getColor());

            glBegin(GL_LINE_STRIP);

            Vec3 lastCrumb = null;

            for (final Vec3 breadcrumb : this.breadcrumbs) {
                if (lastCrumb != null && lastCrumb.distanceTo(breadcrumb) > Math.sqrt(3)) {
                    glEnd();
                    glBegin(GL_LINE_STRIP);
                }

                glVertex3d(breadcrumb.xCoord, breadcrumb.yCoord, breadcrumb.zCoord);

                lastCrumb = breadcrumb;
            }
            glEnd();

            glEnable(GL_TEXTURE_2D);
            glEnable(GL_DEPTH_TEST);
            glDisable(GL_LINE_SMOOTH);
            glDisable(GL_BLEND);
            glDepthMask(true);
            glTranslated(rx, ry, rz);
        }
    }
}