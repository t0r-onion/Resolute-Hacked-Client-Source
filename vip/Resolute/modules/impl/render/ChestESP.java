package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventRender3D;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ColorSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.render.Colors;
import vip.Resolute.util.render.OutlineUtils;
import vip.Resolute.util.render.RenderUtils;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.*;
import static org.lwjgl.opengl.GL11.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Iterator;

public class ChestESP extends Module {

    public static ModeSetting mode = new ModeSetting("Mode", "Filled", "Filled", "Chams", "Box");

    public BooleanSetting outlineProp = new BooleanSetting("Outline", false, () -> mode.is("Box"));
    public ColorSetting colorProp = new ColorSetting("Color", new Color(8158463), () -> mode.is("Box"));
    public NumberSetting alphaProp = new NumberSetting("Alpha", 40, 1, 255, 1);
    public static ColorSetting visibleColor = new ColorSetting("Visible Color", new Color(12216520), () -> mode.is("Chams"));
    public static ColorSetting occludedColor = new ColorSetting("Occluded Color", new Color(-1753449217), () -> mode.is("Chams"));
    public static BooleanSetting visibleFlat = new BooleanSetting("Visible Flat", false, () -> mode.is("Chams"));
    public static BooleanSetting occludedFlat = new BooleanSetting("Occluded Flat", true, () -> mode.is("Chams"));

    public static boolean enabled = false;

    public ChestESP() {
        super("ChestESP", 0, "Highlights any chest", Category.RENDER);
        this.addSettings(mode, outlineProp, colorProp, alphaProp, visibleColor, occludedColor, visibleFlat, occludedFlat);
    }

    public void onEnable() {
        enabled = true;
    }

    public void onDisable() {
        enabled = false;
    }

    public void onEvent(Event e) {

        if(e instanceof EventRender3D) {
            EventRender3D event = (EventRender3D) e;

            final Iterator<TileEntity> iterator;
            TileEntity entity;
            BlockPos pos;
            AxisAlignedBB bb;
            boolean outline;
            double rX;
            double rY;
            double rZ;

            if(mode.is("Box")) {
                iterator = mc.theWorld.loadedTileEntityList.iterator();
                while (iterator.hasNext()) {
                    entity = iterator.next();
                    if (entity instanceof TileEntityChest) {
                        pos = entity.getPos();
                        bb = entity.getBlockType().getCollisionBoundingBox(mc.theWorld, pos, entity.getBlockType().getStateFromMeta(entity.getBlockMetadata()));
                        if (bb != null) {
                            GL11.glDisable(2929);
                            RenderUtils.enableBlending();
                            GL11.glDepthMask(false);
                            GL11.glDisable(3553);
                            RenderUtils.color(this.colorProp.getValue().getRed() / 255, this.colorProp.getValue().getGreen() / 255, this.colorProp.getValue().getBlue() / 255, alphaProp.getValue() / 255);
                            outline = this.outlineProp.isEnabled();
                            if (outline) {
                                GL11.glLineWidth(1.0f);
                                GL11.glEnable(2848);
                                GL11.glHint(3154, 4354);
                            }
                            rX = RenderManager.renderPosX;
                            rY = RenderManager.renderPosY;
                            rZ = RenderManager.renderPosZ;
                            GL11.glTranslated(-rX, -rY, -rZ);
                            RenderGlobal.func_181561_a(bb, outline, true);
                            GL11.glTranslated(rX, rY, rZ);
                            GL11.glEnable(2929);
                            RenderUtils.disableBlending();
                            GL11.glDepthMask(true);
                            if (outline) {
                                GL11.glDisable(2848);
                            }
                            GL11.glEnable(3553);
                        }
                    }
                }
            }

            if(mode.is("Filled")) {
                try {
                    for (TileEntity entity1 : mc.theWorld.loadedTileEntityList) {
                        if (entity1 instanceof TileEntityChest || entity1 instanceof TileEntityEnderChest) {
                            OutlineUtils.renderOne((float) 2.5);
                            TileEntityRendererDispatcher.instance.renderTileEntity(entity1, event.getPartialTicks(), -1);
                            OutlineUtils.renderTwo();
                            TileEntityRendererDispatcher.instance.renderTileEntity(entity1, event.getPartialTicks(), -1);
                            OutlineUtils.renderThree();
                            TileEntityRendererDispatcher.instance.renderTileEntity(entity1, event.getPartialTicks(), -1);
                            OutlineUtils.renderFour(Color.cyan);
                            TileEntityRendererDispatcher.instance.renderTileEntity(entity1, event.getPartialTicks(), -1);
                            OutlineUtils.renderFive();
                            OutlineUtils.setColor(Color.white);
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    public static void preOccludedRender(final int occludedColor, final boolean occludedFlat) {
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

    public static void preVisibleRender(final int visibleColor, final boolean visibleFlat, final boolean occludedFlat) {
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
}