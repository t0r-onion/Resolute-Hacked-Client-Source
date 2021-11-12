package net.minecraft.client.renderer.entity;

import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.modules.impl.combat.KillAura;
import vip.Resolute.modules.impl.movement.Scaffold;
import vip.Resolute.modules.impl.render.ESP;
import vip.Resolute.modules.impl.render.Ghost;
import com.google.common.collect.Lists;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.List;

import vip.Resolute.Resolute;
import vip.Resolute.events.impl.EventRenderNametag;
import vip.Resolute.modules.impl.render.Chams;
import vip.Resolute.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import optifine.Config;
import optifine.Reflector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import shadersmod.client.Shaders;

public abstract class RendererLivingEntity<T extends EntityLivingBase> extends Render<T>
{
    private static final Logger logger = LogManager.getLogger();
    private static final DynamicTexture field_177096_e = new DynamicTexture(16, 16);
    protected ModelBase mainModel;
    protected FloatBuffer brightnessBuffer = GLAllocation.createDirectFloatBuffer(4);
    protected List<LayerRenderer<T>> layerRenderers = Lists.newArrayList();
    protected boolean renderOutlines = false;
    private static final String __OBFID = "CL_00001012";
    public static float NAME_TAG_RANGE = 64.0F;
    public static float NAME_TAG_RANGE_SNEAK = 32.0F;

    public float renderHeadPitch;
    public static boolean renderNametags = true;
    public static boolean field_177098_i;
    public static float PitchHead, previousPitchHead;
    private static boolean unsetPolyOffset;
    public static final boolean animateModelLiving;

    public static float yawHead;
    public static float bodyOffsetYaw;

    static {
        RendererLivingEntity.NAME_TAG_RANGE = 64.0f;
        RendererLivingEntity.NAME_TAG_RANGE_SNEAK = 32.0f;
        final int[] aint = RendererLivingEntity.field_177096_e.getTextureData();
        for (int i = 0; i < 256; ++i) {
            aint[i] = -1;
        }
        RendererLivingEntity.field_177096_e.updateDynamicTexture();
        animateModelLiving = Boolean.getBoolean("animate.model.living");
    }

    public RendererLivingEntity(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn)
    {
        super(renderManagerIn);
        this.mainModel = modelBaseIn;
        this.shadowSize = shadowSizeIn;
    }

    public static float getPitchHead() {
        return PitchHead;
    }

    public static void setPitchHead(float perspitch) {
        PitchHead = perspitch;
    }

    public static float getPreviousPitchHead() {
        return previousPitchHead;
    }

    public static void setPreviousPitchHead(float perspitch) {
        previousPitchHead = perspitch;
    }

    public <V extends EntityLivingBase, U extends LayerRenderer<V>> boolean addLayer(U layer)
    {
        return this.layerRenderers.add((LayerRenderer<T>)layer);
    }

    protected <V extends EntityLivingBase, U extends LayerRenderer<V>> boolean removeLayer(U layer)
    {
        return this.layerRenderers.remove(layer);
    }

    public ModelBase getMainModel()
    {
        return this.mainModel;
    }

    /**
     * Returns a rotation angle that is inbetween two other rotation angles. par1 and par2 are the angles between which
     * to interpolate, par3 is probably a float between 0.0 and 1.0 that tells us where "between" the two angles we are.
     * Example: par1 = 30, par2 = 50, par3 = 0.5, then return = 40
     */
    protected float interpolateRotation(float par1, float par2, float par3)
    {
        float f;

        for (f = par2 - par1; f < -180.0F; f += 360.0F)
        {
            ;
        }

        while (f >= 180.0F)
        {
            f -= 360.0F;
        }

        return par1 + par3 * f;
    }

