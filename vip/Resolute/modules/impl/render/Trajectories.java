package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventRender3D;
import vip.Resolute.modules.Module;

import vip.Resolute.util.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.*;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Trajectories extends Module {

    private boolean isBow;

    public Trajectories() {
        super("Trajectories", 0, "Renders a projectile's trajectory", Category.RENDER);
    }

    public void onEvent(Event e) {
        if(e instanceof EventRender3D) {
            final ItemStack stack = mc.thePlayer.getHeldItem();
            if (stack == null || !isItemValid(stack)) return;

            isBow = stack.getItem() instanceof ItemBow;

            final double playerYaw = mc.thePlayer.rotationYaw;
            final double playerPitch = mc.thePlayer.rotationPitch;

            double projectilePosX = mc.getRenderManager().renderPosX - Math.cos(Math.toRadians(playerYaw)) * .16F;
            double projectilePosY = mc.getRenderManager().renderPosY + mc.thePlayer.getEyeHeight();
            double projectilePosZ = mc.getRenderManager().renderPosZ - Math.sin(Math.toRadians(playerYaw)) * .16F;


            double projectileMotionX = (-Math.sin(Math.toRadians(playerYaw)) * Math.cos(Math.toRadians(playerPitch))) * (isBow ? 1 : .4);
            double projectileMotionY = -Math.sin(Math.toRadians(playerPitch - (isThrowablePotion(stack) ? 20 : 0))) * (isBow ? 1 : .4);
            double projectileMotionZ = (Math.cos(Math.toRadians(playerYaw)) * Math.cos(Math.toRadians(playerPitch))) * (isBow ? 1 : .4);

            double shootPower = mc.thePlayer.getItemInUseDuration();

            if (isBow) {
                shootPower /= 20;
                shootPower = ((shootPower * shootPower) + (shootPower * 2)) / 3;

                if (shootPower < .1) return;
                if (shootPower > 1) shootPower = 1;
            }

            final double distance = Math.sqrt(projectileMotionX * projectileMotionX + projectileMotionY * projectileMotionY + projectileMotionZ * projectileMotionZ);

            projectileMotionX /= distance;
            projectileMotionY /= distance;
            projectileMotionZ /= distance;

            projectileMotionX *= (isBow ? shootPower : .5) * 3;
            projectileMotionY *= (isBow ? shootPower : .5) * 3;
            projectileMotionZ *= (isBow ? shootPower : .5) * 3;

            boolean projectileHasLanded = false;
            MovingObjectPosition landingPosition = null;

            GlStateManager.resetColor();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GlStateManager.disableTexture2D();
            color(10, 120, 200, 200);
            GL11.glLineWidth(1.5F);
            GL11.glBegin(GL11.GL_LINE_STRIP);
            {
                while (!projectileHasLanded && projectilePosY > 0) {
                    final Vec3 currentPosition = new Vec3(projectilePosX, projectilePosY, projectilePosZ);
                    final Vec3 nextPosition = new Vec3(projectilePosX + projectileMotionX, projectilePosY + projectileMotionY, projectilePosZ + projectileMotionZ);

                    final MovingObjectPosition possibleLandingPositon = mc.theWorld.rayTraceBlocks(currentPosition, nextPosition, false, true, false);

                    if (possibleLandingPositon != null) {
                        if (possibleLandingPositon.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
                            landingPosition = possibleLandingPositon;
                            projectileHasLanded = true;
                        }
                    }

                    projectilePosX += projectileMotionX;
                    projectilePosY += projectileMotionY;
                    projectilePosZ += projectileMotionZ;


                    projectileMotionX *= .99;
                    projectileMotionY *= .99;
                    projectileMotionZ *= .99;

                    projectileMotionY -= (isBow ? .05 : isThrowablePotion(stack) ? .05 : .03);

                    GL11.glVertex3d(projectilePosX - mc.getRenderManager().renderPosX, projectilePosY - mc.getRenderManager().renderPosY, projectilePosZ - mc.getRenderManager().renderPosZ);
                }
            }
            GL11.glEnd();
            GlStateManager.enableTexture2D();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glDisable(GL11.GL_BLEND);
            GlStateManager.resetColor();

            if (landingPosition != null) {
                if (landingPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    RenderUtils.drawBox(landingPosition.getBlockPos(), new Color(255, 0, 0, 100), false);
                }
            }
        }
    }

    private boolean isItemValid(ItemStack stack) {
        return (stack.getItem() instanceof ItemBow) || (stack.getItem() instanceof ItemEnderPearl) || (stack.getItem() instanceof ItemEgg) || (stack.getItem() instanceof ItemSnowball) || isThrowablePotion(stack);
    }

    private boolean isThrowablePotion(ItemStack stack) {
        return stack.getItem() instanceof ItemPotion && ItemPotion.isSplash(mc.thePlayer.getHeldItem().getItemDamage());
    }

    public final void color(double red, double green, double blue, double alpha) {
        GL11.glColor4d(red, green, blue, alpha);
    }
}

