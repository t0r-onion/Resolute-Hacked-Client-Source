package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventRender3D;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ColorSetting;
import vip.Resolute.settings.impl.ModeSetting;

import vip.Resolute.util.render.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

public class BlockOverlay extends Module {

    public static ModeSetting mode = new ModeSetting("Mode", "Filled", "Filled");

    public static ColorSetting color = new ColorSetting("Color", new Color(0, 90, 255));

    public static boolean enabled = false;

    public BlockOverlay() {
        super("BlockOverlay", 0, "Highlights what you are looking at", Category.RENDER);
        this.addSettings(color);
    }

    public void onEnable() {
        enabled = true;
    }

    public void onDisable() {
        enabled = false;
    }

    public void onEvent(Event e) {
        if(e instanceof EventRender3D) {
            if(mode.is("Filled")) {
                if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    BlockPos pos = mc.objectMouseOver.getBlockPos();
                    Block block = mc.theWorld.getBlockState(pos).getBlock();
                    RenderManager renderManager = mc.getRenderManager();
                    String s = block.getLocalizedName();
                    mc.getRenderManager();
                    double x = (double)pos.getX() - renderManager.getRenderPosX();
                    mc.getRenderManager();
                    double y = (double)pos.getY() - renderManager.getRenderPosY();
                    mc.getRenderManager();
                    double z = (double)pos.getZ() - renderManager.getRenderPosZ();
                    GL11.glPushMatrix();
                    GL11.glEnable((int)3042);
                    GL11.glBlendFunc((int)770, (int)771);
                    GL11.glDisable((int)3553);
                    GL11.glEnable((int)2848);
                    GL11.glDisable((int)2929);
                    GL11.glDepthMask((boolean)false);
                    Color c = color.getValue();
                    int r = c.getRed();
                    int g = c.getGreen();
                    int b = c.getBlue();
                    RenderUtils.glColor(new Color(r, g, b, 50).getRGB());
                    double minX = block instanceof BlockStairs || Block.getIdFromBlock((Block)block) == 134 ? 0.0 : block.getBlockBoundsMinX();
                    double minY = block instanceof BlockStairs || Block.getIdFromBlock((Block)block) == 134 ? 0.0 : block.getBlockBoundsMinY();
                    double minZ = block instanceof BlockStairs || Block.getIdFromBlock((Block)block) == 134 ? 0.0 : block.getBlockBoundsMinZ();
                    RenderUtils.drawBoundingBox((AxisAlignedBB)new AxisAlignedBB(x + minX, y + minY, z + minZ, x + block.getBlockBoundsMaxX(), y + block.getBlockBoundsMaxY(), z + block.getBlockBoundsMaxZ()));
                    RenderUtils.glColor(new Color(r, g, b).getRGB());
                    GL11.glLineWidth((float)0.5f);
                    GL11.glDisable((int)2848);
                    GL11.glEnable((int)3553);
                    GL11.glEnable((int)2929);
                    GL11.glDepthMask((boolean)true);
                    GL11.glDisable((int)3042);
                    GL11.glPopMatrix();
                }
                GL11.glColor4f(1f, 1f, 1f, 1f);
            }
        }
    }


    public static float getOutlineAlpha() {

        int combinedColor = color.getValue().getRGB();

        return (combinedColor >> 25 & 0xFF) / 255.0f;
    }

    public static int getOutlineColor() {

        int combinedColor = color.getValue().getRGB();

        return combinedColor;
    }
}
