package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventRender2D;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.movement.MovementUtils;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;

public class Graph extends Module {

    int maxHeight = 30;
    private ArrayList<Double> speeds = new ArrayList<Double>(Collections.nCopies(50, 0.0));
    int ticks = 5;

    public NumberSetting height = new NumberSetting("Height", 30, 5, 50, 1);

    public Graph() {
        super("Graph", 0, "Renders a speed graph", Category.MOVEMENT);
        this.addSettings(height);
    }

    public void onEvent(Event e) {
        if(e instanceof EventMotion) {
            if (!this.mc.thePlayer.isEntityAlive()) {
                return;
            }
            double d = MovementUtils.getSpeed() * 20.0f;
            if (this.speeds.size() > this.ticks) {
                this.speeds.remove(0);
            }
            this.speeds.add(d);
        }

        if(e instanceof EventRender2D) {
            int n = 1;
            int n2 = 160;
            GL11.glDisable(2929);
            GL11.glEnable(3042);
            GL11.glDisable(3553);
            GL11.glBlendFunc(770, 771);
            GL11.glDepthMask(true);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            GL11.glHint(3155, 4354);
            GL11.glBegin(7);
            GlStateManager.color(0.015686275f, 0.015686275f, 0.015686275f, 0.0f);
            GL11.glVertex2f(1.0f, (float) (n2 - this.height.getValue() - 2));
            GL11.glVertex2f(1.0f, n2);
            GL11.glVertex2f(2 + this.speeds.size(), n2);
            GL11.glVertex2f(2 + this.speeds.size(), (float) (n2 - this.height.getValue() - 2));
            GL11.glEnd();
            GL11.glEnable(2848);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glBegin(3);
            float f = n2;
            for (double d : this.speeds) {
                float f2 = MathHelper.clamp_float((float)((double)n2 - d * 2.0), (float) (160 - this.height.getValue()), 160.0f);
                GL11.glVertex2f(n, f);
                GL11.glVertex2f(n, f2);
                f = f2;
                ++n;
            }
            GL11.glEnd();
            GL11.glEnable(3553);
            GL11.glDisable(3042);
            GL11.glEnable(2929);
            GL11.glDisable(2848);
            GL11.glHint(3154, 4352);
            GL11.glHint(3155, 4352);
        }
    }
}
