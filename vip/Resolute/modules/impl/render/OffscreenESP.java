package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventRender2D;
import vip.Resolute.events.impl.EventRender3D;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ColorSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.ui.click.skeet.SkeetUI;
import vip.Resolute.util.player.RotationUtils;
import vip.Resolute.util.render.RenderUtils;
import com.google.common.collect.Maps;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class OffscreenESP extends Module {
    public ColorSetting colorProp = new ColorSetting("Color", new Color(0, 104, 161));
    public NumberSetting size = new NumberSetting("Size", 5.0D, 5.0D, 10.0D, 0.1D);
    public NumberSetting radius = new NumberSetting("Radius", 100, 10, 100, 5);
    private EntityListener entityListener = new EntityListener();
    private int alpha;
    private boolean plus_or_minus;

    public OffscreenESP() {
        super("OffscreenESP", 0, "", Category.RENDER);
        this.addSettings(colorProp, size, radius);
    }

    @Override
    public void onEnable() {
        alpha = 0;
        plus_or_minus = false;
    }

    public void onEvent(Event e) {
        if(e instanceof EventRender3D) entityListener.render3d((EventRender3D) e);

        if(e instanceof EventRender2D) {
            EventRender2D event = (EventRender2D) e;

            ScaledResolution lr = new ScaledResolution(mc);
            float middleX = lr.getScaledWidth() / 2.0F;
            float middleY = lr.getScaledHeight() / 2.0F;
            float pt = event.getPartialTicks();

            mc.theWorld.loadedEntityList.forEach(o -> {
                if (o instanceof EntityLivingBase && isValid((EntityLivingBase) o)) {
                    EntityLivingBase entity = (EntityLivingBase) o;
                    Vec3 pos = entityListener.getEntityLowerBounds().get(entity);

                    if (pos != null) {
                        if (!isOnScreen(pos)) {
                            glPushMatrix();
                            float arrowSize = (float) size.getValue();
                            float alpha = Math.max(1.0F - (mc.thePlayer.getDistanceToEntity(entity) / 30.0F), 0.3F);
                            int color = colorProp.getColor();
                            glTranslatef(middleX + 0.5F, middleY, 1.0F);
                            float yaw = (float) (RenderUtils.interpolate(RotationUtils.getYawToEntity(entity, true), RotationUtils.getYawToEntity(entity, false), pt) - RenderUtils.interpolate(mc.thePlayer.prevRotationYaw, mc.thePlayer.rotationYaw, pt));
                            glRotatef(yaw, 0, 0, 1);
                            glTranslatef(0.0F, (float) (-radius.getValue() - (size.getValue())), 0.0F);
                            glDisable(GL_TEXTURE_2D);
                            glBegin(GL_TRIANGLE_STRIP);
                            glColor4ub(
                                    (byte) (color >> 16 & 255),
                                    (byte) (color >> 8 & 255),
                                    (byte) (color & 255),
                                    (byte) (alpha * 255));
                            glVertex2f(0, 0);
                            float offset;

                            offset = (int) (arrowSize / 3.0F);
                            glVertex2f(-arrowSize + offset, arrowSize);
                            glVertex2f(arrowSize - offset, arrowSize);

                            glEnd();
                            glEnable(GL_TEXTURE_2D);
                            glPopMatrix();
                        }
                    }
                }
            });
        }
    }

    private boolean isOnScreen(Vec3 pos) {
        if (pos.xCoord > -1 && pos.zCoord < 1)
            return pos.xCoord / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale) >= 0 && pos.xCoord / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale) <= Display.getWidth() && pos.yCoord / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale) >= 0 && pos.yCoord / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale) <= Display.getHeight();

        return false;
    }

    private boolean isValid(EntityLivingBase entity) {
        return entity != mc.thePlayer && isValidType(entity) && entity.getEntityId() != -1488 && entity.isEntityAlive() && (!entity.isInvisible() || SkeetUI.isInvisibles());
    }

    private boolean isValidType(EntityLivingBase entity) {
        return (SkeetUI.isPlayers() && entity instanceof EntityPlayer);
    }

    public class EntityListener {
        private Map<Entity, Vec3> entityUpperBounds = Maps.newHashMap();
        private Map<Entity, Vec3> entityLowerBounds = Maps.newHashMap();

        private void render3d(EventRender3D event) {
            if (!entityUpperBounds.isEmpty()) {
                entityUpperBounds.clear();
            }
            if (!entityLowerBounds.isEmpty()) {
                entityLowerBounds.clear();
            }
            for (Entity e : mc.theWorld.loadedEntityList) {
                Vec3 bound = getEntityRenderPosition(e);
                bound.add(new Vec3(0, e.height + 0.2, 0));
                Vec3 upperBounds = RenderUtils.to2D(bound.xCoord, bound.yCoord, bound.zCoord), lowerBounds = RenderUtils.to2D(bound.xCoord, bound.yCoord - 2, bound.zCoord);
                if (upperBounds != null && lowerBounds != null) {
                    entityUpperBounds.put(e, upperBounds);
                    entityLowerBounds.put(e, lowerBounds);
                }
            }
        }

        private Vec3 getEntityRenderPosition(Entity entity) {
            double partial = mc.timer.renderPartialTicks;

            double x = entity.lastTickPosX + ((entity.posX - entity.lastTickPosX) * partial) - mc.getRenderManager().viewerPosX;
            double y = entity.lastTickPosY + ((entity.posY - entity.lastTickPosY) * partial) - mc.getRenderManager().viewerPosY;
            double z = entity.lastTickPosZ + ((entity.posZ - entity.lastTickPosZ) * partial) - mc.getRenderManager().viewerPosZ;

            return new Vec3(x, y, z);
        }

        public Map<Entity, Vec3> getEntityLowerBounds() {
            return entityLowerBounds;
        }
    }
}
