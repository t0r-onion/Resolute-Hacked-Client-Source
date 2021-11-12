package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventRender2D;
import vip.Resolute.events.impl.EventRender3D;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ColorSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.player.RotationUtils;
import vip.Resolute.util.render.RenderUtils;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;

public class Pointers extends Module {

    public ColorSetting colorProp = new ColorSetting("Color", new Color(0x7E5EB5));
    public NumberSetting radius = new NumberSetting("Radius", 30, 10, 100, 1);
    public NumberSetting size = new NumberSetting("Size", 6, 3, 30, 1);

    private final Map<EntityPlayer, float[]> entityPosMap;

    public Pointers() {
        super("Pointers", 0, "Draws pointers at entities", Category.RENDER);
        this.addSettings(colorProp, radius, size);

        entityPosMap = new HashMap<>();
    }

    public void onEvent(Event e) {
        if(e instanceof EventRender2D) {
            EventRender2D event = (EventRender2D) e;
            ScaledResolution sr = new ScaledResolution(mc);

            float middleX = sr.getScaledWidth() / 2.0F;
            float middleY = sr.getScaledHeight() / 2.0F;
            float pt = event.getPartialTicks();
            RenderUtils.startBlending();

            for(EntityPlayer player : entityPosMap.keySet()) {
                if(player instanceof EntityOtherPlayerMP) {
                    glPushMatrix();
                    float arrowSize = (float) size.getValue();
                    float alpha = Math.max(1.0F - (mc.thePlayer.getDistanceToEntity(player) / 30.0F), 0.3F);
                    int color = colorProp.getColor();
                    glTranslatef(middleX + 0.5F, middleY, 1.0F);

                    float yaw = (float) (RenderUtils.interpolate(RotationUtils.getYawToEntity(player, true), RotationUtils.getYawToEntity(player, false), pt) - RenderUtils.interpolate(mc.thePlayer.prevRotationYaw, mc.thePlayer.rotationYaw, pt));

                    glRotatef(yaw, 0, 0, 1);
                    glTranslatef(0.0F, (float) (-radius.getValue() - (size.getValue())), 0.0F);
                    glDisable(GL_TEXTURE_2D);
                    glBegin(GL_TRIANGLE_STRIP);
                    glColor4ub((byte) (color >> 16 & 255), (byte) (color >> 8 & 255), (byte) (color & 255), (byte) (alpha * 255));

                    glVertex2f(0, 0);
                    float offset;

                    glVertex2f(-arrowSize, arrowSize);
                    glVertex2f(arrowSize, arrowSize);

                    glEnd();
                    glEnable(GL_TEXTURE_2D);
                    glPopMatrix();
                }
            }
        }

        if(e instanceof EventRender3D) {
            EventRender3D event = (EventRender3D) e;

            if (!entityPosMap.isEmpty())
                entityPosMap.clear();

            float partialTicks = event.getPartialTicks();
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (!(player instanceof EntityOtherPlayerMP || !player.isEntityAlive() || player.isInvisible()))
                    continue;

                GL11.glPushMatrix();
                float posX = (float) (RenderUtils.interpolate(player.prevPosX, player.posX, partialTicks) - RenderManager.viewerPosX);
                float posY = (float) (RenderUtils.interpolate(player.prevPosY, player.posY, partialTicks) - RenderManager.viewerPosY);
                float posZ = (float) (RenderUtils.interpolate(player.prevPosZ, player.posZ, partialTicks) - RenderManager.viewerPosZ);

                double halfWidth = player.width / 2.0D + 0.1D;
                AxisAlignedBB bb = new AxisAlignedBB(posX - halfWidth, posY + 0.1D, posZ - halfWidth, posX + halfWidth, posY + player.height + 0.1D, posZ + halfWidth);

                double[][] vectors = {{bb.minX, bb.minY, bb.minZ}, {bb.minX, bb.maxY, bb.minZ}, {bb.minX, bb.maxY, bb.maxZ}, {bb.minX, bb.minY, bb.maxZ}, {bb.maxX, bb.minY, bb.minZ}, {bb.maxX, bb.maxY, bb.minZ}, {bb.maxX, bb.maxY, bb.maxZ}, {bb.maxX, bb.minY, bb.maxZ}};

                Vector3f projection;
                Vector4f position = new Vector4f(Float.MAX_VALUE, Float.MAX_VALUE, -1.0F, -1.0F);

                for (double[] vec : vectors) {
                    projection = RenderUtils.project2D((float) vec[0], (float) vec[1], (float) vec[2], 2);
                    if (projection != null && projection.z >= 0.0F && projection.z < 1.0F) {
                        position.x = Math.min(position.x, projection.x);
                        position.y = Math.min(position.y, projection.y);
                        position.z = Math.max(position.z, projection.x);
                        position.w = Math.max(position.w, projection.y);
                    }
                }

                entityPosMap.put(player, new float[]{position.x, position.y, position.z, position.w});

                GL11.glPopMatrix();
            }
        }
    }
}
