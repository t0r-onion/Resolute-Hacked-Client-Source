package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventRender3D;
import vip.Resolute.modules.Module;
import vip.Resolute.modules.impl.combat.KillAura;
import vip.Resolute.util.render.Colors;
import vip.Resolute.util.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class MotionPredict extends Module {
    public MotionPredict() {
        super("MotionPredict", 0, "Predicts target motion", Category.RENDER);
    }

    public void onEvent(Event e) {
        if(e instanceof EventRender3D) {
            if(KillAura.target == null)
                return;

            EventRender3D er = (EventRender3D) e;
            EntityLivingBase player = KillAura.target;
            GL11.glPushMatrix();
            RenderUtils.pre3D();
            mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);
            double x = player.prevPosX + (player.posX - player.prevPosX) * (double)er.getPartialTicks() - RenderManager.renderPosX;
            double y = player.prevPosY + (player.posY - player.prevPosY) * (double)er.getPartialTicks() - RenderManager.renderPosY;
            double z = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)er.getPartialTicks() - RenderManager.renderPosZ;
            double xDelta = player.posX - player.prevPosX;
            double yDelta = player.posY - player.prevPosY;
            double zDelta = player.posZ - player.prevPosZ;
            double yMotion = 0.0D;
            double initVel = mc.thePlayer.motionY;

            for(int i = 5; i < 6; ++i) {
                yMotion += initVel - 0.002D * (double)i;
                GlStateManager.pushMatrix();
                GlStateManager.translate(x + xDelta * (double)i, y + (yDelta + yMotion) * (double)i, z + zDelta * (double)i);
                RenderUtils.drawPlatform(player, new Color(0, 255, 88, 75));
                GlStateManager.popMatrix();
            }

            RenderUtils.post3D();
            GL11.glPopMatrix();
        }
    }
}
