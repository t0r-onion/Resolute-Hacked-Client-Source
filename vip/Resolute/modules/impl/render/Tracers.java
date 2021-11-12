package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventRender3D;
import vip.Resolute.modules.Module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class Tracers extends Module {


    public Tracers() {
        super("Tracers", 0, "Renders a line to each player", Category.RENDER);
    }

    public void onEvent(Event ev) {
        if(ev instanceof EventRender3D) {
            EventRender3D event = (EventRender3D) ev;
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();
            GL11.glLineWidth(1);
            float partialTicks = ((EventRender3D)event).getPartialTicks();
            float x = (float) ((float) (mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks) - RenderManager.renderPosX);
            float y = (float) ((float) (mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks) - RenderManager.renderPosY);
            float z = (float) ((float) (mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks) - RenderManager.renderPosZ);
            if (mc.gameSettings.thirdPersonView == 0) {
                GL11.glLoadIdentity();
                Minecraft.getMinecraft().entityRenderer.orientCamera(Minecraft.getMinecraft().timer.renderPartialTicks);
            } else {
                x = (float) (mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * (double)partialTicks);
                y = (float) (mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * (double)partialTicks);
                z = (float) (mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * (double)partialTicks);
                GlStateManager.translate(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
            }
            Vec3 playerPos = new Vec3(x, y + mc.thePlayer.getEyeHeight(), z);
            for (Entity e : mc.theWorld.loadedEntityList) {
                if (e instanceof EntityPlayer && e != mc.thePlayer) {
                    float x2 = (float) ((float) (e.lastTickPosX + (e.posX - e.lastTickPosX) * partialTicks) - RenderManager.renderPosX);
                    float y2 = (float) ((float) (e.lastTickPosY + (e.posY - e.lastTickPosY) * partialTicks) - RenderManager.renderPosY);
                    float z2 = (float) ((float) (e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * partialTicks) - RenderManager.renderPosZ);

                    if (mc.gameSettings.thirdPersonView != 0) {
                        x2 = (float) (e.prevPosX + (e.posX - e.prevPosX) * (double)partialTicks);
                        y2 = (float) (e.prevPosY + (e.posY - e.prevPosY) * (double)partialTicks);
                        z2 = (float) (e.prevPosZ + (e.posZ - e.prevPosZ) * (double)partialTicks);
                    }
                    this.color(e);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3d(playerPos.getX(), playerPos.getY(), playerPos.getZ());
                    GL11.glVertex3d(x2, y2, z2);
                    GL11.glEnd();

                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3d(x2, y2, z2);
                    GL11.glVertex3d(x2, y2 + e.getEyeHeight(), z2);
                    GL11.glEnd();
                }
            }
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
            GlStateManager.enableDepth();
            GL11.glColor3f(1F, 1F, 1F);
        }
    }

    private void color(Entity e) {
        GL11.glColor3f((float)255, (float)255 , (float)255 );
    }
}