    public void transformHeldFull3DItemLayer()
    {
    }

    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if(Ghost.enabled) {
            doRenderGhost(entity, x, y, z, entityYaw, partialTicks);
        } else {
            doRenderNormal(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    public void doRenderNormal(final T entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks) {
        if (!Reflector.RenderLivingEvent_Pre_Constructor.exists() || !Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Pre_Constructor, entity, this, x, y, z)) {
            if (RendererLivingEntity.animateModelLiving) {
                entity.limbSwingAmount = 1.0f;
            }
            GL11.glPushMatrix();
            GlStateManager.disableCull();
            this.mainModel.swingProgress = this.getSwingProgress(entity, partialTicks);
            this.mainModel.isRiding = entity.isRiding();
            if (Reflector.ForgeEntity_shouldRiderSit.exists()) {
                this.mainModel.isRiding = (entity.isRiding() && entity.ridingEntity != null && Reflector.callBoolean(entity.ridingEntity, Reflector.ForgeEntity_shouldRiderSit, new Object[0]));
            }
            this.mainModel.isChild = entity.isChild();
            try {
                EntityPlayerSP player = null;
                final boolean showServerSideRotations = entity instanceof EntityPlayerSP && (player = (EntityPlayerSP)entity).currentEvent.isRotating();
                float headYaw;
                float bodyYaw;
                if (showServerSideRotations) {
                    final EventMotion event = player.currentEvent;
                    bodyYaw = (headYaw = (float) RenderUtils.interpolate(event.getPrevYaw(), event.getYaw(), partialTicks));
                }
                else {
                    bodyYaw = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
                    headYaw = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
                }
                float yawDif = headYaw - bodyYaw;
                if (this.mainModel.isRiding && entity.ridingEntity instanceof EntityLivingBase) {
                    final EntityLivingBase entitylivingbase = (EntityLivingBase)entity.ridingEntity;
                    bodyYaw = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                    yawDif = headYaw - bodyYaw;
                    float f3 = MathHelper.wrapAngleTo180_float(yawDif);
                    if (f3 < -85.0f) {
                        f3 = -85.0f;
                    }
                    if (f3 >= 85.0f) {
                        f3 = 85.0f;
                    }
                    bodyYaw = headYaw - f3;
                    if (f3 * f3 > 2500.0f) {
                        bodyYaw += f3 * 0.2f;
                    }
                    yawDif = headYaw - bodyYaw;
                }
                float pitch;
                if (showServerSideRotations) {
                    final EventMotion event2 = player.currentEvent;
                    pitch = (float) RenderUtils.interpolate(event2.getPrevPitch(), event2.getPitch(), partialTicks);
                }
                else {
                    pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                }
                this.renderLivingAt(entity, x, y, z);
                final float f4 = this.handleRotationFloat(entity, partialTicks);
                this.rotateCorpse(entity, f4, bodyYaw, partialTicks);
                GlStateManager.enableRescaleNormal();
                GL11.glScalef(-1.0f, -1.0f, 1.0f);
                this.preRenderCallback(entity, partialTicks);
                final float f5 = 0.0625f;
                GL11.glTranslatef(0.0f, -1.5078125f, 0.0f);
                float f6 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
                float f7 = entity.limbSwing - entity.limbSwingAmount * (1.0f - partialTicks);
                if (entity.isChild()) {
                    f7 *= 3.0f;
                }
                if (f6 > 1.0f) {
                    f6 = 1.0f;
                }
                GlStateManager.enableAlpha();
                this.mainModel.setLivingAnimations(entity, f7, f6, partialTicks);
                this.mainModel.setRotationAngles(f7, f6, f4, yawDif, pitch, 0.0625f, entity);
                if (this.renderOutlines) {
                    GL11.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
                    this.renderModel(entity, f7, f6, f4, yawDif, pitch, 0.0625f);
                }
                else {
                    final boolean enabled = Chams.enabled;
                    final boolean flag = (!enabled || Chams.hurtEffect.isEnabled()) && this.setDoRenderBrightness(entity, partialTicks, enabled);
                    this.renderModel(entity, f7, f6, f4, yawDif, pitch, f5);
                    if (RendererLivingEntity.unsetPolyOffset) {
                        GL11.glPolygonOffset(0.0f, 1000000.0f);
                        GL11.glDisable(32823);
                        RendererLivingEntity.unsetPolyOffset = false;
                    }
                    if (flag) {
                        this.unsetBrightness();
                    }
                    if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator()) {
                        this.renderLayers(entity, f7, f6, partialTicks, f4, yawDif, pitch, 0.0625f);
                    }
                }
                GlStateManager.disableRescaleNormal();
            }
            catch (Exception exception) {
                RendererLivingEntity.logger.error("Couldn't render entity", (Throwable)exception);
            }
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.enableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableCull();
            GL11.glPopMatrix();
            if (!this.renderOutlines) {
                super.doRender(entity, x, y, z, entityYaw, partialTicks);
            }
            if (Reflector.RenderLivingEvent_Post_Constructor.exists()) {
                Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Post_Constructor, entity, this, x, y, z);
            }
        }
    }

    public void doRenderGhost(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (!Reflector.RenderLivingEvent_Pre_Constructor.exists() || !Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Pre_Constructor, new Object[] { entity, this, Double.valueOf(x), Double.valueOf(y), Double.valueOf(z) })) {
            if (animateModelLiving)
                ((EntityLivingBase)entity).limbSwingAmount = 1.0F;
            GL11.glPushMatrix();
            GlStateManager.disableCull();
            this.mainModel.swingProgress = getSwingProgress(entity, partialTicks);
            this.mainModel.isRiding = entity.isRiding();
            if (Reflector.ForgeEntity_shouldRiderSit.exists())
                this.mainModel.isRiding = (entity.isRiding() && ((EntityLivingBase)entity).ridingEntity != null && Reflector.callBoolean(((EntityLivingBase)entity).ridingEntity, Reflector.ForgeEntity_shouldRiderSit, new Object[0]));
            this.mainModel.isChild = entity.isChild();
            try {
                float bodyYaw, headYaw, pitch;
                EntityPlayerSP player = null;
                boolean showServerSideRotations = (entity instanceof EntityPlayerSP && (player = (EntityPlayerSP)entity).currentEvent.isRotating());
                if (showServerSideRotations) {
                    EventMotion event = player.currentEvent;
                    float yaw = (float) RenderUtils.interpolate(event.getPrevYaw(), event.getYaw(), partialTicks);
                    bodyYaw = yaw;
                    headYaw = yaw;
                } else {
                    bodyYaw = interpolateRotation(((EntityLivingBase)entity).prevRenderYawOffset, ((EntityLivingBase)entity).renderYawOffset, partialTicks);
                    headYaw = interpolateRotation(((EntityLivingBase)entity).prevRotationYawHead, ((EntityLivingBase)entity).rotationYawHead, partialTicks);
                }
                float yawDif = headYaw - bodyYaw;
                if (this.mainModel.isRiding && ((EntityLivingBase)entity).ridingEntity instanceof EntityLivingBase) {
                    EntityLivingBase entitylivingbase = (EntityLivingBase)((EntityLivingBase)entity).ridingEntity;
                    bodyYaw = interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                    yawDif = headYaw - bodyYaw;
                    float f3 = MathHelper.wrapAngleTo180_float(yawDif);
                    if (f3 < -85.0F)
                        f3 = -85.0F;
                    if (f3 >= 85.0F)
                        f3 = 85.0F;
                    bodyYaw = headYaw - f3;
                    if (f3 * f3 > 2500.0F)
                        bodyYaw += f3 * 0.2F;
                    yawDif = headYaw - bodyYaw;
                }
                if (showServerSideRotations) {
                    EventMotion event = player.currentEvent;
                    pitch = (float) RenderUtils.interpolate(event.getPrevPitch(), event.getPitch(), partialTicks);
                } else {
                    pitch = ((EntityLivingBase)entity).prevRotationPitch + (((EntityLivingBase)entity).rotationPitch - ((EntityLivingBase)entity).prevRotationPitch) * partialTicks;
                }
                renderLivingAt(entity, x, y, z);
                float f4 = 0.0625F;
                float f5 = ((EntityLivingBase)entity).prevLimbSwingAmount + (((EntityLivingBase)entity).limbSwingAmount - ((EntityLivingBase)entity).prevLimbSwingAmount) * partialTicks;
                float f6 = ((EntityLivingBase)entity).limbSwing - ((EntityLivingBase)entity).limbSwingAmount * (1.0F - partialTicks);
                float f8 = handleRotationFloat(entity, partialTicks);
                float f7 = ((EntityLivingBase)entity).prevRotationPitch + (((EntityLivingBase)entity).rotationPitch - ((EntityLivingBase)entity).prevRotationPitch) * partialTicks;
                float f = interpolateRotation(((EntityLivingBase)entity).prevRenderYawOffset, ((EntityLivingBase)entity).renderYawOffset, partialTicks);
                float f1 = interpolateRotation(((EntityLivingBase)entity).prevRotationYawHead, ((EntityLivingBase)entity).rotationYawHead, partialTicks);
                float f2 = f1 - f;
                boolean flag = setDoRenderBrightness(entity, partialTicks);
                if ((KillAura.target != null || Scaffold.enabled) && entity == (Minecraft.getMinecraft()).thePlayer) {
                    GlStateManager.pushMatrix();
                    rotateCorpse(entity, f7, interpolateRotation(((EntityLivingBase)entity).prevRenderYawOffset, ((EntityLivingBase)entity).renderYawOffset, partialTicks), partialTicks);
                    GlStateManager.enableRescaleNormal();
                    GlStateManager.scale(-1.0F, -1.0F, 1.0F);
                    preRenderCallback(entity, partialTicks);
                    GlStateManager.translate(0.0F, -1.5078125F, 0.0F);
                    if (entity.isChild())
                        f6 *= 3.0F;
                    if (f5 > 1.0F)
                        f5 = 1.0F;
                    GlStateManager.enableAlpha();
                    this.mainModel.setLivingAnimations((EntityLivingBase)entity, f6, f5, partialTicks);
                    this.mainModel.setRotationAngles(f6, f5, f7, f2, f7, f4, (Entity)entity);
                    GlStateManager.pushMatrix();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 0.3F);
                    GlStateManager.depthMask(false);
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(770, 771);
                    GlStateManager.alphaFunc(516, 0.003921569F);
                    renderModel(entity, f6, f5, f7, f2, f7, f4);
                    GlStateManager.disableBlend();
                    GlStateManager.alphaFunc(516, 0.1F);
                    GlStateManager.popMatrix();
                    GlStateManager.depthMask(true);
                    if (flag)
                        unsetBrightness();
                    GlStateManager.depthMask(true);
                    if (!((EntityPlayer)entity).isSpectator())
                        renderLayers(entity, f6, f5, partialTicks, f7, f2, f7, f4);
                    GlStateManager.popMatrix();
                }
                rotateCorpse(entity, f8, bodyYaw, partialTicks);
                GlStateManager.enableRescaleNormal();
                GL11.glScalef(-1.0F, -1.0F, 1.0F);
                preRenderCallback(entity, partialTicks);
                GL11.glTranslatef(0.0F, -1.5078125F, 0.0F);
                if (entity.isChild())
                    f6 *= 3.0F;
                if (f5 > 1.0F)
                    f5 = 1.0F;
                GlStateManager.enableAlpha();
                this.mainModel.setLivingAnimations((EntityLivingBase)entity, f6, f5, partialTicks);
                this.mainModel.setRotationAngles(f6, f5, f8, yawDif, pitch, 0.0625F, (Entity)entity);
                if (this.renderOutlines) {
                    renderModel(entity, f6, f5, f8, yawDif, pitch, 0.0625F);
                    boolean flag1 = setScoreTeamColor(entity);
                    renderModel(entity, f6, f5, f8, yawDif, pitch, 0.0625F);
                    if (flag1)
                        unsetScoreTeamColor();
                } else if ((KillAura.target != null || Scaffold.enabled) && entity == (Minecraft.getMinecraft()).thePlayer) {
                    GL11.glPushMatrix();
                    GL11.glPushAttrib(1048575);
                    GL11.glDisable(2929);
                    GL11.glDisable(3553);
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    GL11.glDisable(2896);
                    GL11.glPolygonMode(1032, 6914);
                    RenderUtils.glColor((new Color(Ghost.color.getValue().getRed(), Ghost.color.getValue().getGreen(), Ghost.color.getValue().getBlue(), 40).getRGB()));
                    renderModel(entity, f6, f5, f8, yawDif, pitch, f4);
                    GL11.glEnable(2896);
                    GL11.glDisable(3042);
                    GL11.glEnable(3553);
                    GL11.glEnable(2929);
                    GL11.glColor3d(1.0D, 1.0D, 1.0D);
                    GL11.glPopAttrib();
                    GL11.glPopMatrix();
                } else {
                    renderModel(entity, f6, f5, f8, yawDif, pitch, f4);
                    if (flag)
                        unsetBrightness();
                    GlStateManager.depthMask(true);
                    if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator())
                        renderLayers(entity, f6, f5, partialTicks, f8, yawDif, pitch, 0.0625F);
                }
                GlStateManager.disableRescaleNormal();
            } catch (Exception exception) {
                logger.error("Couldn't render entity", exception);
            }
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.enableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableCull();
            GL11.glPopMatrix();
            if (!this.renderOutlines)
                super.doRender(entity, x, y, z, entityYaw, partialTicks);
            if (Reflector.RenderLivingEvent_Post_Constructor.exists())
                Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Post_Constructor, new Object[] { entity, this, Double.valueOf(x), Double.valueOf(y), Double.valueOf(z) });
        }
    }

    protected boolean setDoRenderBrightness(T entityLivingBaseIn, float partialTicks) {
        return setBrightness(entityLivingBaseIn, partialTicks, true);
    }

    protected boolean setScoreTeamColor(EntityLivingBase entityLivingBaseIn)
    {
        int i = 16777215;

        if (entityLivingBaseIn instanceof EntityPlayer)
        {
            ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam)entityLivingBaseIn.getTeam();

            if (scoreplayerteam != null)
            {
                String s = FontRenderer.getFormatFromString(scoreplayerteam.getColorPrefix());

                if (s.length() >= 2)
                {
                    i = this.getFontRendererFromRenderManager().getColorCode(s.charAt(1));
                }
            }
        }

        float f1 = (float)(i >> 16 & 255) / 255.0F;
        float f2 = (float)(i >> 8 & 255) / 255.0F;
        float f = (float)(i & 255) / 255.0F;
        GlStateManager.disableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.color(f1, f2, f, 1.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        return true;
    }

    protected void unsetScoreTeamColor()
    {
        GlStateManager.enableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    protected boolean setBrightness(T entitylivingbaseIn, float partialTicks, boolean combineTextures) {
        float f = entitylivingbaseIn.getBrightness(partialTicks);
        int i = getColorMultiplier(entitylivingbaseIn, f, partialTicks);
        boolean flag = ((i >> 24 & 0xFF) > 0);
        boolean flag1 = (((EntityLivingBase)entitylivingbaseIn).hurtTime > 0 || ((EntityLivingBase)entitylivingbaseIn).deathTime > 0);
        if (!flag && !flag1)
            return false;
        if (!flag && !combineTextures)
            return false;
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, OpenGlHelper.GL_INTERPOLATE);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_CONSTANT);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE2_RGB, OpenGlHelper.GL_CONSTANT);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND2_RGB, 770);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
        this.brightnessBuffer.position(0);
        if (flag1) {
            this.brightnessBuffer.put(1.0F);
            this.brightnessBuffer.put(0.0F);
            this.brightnessBuffer.put(0.0F);
            this.brightnessBuffer.put(0.3F);
        } else {
            float f1 = (i >> 24 & 0xFF) / 255.0F;
            float f2 = (i >> 16 & 0xFF) / 255.0F;
            float f3 = (i >> 8 & 0xFF) / 255.0F;
            float f4 = (i & 0xFF) / 255.0F;
            this.brightnessBuffer.put(f2);
            this.brightnessBuffer.put(f3);
            this.brightnessBuffer.put(f4);
            this.brightnessBuffer.put(1.0F - f1);
        }
        this.brightnessBuffer.flip();
        GL11.glTexEnv(8960, 8705, this.brightnessBuffer);
        GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(field_177096_e.getGlTextureId());
        GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.lightmapTexUnit);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        return true;
    }

    protected void renderModel(T entitylivingbaseIn, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_)
    {
        final boolean flag = !entitylivingbaseIn.isInvisible();
        final boolean flag2 = !flag && !entitylivingbaseIn.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer);
        if (flag || flag2) {
            final boolean colorChams = Chams.mode.is("Blend");
            final boolean chamsRendering = Chams.enabled && Chams.isValid(entitylivingbaseIn);
            if ((!chamsRendering || !colorChams) && !this.bindEntityTexture(entitylivingbaseIn)) {
                return;
            }
            if (flag2) {
                GL11.glPushMatrix();
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.15f);
                GL11.glDepthMask(false);
                GL11.glBlendFunc(770, 771);
            }

            if(ESP.enabled && ESP.isValid(entitylivingbaseIn) && ESP.espMode.is("Wireframe")) {
                GL11.glPushMatrix();
                GL11.glPushAttrib(1048575);
                GL11.glPolygonMode(1032, 6913);
                GL11.glDisable(3553);
                GL11.glDisable(2896);
                GL11.glDisable(2929);
                GL11.glEnable(2848);
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                RenderUtils.glColor(ESP.wireColor.getColor());
                GL11.glLineWidth((float) ESP.wireWidth.getValue());
                this.mainModel.render((Entity)entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
                GL11.glPopAttrib();
                GL11.glPopMatrix();
            }

            if (chamsRendering) {
                if (colorChams) {
                    final boolean visibleFlat = Chams.visibleFlat.isEnabled();
                    final boolean occludedFlat = Chams.occludedFlat.isEnabled();
                    final long currentMillis = System.currentTimeMillis();
                    final int entityId = entitylivingbaseIn.getEntityId();
                    int visibleColor = 0;

                    switch (Chams.visibleColorMode.getMode()) {
                        case "Static": {
                            visibleColor = new Color(Chams.visibleColor.getValue().getRed() / 255f, Chams.visibleColor.getValue().getGreen() / 255f, Chams.visibleColor.getValue().getBlue() / 255f, (float) Chams.visibleAlpha.getValue()).getRGB();
                            break;
                        }

                        case "Rainbow": {
                            visibleColor = RenderUtils.getRainbowFromEntity(currentMillis, 6000, entityId, true, (float) Chams.visibleAlpha.getValue());
                            break;
                        }

                        case "Pulsing": {
                            visibleColor = RenderUtils.fadeBetween(Chams.visibleColor.getColor(), Chams.secondVisibleColor.getColor(), System.currentTimeMillis() % 3000L / 1500.0f);
                            break;
                        }

                        default: {
                            visibleColor = 0;
                            break;
                        }
                    }

                    int occludedColor = 0;

                    switch (Chams.occludedColorMode.getMode()) {
                        case "Static": {
                            occludedColor = new Color(Chams.obstructedColor.getValue().getRed() / 255f, Chams.obstructedColor.getValue().getGreen() / 255f, Chams.obstructedColor.getValue().getBlue() / 255f, (float) Chams.occludedAlpha.getValue()).getRGB();
                            break;
                        }

                        case "Rainbow": {
                            occludedColor = RenderUtils.getRainbowFromEntity(currentMillis, 6000, entityId, false, (float) Chams.occludedAlpha.getValue());
                            break;
                        }

                        case "Pulsing": {
                            occludedColor = RenderUtils.fadeBetween(Chams.obstructedColor.getColor(), Chams.secondObstructedColor.getColor(), System.currentTimeMillis() % 3000L / 1500.0f);
                            break;
                        }

                        default: {
                            occludedColor = 0;
                            break;
                        }
                    }

                    Chams.preRenderOccluded(occludedColor, occludedFlat);
                    this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
                    Chams.preRenderVisible(visibleColor, visibleFlat, occludedFlat);
                    this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
                    Chams.postRender(visibleFlat);
                }
                else {
                    this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
                }
            }
            else {
                this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
            }
            if (flag2) {
                GL11.glDepthMask(true);
                GL11.glPopMatrix();
            }
        }
    }

    protected boolean setDoRenderBrightness(T entityLivingBaseIn, float partialTicks, final boolean customHitColor)
    {
        return this.setBrightness(entityLivingBaseIn, partialTicks, true, customHitColor);
    }

    protected boolean setBrightness(T entitylivingbaseIn, float partialTicks, boolean combineTextures, final boolean customHitColor)
    {
        float f = entitylivingbaseIn.getBrightness(partialTicks);
        int i = this.getColorMultiplier(entitylivingbaseIn, f, partialTicks);
        boolean flag = (i >> 24 & 255) > 0;
        boolean flag1 = entitylivingbaseIn.hurtTime > 0 || entitylivingbaseIn.deathTime > 0;

        if (!flag && !flag1)
        {
            return false;
        }
        else if (!flag && !combineTextures)
        {
            return false;
        }
        else
        {
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableTexture2D();
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_REPLACE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.enableTexture2D();
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, OpenGlHelper.GL_INTERPOLATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_CONSTANT);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE2_RGB, OpenGlHelper.GL_CONSTANT);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND2_RGB, GL11.GL_SRC_ALPHA);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_REPLACE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
            this.brightnessBuffer.position(0);

            if (flag1)
            {
                if(customHitColor) {
                    this.brightnessBuffer.put((float) (Chams.hurtColor.getValue().getRed()));
                    this.brightnessBuffer.put((float) (Chams.hurtColor.getValue().getGreen()));
                    this.brightnessBuffer.put((float) (Chams.hurtColor.getValue().getBlue()));
                    this.brightnessBuffer.put((float) (Chams.hurtAlpha.getValue()));

                    if (Config.isShaders())
                    {
                        Shaders.setEntityColor(1.0F, 0.0F, 0.0F, 0.3F);
                    }
                } else {
                    this.brightnessBuffer.put(1.0f);
                    this.brightnessBuffer.put(0.0f);
                    this.brightnessBuffer.put(0.0f);
                    this.brightnessBuffer.put(0.3f);
                    if (Config.isShaders()) {
                        Shaders.setEntityColor(1.0f, 0.0f, 0.0f, 0.3f);
                    }
                }

            }
            else
            {
                float f1 = (float)(i >> 24 & 255) / 255.0F;
                float f2 = (float)(i >> 16 & 255) / 255.0F;
                float f3 = (float)(i >> 8 & 255) / 255.0F;
                float f4 = (float)(i & 255) / 255.0F;
                this.brightnessBuffer.put(f2);
                this.brightnessBuffer.put(f3);
                this.brightnessBuffer.put(f4);
                this.brightnessBuffer.put(1.0F - f1);

                if (Config.isShaders())
                {
                    Shaders.setEntityColor(f2, f3, f4, 1.0F - f1);
                }
            }

            this.brightnessBuffer.flip();
            GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, (FloatBuffer)this.brightnessBuffer);
            GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
            GlStateManager.enableTexture2D();
            GlStateManager.bindTexture(field_177096_e.getGlTextureId());
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.lightmapTexUnit);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_REPLACE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            return true;
        }
    }

    protected void unsetBrightness()
    {
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_ALPHA, OpenGlHelper.GL_PRIMARY_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA);
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, GL11.GL_TEXTURE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
        GlStateManager.disableTexture2D();
        GlStateManager.bindTexture(0);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, GL11.GL_TEXTURE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        if (Config.isShaders())
        {
            Shaders.setEntityColor(0.0F, 0.0F, 0.0F, 0.0F);
        }
    }

    /**
     * Sets a simple glTranslate on a LivingEntity.
     */
    protected void renderLivingAt(T entityLivingBaseIn, double x, double y, double z)
    {
        GlStateManager.translate((float)x, (float)y, (float)z);
    }

    protected void rotateCorpse(T bat, float p_77043_2_, float p_77043_3_, float partialTicks)
    {
        GlStateManager.rotate(180.0F - p_77043_3_, 0.0F, 1.0F, 0.0F);

        if (bat.deathTime > 0)
        {
            float f = ((float)bat.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt_float(f);

            if (f > 1.0F)
            {
                f = 1.0F;
            }

            GlStateManager.rotate(f * this.getDeathMaxRotation(bat), 0.0F, 0.0F, 1.0F);
        }
        else
        {
            String s = EnumChatFormatting.getTextWithoutFormattingCodes(bat.getName());

            if (s != null && (s.equals("Dinnerbone") || s.equals("Grumm")) && (!(bat instanceof EntityPlayer) || ((EntityPlayer)bat).isWearing(EnumPlayerModelParts.CAPE)))
            {
                GlStateManager.translate(0.0F, bat.height + 0.1F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            }
        }
    }

    /**
     * Returns where in the swing animation the living entity is (from 0 to 1).  Args : entity, partialTickTime
     */
    protected float getSwingProgress(T livingBase, float partialTickTime)
    {
        return livingBase.getSwingProgress(partialTickTime);
    }

    /**
     * Defines what float the third param in setRotationAngles of ModelBase is
     */
    protected float handleRotationFloat(T livingBase, float partialTicks)
    {
        return (float)livingBase.ticksExisted + partialTicks;
    }

    protected void renderLayers(T entitylivingbaseIn, float p_177093_2_, float p_177093_3_, float partialTicks, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_)
    {
        for (LayerRenderer<T> layerrenderer : this.layerRenderers)
        {
            boolean flag = this.setBrightness(entitylivingbaseIn, partialTicks, layerrenderer.shouldCombineTextures(), false);
            layerrenderer.doRenderLayer(entitylivingbaseIn, p_177093_2_, p_177093_3_, partialTicks, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_);

            if (flag)
            {
                this.unsetBrightness();
            }
        }
    }

    protected float getDeathMaxRotation(T entityLivingBaseIn)
    {
        return 90.0F;
    }

    /**
     * Returns an ARGB int color back. Args: entityLiving, lightBrightness, partialTickTime
     */
    protected int getColorMultiplier(T entitylivingbaseIn, float lightBrightness, float partialTickTime)
    {
        return 0;
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(T entitylivingbaseIn, float partialTickTime)
    {
    }

    public void renderName(T entity, double x, double y, double z)
    {
        if (!Reflector.RenderLivingEvent_Specials_Pre_Constructor.exists() || !Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Specials_Pre_Constructor, new Object[] {entity, this, Double.valueOf(x), Double.valueOf(y), Double.valueOf(z)}))
        {

            if(entity instanceof EntityPlayer) {
                EventRenderNametag eventRenderNametag = new EventRenderNametag();
                Resolute.onEvent(eventRenderNametag);
                if(eventRenderNametag.isCancelled()) {
                    return;
                }
            }

            if (this.canRenderName(entity) && renderNametags)
            {
                double d0 = entity.getDistanceSqToEntity(this.renderManager.livingPlayer);
                float f = entity.isSneaking() ? NAME_TAG_RANGE_SNEAK : NAME_TAG_RANGE;

                if (d0 < (double)(f * f))
                {
                    String s = entity.getDisplayName().getFormattedText();
                    float f1 = 0.02666667F;
                    GlStateManager.alphaFunc(516, 0.1F);

                    if (entity.isSneaking())
                    {
                        FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
                        GlStateManager.pushMatrix();
                        GlStateManager.translate((float)x, (float)y + entity.height + 0.5F - (entity.isChild() ? entity.height / 2.0F : 0.0F), (float)z);
                        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                        GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                        GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                        GlStateManager.scale(-0.02666667F, -0.02666667F, 0.02666667F);
                        GlStateManager.translate(0.0F, 9.374999F, 0.0F);
                        GlStateManager.disableLighting();
                        GlStateManager.depthMask(false);
                        GlStateManager.enableBlend();
                        GlStateManager.disableTexture2D();
                        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                        int i = fontrenderer.getStringWidth(s) / 2;
                        Tessellator tessellator = Tessellator.getInstance();
                        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                        worldrenderer.pos((double)(-i - 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                        worldrenderer.pos((double)(-i - 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                        worldrenderer.pos((double)(i + 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                        worldrenderer.pos((double)(i + 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                        tessellator.draw();
                        GlStateManager.enableTexture2D();
                        GlStateManager.depthMask(true);
                        fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, 0, 553648127);
                        GlStateManager.enableLighting();
                        GlStateManager.disableBlend();
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        GlStateManager.popMatrix();
                    }
                    else
                    {
                        this.renderOffsetLivingLabel(entity, x, y - (entity.isChild() ? (double)(entity.height / 2.0F) : 0.0D), z, s, 0.02666667F, d0);
                    }
                }
            }

            if (!Reflector.RenderLivingEvent_Specials_Post_Constructor.exists() || !Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Specials_Post_Constructor, new Object[] {entity, this, Double.valueOf(x), Double.valueOf(y), Double.valueOf(z)}))
            {
                ;
            }
        }
    }

    protected boolean canRenderName(T entity)
    {
        EntityPlayerSP entityplayersp = Minecraft.getMinecraft().thePlayer;

        if (entity instanceof EntityPlayer && entity != entityplayersp)
        {
            Team team = entity.getTeam();
            Team team1 = entityplayersp.getTeam();

            if (team != null)
            {
                Team.EnumVisible team$enumvisible = team.getNameTagVisibility();

                switch (RendererLivingEntity.RendererLivingEntity$1.field_178679_a[team$enumvisible.ordinal()])
                {
                    case 1:
                        return true;

                    case 2:
                        return false;

                    case 3:
                        return team1 == null || team.isSameTeam(team1);

                    case 4:
                        return team1 == null || !team.isSameTeam(team1);

                    default:
                        return true;
                }
            }
        }

        return Minecraft.isGuiEnabled() && entity != this.renderManager.livingPlayer && !entity.isInvisibleToPlayer(entityplayersp) && entity.riddenByEntity == null;
    }

    public void setRenderOutlines(boolean renderOutlinesIn)
    {
        this.renderOutlines = renderOutlinesIn;
    }

    static
    {
        int[] aint = field_177096_e.getTextureData();

        for (int i = 0; i < 256; ++i)
        {
            aint[i] = -1;
        }

        field_177096_e.updateDynamicTexture();
    }

    static final class RendererLivingEntity$1
    {
        static final int[] field_178679_a = new int[Team.EnumVisible.values().length];
        private static final String __OBFID = "CL_00002435";

        static
        {
            try
            {
                field_178679_a[Team.EnumVisible.ALWAYS.ordinal()] = 1;
            }
            catch (NoSuchFieldError var4)
            {
                ;
            }

            try
            {
                field_178679_a[Team.EnumVisible.NEVER.ordinal()] = 2;
            }
            catch (NoSuchFieldError var3)
            {
                ;
            }

            try
            {
                field_178679_a[Team.EnumVisible.HIDE_FOR_OTHER_TEAMS.ordinal()] = 3;
            }
            catch (NoSuchFieldError var2)
            {
                ;
            }

            try
            {
                field_178679_a[Team.EnumVisible.HIDE_FOR_OWN_TEAM.ordinal()] = 4;
            }
            catch (NoSuchFieldError var1)
            {
                ;
            }
        }
    }
}
