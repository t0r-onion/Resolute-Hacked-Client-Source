package net.minecraft.client.renderer;

import vip.Resolute.modules.impl.combat.KillAura;
import vip.Resolute.modules.impl.render.Animation;
import vip.Resolute.modules.impl.render.Chams;
import vip.Resolute.util.misc.TimerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import optifine.Config;
import optifine.DynamicLights;
import optifine.Reflector;

import org.lwjgl.opengl.GL11;
import shadersmod.client.Shaders;

public class ItemRenderer
{
    private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    private static final ResourceLocation RES_UNDERWATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");

    /** A reference to the Minecraft object. */
    private final Minecraft mc;
    private ItemStack itemToRender;

    /**
     * How far the current item has been equipped (0 disequipped and 1 fully up)
     */
    private float equippedProgress;
    private float prevEquippedProgress;
    private final RenderManager renderManager;
    private final RenderItem itemRenderer;

    private TimerUtils rotateTimer = new TimerUtils();
    private float delay = 0;

    /** The index of the currently held item (0-8, or -1 if not yet updated) */
    private int equippedItemSlot = -1;
    private static final String __OBFID = "CL_00000953";

    public ItemRenderer(Minecraft mcIn)
    {
        this.mc = mcIn;
        this.renderManager = mcIn.getRenderManager();
        this.itemRenderer = mcIn.getRenderItem();
    }

