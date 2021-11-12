package vip.Resolute.util.render;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import org.lwjgl.util.glu.Cylinder;
import vip.Resolute.events.impl.EventRender3D;
import vip.Resolute.modules.impl.combat.KillAura;
import vip.Resolute.util.font.MinecraftFontRenderer;
import vip.Resolute.util.misc.MathUtils;
import vip.Resolute.util.world.Vec;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.NumberFormat;
import java.util.regex.Pattern;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;


import static org.lwjgl.opengl.GL11.*;

public class RenderUtils {



    private static int lastScaledWidth;
    private static int lastScaledHeight;
    private static int lastGuiScale;
    private static final double DOUBLE_PI = Math.PI * 2;
    private static ScaledResolution scaledResolution;
    private static LockedResolution lockedResolution;
    private static int lastWidth;
    private static int lastHeight;
    private static final Frustum FRUSTUM;

    private static final FloatBuffer windowPosition = GLAllocation.createDirectFloatBuffer(4);
    private static final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
    private static final FloatBuffer modelMatrix = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer projectionMatrix = GLAllocation.createDirectFloatBuffer(16);

    public static final Pattern COLOR_PATTERN;

    static {
        FRUSTUM = new Frustum();
        COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
    }

    public static void disableSmoothLine() {
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glDepthMask(true);
        GL11.glCullFace(1029);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static void drawHead(AbstractClientPlayer target, int x, int y, int width, int height) {
        ResourceLocation skin = target.getLocationSkin();

        GL11.glColor4f(1F, 1F, 1F, 1F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(skin);
        RenderUtils.drawScaledCustomSizeModalRect(x, y, 8F, 8F, 8, 8, width, height,
                64F, 64F);
        RenderUtils.drawScaledCustomSizeModalRect(x, y, 40F, 8F, 8, 8, width, height,
                64F, 64F);
    }

    public static void drawScaledCustomSizeModalRect(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + (float) vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + (float) uWidth) * f, (v + (float) vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + (float) uWidth) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    public static Color pulseBrightness(Color color, int index, int count) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs(((float)(System.currentTimeMillis() % 2000L) / 1000.0F + (float)index / (float)count * 2.0F) % 2.0F - 1.0F);
        brightness = 0.5F + 0.5F * brightness;
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], brightness % 2.0F));
    }

    public static void drawRoundedRect2(float x, float y, float x1, float y1, float borderC, int insideC) {
        enableGL2D();
        drawRect1(x + 0.5F, y, x + x1 - 0.5F, y + y1 + 0.5F, insideC);
        drawRect1(x + 0.5F, y - 0.5F, x + x1 - 0.5F, y + y1, insideC);
        drawRect1(x, y + y1 + 0.5F, x + x1, y + y1, insideC);
        disableGL2D();
    }

    public static void drawRoundedRect2(double x, double y, double width, double height, double radius, int color) {
        drawRoundedRect(x, y, width - x, height - y, radius, color);
    }

    public static void drawRect1(float x, float y, float x1, float y1, int color) {
        enableGL2D();
        glColor(color);
        drawRect(x, y, x1, y1);
        disableGL2D();
    }

    public static void drawRect(float x, float y, float x1, float y1, float r, float g, float b, float a) {
        enableGL2D();
        GL11.glColor4f(r, g, b, a);
        drawRect(x, y, x1, y1);
        disableGL2D();
    }

    public static void drawRect(float x, float y, float x1, float y1) {
        GL11.glBegin(7);
        GL11.glVertex2f(x, y1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
    }

    public static void drawRect1(float x, float y, float x1, float y1) {
        GL11.glBegin(7);
        GL11.glVertex2f(x, y1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
    }

    public static void drawOutlinedString(String s, float x, float y, int color, int outlineColor) {
        Minecraft.getMinecraft().fontRendererObj.drawString(s, (int) (x - 0.5f), (int) y, outlineColor);
        Minecraft.getMinecraft().fontRendererObj.drawString(s, (int) x, (int) (y - 0.5f), outlineColor);
        Minecraft.getMinecraft().fontRendererObj.drawString(s, (int) (x + 0.5f), (int) y, outlineColor);
        Minecraft.getMinecraft().fontRendererObj.drawString(s, (int) x, (int) (y + 0.5f), outlineColor);
        Minecraft.getMinecraft().fontRendererObj.drawString(s, (int) x, (int) y, color);
    }

    public static void drawExhi(EntityLivingBase it, EventRender3D event) {
        float radius = 0.25f;
        float side = 4;
        GL11.glPushMatrix();
        GL11.glTranslated(it.lastTickPosX + (it.posX - it.lastTickPosX) * event.getPartialTicks() - Minecraft.getMinecraft().renderManager.viewerPosX,
                (it.lastTickPosY + (it.posY - it.lastTickPosY) * event.getPartialTicks() - Minecraft.getMinecraft().renderManager.viewerPosY) + it.height*1.1,
                it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * event.getPartialTicks() - Minecraft.getMinecraft().renderManager.viewerPosZ);
        GL11.glRotatef(-it.width, 0.0f, 1.0f, 0.0f);
        RenderUtils.glColor(it.hurtTime<=0 ? new Color(80, 255, 80, 80).getRGB() : new Color(255, 0, 0, 80).getRGB());
        RenderUtils.enableSmoothLine(1.5F);
        Cylinder c = new Cylinder();
        GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
        c.draw(0F, radius, 0.3f, (int) side, 1);
        c.setDrawStyle(100012);
        GL11.glTranslated(0.0, 0.0, 0.3);
        c.draw(radius, 0f, 0.3f, (int) side, 1);
        GL11.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
        GL11.glTranslated(0.0, 0.0, -0.3);
        c.draw(0F, radius, 0.3f, (int) side, 1);
        GL11.glTranslated(0.0, 0.0, 0.3);
        c.draw(radius, 0F, 0.3f, (int) side, 1);
        RenderUtils.disableSmoothLine();
        GL11.glPopMatrix();
    }

    public static void drawPlatform(Entity entity, Color color) {
        Timer timer = Minecraft.getMinecraft().timer;
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)timer.renderPartialTicks - RenderManager.renderPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)timer.renderPartialTicks - RenderManager.renderPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)timer.renderPartialTicks - RenderManager.renderPosZ;
        AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().offset(-entity.posX, -entity.posY, -entity.posZ).offset(x, y, z);
        drawAxisAlignedBB(new AxisAlignedBB(axisAlignedBB.minX - 0.1D, axisAlignedBB.minY - 0.1D, axisAlignedBB.minZ - 0.1D, axisAlignedBB.maxX + 0.1D, axisAlignedBB.maxY + 0.2D, axisAlignedBB.maxZ + 0.1D), color);
    }

    public static void renderEnchantText(ItemStack stack, int x, int y) {
        int unbreakingLevel2;
        RenderHelper.disableStandardItemLighting();
        int enchantmentY = y + 24;
        if (stack.getItem() instanceof ItemArmor) {
            int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack);
            int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            int thornLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack);
            if (protectionLevel > 0) {
                RenderUtils.drawEnchantTag("P" + RenderUtils.getColor(protectionLevel) + protectionLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (unbreakingLevel > 0) {
                RenderUtils.drawEnchantTag("U" + RenderUtils.getColor(unbreakingLevel) + unbreakingLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (thornLevel > 0) {
                RenderUtils.drawEnchantTag("T" + RenderUtils.getColor(thornLevel) + thornLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
        }
        if (stack.getItem() instanceof ItemBow) {
            int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
            int flameLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack);
            unbreakingLevel2 = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (powerLevel > 0) {
                RenderUtils.drawEnchantTag("Pow" + RenderUtils.getColor(powerLevel) + powerLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (punchLevel > 0) {
                RenderUtils.drawEnchantTag("Pun" + RenderUtils.getColor(punchLevel) + punchLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (flameLevel > 0) {
                RenderUtils.drawEnchantTag("F" + RenderUtils.getColor(flameLevel) + flameLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (unbreakingLevel2 > 0) {
                RenderUtils.drawEnchantTag("U" + RenderUtils.getColor(unbreakingLevel2) + unbreakingLevel2, x * 2, enchantmentY);
                enchantmentY += 8;
            }
        }
        if (stack.getItem() instanceof ItemSword) {
            int sharpnessLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
            int knockbackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack);
            int fireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            unbreakingLevel2 = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (sharpnessLevel > 0) {
                RenderUtils.drawEnchantTag("S" + RenderUtils.getColor(sharpnessLevel) + sharpnessLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (knockbackLevel > 0) {
                RenderUtils.drawEnchantTag("K" + RenderUtils.getColor(knockbackLevel) + knockbackLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (fireAspectLevel > 0) {
                RenderUtils.drawEnchantTag("F" + RenderUtils.getColor(fireAspectLevel) + fireAspectLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (unbreakingLevel2 > 0) {
                RenderUtils.drawEnchantTag("U" + RenderUtils.getColor(unbreakingLevel2) + unbreakingLevel2, x * 2, enchantmentY);
                enchantmentY += 8;
            }
        }
        if (stack.getRarity() == EnumRarity.EPIC) {
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GL11.glScalef(0.5f, 0.5f, 0.5f);
            RenderUtils.drawOutlinedString("God", x * 2, enchantmentY, new Color(255, 255, 0).getRGB(), new Color(100, 100, 0, 200).getRGB());
            GL11.glScalef(1.0f, 1.0f, 1.0f);
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }


    public static void drawRectBordered(double x, double y, double x1, double y1, double width, int internalColor, int borderColor) {
        RenderUtils.rectangle(x + width, y + width, x1 - width, y1 - width, internalColor);
        RenderUtils.rectangle(x + width, y, x1 - width, y + width, borderColor);
        RenderUtils.rectangle(x, y, x + width, y1, borderColor);
        RenderUtils.rectangle(x1 - width, y, x1, y1, borderColor);
        RenderUtils.rectangle(x + width, y1 - width, x1 - width, y1, borderColor);
    }

    private static void drawEnchantTag(String text, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        RenderUtils.drawOutlinedString(text, x, y, Colors.getColor(255), new Color(0, 0, 0, 100).darker().getRGB());
        GL11.glScalef(1.0f, 1.0f, 1.0f);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public static void targetHudRect(double x, double y, double x1, double y1, double size) {
        RenderUtils.rectangleBordered(x, y + -4.0, x1 + size, y1 + size, 0.5, new Color(60, 60, 60).getRGB(), new Color(10, 10, 10).getRGB());
        RenderUtils.rectangleBordered(x + 1.0, y + -3.0, x1 + size - 1.0, y1 + size - 1.0, 1.0, new Color(40, 40, 40).getRGB(), new Color(40, 40, 40).getRGB());
        RenderUtils.rectangleBordered(x + 2.5, y + -1.5, x1 + size - 2.5, y1 + size - 2.5, 0.5, new Color(40, 40, 40).getRGB(), new Color(60, 60, 60).getRGB());
        RenderUtils.rectangleBordered(x + 2.5, y + -1.5, x1 + size - 2.5, y1 + size - 2.5, 0.5, new Color(22, 22, 22).getRGB(), new Color(255, 255, 255, 0).getRGB());
    }

    public static void targetHudRect1(double x, double y, double x1, double y1, double size) {
        RenderUtils.rectangleBordered(x + 4.35, y + 0.5, x1 + size - 84.5, y1 + size - 4.35, 0.5, new Color(48, 48, 48).getRGB(), new Color(10, 10, 10).getRGB());
        RenderUtils.rectangleBordered(x + 5.0, y + 1.0, x1 + size - 85.0, y1 + size - 5.0, 0.5, new Color(17, 17, 17).getRGB(), new Color(255, 255, 255, 0).getRGB());
    }

    private static String getColor(int n) {
        if (n != 1) {
            if (n == 2) {
                return "§a";
            }
            if (n == 3) {
                return "§3";
            }
            if (n == 4) {
                return "§4";
            }
            if (n >= 5) {
                return "§e";
            }
        }
        return "§f";
    }

    public static void drawESPRect(float left, float top, float right, float bottom, int color) {
        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            float j = top;
            top = bottom;
            bottom = j;
        }
        float f3 = (float)(color >> 24 & 0xFF) / 255.0f;
        float f = (float)(color >> 16 & 0xFF) / 255.0f;
        float f1 = (float)(color >> 8 & 0xFF) / 255.0f;
        float f2 = (float)(color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0).endVertex();
        worldrenderer.pos(right, bottom, 0.0).endVertex();
        worldrenderer.pos(right, top, 0.0).endVertex();
        worldrenderer.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawOutlineString(MinecraftFontRenderer fr, String s, float x, float y, int color, int outlineColor) {
        fr.drawString(RenderUtils.stripColor(s), x - 0.5f, y, outlineColor);
        fr.drawString(RenderUtils.stripColor(s), x + 0.5f, y, outlineColor);
        fr.drawString(RenderUtils.stripColor(s), x, y + 0.5f, outlineColor);
        fr.drawString(RenderUtils.stripColor(s), x, y - 0.5f, outlineColor);
        fr.drawString(s, x, y, color);
    }

    public static String stripColor(String input) {
        return COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static void renderItemStack(ItemStack stack, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.clear(256);
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().zLevel = -150.0f;
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        Minecraft.getMinecraft().getRenderItem().renderItemOverlays(Minecraft.getMinecraft().fontRendererObj, stack, x, y);
        Minecraft.getMinecraft().getRenderItem().zLevel = 0.0f;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public static void enableSmoothLine(float width) {
        GL11.glDisable(3008);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glEnable(2884);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glLineWidth(width);
    }

    public static void quickDrawRect(final float x, final float y, final float x2, final float y2) {
        glBegin(GL_QUADS);

        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);

        glEnd();
    }

    public static boolean isHoveredFull(float x, float y, float w, float h, int mouseX, int mouseY) {
        return (mouseX >= x && mouseX <= w && mouseY >= y && mouseY <= h);
    }

    public static void drawWaveString(String str, float x, float y) {
        float posX = x;
        long ms = (long) (4 * 1000L);
        long currentMillis = System.currentTimeMillis();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        for (int i = 0; i < str.length(); i++) {
            final float offset = (float) ((currentMillis + (i * 100)) % ms / (ms / 2.0F));
            String ch = str.charAt(i) + "";



            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(ch, (int) posX, (int) y, fadeBetween(new Color(42, 0, 255).getRGB(), darker(new Color(0, 255, 175).getRGB(), 0.49f), offset));


            posX += Minecraft.getMinecraft().fontRendererObj.getStringWidth(ch);
        }
    }

    public static void drawWaveStringRainbow(String str, float x, float y) {
        float posX = x;
        long ms = (long) (4 * 1000L);
        long currentMillis = System.currentTimeMillis();
        for (int i = 0; i < str.length(); i++) {
            final float offset = (float) ((currentMillis + (i * 100)) % ms / (ms / 2.0F));
            String ch = str.charAt(i) + "";
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(ch, (int) posX, (int) y, getRainbow(2000, i));
            posX += Minecraft.getMinecraft().fontRendererObj.getStringWidth(ch);
        }
    }

    private static int getColor(int color1, int color2, float offset) {
        if (offset > 1)
            offset = 1 - offset % 1;

        double invert = 1 - offset;
        int r = (int) ((color1 >> 16 & 0xFF) * invert +
                (color2 >> 16 & 0xFF) * offset);
        int g = (int) ((color1 >> 8 & 0xFF) * invert +
                (color2 >> 8 & 0xFF) * offset);
        int b = (int) ((color1 & 0xFF) * invert +
                (color2 & 0xFF) * offset);
        int a = (int) ((color1 >> 24 & 0xFF) * invert +
                (color2 >> 24 & 0xFF) * offset);
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF);
    }

    public static void bob(float f2, float f3) {
        Minecraft minecraft = Minecraft.getMinecraft();
        Minecraft.getMinecraft().thePlayer.cameraYaw += f2 / 100.0f;
        Minecraft.getMinecraft().thePlayer.cameraPitch += f3 / 100.0f;
    }

    public static float animate(double target, double current, double speed) {
        boolean larger = (target > current);
        if (speed < 0.0F) speed = 0.0F;
        else if (speed > 1.0F) speed = 1.0F;
        double dif = Math.max(target, current) - Math.min(target, current);
        double factor = dif * speed;
        if (factor < 0.1f) factor = 0.1F;
        if (larger) current += factor;
        else current -= factor;
        return (float) current;
    }

    public static void drawImage(ResourceLocation image, float x, float y, float width, float height) {
        drawImage(image, x, y, width, height, 255);
    }

    public static void drawImage(ResourceLocation image, float x, float y, float width, float height, float opacity) {
        glPushMatrix();
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        glColor4f(1.0f, 1.0f, 1.0f, opacity / 255);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glPopMatrix();
    }

    public static void drawAuraMark(Entity entity, Color color) {
        Timer timer = Minecraft.getMinecraft().timer;
        if (KillAura.target != null) {
            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)timer.renderPartialTicks - RenderManager.renderPosX;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)timer.renderPartialTicks - RenderManager.renderPosY;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)timer.renderPartialTicks - RenderManager.renderPosZ;
            AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().offset(-entity.posX, -entity.posY, -entity.posZ).offset(x, y - 0.41D, z);
            drawAxisAlignedBB(new AxisAlignedBB(axisAlignedBB.minX, axisAlignedBB.maxY + 0.2D, axisAlignedBB.minZ, axisAlignedBB.maxX, axisAlignedBB.maxY + 0.26D, axisAlignedBB.maxZ), color);
        }
    }

    public static void drawAxisAlignedBB(AxisAlignedBB axisAlignedBB, Color color) {
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        glColor2(color);
        drawFilledBox(axisAlignedBB);
        GlStateManager.resetColor();
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }

    public static void drawFilledBox(AxisAlignedBB axisAlignedBB) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
    }

    public static void glColor2(Color color) {
        float red = (float)color.getRed() / 255.0F;
        float green = (float)color.getGreen() / 255.0F;
        float blue = (float)color.getBlue() / 255.0F;
        float alpha = (float)color.getAlpha() / 255.0F;
        GlStateManager.color(red, green, blue, alpha);
    }

    public static int getColorFromPercentage(float current, float max) {
        float percentage = (current / max) / 3;
        return Color.HSBtoRGB(percentage, 1.0F, 1.0F);
    }

    public static boolean isBBInFrustum(final AxisAlignedBB aabb) {
        final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        FRUSTUM.setPosition(player.posX, player.posY, player.posZ);
        return FRUSTUM.isBoundingBoxInFrustum(aabb);
    }

    public static void draw2DImage(ResourceLocation image, double x, double y, int width, int height, Color c) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        glColor4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha());
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture2(x, y, 0.0F, 0.0F, width, height, width, height);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawImage(float x, float y, float width, float height, int color) {
        float f = 1.0F / width;
        float f1 = 1.0F / height;
        GL11.glEnable(3042);
        color(color);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double)x, (double)(y + height), 0.0D).tex(0.0D, (double)(height * f1)).endVertex();
        worldrenderer.pos((double)(x + width), (double)(y + height), 0.0D).tex((double)(width * f), (double)(height * f1)).endVertex();
        worldrenderer.pos((double)(x + width), (double)y, 0.0D).tex((double)(width * f), 0.0D).endVertex();
        worldrenderer.pos((double)x, (double)y, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        GL11.glDisable(3042);
    }

    public static Color blendColors(float[] fractions, Color[] colors, float progress) {
        Color color = null;
        if (fractions == null) throw new IllegalArgumentException("Fractions can't be null");
        if (colors == null) throw new IllegalArgumentException("Colours can't be null");
        if (fractions.length != colors.length) throw new IllegalArgumentException("Fractions and colours must have equal number of elements");
        int[] indicies = getFractionIndicies(fractions, progress);
        float[] range = new float[]{fractions[indicies[0]], fractions[indicies[1]]};
        Color[] colorRange = new Color[]{colors[indicies[0]], colors[indicies[1]]};
        float max = range[1] - range[0];
        float value = progress - range[0];
        float weight = value / max;
        return blend(colorRange[0], colorRange[1], 1.0f - weight);
    }

    public static void filledBox(AxisAlignedBB boundingBox, int color, boolean shouldColor) {
        GlStateManager.pushMatrix();
        float var11 = (float)(color >> 24 & 255) / 255.0F;
        float var6 = (float)(color >> 16 & 255) / 255.0F;
        float var7 = (float)(color >> 8 & 255) / 255.0F;
        float var8 = (float)(color & 255) / 255.0F;
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
        if (shouldColor) {
            GlStateManager.color(var6, var7, var8, var11);
        }

        byte draw = 7;
        worldRenderer.begin(draw, DefaultVertexFormats.POSITION);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        Tessellator.getInstance().draw();
        worldRenderer.begin(draw, DefaultVertexFormats.POSITION);
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        Tessellator.getInstance().draw();
        worldRenderer.begin(draw, DefaultVertexFormats.POSITION);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        Tessellator.getInstance().draw();
        worldRenderer.begin(draw, DefaultVertexFormats.POSITION);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        Tessellator.getInstance().draw();
        worldRenderer.begin(draw, DefaultVertexFormats.POSITION);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        Tessellator.getInstance().draw();
        worldRenderer.begin(draw, DefaultVertexFormats.POSITION);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static void drawEntityOnScreen(final int posX, final int posY, final int scale, final EntityLivingBase entity) {
        GlStateManager.pushMatrix();
        GlStateManager.enableColorMaterial();

        GlStateManager.translate(posX, posY, 50.0);
        GlStateManager.scale((-scale), scale, scale);
        GlStateManager.rotate(180F, 0F, 0F, 1F);
        GlStateManager.rotate(135F, 0F, 1F, 0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135F, 0F, 1F, 0F);
        GlStateManager.translate(0.0, 0.0, 0.0);

        float renderYawOffset = entity.renderYawOffset;
        float rotationYaw = entity.rotationYaw;
        float rotationPitch = entity.rotationPitch;
        float prevRotationYawHead = entity.prevRotationYawHead;
        float rotationYawHead = entity.rotationYawHead;


        entity.renderYawOffset = 0;
        entity.rotationYaw = 0;
        entity.rotationPitch = 90;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;

        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(entity, 0.0, 0.0, 0.0, 0F, 1F);
        rendermanager.setRenderShadow(true);

        entity.renderYawOffset = renderYawOffset;
        entity.rotationYaw = rotationYaw;
        entity.rotationPitch = rotationPitch;
        entity.prevRotationYawHead = prevRotationYawHead;
        entity.rotationYawHead = rotationYawHead;

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent)
    {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)posX, (float)posY, 50.0F);
        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float)Math.atan((double)(mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        ent.renderYawOffset = (float)Math.atan((double)(mouseX / 40.0F)) * 20.0F;
        ent.rotationYaw = (float)Math.atan((double)(mouseX / 40.0F)) * 40.0F;
        ent.rotationPitch = -((float)Math.atan((double)(mouseY / 40.0F))) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public static int[] getFractionIndicies(float[] fractions, float progress) {
        int startPoint;
        int[] range = new int[2];
        for (startPoint = 0; startPoint < fractions.length && fractions[startPoint] <= progress; ++startPoint) {
        }
        if (startPoint >= fractions.length) {
            startPoint = fractions.length - 1;
        }
        range[0] = startPoint - 1;
        range[1] = startPoint;
        return range;
    }

    public static Color blend(Color color1, Color color2, double ratio) {
        float r = (float)ratio;
        float ir = 1.0f - r;
        float[] rgb1 = new float[3];
        float[] rgb2 = new float[3];
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        float red = rgb1[0] * r + rgb2[0] * ir;
        float green = rgb1[1] * r + rgb2[1] * ir;
        float blue = rgb1[2] * r + rgb2[2] * ir;
        if (red < 0.0f) {
            red = 0.0f;
        } else if (red > 255.0f) {
            red = 255.0f;
        }
        if (green < 0.0f) {
            green = 0.0f;
        } else if (green > 255.0f) {
            green = 255.0f;
        }
        if (blue < 0.0f) {
            blue = 0.0f;
        } else if (blue > 255.0f) {
            blue = 255.0f;
        }
        Color color = null;
        try {
            color = new Color(red, green, blue);
        }
        catch (IllegalArgumentException exp) {
            NumberFormat nf = NumberFormat.getNumberInstance();
            System.out.println(nf.format(red) + "; " + nf.format(green) + "; " + nf.format(blue));
            exp.printStackTrace();
        }
        return color;
    }

    public static double linearAnimation(double now, double desired, double speed) {
        double dif = Math.abs(now - desired);

        final int fps = Minecraft.getDebugFPS();

        if (dif > 0) {
            double animationSpeed = MathUtils.round(Math.min(
                    10.0D, Math.max(0.005D, (144.0D / fps) * speed)), 0.005D);

            if (dif != 0 && dif < animationSpeed)
                animationSpeed = dif;

            if (now < desired)
                return now + animationSpeed;
            else if (now > desired)
                return now - animationSpeed;
        }

        return now;
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB boundingBox) {
        GL11.glPushMatrix();
        GL11.glBegin(3);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glEnd();
        GL11.glBegin(3);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        GL11.glEnd();
        GL11.glBegin(1);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glEnd();
        GL11.glPopMatrix();
    }

    public static void rectangleBordered(final double x, final double y, final double x1, final double y1, final double width, final int internalColor, final int borderColor) {
        rectangle(x + width, y + width, x1 - width, y1 - width, internalColor);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        rectangle(x + width, y, x1 - width, y + width, borderColor);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        rectangle(x, y, x + width, y1, borderColor);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        rectangle(x1 - width, y, x1, y1, borderColor);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        rectangle(x + width, y1 - width, x1 - width, y1, borderColor);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static final void polygon(double x, double y, double sideLength, double amountOfSides, boolean filled, Color color) {
        sideLength /= 2;
        start();
        if (color != null)
            color(color);
        if (!filled) GL11.glLineWidth(1);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        begin(filled ? GL11.GL_TRIANGLE_FAN : GL11.GL_LINE_STRIP);
        {
            for (double i = 0; i <= amountOfSides; i++) {
                double angle = i * (Math.PI * 2) / amountOfSides;
                vertex(x + (sideLength * Math.cos(angle)) + sideLength, y + (sideLength * Math.sin(angle)) + sideLength);
            }
        }
        end();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        stop();
    }

    public final void polygon(double x, double y, double sideLength, int amountOfSides, boolean filled) {
        polygon(x, y, sideLength, amountOfSides, filled, null);
    }

    public static final void polygon(double x, double y, double sideLength, int amountOfSides, Color color) {
        polygon(x, y, sideLength, amountOfSides, true, color);
    }

    public final void polygon(double x, double y, double sideLength, int amountOfSides) {
        polygon(x, y, sideLength, amountOfSides, true, null);
    }

    public static final void line(double firstX, double firstY, double secondX, double secondY, double lineWidth, Color color) {
        start();
        if (color != null)
            color(color);
        lineWidth(lineWidth <= 1 ? 1 : lineWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        begin(GL11.GL_LINES);
        {
            vertex(firstX, firstY);
            vertex(secondX, secondY);
        }
        end();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        stop();
    }

    public final void push() {
        GL11.glPushMatrix();
    }

    public final void pop() {
        GL11.glPopMatrix();
    }

    public static final void enable(int glTarget) {
        GL11.glEnable(glTarget);
    }

    public static final void disable(int glTarget) {
        GL11.glDisable(glTarget);
    }

    public static final void start() {
        enable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        disable(GL11.GL_TEXTURE_2D);
        disable(GL11.GL_CULL_FACE);
        GlStateManager.disableAlpha();
    }

    public static final void stop() {
        GlStateManager.enableAlpha();
        enable(GL11.GL_CULL_FACE);
        enable(GL11.GL_TEXTURE_2D);
        disable(GL11.GL_BLEND);
        color(Color.white);
    }

    public static final void begin(int glMode) {
        GL11.glBegin(glMode);
    }

    public static final void end() {
        GL11.glEnd();
    }

    public static final void vertex(double x, double y) {
        GL11.glVertex2d(x, y);
    }

    public static final void lineWidth(double width) {
        GL11.glLineWidth((float)width);
    }

    public final void translate(double x, double y) {
        GL11.glTranslated(x, y, 0);
    }

    public final void scale(double x, double y) {
        GL11.glScaled(x, y, 1);
    }

    public static final void color(Color color) {
        if (color == null)
            color = Color.white;
        color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
    }

    public final void rotate(double x, double y, double z, double angle) {
        GL11.glRotated(angle, x, y, z);
    }

    public static final void color(double red, double green, double blue, double alpha) {
        GL11.glColor4d(red, green, blue, alpha);
    }

    public final void color(double red, double green, double blue) {
        color(red, green, blue, 1);
    }

    public final void line(double firstX, double firstY, double secondX, double secondY, double lineWidth) {
        line(firstX, firstY, secondX, secondY, lineWidth, null);
    }

    public final void line(double firstX, double firstY, double secondX, double secondY, Color color) {
        line(firstX, firstY, secondX, secondY, 0, color);
    }

    public final void line(double firstX, double firstY, double secondX, double secondY) {
        line(firstX, firstY, secondX, secondY, 0, null);
    }

    public static final void line(Vec firstPoint, Vec secondPoint, double lineWidth, Color color) {
        line(firstPoint.getX(), firstPoint.getY(), secondPoint.getX(), secondPoint.getY(), lineWidth, color);
    }

    public final void line(Vec firstPoint, Vec secondPoint, Color color) {
        line(firstPoint.getX(), firstPoint.getY(), secondPoint.getX(), secondPoint.getY(), color);
    }

    public final void line(Vec firstPoint, Vec secondPoint) {
        line(firstPoint.getX(), firstPoint.getY(), secondPoint.getX(), secondPoint.getY());
    }



    public static Vector3f project2D(float x,
                                     float y,
                                     float z,
                                     int scaleFactor) {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelMatrix);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

        if (GLU.gluProject(x, y, z, modelMatrix, projectionMatrix, viewport, windowPosition)) {
            return new Vector3f(
                    windowPosition.get(0) / scaleFactor,
                    (Display.getHeight() - windowPosition.get(1)) / scaleFactor,
                    windowPosition.get(2));
        }

        return null;
    }

    public static int darker(int color) {
        return darker(color, 0.6F);
    }

    public static int blendColors(final float progress) {
        final int[] colors = {
                0xFF00FF69,
                0xFFFFFF00,
                0xFFFF0000,
                0xFF800000
        };

        return blendColors(colors, progress);
    }

    public static int fadeBetween(int startColor, int endColor) {
        return fadeBetween(startColor, endColor, (System.currentTimeMillis() % 2000) / 1000.0F);
    }

    public static int blendColors(final int[] colors, final float progress) {
        final int size = colors.length;

        if (progress == 1.0F)
            return colors[0];
        else if (progress == 0.0F)
            return colors[size - 1];

        final float mulProgress = Math.max(0, (1.0F - progress) * (size - 1));
        final int index = (int) (mulProgress);
        final int startCol = colors[index];
        final int endColor = colors[index+1];
        return RenderUtils.fadeBetween(startCol, endColor, mulProgress - index);
    }

    public static int fadeBetween(final int startColor, final int endColor, float progress) {
        if (progress > 1.0f) {
            progress = 1.0f - progress % 1.0f;
        }
        return fadeTo(startColor, endColor, progress);
    }

    public static double progressiveAnimation(final double now, final double desired, final double speed) {
        final double dif = Math.abs(now - desired);
        final int fps = Minecraft.getDebugFPS();
        if (dif > 0.0) {
            double animationSpeed = MathUtils.roundToDecimalPlace(Math.min(10.0, Math.max(0.05, 144.0 / fps * (dif / 10.0) * speed)), 0.05);
            if (dif != 0.0 && dif < animationSpeed) {
                animationSpeed = dif;
            }
            if (now < desired) {
                return now + animationSpeed;
            }
            if (now > desired) {
                return now - animationSpeed;
            }
        }
        return now;
    }



    public static int fadeTo(final int startColor, final int endColor, final float progress) {
        final float invert = 1.0f - progress;
        final int r = (int)((startColor >> 16 & 0xFF) * invert + (endColor >> 16 & 0xFF) * progress);
        final int g = (int)((startColor >> 8 & 0xFF) * invert + (endColor >> 8 & 0xFF) * progress);
        final int b = (int)((startColor & 0xFF) * invert + (endColor & 0xFF) * progress);
        final int a = (int)((startColor >> 24 & 0xFF) * invert + (endColor >> 24 & 0xFF) * progress);
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    public static int getColorFromPercentage(float percentage) {
        return Color.HSBtoRGB(percentage / 3, 1.0F, 1.0F);
    }

    public static void drawBox(BlockPos pos, int r, int g, int b, boolean depth) {
        final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        final net.minecraft.util.Timer timer = Minecraft.getMinecraft().timer;

        final double x = pos.getX() - renderManager.renderPosX;
        final double y = pos.getY() - renderManager.renderPosY;
        final double z = pos.getZ() - renderManager.renderPosZ;

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0);
        final Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();

        if (block != null) {
            final EntityPlayer player = Minecraft.getMinecraft().thePlayer;

            final double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) timer.renderPartialTicks;
            final double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) timer.renderPartialTicks;
            final double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) timer.renderPartialTicks;
            axisAlignedBB = block.getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, pos).expand(.002, .002, .002).offset(-posX, -posY, -posZ);

            drawAxisAlignedBBFilled(axisAlignedBB, r,g,b, depth);
        }
    }

    public static void drawAxisAlignedBBFilled(AxisAlignedBB axisAlignedBB, float r, float g, float b, boolean depth) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        if (depth) glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glColor4f(r,g,b,0.3f);
        drawBoxFilled(axisAlignedBB);
        glEnable(GL_TEXTURE_2D);
        if (depth) glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    public static int getRainbow(int speed, float offset, float saturation) {
        float rainbow = (float) ((System.currentTimeMillis() - (offset % speed) / 0.25) % speed);
        rainbow /= speed;
        return Color.getHSBColor(rainbow, saturation, 1.0F).getRGB();
    }

    public static void makeScissorBox (final int x, final int y, final int x2, final int y2) {
        final ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        final int factor = scaledResolution.getScaleFactor();
        GL11.glScissor((int) (x * factor), (int) ((scaledResolution.getScaledHeight() - y2) * factor), (int) ((x2 - x) * factor), (int) ((y2 - y) * factor));
    }
    public static void relativeRect (float left, float top, float right, float bottom, int color) {
        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            float j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double) left, (double) bottom, 0.0D).endVertex();
        worldrenderer.pos((double) right, (double) bottom, 0.0D).endVertex();
        worldrenderer.pos((double) right, (double) top, 0.0D).endVertex();
        worldrenderer.pos((double) left, (double) top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawUnfilledCircle(float x, float y, float r, int c) {
        float f = (c >> 24 & 0xFF) / 255.0f;
        float f2 = (c >> 16 & 0xFF) / 255.0f;
        float f3 = (c >> 8 & 0xFF) / 255.0f;
        float f4 = (c & 0xFF) / 255.0f;
        GL11.glPushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glColor4f(f2, f3, f4, f);
        GL11.glLineWidth(1);
        GL11.glBegin(2);
        for (int i = 0; i <= 360; ++i) {
            double x2 = Math.sin(i * Math.PI / 180.0) * (r / 2);
            double y2 = Math.cos(i * Math.PI / 180.0) * (r / 2);
            GL11.glVertex2d(x + r / 2 + x2, y + r / 2 + y2);
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }

    public static void drawCheckMark(float x, float y, int width, int color) {
        float f = (color >> 24 & 255) / 255.0f;
        float f1 = (color >> 16 & 255) / 255.0f;
        float f2 = (color >> 8 & 255) / 255.0f;
        float f3 = (color & 255) / 255.0f;
        GL11.glPushMatrix();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(3553);
        glEnable(2848);
        glBlendFunc(770, 771);
        GL11.glLineWidth(1.5f);
        GL11.glBegin(3);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glVertex2d(x + width - 6.5, y + 3);
        GL11.glVertex2d(x + width - 11.5, y + 10);
        GL11.glVertex2d(x + width - 13.5, y + 8);
        GL11.glEnd();
        glEnable(3553);
        glDisable(GL_BLEND);
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawArrow(float x, float y,boolean up, int hexColor) {
        GL11.glPushMatrix();
        GL11.glScaled(1.3, 1.3, 1.3);

        x /= 1.3;
        y /= 1.3;
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        hexColor(hexColor);
        GL11.glLineWidth(2);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x, y + (up ? 4:0));
        GL11.glVertex2d(x + 3, y + (up ? 0:4));
        GL11.glEnd();
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x + 3, y + (up ? 0:4));
        GL11.glVertex2d(x + 6, y+ (up ? 4:0));
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glPopMatrix();
    }

    public static void drawRoundedRect(double x, double y, double width, double height, double radius, int color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        double x1 = x + width;
        double y1 = y + height;
        float f = (color >> 24 & 0xFF) / 255.0F;
        float f1 = (color >> 16 & 0xFF) / 255.0F;
        float f2 = (color >> 8 & 0xFF) / 255.0F;
        float f3 = (color & 0xFF) / 255.0F;
        GL11.glPushAttrib(0);
        GL11.glScaled(0.5, 0.5, 0.5);

        x *= 2;
        y *= 2;
        x1 *= 2;
        y1 *= 2;

        glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(f1, f2, f3, f);
        glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glBegin(GL11.GL_POLYGON);

        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x + radius + +(Math.sin((i * Math.PI / 180)) * (radius * -1)), y + radius + (Math.cos((i * Math.PI / 180)) * (radius * -1)));
        }

        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x + radius + (Math.sin((i * Math.PI / 180)) * (radius * -1)), y1 - radius + (Math.cos((i * Math.PI / 180)) * (radius * -1)));
        }

        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x1 - radius + (Math.sin((i * Math.PI / 180)) * radius), y1 - radius + (Math.cos((i * Math.PI / 180)) * radius));
        }

        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x1 - radius + (Math.sin((i * Math.PI / 180)) * radius), y + radius + (Math.cos((i * Math.PI / 180)) * radius));
        }

        GL11.glEnd();

        glEnable(GL11.GL_TEXTURE_2D);
        glDisable(GL11.GL_LINE_SMOOTH);
        glEnable(GL11.GL_TEXTURE_2D);

        GL11.glScaled(2, 2, 2);

        GL11.glPopAttrib();
        GL11.glColor4f(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

    }

    public static void hexColor(int hexColor) {
        float red = (hexColor >> 16 & 0xFF) / 255.0F;
        float green = (hexColor >> 8 & 0xFF) / 255.0F;
        float blue = (hexColor & 0xFF) / 255.0F;
        float alpha = (hexColor >> 24 & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void drawOutlinedRoundedRect(double x, double y, double width, double height, double radius, float linewidth, int color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        double x1 = x + width;
        double y1 = y + height;
        float f = (color >> 24 & 0xFF) / 255.0F;
        float f1 = (color >> 16 & 0xFF) / 255.0F;
        float f2 = (color >> 8 & 0xFF) / 255.0F;
        float f3 = (color & 0xFF) / 255.0F;
        GL11.glPushAttrib(0);
        GL11.glScaled(0.5, 0.5, 0.5);

        x *= 2;
        y *= 2;
        x1 *= 2;
        y1 *= 2;
        GL11.glLineWidth(linewidth);

        glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(f1, f2, f3, f);
        glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(2);

        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x + radius + +(Math.sin((i * Math.PI / 180)) * (radius * -1)), y + radius + (Math.cos((i * Math.PI / 180)) * (radius * -1)));
        }

        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x + radius + (Math.sin((i * Math.PI / 180)) * (radius * -1)), y1 - radius + (Math.cos((i * Math.PI / 180)) * (radius * -1)));
        }

        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x1 - radius + (Math.sin((i * Math.PI / 180)) * radius), y1 - radius + (Math.cos((i * Math.PI / 180)) * radius));
        }

        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x1 - radius + (Math.sin((i * Math.PI / 180)) * radius), y + radius + (Math.cos((i * Math.PI / 180)) * radius));
        }

        GL11.glEnd();

        glEnable(GL11.GL_TEXTURE_2D);
        glDisable(GL11.GL_LINE_SMOOTH);
        glEnable(GL11.GL_TEXTURE_2D);

        GL11.glScaled(2, 2, 2);

        GL11.glPopAttrib();
        GL11.glColor4f(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

    }

    public static void prepareScissorBox(float x, float y, float width, float height) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        prepareScissorBox(x, y, width, height, scaledResolution);
    }

    public static void prepareScissorBox(float x, float y, float width, float height, ScaledResolution scaledResolution) {
        int factor = scaledResolution.getScaleFactor();
        GL11.glScissor((int)(x * factor), (int)((scaledResolution.getScaledHeight() - height) * factor), (int)((width - x) * factor), (int)((height - y) * factor));
    }

    public static Color brighter(Color color, float factor) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int alpha = color.getAlpha();
        int i = (int)(1.0/(1.0-factor));
        if (r == 0 && g == 0 && b == 0) return new Color(i, i, i, alpha);
        if (r > 0 && r < i) r = i;
        if (g > 0 && g < i) g = i;
        if (b > 0 && b < i) b = i;
        return new Color(Math.min((int)(r/factor), 255), Math.min((int)(g/factor), 255), Math.min((int)(b/factor), 255), alpha);
    }

    public static void prepareScissorBox(ScaledResolution sr, float x, float y, float width, float height) {
        float x2 = x + width;
        float y2 = y + height;
        int factor = sr.getScaleFactor();
        GL11.glScissor((int) (x * factor), (int) ((sr.getScaledHeight() - y2) * factor), (int) ((x2 - x) * factor), (int) ((y2 - y) * factor));
    }


    public static void circle(final float x, final float y, final float radius, final Color fill) {
        arc(x, y, 0.0f, 360.0f, radius, fill);
    }

    public static void arc(final float x, final float y, final float start, final float end, final float radius,
                           final Color color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void arcEllipse(final float x, final float y, float start, float end, final float w, final float h,
                                  final Color color) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        float temp = 0.0f;
        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }
        final Tessellator var9 = Tessellator.getInstance();
        final WorldRenderer var10 = var9.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f,
                color.getAlpha() / 255.0f);
        if (color.getAlpha() > 0.5f) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0f);
            GL11.glBegin(3);
            for (float i = end; i >= start; i -= 4.0f) {
                final float ldx = (float) Math.cos(i * 3.141592653589793 / 180.0) * w * 1.001f;
                final float ldy = (float) Math.sin(i * 3.141592653589793 / 180.0) * h * 1.001f;
                GL11.glVertex2f(x + ldx, y + ldy);
            }
            GL11.glEnd();
            GL11.glDisable(2848);
        }
        GL11.glBegin(6);
        for (float i = end; i >= start; i -= 4.0f) {
            final float ldx = (float) Math.cos(i * 3.141592653589793 / 180.0) * w;
            final float ldy = (float) Math.sin(i * 3.141592653589793 / 180.0) * h;
            GL11.glVertex2f(x + ldx, y + ldy);
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static float calculateCompensation(float target, float current, long delta, double speed) {
        float diff = current - target;
        if (delta < 1L) {
            delta = 1L;
        }
        if (delta > 1000L) {
            delta = 16L;
        }
        if ((double) diff > speed) {
            double xD = speed * (double) delta / 16.0 < 0.5 ? 0.5 : speed * (double) delta / 16.0;
            if ((current = (float) ((double) current - xD)) < target) {
                current = target;
            }
        } else if ((double) diff < -speed) {
            double xD = speed * (double) delta / 16.0 < 0.5 ? 0.5 : speed * (double) delta / 16.0;
            if ((current = (float) ((double) current + xD)) > target) {
                current = target;
            }
        } else {
            current = target;
        }
        return current;
    }


    public static int getRainbowFromEntity(final long currentMillis, final int speed, final int offset, final boolean invert, final float alpha) {
        final float time = (currentMillis + offset * 300L) % speed / (float)speed;
        final int rainbow = Color.HSBtoRGB(invert ? (1.0f - time) : time, 0.9f, 0.9f);
        final int r = rainbow >> 16 & 0xFF;
        final int g = rainbow >> 8 & 0xFF;
        final int b = rainbow & 0xFF;
        final int a = (int)(alpha * 255.0f);
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    public static boolean isHovered(double x, double y, double width, double height, int mouseX, int mouseY) {
        return mouseX > x && mouseY > y && mouseX < width && mouseY < height;
    }

    public static void doGlScissor(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        int scaleFactor = 1;
        int k = mc.gameSettings.guiScale;
        if (k == 0) {
            k = 1000;
        }
        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320
                && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        GL11.glScissor((int) (x * scaleFactor), (int) (mc.displayHeight - (y + height) * scaleFactor),
                (int) (width * scaleFactor), (int) (height * scaleFactor));
    }


    public static void drawAndRotateArrow(float x, float y, float size, boolean rotate) {
        glPushMatrix();
        glTranslatef(x, y + (rotate ? size / 2 : 0), 1.0F);
        startBlending();
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glLineWidth(1.0F);
        if (rotate)
            glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_LINE_STRIP);
        glVertex2f(0, 0);
        glVertex2f(size / 2, size / 2);
        glVertex2f(size, 0);
        glEnd();
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        endBlending();
        glPopMatrix();
    }

    public static void startBlending() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void endBlending() {
        GL11.glDisable(GL11.GL_BLEND);
    }




    public static void endScissorBox() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public static void drawOutlineRect(double left, double top, double right, double bottom, double lineStrength, Color color) {
        drawRect(left, top, right, top + lineStrength, color.getRGB());
        drawRect(left, bottom - lineStrength, right, bottom, color.getRGB());
        drawRect(left, top, left + lineStrength, bottom, color.getRGB());
        drawRect(right - lineStrength, top, right, bottom, color.getRGB());
    }

    public static void drawRoundedRect2(int x, int y, int width, int height, int cornerRadius, Color color) {
        Gui.drawRect(x, y + cornerRadius, x + cornerRadius, y + height - cornerRadius, color.getRGB());
        Gui.drawRect(x + cornerRadius, y, x + width - cornerRadius, y + height, color.getRGB());
        Gui.drawRect(x + width - cornerRadius, y + cornerRadius, x + width, y + height - cornerRadius, color.getRGB());

        drawArc(x + cornerRadius, y + cornerRadius, cornerRadius, 0, 90, color);
        drawArc(x + width - cornerRadius, y + cornerRadius, cornerRadius, 270, 360, color);
        drawArc(x + width - cornerRadius, y + height - cornerRadius, cornerRadius, 180, 270, color);
        drawArc(x + cornerRadius, y + height - cornerRadius, cornerRadius, 90, 180, color);
    }

    public static void disableTexture2D() {
        GL11.glDisable(3553);
    }

    public static void enableTexture2D() {
        GL11.glEnable(3553);
    }

    public static void enableDepth() {
        GL11.glDepthMask(true);
        GL11.glEnable(2929);
    }

    public static void disableDepth() {
        GL11.glDepthMask(false);
        GL11.glDisable(2929);
    }

    public static void disableBlending() {
        GL11.glDisable(3042);
    }

    public static void createScissorBox(double x, double y, double x2, double y2) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int factor = sr.getScaleFactor();
        GL11.glScissor((int) (x * factor), (int) ((sr.getScaledHeight() - y2) * factor), (int) ((x2 - x) * factor), (int) ((y2 - y) * factor));
    }


    public static int getRainbow(int speed, int offset) {
        return Color.HSBtoRGB(((System.currentTimeMillis() + (offset * 100)) % speed) / (float) speed, 0.55F, 0.9F);
    }

    public static void drawBorderedRect(double left, double top, double right, double bottom, double borderWidth, int insideColor, int borderColor, boolean borderIncludedInBounds) {
        drawRect(left - (!borderIncludedInBounds ? borderWidth : 0), top - (!borderIncludedInBounds ? borderWidth : 0), right + (!borderIncludedInBounds ? borderWidth : 0), bottom + (!borderIncludedInBounds ? borderWidth : 0), borderColor);
        drawRect(left + (borderIncludedInBounds ? borderWidth : 0), top + (borderIncludedInBounds ? borderWidth : 0), right - ((borderIncludedInBounds ? borderWidth : 0)), bottom - ((borderIncludedInBounds ? borderWidth : 0)), insideColor);
    }

    public static void drawGradientSideways(double left, double top, double right, double bottom, int col1, int col2) {
        float f = (col1 >> 24 & 255) / 255.0f;
        float f1 = (col1 >> 16 & 255) / 255.0f;
        float f2 = (col1 >> 8 & 255) / 255.0f;
        float f3 = (col1 & 255) / 255.0f;
        float f4 = (col2 >> 24 & 255) / 255.0f;
        float f5 = (col2 >> 16 & 255) / 255.0f;
        float f6 = (col2 >> 8 & 255) / 255.0f;
        float f7 = (col2 & 255) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);
        GL11.glPushMatrix();
        GL11.glBegin(7);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glVertex2d(left, top);
        GL11.glVertex2d(left, bottom);
        GL11.glColor4f(f5, f6, f7, f4);
        GL11.glVertex2d(right, bottom);
        GL11.glVertex2d(right, top);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
    }

    public static void drawHollowRect(int x, int y, int w, int h, int color) {

        Gui.drawHorizontalLine(x, x + w, y, color);
        Gui.drawHorizontalLine(x, x + w, y + h, color);

        Gui.drawVerticalLine(x, y + h, y, color);
        Gui.drawVerticalLine(x + w, y + h, y, color);

    }

    public static void drawGuiBackground(final int width, final int height) {
        Gui.drawRect(0.0f, 0.0f, (float)width, (float)height, -14144460);
    }

    public static void drawLine3D(float x, float y, float z, float x1, float y1, float z1, int color) {
        pre3D();
        GL11.glLoadIdentity();
        Minecraft.getMinecraft().entityRenderer.orientCamera(Minecraft.getMinecraft().timer.renderPartialTicks);
        float var11 = (color >> 24 & 0xFF) / 255.0F;
        float var6 = (color >> 16 & 0xFF) / 255.0F;
        float var7 = (color >> 8 & 0xFF) / 255.0F;
        float var8 = (color & 0xFF) / 255.0F;
        GL11.glColor4f(var6, var7, var8, var11);
        GL11.glLineWidth(0.5f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(x, y, z);
        GL11.glVertex3d(x1, y1, z1);
        GL11.glEnd();
        post3D();
    }

    public static void connectPoints(float xOne, float yOne, float xTwo, float yTwo) {
        glPushMatrix();
        glEnable(GL_LINE_SMOOTH);
        glColor4f(1.0F, 1.0F, 1.0F, 0.8F);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);
        glLineWidth(0.5F);
        glBegin(GL_LINES);
        glVertex2f(xOne, yOne);
        glVertex2f(xTwo, yTwo);
        glEnd();
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }


    public static void drawBordered(double x2, double y2, double width, double height, double length, int innerColor, int outerColor) {
        Gui.drawRect(x2, y2, x2 + width, y2 + height, innerColor);
        Gui.drawRect(x2 - length, y2, x2, y2 + height, outerColor);
        Gui.drawRect(x2 - length, y2 - length, x2 + width, y2, outerColor);
        Gui.drawRect(x2 + width, y2 - length, x2 + width + length, y2 + height + length, outerColor);
        Gui.drawRect(x2 - length, y2 + height, x2 + width, y2 + height + length, outerColor);
    }

    public static void drawImage(GuiIngame gui, ResourceLocation fileLocation, int x, int y, int w, int h, float fw, float fh, float u, float v) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(fileLocation);
        Gui.drawModalRectWithCustomSizedTexture(x, y, u, v, w, h, fw, fh);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawImage(ResourceLocation image, float x, float y, int width, int height) {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        float f = 1.0f / (float)width;
        float f1 = 1.0f / (float)height;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + (float)height, 0.0).tex(0.0f * f, (float)height * f1).endVertex();
        worldrenderer.pos(x + (float)width, y + (float)height, 0.0).tex((float)width * f, (float)height * f1).endVertex();
        worldrenderer.pos(x + (float)width, y, 0.0).tex((float)width * f, 0.0f * f1).endVertex();
        worldrenderer.pos(x, y, 0.0).tex(0.0f * f, 0.0f * f1).endVertex();
        tessellator.draw();
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }

    public static void drawImage(float x, float y, float width, float height, float r, float g, float b, ResourceLocation image) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        float f = 1.0f / width;
        float f1 = 1.0f / height;
        GL11.glColor4f(r, g, b, 1.0f);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0).tex(0.0, height * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0).tex(width * f, height * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0).tex(width * f, 0.0).endVertex();
        worldrenderer.pos(x, y, 0.0).tex(0.0, 0.0).endVertex();
        tessellator.draw();
    }

    public static void drawImage(ResourceLocation image, int x, int y, int width, int height) {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }

    public static void drawCircle(float cx, float cy, float r, final int num_segments, final int c) {
        GL11.glPushMatrix();
        cx *= 2.0f;
        cy *= 2.0f;
        final float f = (c >> 24 & 0xFF) / 255.0f;
        final float f2 = (c >> 16 & 0xFF) / 255.0f;
        final float f3 = (c >> 8 & 0xFF) / 255.0f;
        final float f4 = (c & 0xFF) / 255.0f;
        final float theta = (float)(6.2831852 / num_segments);
        final float p = (float)Math.cos(theta);
        final float s = (float)Math.sin(theta);
        float x;
        r = (x = r * 2.0f);
        float y = 0.0f;
        enableGL2D();
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        GL11.glColor4f(f2, f3, f4, f);
        GL11.glBegin(2);
        for (int ii = 0; ii < num_segments; ++ii) {
            GL11.glVertex2f(x + cx, y + cy);
            final float t = x;
            x = p * x - s * y;
            y = s * t + p * y;
        }
        GL11.glEnd();
        GL11.glScalef(2.0f, 2.0f, 2.0f);
        disableGL2D();
        GL11.glPopMatrix();
    }

    public static void enableGL2D() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void disableGL2D() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static void drawCircle(double x, double y, double radius, int c) {
        GL11.glEnable(GL_MULTISAMPLE);
        GL11.glEnable(GL_POLYGON_SMOOTH);
        float alpha = (float) (c >> 24 & 255) / 255.0f;
        float red = (float) (c >> 16 & 255) / 255.0f;
        float green = (float) (c >> 8 & 255) / 255.0f;
        float blue = (float) (c & 255) / 255.0f;
        boolean blend = GL11.glIsEnabled((int) 3042);
        boolean line = GL11.glIsEnabled((int) 2848);
        boolean texture = GL11.glIsEnabled((int) 3553);
        if (!blend) {
            GL11.glEnable((int) 3042);
        }
        if (!line) {
            GL11.glEnable((int) 2848);
        }
        if (texture) {
            GL11.glDisable((int) 3553);
        }
        GL11.glBlendFunc((int) 770, (int) 771);
        GL11.glColor4f((float) red, (float) green, (float) blue, (float) alpha);
        GL11.glBegin((int) 9);
        int i = 0;
        while (i <= 360) {
            GL11.glVertex2d(
                    (double) ((double) x + Math.sin((double) ((double) i * 3.141526 / 180.0)) * (double) radius),
                    (double) ((double) y + Math.cos((double) ((double) i * 3.141526 / 180.0)) * (double) radius));
            ++i;
        }
        GL11.glEnd();
        if (texture) {
            GL11.glEnable((int) 3553);
        }
        if (!line) {
            GL11.glDisable((int) 2848);
        }
        if (!blend) {
            GL11.glDisable((int) 3042);
        }
        GL11.glDisable(GL_POLYGON_SMOOTH);
        GL11.glClear(0);
    }

    public static void drawRoundRect(float x, float y, float x1, float y1, int color) {
        drawRoundedRect(x, y, x1, y1, color, color);
        GlStateManager.color(1,1,1);
    }

    public static void drawRoundedRect(float x, float y, float x1, float y1, int borderC, int insideC) {
        R2DUtils.enableGL2D();
        GL11.glScalef((float) 0.5f, (float) 0.5f, (float) 0.5f);
        R2DUtils.drawVLine(x *= 2.0f, (y *= 2.0f) + 1.0f, (y1 *= 2.0f) - 2.0f, borderC);
        R2DUtils.drawVLine((x1 *= 2.0f) - 1.0f, y + 1.0f, y1 - 2.0f, borderC);
        R2DUtils.drawHLine(x + 2.0f, x1 - 3.0f, y, borderC);
        R2DUtils.drawHLine(x + 2.0f, x1 - 3.0f, y1 - 1.0f, borderC);
        R2DUtils.drawHLine(x + 1.0f, x + 1.0f, y + 1.0f, borderC);
        R2DUtils.drawHLine(x1 - 2.0f, x1 - 2.0f, y + 1.0f, borderC);
        R2DUtils.drawHLine(x1 - 2.0f, x1 - 2.0f, y1 - 2.0f, borderC);
        R2DUtils.drawHLine(x + 1.0f, x + 1.0f, y1 - 2.0f, borderC);
        R2DUtils.drawRect(x + 1.0f, y + 1.0f, x1 - 1.0f, y1 - 1.0f, insideC);
        GL11.glScalef((float) 2.0f, (float) 2.0f, (float) 2.0f);
        R2DUtils.disableGL2D();
        Gui.drawRect(0, 0, 0, 0, 0);
    }

    public static void drawGradientRect(float x, float y, float x1, float y1, int topColor, int bottomColor) {
        R2DUtils.enableGL2D();
        GL11.glShadeModel((int) 7425);
        GL11.glBegin((int) 7);
        RenderUtils.glColor(topColor);
        GL11.glVertex2f((float) x, (float) y1);
        GL11.glVertex2f((float) x1, (float) y1);
        RenderUtils.glColor(bottomColor);
        GL11.glVertex2f((float) x1, (float) y);
        GL11.glVertex2f((float) x, (float) y);
        GL11.glEnd();
        GL11.glShadeModel((int) 7424);
        R2DUtils.disableGL2D();
    }

    public static void drawGradientRect(final double left, final double top, final double right, final double bottom, final boolean sideways, final int startColor, final int endColor) {
        GL11.glDisable(3553);
        enableBlending();
        GL11.glShadeModel(7425);
        GL11.glBegin(7);
        color(startColor);
        if (sideways) {
            GL11.glVertex2d(left, top);
            GL11.glVertex2d(left, bottom);
            color(endColor);
            GL11.glVertex2d(right, bottom);
            GL11.glVertex2d(right, top);
        }
        else {
            GL11.glVertex2d(left, top);
            color(endColor);
            GL11.glVertex2d(left, bottom);
            GL11.glVertex2d(right, bottom);
            color(startColor);
            GL11.glVertex2d(right, top);
        }
        GL11.glEnd();
        GL11.glDisable(3042);
        GL11.glShadeModel(7424);
        GL11.glEnable(3553);
    }



    public static void enableBlending() {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
    }

    public static int darker(final int color, final float factor) {
        final int r = (int)((color >> 16 & 0xFF) * factor);
        final int g = (int)((color >> 8 & 0xFF) * factor);
        final int b = (int)((color & 0xFF) * factor);
        final int a = color >> 24 & 0xFF;
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF) | (a & 0xFF) << 24;
    }

    public static int drawHealth(final EntityLivingBase entityLivingBase) {
        final float health = entityLivingBase.getHealth();
        final float maxHealth = entityLivingBase.getMaxHealth();
        return Color.HSBtoRGB(Math.max(0.0f, Math.min(health, maxHealth) / maxHealth) / 3.0f, 1.0f, 1.0f) | 0xFF000000;
    }

    public static void startScissorBox(final ScaledResolution sr, final int x, final int y, final int width, final int height) {
        final int sf = sr.getScaleFactor();
        GL11.glScissor(x * sf, (sr.getScaledHeight() - (y + height)) * sf, width * sf, height * sf);
    }

    public static void startScissorBox(final LockedResolution lr, final int x, final int y, final int width, final int height) {
        GL11.glScissor(x * 2, (lr.getHeight() - (y + height)) * 2, width * 2, height * 2);
    }


    public static LockedResolution getLockedResolution() {
        final int width = Display.getWidth();
        final int height = Display.getHeight();
        if (width != RenderUtils.lastWidth || height != RenderUtils.lastHeight) {
            RenderUtils.lastWidth = width;
            RenderUtils.lastHeight = height;
            return RenderUtils.lockedResolution = new LockedResolution(width / 2, height / 2);
        }
        return RenderUtils.lockedResolution;
    }

    public static void drawRadius(Entity entity, double radius, float partialTicks, int points, float width, int color) {
        glPushMatrix();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glDisable(GL_DEPTH_TEST);
        glLineWidth(width);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        glBegin(GL_LINE_STRIP);
        final double x = RenderUtils.interpolate(entity.prevPosX, entity.posX, partialTicks) - RenderManager.viewerPosX;
        final double y = RenderUtils.interpolate(entity.prevPosY, entity.posY, partialTicks) - RenderManager.viewerPosY;
        final double z = RenderUtils.interpolate(entity.prevPosZ, entity.posZ, partialTicks) - RenderManager.viewerPosZ;
        RenderUtils.color(color);
        for (int i = 0; i <= points; i++) glVertex3d(x + radius * Math.cos(i * DOUBLE_PI / points), y, z + radius * Math.sin(i * DOUBLE_PI / points));
        glEnd();
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    public static void drawHat(Entity entity, double radius, float partialTicks, int points, float width, float yAdd, int color) {
        glPushMatrix();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glDisable(GL_DEPTH_TEST);
        glLineWidth(width);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        glBegin(GL_LINE_STRIP);

        final double x = RenderUtils.interpolate(entity.prevPosX, entity.posX, partialTicks) - RenderManager.viewerPosX;
        final double y = RenderUtils.interpolate(entity.prevPosY + yAdd, entity.posY + yAdd, partialTicks) - RenderManager.viewerPosY;
        final double z = RenderUtils.interpolate(entity.prevPosZ, entity.posZ, partialTicks) - RenderManager.viewerPosZ;

        for (int i = 0; i <= points; i++) {
            glVertex3d(x + radius * Math.cos(i * (Math.PI * 2) / points), y, z + radius * Math.sin(i * (Math.PI * 2) / points));
            RenderUtils.color(color);
        }

        glEnd();
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    public static void color(int color) {
        glColor4ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF), (byte) (color >> 24 & 0xFF));
    }

    public static void drawArc(int x, int y, int radius, int startAngle, int endAngle, Color color) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, (float) color.getAlpha() / 255);

        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();

        worldRenderer.begin(6, DefaultVertexFormats.POSITION);
        worldRenderer.pos(x, y, 0).endVertex();

        for (int i = (int) (startAngle / 360.0 * 100); i <= (int) (endAngle / 360.0 * 100); i++) {
            double angle = (Math.PI * 2 * i / 100) + Math.toRadians(180);
            worldRenderer.pos(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, 0).endVertex();
        }

        Tessellator.getInstance().draw();

        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawFilledCircle(int xx, int yy, float radius, Color color) {
        int sections = 50;
        double dAngle = 6.283185307179586D / sections;
        GL11.glPushAttrib(8192);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glBegin(6);
        for (int i = 0; i < sections; i++) {
            float x = (float)(radius * Math.sin(i * dAngle));
            float y = (float)(radius * Math.cos(i * dAngle));
            GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
            GL11.glVertex2f(xx + x, yy + y);
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnd();
        GL11.glPopAttrib();
    }

    public static final void drawBox(BlockPos pos, Color color, boolean depth) {
        final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        final Timer timer = Minecraft.getMinecraft().timer;

        final double x = pos.getX() - renderManager.renderPosX;
        final double y = pos.getY() - renderManager.renderPosY;
        final double z = pos.getZ() - renderManager.renderPosZ;

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0);
        final Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();

        if (block != null) {
            final EntityPlayer player = Minecraft.getMinecraft().thePlayer;

            final double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) timer.renderPartialTicks;
            final double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) timer.renderPartialTicks;
            final double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) timer.renderPartialTicks;
            axisAlignedBB = block.getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, pos).expand(.002, .002, .002).offset(-posX, -posY, -posZ);

            drawAxisAlignedBBFilled(axisAlignedBB, color, depth);
        }
    }

    public static void drawAxisAlignedBBFilled(AxisAlignedBB axisAlignedBB, Color color, boolean depth) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        if (depth) GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        Render2DUtils.color(color);
        drawBoxFilled(axisAlignedBB);
        GlStateManager.resetColor();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        if (depth) GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void drawBoxFilled(AxisAlignedBB axisAlignedBB) {
        GL11.glBegin(GL11.GL_QUADS);
        {
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);

            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);

            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);

            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);

            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);

            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);

            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);

            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);

            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);

            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);

            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);

            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
            GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
            GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
        }
        GL11.glEnd();
    }

    public static void setColor(final int colorHex) {
        final float alpha = (colorHex >> 24 & 0xFF) / 255.0f;
        final float red = (colorHex >> 16 & 0xFF) / 255.0f;
        final float green = (colorHex >> 8 & 0xFF) / 255.0f;
        final float blue = (colorHex & 0xFF) / 255.0f;
        GL11.glColor4f(red, green, blue, (alpha == 0.0f) ? 1.0f : alpha);
    }

    public static void pre3D() {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glShadeModel(7425);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDisable(2896);
        GL11.glDepthMask(false);
        GL11.glHint(3154, 4354);
    }

    public static void post3D() {
        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (!GL11.glIsEnabled(2896)) {
            GL11.glEnable(2896);
        }

    }

    public static void drawBorderRect(double x, double y, double x1, double y1, int color, double lwidth) {
        drawHLine(x, y, x1, y, (float) lwidth, color);
        drawHLine(x1, y, x1, y1, (float) lwidth, color);
        drawHLine(x, y1, x1, y1, (float) lwidth, color);
        drawHLine(x, y1, x, y, (float) lwidth, color);
    }

    public static void drawHLine(double x, double y, double x1, double y1, float width, int color) {
        float var11 = (color >> 24 & 0xFF) / 255.0F;
        float var6 = (color >> 16 & 0xFF) / 255.0F;
        float var7 = (color >> 8 & 0xFF) / 255.0F;
        float var8 = (color & 0xFF) / 255.0F;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var6, var7, var8, var11);
        glPushMatrix();
        glLineWidth(width);
        glBegin(GL_LINE_STRIP);
        glVertex2d(x, y);
        glVertex2d(x1, y1);
        glEnd();

        glLineWidth(1);


        glPopMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);

    }

    public static void glColor(int hex) {
        float alpha = (float)(hex >> 24 & 255) / 255.0F;
        float red = (float)(hex >> 16 & 255) / 255.0F;
        float green = (float)(hex >> 8 & 255) / 255.0F;
        float blue = (float)(hex & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void drawBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
    }

    public static ScaledResolution getScaledResolution() {
        int displayWidth = Display.getWidth();
        int displayHeight = Display.getHeight();
        int guiScale = Minecraft.getMinecraft().gameSettings.guiScale;

        if (displayWidth != lastScaledWidth ||
                displayHeight != lastScaledHeight ||
                guiScale != lastGuiScale) {
            lastScaledWidth = displayWidth;
            lastScaledHeight = displayHeight;
            lastGuiScale = guiScale;
            return scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        }

        return scaledResolution;
    }

    public static double transition(double now, double desired, double speed) {
        final double dif = Math.abs(now - desired);

        final int fps = Minecraft.getDebugFPS();

        if (dif > 0) {
            double animationSpeed = MathUtils.roundToDecimalPlace(Math.min(10.0D, Math.max(0.0625D, (144.0D / fps) * (dif / 10) * speed)), 0.0625D);

            if (dif != 0 && dif < animationSpeed)
                animationSpeed = dif;

            if (now < desired)
                return now + animationSpeed;
            else if (now > desired)
                return now - animationSpeed;
        }

        return now;
    }


    public static void rectangle(double left, double top, double right, double bottom, int color) {
        double var5;
        if (left < right) {
            var5 = left;
            left = right;
            right = var5;
        }

        if (top < bottom) {
            var5 = top;
            top = bottom;
            bottom = var5;
        }

        float var11 = (float)(color >> 24 & 255) / 255.0F;
        float var6 = (float)(color >> 16 & 255) / 255.0F;
        float var7 = (float)(color >> 8 & 255) / 255.0F;
        float var8 = (float)(color & 255) / 255.0F;
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var6, var7, var8, var11);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(left, bottom, 0.0D);
        worldRenderer.pos(right, bottom, 0.0D);
        worldRenderer.pos(right, top, 0.0D);
        worldRenderer.pos(left, top, 0.0D);
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawRect(double left, double top, double right, double bottom, int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double) left, (double) bottom, 0.0D).endVertex();
        worldrenderer.pos((double) right, (double) bottom, 0.0D).endVertex();
        worldrenderer.pos((double) right, (double) top, 0.0D).endVertex();
        worldrenderer.pos((double) left, (double) top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawImage(float x, float y, final int width, final int height, final ResourceLocation image, Color color) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, (float) width, (float) height);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }


    public static class R2DUtils {
        public static void enableGL2D() {
            glDisable((int) 2929);
            glEnable((int) 3042);
            glDisable((int) 3553);
            GL11.glBlendFunc((int) 770, (int) 771);
            GL11.glDepthMask((boolean) true);
            glEnable((int) 2848);
            GL11.glHint((int) 3154, (int) 4354);
            GL11.glHint((int) 3155, (int) 4354);
        }

        public static void disableGL2D() {
            glEnable((int) 3553);
            glDisable((int) 3042);
            glEnable((int) 2929);
            glDisable((int) 2848);
            GL11.glHint((int) 3154, (int) 4352);
            GL11.glHint((int) 3155, (int) 4352);
        }

        public static void drawRect(double x2, double y2, double x1, double y1, int color) {
            R2DUtils.enableGL2D();
            R2DUtils.glColor(color);
            R2DUtils.drawRect(x2, y2, x1, y1);
            R2DUtils.disableGL2D();
        }

        private static void drawRect(double x2, double y2, double x1, double y1) {
            GL11.glBegin((int) 7);
            GL11.glVertex2d((double) x2, (double) y1);
            GL11.glVertex2d((double) x1, (double) y1);
            GL11.glVertex2d((double) x1, (double) y2);
            GL11.glVertex2d((double) x2, (double) y2);
            GL11.glEnd();
        }

        public static void glColor(int hex) {
            float alpha = (float) (hex >> 24 & 255) / 255.0f;
            float red = (float) (hex >> 16 & 255) / 255.0f;
            float green = (float) (hex >> 8 & 255) / 255.0f;
            float blue = (float) (hex & 255) / 255.0f;
            GL11.glColor4f((float) red, (float) green, (float) blue, (float) alpha);
        }

        public static void drawRect(float x, float y, float x1, float y1, int color) {
            R2DUtils.enableGL2D();
            glColor(color);
            R2DUtils.drawRect(x, y, x1, y1);
            R2DUtils.disableGL2D();
        }

        public static void drawGradientRect(float x, float y, float x1, float y1, int topColor, int bottomColor) {
            R2DUtils.enableGL2D();
            GL11.glShadeModel((int) 7425);
            GL11.glBegin((int) 7);
            glColor(topColor);
            GL11.glVertex2f((float) x, (float) y1);
            GL11.glVertex2f((float) x1, (float) y1);
            glColor(bottomColor);
            GL11.glVertex2f((float) x1, (float) y);
            GL11.glVertex2f((float) x, (float) y);
            GL11.glEnd();
            GL11.glShadeModel((int) 7424);
            R2DUtils.disableGL2D();
        }

        public static void drawHLine(float x, float y, float x1, int y1) {
            if (y < x) {
                float var5 = x;
                x = y;
                y = var5;
            }
            R2DUtils.drawRect(x, x1, y + 1.0f, x1 + 1.0f, y1);
        }

        public static void drawVLine(float x, float y, float x1, int y1) {
            if (x1 < y) {
                float var5 = y;
                y = x1;
                x1 = var5;
            }
            R2DUtils.drawRect(x, y + 1.0f, x + 1.0f, x1, y1);
        }



        public static void drawRect(float x, float y, float x1, float y1) {
            GL11.glBegin((int) 7);
            GL11.glVertex2f((float) x, (float) y1);
            GL11.glVertex2f((float) x1, (float) y1);
            GL11.glVertex2f((float) x1, (float) y);
            GL11.glVertex2f((float) x, (float) y);
            GL11.glEnd();
        }
    }



    public static void pre() {
        glDisable(2929);
        glDisable(3553);
        glEnable(3042);
        GL11.glBlendFunc(770, 771);
    }

    public static void post() {
        glDisable(3042);
        glEnable(3553);
        glEnable(2929);
        GL11.glColor3d(1.0, 1.0, 1.0);
    }

    public static double lerp(double v0, double v1, double t) {
        return (1.0 - t) * v0 + t * v1;
    }


    public static double interpolate(double old, double now, float partialTicks) {
        return old + (now - old) * partialTicks;
    }


    private final static Frustum frustrum = new Frustum();

    public static boolean isInViewFrustrum(Entity entity) {
        return isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    private static boolean isInViewFrustrum(AxisAlignedBB bb) {
        Entity current = Minecraft.getMinecraft().getRenderViewEntity();
        frustrum.setPosition(current.posX, current.posY, current.posZ);
        return frustrum.isBoundingBoxInFrustum(bb);
    }

    public static void renderCircle(EventRender3D event, float height, float alpha) {
        Entity entity = KillAura.target;
        double rad = 0.6;
        float points = 90.0F;
        GlStateManager.enableDepth();
        GL11.glPushMatrix();
        GL11.glDisable(3553);
        GL11.glEnable(2881);
        GL11.glEnable(2832);
        GL11.glEnable(3042);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBlendFunc(770, 771);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glHint(3153, 4354);
        GL11.glDisable(2929);
        GL11.glLineWidth(1.3F);
        GL11.glBegin(3);
        GlStateManager.color(84, 95, 255, alpha);

        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.getPartialTicks()
                - RenderManager.renderPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.getPartialTicks()
                - RenderManager.renderPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.getPartialTicks()
                - RenderManager.renderPosZ;
        for (int i = 0; i <= 90; i++) {
            GL11.glColor4f(84, 95, 255, alpha);
            GL11.glVertex3d(x + rad * Math.cos(i * 6.283185307179586D / points), y + height,
                    z + rad * Math.sin(i * 6.283185307179586D / points));
        }

        GL11.glEnd();
        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glDisable(2881);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(2832);
        GL11.glEnable(3553);
        GL11.glPopMatrix();
    }

    public static void drawTracerPointer(float x, float y, float size, float widthDiv, float heightDiv, boolean outline, float outlineWidth, int color) {
        boolean blend = GL11.glIsEnabled(3042);
        float alpha = (float) (color >> 24 & 0xFF) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        hexColor(color);
        GL11.glBegin(7);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x - size / widthDiv, y + size);
        GL11.glVertex2d(x, y + size / heightDiv);
        GL11.glVertex2d(x + size / widthDiv, y + size);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
        if (outline) {
            GL11.glLineWidth(outlineWidth);
            GL11.glColor4f(0.0f, 0.0f, 0.0f, alpha);
            GL11.glBegin(2);
            GL11.glVertex2d(x, y);
            GL11.glVertex2d(x - size / widthDiv, y + size);
            GL11.glVertex2d(x, y + size / heightDiv);
            GL11.glVertex2d(x + size / widthDiv, y + size);
            GL11.glVertex2d(x, y);
            GL11.glEnd();
        }
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        if (!blend) {
            GL11.glDisable(3042);
        }
        GL11.glDisable(2848);
    }

    private static final FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
    private static final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer projection = BufferUtils.createFloatBuffer(16);

    public static Vec3 to2D(double x, double y, double z) {
        GL11.glGetFloat(2982, modelView);
        GL11.glGetFloat(2983, projection);
        GL11.glGetInteger(2978, viewport);
        boolean result = GLU.gluProject((float) x, (float) y, (float) z, modelView, projection, viewport, screenCoords);
        if (result) {
            return new Vec3(screenCoords.get(0), (float) Display.getHeight() - screenCoords.get(1), screenCoords.get(2));
        }
        return null;
    }

    public static Color getColor(final EntityPlayer entity, final Color colorValue, final int alpha, final boolean colorFriends) {
        Color color = new Color(colorValue.getRed() / 255.0f, colorValue.getGreen() / 255.0f, colorValue.getBlue() / 255.0f, alpha / 255.0f);
        return color;
    }
}