    public void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform)
    {
        if (heldStack != null)
        {
            Item item = heldStack.getItem();
            Block block = Block.getBlockFromItem(item);
            GlStateManager.pushMatrix();

            if (this.itemRenderer.shouldRenderItemIn3D(heldStack))
            {
                GlStateManager.scale(2.0F, 2.0F, 2.0F);

                if (this.isBlockTranslucent(block) && (!Config.isShaders() || !Shaders.renderItemKeepDepthMask))
                {
                    GlStateManager.depthMask(false);
                }
            }

            this.itemRenderer.renderItemModelForEntity(heldStack, entityIn, transform);

            if (this.isBlockTranslucent(block))
            {
                GlStateManager.depthMask(true);
            }

            GlStateManager.popMatrix();
        }
    }

    /**
     * Returns true if given block is translucent
     */
    private boolean isBlockTranslucent(Block blockIn)
    {
        return blockIn != null && blockIn.getBlockLayer() == EnumWorldBlockLayer.TRANSLUCENT;
    }

    private void func_178101_a(float angle, float p_178101_2_)
    {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(angle, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(p_178101_2_, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private void func_178109_a(AbstractClientPlayer clientPlayer)
    {
        int i = this.mc.theWorld.getCombinedLight(new BlockPos(clientPlayer.posX, clientPlayer.posY + (double)clientPlayer.getEyeHeight(), clientPlayer.posZ), 0);

        if (Config.isDynamicLights())
        {
            i = DynamicLights.getCombinedLight(this.mc.getRenderViewEntity(), i);
        }

        float f = (float)(i & 65535);
        float f1 = (float)(i >> 16);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
    }

    private void func_178110_a(EntityPlayerSP entityplayerspIn, float partialTicks)
    {
        float f = entityplayerspIn.prevRenderArmPitch + (entityplayerspIn.renderArmPitch - entityplayerspIn.prevRenderArmPitch) * partialTicks;
        float f1 = entityplayerspIn.prevRenderArmYaw + (entityplayerspIn.renderArmYaw - entityplayerspIn.prevRenderArmYaw) * partialTicks;
        GlStateManager.rotate((entityplayerspIn.rotationPitch - f) * 0.1F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((entityplayerspIn.rotationYaw - f1) * 0.1F, 0.0F, 1.0F, 0.0F);
    }

    private float func_178100_c(float p_178100_1_)
    {
        float f = 1.0F - p_178100_1_ / 45.0F + 0.1F;
        f = MathHelper.clamp_float(f, 0.0F, 1.0F);
        f = -MathHelper.cos(f * (float)Math.PI) * 0.5F + 0.5F;
        return f;
    }

    private void renderRightArm(RenderPlayer renderPlayerIn)
    {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(54.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(64.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-62.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0.25F, -0.85F, 0.75F);
        renderPlayerIn.renderRightArm(this.mc.thePlayer);
        GlStateManager.popMatrix();
    }

    private void renderLeftArm(RenderPlayer renderPlayerIn)
    {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(92.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(41.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(-0.3F, -1.1F, 0.45F);
        renderPlayerIn.renderLeftArm(this.mc.thePlayer);
        GlStateManager.popMatrix();
    }

    private void renderPlayerArms(AbstractClientPlayer clientPlayer)
    {
        this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
        Render render = this.renderManager.getEntityRenderObject(this.mc.thePlayer);
        RenderPlayer renderplayer = (RenderPlayer)render;

        if (!clientPlayer.isInvisible())
        {
            GlStateManager.disableCull();
            this.renderRightArm(renderplayer);
            this.renderLeftArm(renderplayer);
            GlStateManager.enableCull();
        }
    }

    private void renderItemMap(AbstractClientPlayer clientPlayer, float p_178097_2_, float p_178097_3_, float p_178097_4_)
    {
        float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(p_178097_4_) * (float)Math.PI);
        float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(p_178097_4_) * (float)Math.PI * 2.0F);
        float f2 = -0.2F * MathHelper.sin(p_178097_4_ * (float)Math.PI);
        GlStateManager.translate(f, f1, f2);
        float f3 = this.func_178100_c(p_178097_2_);
        GlStateManager.translate(0.0F, 0.04F, -0.72F);
        GlStateManager.translate(0.0F, p_178097_3_ * -1.2F, 0.0F);
        GlStateManager.translate(0.0F, f3 * -0.5F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f3 * -85.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
        this.renderPlayerArms(clientPlayer);
        float f4 = MathHelper.sin(p_178097_4_ * p_178097_4_ * (float)Math.PI);
        float f5 = MathHelper.sin(MathHelper.sqrt_float(p_178097_4_) * (float)Math.PI);
        GlStateManager.rotate(f4 * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f5 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f5 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.38F, 0.38F, 0.38F);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-1.0F, -1.0F, 0.0F);
        GlStateManager.scale(0.015625F, 0.015625F, 0.015625F);
        this.mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GL11.glNormal3f(0.0F, 0.0F, -1.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        MapData mapdata = Items.filled_map.getMapData(this.itemToRender, this.mc.theWorld);

        if (mapdata != null)
        {
            this.mc.entityRenderer.getMapItemRenderer().renderMap(mapdata, false);
        }
    }

    private void func_178095_a(AbstractClientPlayer clientPlayer, float p_178095_2_, float p_178095_3_)
    {
        float f = -0.3F * MathHelper.sin(MathHelper.sqrt_float(p_178095_3_) * (float)Math.PI);
        float f1 = 0.4F * MathHelper.sin(MathHelper.sqrt_float(p_178095_3_) * (float)Math.PI * 2.0F);
        float f2 = -0.4F * MathHelper.sin(p_178095_3_ * (float)Math.PI);
        GlStateManager.translate(f, f1, f2);
        GlStateManager.translate(0.64000005F, -0.6F, -0.71999997F);
        GlStateManager.translate(0.0F, p_178095_2_ * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f3 = MathHelper.sin(p_178095_3_ * p_178095_3_ * (float)Math.PI);
        float f4 = MathHelper.sin(MathHelper.sqrt_float(p_178095_3_) * (float)Math.PI);
        GlStateManager.rotate(f4 * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f3 * -20.0F, 0.0F, 0.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
        GlStateManager.translate(-1.0F, 3.6F, 3.5F);
        GlStateManager.rotate(120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F);
        GlStateManager.translate(5.6F, 0.0F, 0.0F);
        Render render = this.renderManager.getEntityRenderObject(this.mc.thePlayer);
        GlStateManager.disableCull();
        RenderPlayer renderplayer = (RenderPlayer)render;

        if(Chams.enabled) {
            if(Chams.handsProp.isEnabled()) {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glColor4f(Chams.handsColor.getValue().getRed() / 255f, Chams.handsColor.getValue().getGreen() / 255f, Chams.handsColor.getValue().getBlue() / 255f, (float) Chams.handsAlpha.getValue());
            }

            renderplayer.renderRightArm(this.mc.thePlayer);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
        } else {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            renderplayer.renderRightArm(this.mc.thePlayer);
        }


        GlStateManager.enableCull();
    }

    private void func_178105_d(float p_178105_1_)
    {
        float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(p_178105_1_) * (float)Math.PI);
        float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(p_178105_1_) * (float)Math.PI * 2.0F);
        float f2 = -0.2F * MathHelper.sin(p_178105_1_ * (float)Math.PI);
        GlStateManager.translate(f, f1, f2);
    }

    private void func_178104_a(AbstractClientPlayer clientPlayer, float p_178104_2_)
    {
        float f = (float)clientPlayer.getItemInUseCount() - p_178104_2_ + 1.0F;
        float f1 = f / (float)this.itemToRender.getMaxItemUseDuration();
        float f2 = MathHelper.abs(MathHelper.cos(f / 4.0F * (float)Math.PI) * 0.1F);

        if (f1 >= 0.8F)
        {
            f2 = 0.0F;
        }

        GlStateManager.translate(0.0F, f2, 0.0F);
        float f3 = 1.0F - (float)Math.pow((double)f1, 27.0D);
        GlStateManager.translate(f3 * 0.6F, f3 * -0.5F, f3 * 0.0F);
        GlStateManager.rotate(f3 * 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f3 * 10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f3 * 30.0F, 0.0F, 0.0F, 1.0F);
    }

    /**
     * Performs transformations prior to the rendering of a held item in first person.
     */
    private void transformFirstPersonItem(float equipProgress, float swingProgress)
    {
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);

        if(Animation.mode.is("1.7")) {
            GlStateManager.rotate(f1 * -50.0F, 1.0F, 0.0F, 0.0F);
        } else {
            GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
        }


        float scale = Animation.enabled ? (float) Animation.scale.getValue() : 0.4f;

        GlStateManager.scale(scale, scale, scale);
    }

    private void func_178098_a(float p_178098_1_, AbstractClientPlayer clientPlayer)
    {
        GlStateManager.rotate(-18.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-12.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-8.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-0.9F, 0.2F, 0.0F);
        float f = (float)this.itemToRender.getMaxItemUseDuration() - ((float)clientPlayer.getItemInUseCount() - p_178098_1_ + 1.0F);
        float f1 = f / 20.0F;
        f1 = (f1 * f1 + f1 * 2.0F) / 3.0F;

        if (f1 > 1.0F)
        {
            f1 = 1.0F;
        }

        if (f1 > 0.1F)
        {
            float f2 = MathHelper.sin((f - 0.1F) * 1.3F);
            float f3 = f1 - 0.1F;
            float f4 = f2 * f3;
            GlStateManager.translate(f4 * 0.0F, f4 * 0.01F, f4 * 0.0F);
        }

        GlStateManager.translate(f1 * 0.0F, f1 * 0.0F, f1 * 0.1F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F + f1 * 0.2F);
    }

    private void func_178103_d()
    {
        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }

    private void monsoon(float p_178096_1_, float p_178096_2_) {
        GlStateManager.translate(0.26F, -0.18F, -0.71999997F);
        GlStateManager.translate(0.0F, p_178096_1_ * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float var3 = MathHelper.sin(p_178096_2_ * p_178096_2_ * 3.1415927F);
        float var4 = MathHelper.sin(MathHelper.sqrt_float(p_178096_2_) * 3.1415927F);
        GlStateManager.rotate(var3 * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(var4 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(var4 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.2F, 0.2F, 0.2F);
    }

    private void func_178096_b(float p_178096_1_, float p_178096_2_) {
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, p_178096_1_ * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float var3 = MathHelper.sin(p_178096_2_ * p_178096_2_ * (float) Math.PI);
        float var4 = MathHelper.sin(MathHelper.sqrt_float(p_178096_2_) * (float) Math.PI);
        GlStateManager.rotate(var3 * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(var4 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(var4 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }


    /**
     * Renders the active item in the player's hand when in first person mode. Args: partialTickTime
     */
    public void renderItemInFirstPerson(float partialTicks)
    {
        float f = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
        EntityPlayerSP entityplayersp = this.mc.thePlayer;
        float f1 = entityplayersp.getSwingProgress(partialTicks);
        float f2 = entityplayersp.prevRotationPitch + (entityplayersp.rotationPitch - entityplayersp.prevRotationPitch) * partialTicks;
        float f3 = entityplayersp.prevRotationYaw + (entityplayersp.rotationYaw - entityplayersp.prevRotationYaw) * partialTicks;
        this.func_178101_a(f2, f3);
        this.func_178109_a(entityplayersp);
        this.func_178110_a(entityplayersp, partialTicks);
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();

        //if(mc.thePlayer.getCurrentEquippedItem() != null)
        if(mc.thePlayer.getHeldItem() != null && Animation.onItem.isEnabled())
            GL11.glTranslated(Animation.xPos.getValue(), Animation.yPos.getValue(), Animation.zPos.getValue());

         if(!Animation.onItem.isEnabled())
            GL11.glTranslated(Animation.xPos.getValue(), Animation.yPos.getValue(), Animation.zPos.getValue());



        if (this.itemToRender != null)
        {

            if (this.itemToRender.getItem() instanceof ItemMap)
            {
                this.renderItemMap(entityplayersp, f2, f, f1);
            }

            else if (entityplayersp.getItemInUseCount() > 0 || KillAura.blocking)
            {

                EnumAction enumaction = this.itemToRender.getItemUseAction();

                switch (ItemRenderer.ItemRenderer$1.field_178094_a[enumaction.ordinal()])
                {
                    case 1:
                        this.transformFirstPersonItem(f, 0.0F);
                        break;

                    case 2:
                    case 3:
                        this.func_178104_a(entityplayersp, partialTicks);
                        this.transformFirstPersonItem(f, 0.0F);
                        break;

                    case 4:

                        float eqiupProgress;
                        eqiupProgress = 0.0f;

                        float var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * (float) Math.PI);
                        float var9 = MathHelper.sin(MathHelper.sqrt_float(f1) * MathHelper.PI);

                        if(Animation.mode.is("Resolute")) {
                            this.transformFirstPersonItem(f / 2, f1);
                            GlStateManager.translate(0, 0.3, 0);
                        }

                        if(Animation.mode.is("Swing")) {
                            this.func_178096_b(f / 2.0F, f1);
                        }

                        if(Animation.mode.is("Swang")) {
                            this.func_178096_b(f / 2.0F, f1);
                            var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                            GlStateManager.rotate(var15 * 30.0F / 2.0F, -var15, -0.0F, 9.0F);
                            GlStateManager.rotate(var15 * 40.0F, 1.0F, -var15 / 2.0F, -0.0F);
                        }

                        if(Animation.mode.is("Swong")) {
                            this.func_178096_b(f / 2.0F, 0.0F);
                            var15 = MathHelper.sin(f1 * f1 * 3.1415927F);
                            GlStateManager.rotate(-var15 * 40.0F / 2.0F, var15 / 2.0F, -0.0F, 9.0F);
                            GlStateManager.rotate(-var15 * 30.0F, 1.0F, var15 / 2.0F, -0.0F);
                        }

                        if(Animation.mode.is("Swank")) {
                            this.func_178096_b(f / 2.0F, f1);
                            var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                            GlStateManager.rotate(var15 * 30.0F, -var15, -0.0F, 9.0F);
                            GlStateManager.rotate(var15 * 40.0F, 1.0F, -var15, -0.0F);
                        }

                        if(Animation.mode.is("Old")) {
                            this.transformFirstPersonItem(eqiupProgress, f1);
                        }

                        if(Animation.mode.is("Astro")) {
                            this.transformFirstPersonItem(eqiupProgress, f1);
                            GL11.glRotatef(var9 * 50.0F / 9.0F, -var9, 0.0F, 90.0F);
                            GL11.glRotatef(var9 * 50.0F, 200.0F, -var9 / 2.0F, 0.0F);
                        }

                        if(Animation.mode.is("Exhibition")) {
                            this.transformFirstPersonItem(eqiupProgress, 0);
                            GL11.glRotatef(-var9 * 40.0F / 2.0F, var9 / 2.0F, -0.0F, 9.0F);
                            GL11.glRotatef(-var9 * 30.0F, 1.0F, var9 / 2.0F, -0.0F);
                        }

                        if(Animation.mode.is("Exhibobo")) {
                            this.transformFirstPersonItem(eqiupProgress, 0.0f);
                            GL11.glTranslatef(0.1F, 0.4F, -0.1F);
                            GL11.glRotated(-var9 * 20.0F, var9 / 2, 0.0F, 9.0F);
                            GL11.glRotated(-var9 * 50.0F, 0.8F, var9 / 2, 0F);
                        }

                        if(Animation.mode.is("Slide")) {
                            this.transformFirstPersonItem(f, 0.0F);
                        }

                        this.func_178103_d();

                        if(Animation.mode.is("Slide")) {
                            GL11.glTranslated(-0.3D, 0.3D, 0.0D);
                            GL11.glRotatef(-var9 * 70.0F / 2.0F, -8.0F, 0.0F, 9.0F);
                            GL11.glRotatef(-var9 * 70.0F, 1.0F, -0.4F, -0.0F);
                        }

                        if (enumaction == EnumAction.BLOCK) {
                            GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
                            GL11.glRotatef(10.0F, 1.0F, 0.0F, 0.0F);
                            GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
                            GL11.glTranslatef(-0.05F, this.mc.thePlayer.isSneaking() ? -0.2F : 0.0F, 0.1F);
                        }

                        /*
                        if(Animation.mode.is("1.7")) {
                            float f8 = (float)Math.sin(Math.sqrt(f1) * Math.PI);
                            this.transformFirstPersonItem( eqiupProgress, 0.0f);
                            GL11.glRotated((-f8 * 40.0F), (f8 / 2.0F), 0.0D, 3.0D);
                            GL11.glRotated((-f8 * 50.0F), 0.800000011920929D, (f8 / 2.0F), 0.0D);
                            GL11.glRotated((-f8 * 50.0F / 4.0F), 0.0D, 0.0D, 1.0D);
                            func_178103_d();
                        }

                        if(Animation.mode.is("Ethereal")) {
                            this.transformFirstPersonItem(0, 1);
                            GlStateManager.rotate(var9 * 35, -0.1f, 0.1f, 0.1f);
                            GlStateManager.rotate(-20, 1.0F, -0.5f, 1F);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Resolute")) {
                            this.transformFirstPersonItem(f / 2, f1);
                            GlStateManager.translate(0, 0.3, 0);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Gravity")) {
                            float f16 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                            GL11.glTranslated(0.3D, 0.1D, -0.3D);
                            this.transformFirstPersonItem(f - 0.2F, 4.5F);
                            GlStateManager.rotate(-f16 * 20.0F, 1000.0F, 100.0F, -1000.0F);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Down")) {
                            this.transformFirstPersonItem(eqiupProgress, 0.0f);
                            GL11.glTranslatef(0.1f, 0.4f, -0.1f);
                            GL11.glRotated((double)(-var9 * 20.0f), (double)(var9 / 2.0f), 0.0, 9.0);
                            GL11.glRotated((double)(-var9 * 50.0f), 0.800000011920929, (double)(var9 / 2.0f), 0.0);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("ETB")) {
                            this.ETB(f, f1);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Autumn")) {
                            float var92 = MathHelper.sin(MathHelper.sqrt_float(f1) * (float) Math.PI);
                            this.transformFirstPersonItem(f / 2.0f, 0.0f);
                            GL11.glRotatef((float) (-var92 * 40.0f / 2.0f), (float) (var92 / 2.0f), (float) -0.0f,
                                    (float) 9.0f);
                            GL11.glRotatef((float) (-var92 * 30.0f), (float) 1.0f, (float) (var92 / 2.0f),
                                    (float) -0.0f);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Monsoon")) {
                            monsoon(f, 0.0F);
                            GlStateManager.scale(2.0F, 2.0F, 2.0F);
                            func_178103_d();
                            float var10 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                            GlStateManager.translate(1.0D, -0.8D, 1.0D);
                            GlStateManager.rotate(-var10 * 50.0F / 2.0F, -8.0F, 0.4F, 9.0F);
                            GlStateManager.rotate(-var10 * 40.0F, 1.5F, -0.4F, -0.0F);
                        }

                        if(Animation.mode.is("Twist")) {
                            transformFirstPersonItem(0.2F, f1);
                            GlStateManager.translate(-0.2F, 0.6F, 0.0F);
                            func_178103_d();
                        }

                        if(Animation.mode.is("Smooth")) {
                            this.transformFirstPersonItem(eqiupProgress, f1);

                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Astolfo")) {
                            astolfoCircle(mc.thePlayer.getSwingProgress(f1));
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Sigma")) {
                            float f8 = MathHelper.sin((float) (MathHelper.sqrt_float(f1) * Math.PI));
                            GlStateManager.translate(0.0F, 0.135F, 0.0F);
                            this.transformFirstPersonItem(eqiupProgress / 2F, 0.0F);
                            GL11.glRotated(-f8 * 15.0F, f8 / 2, 0.0F, 9.0F);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Swank")) {
                            GL11.glTranslated(-0.1F, 0.15F, 0.0F);
                            this.transformFirstPersonItem(eqiupProgress / 2.0F, f1);
                            var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                            GlStateManager.rotate(var15 * 30.0F, -var15, -0.0F, 9.0F);
                            GlStateManager.rotate(var15 * 40.0F, 1.0F, -var15, -0.0F);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Swang")) {
                            GL11.glTranslated(-0.1F, 0.12F, 0.0F);
                            this.transformFirstPersonItem(eqiupProgress / 2.0F, f1);
                            var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                            GlStateManager.rotate(var15 * 30.0F / 2.0F, -var15, -0.0F, 9.0F);
                            GlStateManager.rotate(var15 * 40.0F, 1.0F, -var15 / 2.0F, -0.0F);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Tap")) {
                            this.tap(eqiupProgress, f1);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Tap2")) {
                            this.tap2(eqiupProgress, f1);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Impulse")) {
                            this.transformFirstPersonItem( 0.0F, eqiupProgress);
                            this.func_178103_d();
                            final float var99 = (float) Math.sin(Math.sqrt(f1) * 3.1415927f);
                            GlStateManager.translate(0f, 0.6f, -0.2f);
                            GlStateManager.rotate(-var99 * (float) 70 / 1.78789789f, -8.0f, -0.0f, 9.0f);
                            GlStateManager.rotate(-var99 * (float) 70, 1.5f, -0.5f, -0.2f);
                            break;
                        }
                        if(Animation.mode.is("1.8")) {
                            this.transformFirstPersonItem(eqiupProgress, 0.0F);
                            this.func_178103_d();
                            break;
                        }
                        if(Animation.mode.is("Exhibition")) {
                            float exhi = MathHelper.sin((float) (MathHelper.sqrt_float(f1) * 3.0));
                            this.transformFirstPersonItem(eqiupProgress, 0.0F);
                            GlStateManager.translate(0.1F, 0.2F, -0.1F);
                            GL11.glRotated(-exhi * 23.0F, exhi / 1.5, 0.0F, 4.0F);
                            GL11.glRotated(-exhi * 35.0F, 0.8F, exhi / 1.5, 0F);
                            this.func_178103_d();
                            break;
                        }

                        if(Animation.mode.is("Light")) {
                            this.transformFirstPersonItem(eqiupProgress, f1);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Exhibition2")) {
                            float f81 = MathHelper.sin((float) (MathHelper.sqrt_float(f1) * 3.0));
                            this.transformFirstPersonItem(eqiupProgress, 0.0f);
                            GlStateManager.translate(0.1F, 0.4F, -0.1F);
                            GL11.glRotated(-f81 * 20.0F, f81 / 2, 0.0F, 9.0F);
                            GL11.glRotated(-f81 * 50.0F, 0.8F, f81 / 2, 0F);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Old")) {
                            this.func_178096_b(-0.25F, 0.0F);
                            this.oldAnim(eqiupProgress, f1);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Astro")) {
                            this.transformFirstPersonItem(0, f1);
                            GL11.glRotatef(var9 * 50.0F / 9.0F, -var9, 0.0F, 90.0F);
                            GL11.glRotatef(var9 * 50.0F, 200.0F, -var9 / 2.0F, 0.0F);
                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Remix")) {
                            this.transformFirstPersonItem(eqiupProgress / 2, 0);
                            this.func_178103_d();
                            GL11.glRotatef(-var9 * (float) 50.0 / 2.0f, -8.0f, 0.4f, 9.0f);
                            GL11.glRotatef(-var9 * (float) 40, 1.5f, -0.4f, -0.0f);
                            break;
                        }
                        if(Animation.mode.is("Stab")) {
                            final float var20 = (float) Math.sin(Math.sqrt(f1) * 3.1415927f);
                            GlStateManager.translate(0.6f, 0.3f, -0.6f + -var20 * 0.7);
                            GlStateManager.rotate(6090 ,0.0f, 0.0f, 0.1f);
                            GlStateManager.rotate(6085, 0.0f, 0.1f, 0.0f);
                            GlStateManager.rotate(6110 ,0.1f, 0.0f, 0.0f);
                            this.transformFirstPersonItem( 0.0F, 0.0f);
                            this.func_178103_d();
                            break;
                        }

                        if(Animation.mode.is("Spin")) {
                            GlStateManager.translate(0, 0.1, -0.1);

                            this.transformFirstPersonItem(eqiupProgress / 2F, 0.0f);
                            GlStateManager.rotate(45, -0.0f, 0.0f, -0.1f);
                            var15 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.0F);
                            GlStateManager.rotate(delay, -0.1f, 0.1f, -0.1f);
                            if(delay > 360){
                                delay = 0;
                            }
                            for(int i = 0; i < 1; i++) {
                                ++this.delay;
                            }

                            this.func_178103_d();
                        }

                        if(Animation.mode.is("Summer")) {
                            this.transformFirstPersonItem(eqiupProgress, 0.0F);
                            this.func_178103_d();
                            float var14 = MathHelper.sin(f1 * f1 * 3.1415927F);
                            float var16 = MathHelper.sin(MathHelper.sqrt_float(f1) * 3.1415927F);
                            GlStateManager.translate(-0.0F, 0.4F, 0.3F);
                            GlStateManager.rotate(-var16 * 35.0F, -8.0F, -0.0F, 9.0F);
                            GlStateManager.rotate(-var16 * 10.0F, 1.0F, -0.4F, -0.5F);
                        }

                         */



                        break;

                    case 5:
                        this.transformFirstPersonItem(f, 0.0F);
                        this.func_178098_a(partialTicks, entityplayersp);
                }
            }
            else
            {

                if(Animation.smoothHitting.isEnabled()) {
                    this.func_178096_b(f * 0.5F, f1);
                } else {
                    this.func_178105_d(f1);
                    this.transformFirstPersonItem(f, f1);

                }
            }

            this.renderItem(entityplayersp, this.itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
        }
        else if (!entityplayersp.isInvisible())
        {
            this.func_178095_a(entityplayersp, f, f1);
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }

    private void tap(float var2, float swingProgress) {
        float smooth = (swingProgress * 0.8f - (swingProgress * swingProgress) * 0.8f);
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, var2 * -0.15F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float var3 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float var4 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(smooth * -90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.37F, 0.37F, 0.37F);
    }

    private void tap2(float var2, float swing) {
        float var3 = MathHelper.sin(swing * swing * (float) Math.PI);
        float var4 = MathHelper.sin(MathHelper.sqrt_float(swing) * (float) Math.PI);
        GlStateManager.translate(0.56F, -0.42F, -0.71999997F);
        GlStateManager.translate(0.0F,  var2 * -0.15F, 0.0F);
        GlStateManager.rotate(30 , 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(var4 * -30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }

    private void ETB(float p_178096_1_, float p_178096_2_) {
        GlStateManager.translate(0.56f, -0.52f, -0.71999997f);
        GlStateManager.translate(0.0f, p_178096_1_ * -0.6f, 0.0f);
        GlStateManager.rotate(45.0f, 0.0f, 1.0f, 0.0f);
        float var3 = MathHelper.sin(p_178096_2_ * p_178096_2_ * 3.1415927f);
        float var4 = MathHelper.sin(MathHelper.sqrt_float(p_178096_2_) * 3.1415927f);
        GlStateManager.rotate(var3 * -34.0f, 0.0f, 1.0f, 0.2f);
        GlStateManager.rotate(var4 * -20.7f, 0.2f, 0.1f, 1.0f);
        GlStateManager.rotate(var4 * -68.6f, 1.3f, 0.1f, 0.2f);
        GlStateManager.scale(0.4f, 0.4f, 0.4f);
    }

    private static transient int astolfoTicks = 0;

    private void astolfoCircle(float swingProgress) {
        float f1 = MathHelper.sin((float) (MathHelper.sqrt_float(swingProgress) * Math.PI));
        this.astolfoTicks++;
        GlStateManager.translate(-0.0F, -0.2F, -0.6F);
        GlStateManager.rotate(-this.astolfoTicks * 0.07F * 30.0F, 0.0F, 0.0F, -1.0F);
        GlStateManager.rotate(44.0F, 0.0F, 1.0F, 0.6F);
        GlStateManager.rotate(44.0F, 1.0F, 0.0F, -0.6F);
        GlStateManager.translate(1.0F, -0.2F, 0.5F);
        GlStateManager.rotate(-44.0F, 1.0F, 0.0F, -0.6F);
        GlStateManager.scale(0.5D, 0.5D, 0.5D);
    }

    private void oldAnim(final float equipProgress, final float swingProgress) {
        final float smooth = swingProgress * 0.78f - swingProgress * swingProgress * 0.78f;
        GlStateManager.scale(1.7f, 1.7f, 1.7f);
        GlStateManager.rotate(48.0f, 0.0f, -0.6f, 0.0f);
        GlStateManager.translate(-0.3f, 0.4f, 0.0f);
        GlStateManager.translate(0.0f, 0.08f, 0.0f);
        GlStateManager.translate(0.56f, -0.489f, -0.71999997f);
        GlStateManager.translate(0.0f, 0.0f, 0.0f);
        GlStateManager.rotate(52.0f, 0.0f, 180.0f + smooth * 0.5f, smooth * 20.0f);
        final float f = MathHelper.sin(swingProgress * swingProgress * 3.1415927f);
        final float f2 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927f);
        GlStateManager.rotate(f2 * -51.3f, 2.0f, 0.0f, 0.0f);
        GlStateManager.translate(0.0f, -0.2f, 0.0f);
    }

    /**
     * Renders all the overlays that are in first person mode. Args: partialTickTime
     */
    public void renderOverlays(float partialTicks)
    {
        GlStateManager.disableAlpha();

        if (this.mc.thePlayer.isEntityInsideOpaqueBlock())
        {
            IBlockState iblockstate = this.mc.theWorld.getBlockState(new BlockPos(this.mc.thePlayer));
            BlockPos blockpos = new BlockPos(this.mc.thePlayer);
            EntityPlayerSP entityplayersp = this.mc.thePlayer;

            for (int i = 0; i < 8; ++i)
            {
                double d0 = entityplayersp.posX + (double)(((float)((i >> 0) % 2) - 0.5F) * entityplayersp.width * 0.8F);
                double d1 = entityplayersp.posY + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
                double d2 = entityplayersp.posZ + (double)(((float)((i >> 2) % 2) - 0.5F) * entityplayersp.width * 0.8F);
                BlockPos blockpos1 = new BlockPos(d0, d1 + (double)entityplayersp.getEyeHeight(), d2);
                IBlockState iblockstate1 = this.mc.theWorld.getBlockState(blockpos1);

                if (iblockstate1.getBlock().isVisuallyOpaque())
                {
                    iblockstate = iblockstate1;
                    blockpos = blockpos1;
                }
            }

            if (iblockstate.getBlock().getRenderType() != -1)
            {
                Object object = Reflector.getFieldValue(Reflector.RenderBlockOverlayEvent_OverlayType_BLOCK);

                if (!Reflector.callBoolean(Reflector.ForgeEventFactory_renderBlockOverlay, new Object[] {this.mc.thePlayer, Float.valueOf(partialTicks), object, iblockstate, blockpos}))
                {
                    this.func_178108_a(partialTicks, this.mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(iblockstate));
                }
            }
        }

        if (!this.mc.thePlayer.isSpectator())
        {
            if (this.mc.thePlayer.isInsideOfMaterial(Material.water) && !Reflector.callBoolean(Reflector.ForgeEventFactory_renderWaterOverlay, new Object[] {this.mc.thePlayer, Float.valueOf(partialTicks)}))
            {
                this.renderWaterOverlayTexture(partialTicks);
            }

            if (this.mc.thePlayer.isBurning() && !Reflector.callBoolean(Reflector.ForgeEventFactory_renderFireOverlay, new Object[] {this.mc.thePlayer, Float.valueOf(partialTicks)}))
            {
                this.renderFireInFirstPerson(partialTicks);
            }
        }

        GlStateManager.enableAlpha();
    }

    private void func_178108_a(float p_178108_1_, TextureAtlasSprite p_178108_2_)
    {
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        float f = 0.1F;
        GlStateManager.color(0.1F, 0.1F, 0.1F, 0.5F);
        GlStateManager.pushMatrix();
        float f1 = -1.0F;
        float f2 = 1.0F;
        float f3 = -1.0F;
        float f4 = 1.0F;
        float f5 = -0.5F;
        float f6 = p_178108_2_.getMinU();
        float f7 = p_178108_2_.getMaxU();
        float f8 = p_178108_2_.getMinV();
        float f9 = p_178108_2_.getMaxV();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-1.0D, -1.0D, -0.5D).tex((double)f7, (double)f9).endVertex();
        worldrenderer.pos(1.0D, -1.0D, -0.5D).tex((double)f6, (double)f9).endVertex();
        worldrenderer.pos(1.0D, 1.0D, -0.5D).tex((double)f6, (double)f8).endVertex();
        worldrenderer.pos(-1.0D, 1.0D, -0.5D).tex((double)f7, (double)f8).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Renders a texture that warps around based on the direction the player is looking. Texture needs to be bound
     * before being called. Used for the water overlay. Args: parialTickTime
     */
    private void renderWaterOverlayTexture(float p_78448_1_)
    {
        if (!Config.isShaders() || Shaders.isUnderwaterOverlay())
        {
            this.mc.getTextureManager().bindTexture(RES_UNDERWATER_OVERLAY);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            float f = this.mc.thePlayer.getBrightness(p_78448_1_);
            GlStateManager.color(f, f, f, 0.5F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            float f1 = 4.0F;
            float f2 = -1.0F;
            float f3 = 1.0F;
            float f4 = -1.0F;
            float f5 = 1.0F;
            float f6 = -0.5F;
            float f7 = -this.mc.thePlayer.rotationYaw / 64.0F;
            float f8 = this.mc.thePlayer.rotationPitch / 64.0F;
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(-1.0D, -1.0D, -0.5D).tex((double)(4.0F + f7), (double)(4.0F + f8)).endVertex();
            worldrenderer.pos(1.0D, -1.0D, -0.5D).tex((double)(0.0F + f7), (double)(4.0F + f8)).endVertex();
            worldrenderer.pos(1.0D, 1.0D, -0.5D).tex((double)(0.0F + f7), (double)(0.0F + f8)).endVertex();
            worldrenderer.pos(-1.0D, 1.0D, -0.5D).tex((double)(4.0F + f7), (double)(0.0F + f8)).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
        }
    }

    /**
     * Renders the fire on the screen for first person mode. Arg: partialTickTime
     */
    private void renderFireInFirstPerson(float p_78442_1_)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
        GlStateManager.depthFunc(519);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        float f = 1.0F;

        for (int i = 0; i < 2; ++i)
        {
            GlStateManager.pushMatrix();
            TextureAtlasSprite textureatlassprite = this.mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_1");
            this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            float f1 = textureatlassprite.getMinU();
            float f2 = textureatlassprite.getMaxU();
            float f3 = textureatlassprite.getMinV();
            float f4 = textureatlassprite.getMaxV();
            float f5 = (0.0F - f) / 2.0F;
            float f6 = f5 + f;
            float f7 = 0.0F - f / 2.0F;
            float f8 = f7 + f;
            float f9 = -0.5F;
            GlStateManager.translate((float)(-(i * 2 - 1)) * 0.24F, -0.3F, 0.0F);
            GlStateManager.rotate((float)(i * 2 - 1) * 10.0F, 0.0F, 1.0F, 0.0F);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos((double)f5, (double)f7, (double)f9).tex((double)f2, (double)f4).endVertex();
            worldrenderer.pos((double)f6, (double)f7, (double)f9).tex((double)f1, (double)f4).endVertex();
            worldrenderer.pos((double)f6, (double)f8, (double)f9).tex((double)f1, (double)f3).endVertex();
            worldrenderer.pos((double)f5, (double)f8, (double)f9).tex((double)f2, (double)f3).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
    }

    public void updateEquippedItem()
    {
        this.prevEquippedProgress = this.equippedProgress;
        EntityPlayerSP entityplayersp = this.mc.thePlayer;
        ItemStack itemstack = entityplayersp.inventory.getCurrentItem();
        boolean flag = false;

        if (this.itemToRender != null && itemstack != null)
        {
            if (!this.itemToRender.getIsItemStackEqual(itemstack))
            {
                if (Reflector.ForgeItem_shouldCauseReequipAnimation.exists())
                {
                    boolean flag1 = Reflector.callBoolean(this.itemToRender.getItem(), Reflector.ForgeItem_shouldCauseReequipAnimation, new Object[] {this.itemToRender, itemstack, Boolean.valueOf(this.equippedItemSlot != entityplayersp.inventory.currentItem)});

                    if (!flag1)
                    {
                        this.itemToRender = itemstack;
                        this.equippedItemSlot = entityplayersp.inventory.currentItem;
                        return;
                    }
                }

                flag = true;
            }
        }
        else if (this.itemToRender == null && itemstack == null)
        {
            flag = false;
        }
        else
        {
            flag = true;
        }

        float f2 = 0.4F;
        float f = flag ? 0.0F : 1.0F;
        float f1 = MathHelper.clamp_float(f - this.equippedProgress, -f2, f2);
        this.equippedProgress += f1;

        if (this.equippedProgress < 0.1F)
        {
            if (Config.isShaders())
            {
                Shaders.setItemToRenderMain(itemstack);
            }

            this.itemToRender = itemstack;
            this.equippedItemSlot = entityplayersp.inventory.currentItem;
        }
    }

    /**
     * Resets equippedProgress
     */
    public void resetEquippedProgress()
    {
        this.equippedProgress = 0.0F;
    }

    /**
     * Resets equippedProgress
     */
    public void resetEquippedProgress2()
    {
        this.equippedProgress = 0.0F;
    }

    static final class ItemRenderer$1
    {
        static final int[] field_178094_a = new int[EnumAction.values().length];
        private static final String __OBFID = "CL_00002537";

        static
        {
            try
            {
                field_178094_a[EnumAction.NONE.ordinal()] = 1;
            }
            catch (NoSuchFieldError var5)
            {
                ;
            }

            try
            {
                field_178094_a[EnumAction.EAT.ordinal()] = 2;
            }
            catch (NoSuchFieldError var4)
            {
                ;
            }

            try
            {
                field_178094_a[EnumAction.DRINK.ordinal()] = 3;
            }
            catch (NoSuchFieldError var3)
            {
                ;
            }

            try
            {
                field_178094_a[EnumAction.BLOCK.ordinal()] = 4;
            }
            catch (NoSuchFieldError var2)
            {
                ;
            }

            try
            {
                field_178094_a[EnumAction.BOW.ordinal()] = 5;
            }
            catch (NoSuchFieldError var1)
            {
                ;
            }
        }
    }
}
