package vip.Resolute.modules.impl.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventRender2D;
import vip.Resolute.events.impl.EventRender3D;
import vip.Resolute.events.impl.EventRenderNametag;
import vip.Resolute.modules.Module;
import vip.Resolute.modules.impl.exploit.HackerDetector;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ColorSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import org.lwjgl.util.glu.GLU;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.font.MinecraftFontRenderer;
import vip.Resolute.util.render.Colors;
import vip.Resolute.util.render.RenderUtils;

import java.util.List;


public class ESP extends Module {
    public static ModeSetting espMode = new ModeSetting("Mode", "Multi", "Multi", "Wireframe");

    public static ColorSetting wireColor = new ColorSetting("Wire Color", new Color(255, 255, 255), () -> espMode.is("Wireframe"));
    public static NumberSetting wireWidth = new NumberSetting("Wire Width", 2.0D, () -> espMode.is("Wireframe"), 0.5D, 5.0D, 0.1D);

    public BooleanSetting boxProp = new BooleanSetting("Box", true, () -> espMode.is("Multi"));

    public ModeSetting boxMode = new ModeSetting("2D Mode", "Corner", () -> boxProp.isEnabled() && boxProp.isAvailable(), "Corner", "Box");

    public ColorSetting colorProp = new ColorSetting("Color", new Color(11010027));

    public NumberSetting boxWidth = new NumberSetting("Box Width", 0.5, () -> boxProp.isEnabled() && boxProp.isAvailable(), 0.1, 1.0, 0.1);
    public BooleanSetting armorProp = new BooleanSetting("Armor", true, () -> espMode.is("Multi"));
    public BooleanSetting healthProp = new BooleanSetting("Health", true, () -> espMode.is("Multi"));
    public BooleanSetting itemProp = new BooleanSetting("Item", true, () -> espMode.is("Multi"));
    public BooleanSetting nametagProp = new BooleanSetting("Nametag", true, () -> espMode.is("Multi"));

    private final DecimalFormat decimalFormat = new DecimalFormat("0.0#", new DecimalFormatSymbols(Locale.ENGLISH));
    private static final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer projection = BufferUtils.createFloatBuffer(16);
    private static final IntBuffer viewport = BufferUtils.createIntBuffer(16);
    private final List<Vec3> positions = new ArrayList<Vec3>();

    public static boolean enabled = false;

    public ESP() {
        super("ESP", 0, "", Category.RENDER);
        this.addSettings(espMode, wireColor, wireWidth, boxProp, boxMode, colorProp, boxWidth,
                armorProp, healthProp, itemProp, nametagProp);
    }

    public void onEnable() {
        enabled = true;
    }

    public void onDisable() {
        enabled = false;
    }

    public void onEvent(Event e) {
        this.setSuffix(espMode.getMode());

        if(e instanceof EventRenderNametag) {
            if(espMode.is("Multi")) {
                if(nametagProp.isEnabled()) {
                    e.setCancelled(true);
                }
            }
        }

        if(e instanceof EventRender3D) {
            if(espMode.is("Multi")) {
                if (this.boxMode.is("Box")) {
                    for (Entity entity : ESP.mc.theWorld.loadedEntityList) {
                        if (!(entity instanceof EntityItem) && !this.isValid(entity)) continue;
                        ESP.updateView();
                    }
                }
                if (this.boxMode.is("Corner")) {
                    for (Entity entity : ESP.mc.theWorld.loadedEntityList) {
                        if (!(entity instanceof EntityItem) && !this.isValid(entity)) continue;
                        ESP.updateView();
                    }
                }
            }
        }

        if(e instanceof EventRender2D) {
            if(espMode.is("Multi")) {
                ScaledResolution sr = new ScaledResolution(mc);
                GlStateManager.pushMatrix();
                GL11.glDisable(2929);
                double twoScale = (double)sr.getScaleFactor() / Math.pow(sr.getScaleFactor(), 2.0);
                GlStateManager.scale(twoScale, twoScale, twoScale);
                for (Entity entity : ESP.mc.theWorld.loadedEntityList) {
                    if (!this.isValid(entity)) continue;
                    this.updatePositions(entity);
                    int maxLeft = Integer.MAX_VALUE;
                    int maxRight = Integer.MIN_VALUE;
                    int maxBottom = Integer.MIN_VALUE;
                    int maxTop = Integer.MAX_VALUE;
                    Iterator<Vec3> iterator2 = this.positions.iterator();
                    boolean canEntityBeSeen = false;
                    while (iterator2.hasNext()) {
                        Vec3 screenPosition = ESP.WorldToScreen(iterator2.next());
                        if (screenPosition == null || !(screenPosition.zCoord >= 0.0) || !(screenPosition.zCoord < 1.0)) continue;
                        maxLeft = (int)Math.min(screenPosition.xCoord, (double)maxLeft);
                        maxRight = (int)Math.max(screenPosition.xCoord, (double)maxRight);
                        maxBottom = (int)Math.max(screenPosition.yCoord, (double)maxBottom);
                        maxTop = (int)Math.min(screenPosition.yCoord, (double)maxTop);
                        canEntityBeSeen = true;
                    }
                    if (!canEntityBeSeen) continue;
                    Gui.drawRect(0.0f, 0.0f, 0.0f, 0.0f, 0);
                    if (this.healthProp.isEnabled()) {
                        this.drawHealth((EntityLivingBase)entity, maxLeft, maxTop, maxRight, maxBottom);
                    }
                    if (this.armorProp.isEnabled()) {
                        this.drawArmor((EntityLivingBase)entity, maxLeft, maxTop, maxRight, maxBottom);
                    }
                    if (this.boxProp.isEnabled()) {
                        this.drawBox(entity, maxLeft, maxTop, maxRight, maxBottom);
                    }
                    if (((EntityPlayer)entity).getCurrentEquippedItem() != null && this.itemProp.isEnabled()) {
                        this.drawItem(entity, maxLeft, maxTop, maxRight, maxBottom);
                    }
                    if (!this.nametagProp.isEnabled()) continue;
                    this.drawName(entity, maxLeft, maxTop, maxRight, maxBottom);
                }
                GL11.glEnable(2929);
                GlStateManager.popMatrix();
            }
        }
    }

    private void drawName(Entity e, int left, int top, int right, int bottom) {
        EntityPlayer ent = (EntityPlayer)e;
        String renderName = getPing(ent) + "ms " + ent.getName();
        MinecraftFontRenderer font = FontUtil.tahomaVerySmall;
        float meme2 = (float)((double)(right - left) / 2.0 - (double)font.getStringWidth(renderName));
        float halfWidth = font.getHeight() / 2.0f;
        float xDif = right - left;
        float middle = (float)left + xDif / 2.0f;
        float textHeight = font.getHeight();
        float renderY = (float)top - textHeight - 2.0f;
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0f, -1.0f, 0.0f);
        if (HackerDetector.enabled && HackerDetector.isHacker(ent) || isTeamMate(ent)) {
            RenderUtils.drawRect(middle - halfWidth * 2.0f - 2.0f, renderY - 10.0f, middle + halfWidth * 2.0f + 2.0f, renderY + textHeight - 0.5f, new Color(0, 0, 0).getRGB());
            RenderUtils.drawRect(middle - halfWidth * 2.0f - 1.0f, renderY - 9.0f, middle + halfWidth * 2.0f + 0.5f, renderY + textHeight - 1.5f, this.getColor(ent));
        }
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        font.drawStringWithShadow(renderName, ((float)left + meme2) / 2.0f, ((float)top - font.getHeight() / 1.5f * 2.0f) / 2.0f - 4.0f, new Color(0, 0, 0, 210).getRGB());
        GlStateManager.popMatrix();
    }

    private void drawItem(Entity e, int left, int top, int right, int bottom) {
        EntityPlayer ent = (EntityPlayer)e;
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        ItemStack stack = ent.getCurrentEquippedItem();
        String customName = this.nametagProp.isEnabled() != false ? ent.getCurrentEquippedItem().getDisplayName() : ent.getCurrentEquippedItem().getItem().getItemStackDisplayName(stack);
        float meme5 = (float)((double)(right - left) / 2.0 - (double)FontUtil.tahomaVerySmall.getStringWidth(customName));
        FontUtil.tahomaVerySmall.drawStringWithShadow(customName, ((float)left + meme5) / 2.0f, ((float)bottom + FontUtil.tahomaVerySmall.getHeight() / 2.0f * 2.0f) / 2.0f + 1.0f, -1);
        GlStateManager.popMatrix();
        if (stack != null) {
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemIntoGUI(stack, (int)((float)left + meme5) + 29, (int)((float)bottom + FontUtil.tahomaVerySmall.getHeight() / 2.0f * 2.0f) + 15);
            mc.getRenderItem().renderItemOverlays(ESP.mc.fontRendererObj, stack, (int)((float)left + meme5) + 29, (int)((float)bottom + FontUtil.tahomaVerySmall.getHeight() / 2.0f * 2.0f) + 15);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }

    public static int getPing(EntityPlayer entityPlayer) {
        if (entityPlayer == null) {
            return 0;
        }
        NetworkPlayerInfo networkPlayerInfo = mc.getNetHandler().getPlayerInfo(entityPlayer.getUniqueID());
        return networkPlayerInfo == null ? 0 : networkPlayerInfo.getResponseTime();
    }

    private void drawBox(Entity e, int left, int top, int right, int bottom) {
        int line = 1;
        int bg = new Color(0, 0, 0).getRGB();
        if (boxMode.is("Corner")) {
            int p_drawRect_0_ = left + (right - left) / 3 + line;
            int p_drawRect_2_ = right - (right - left) / 3 - line;
            int p_drawRect_3_ = top + (bottom - top) / 3 + line;
            int p_drawRect_3_1 = bottom - 1 - (bottom - top) / 3 - line;
            Gui.drawRect(left + 1 + line, top - line, left - line, p_drawRect_3_, bg);
            Gui.drawRect(p_drawRect_0_, top + line, left, top - 1 - line, bg);
            Gui.drawRect(right + line, top - line, right - 1 - line, p_drawRect_3_, bg);
            Gui.drawRect(right, top + line, p_drawRect_2_, top - 1 - line, bg);
            Gui.drawRect(left + 1 + line, bottom - 1 - line, left - line, p_drawRect_3_1, bg);
            Gui.drawRect(p_drawRect_0_, bottom + line, left - line, bottom - 1 - line, bg);
            Gui.drawRect(right + line, bottom - 1 + line, right - 1 - line, p_drawRect_3_1, bg);
            Gui.drawRect(right + line, bottom + line, p_drawRect_2_, bottom - 1 - line, bg);
            Gui.drawRect(left + 1, top, left, (float)top + (float)(bottom - top) / 3.0f, this.getColor(e).getRGB());
            Gui.drawRect((float)left + (float)(right - left) / 3.0f, top, left, top - 1, this.getColor(e).getRGB());
            Gui.drawRect(right, top, right - 1, (float)top + (float)(bottom - top) / 3.0f, this.getColor(e).getRGB());
            Gui.drawRect(right, top, (float)right - (float)(right - left) / 3.0f, top - 1, this.getColor(e).getRGB());
            Gui.drawRect(left + 1, bottom - 1, left, (float)(bottom - 1) - (float)(bottom - top) / 3.0f, this.getColor(e).getRGB());
            Gui.drawRect((float)left + (float)(right - left) / 3.0f, bottom, left, bottom - 1, this.getColor(e).getRGB());
            Gui.drawRect(right, bottom - 1, right - 1, (float)(bottom - 1) - (float)(bottom - top) / 3.0f, this.getColor(e).getRGB());
            Gui.drawRect(right, bottom, (float)right - (float)(right - left) / 3.0f, bottom - 1, this.getColor(e).getRGB());
        } else if (boxMode.is("Box")) {
            Gui.drawRect(right + line, top + line, left - line, top - 1 - line, bg);
            Gui.drawRect(right + line, bottom + line, left - line, bottom - 1 - line, bg);
            Gui.drawRect(left + 1 + line, top, left - line, bottom, bg);
            Gui.drawRect(right + line, top, right - 1 - line, bottom, bg);
            Gui.drawRect(right, top, left, top - 1, this.getColor(e).getRGB());
            Gui.drawRect(right, bottom, left, bottom - 1, this.getColor(e).getRGB());
            Gui.drawRect(left + 1, top, left, bottom, this.getColor(e).getRGB());
            Gui.drawRect(right, top, right - 1, bottom, this.getColor(e).getRGB());
        }
    }

    private void drawArmor(EntityLivingBase entityLivingBase, float left, float top, float right, float bottom) {
        float height = bottom + 1.0f - top;
        float currentArmor = entityLivingBase.getTotalArmorValue();
        float armorPercent = currentArmor / 20.0f;
        float MOVE = 2.0f;
        float line = 1.0f;
        if (ESP.mc.thePlayer.getDistanceToEntity(entityLivingBase) > 16.0f) {
            return;
        }
        for (int i = 0; i < 4; ++i) {
            double h = (bottom - top) / 4.0f;
            ItemStack itemStack = entityLivingBase.getEquipmentInSlot(i + 1);
            double difference = (double)(top - bottom) + 0.5;
            if (itemStack == null) continue;
            RenderUtils.drawESPRect(right + 2.0f + 1.0f + MOVE, top - 2.0f, right + 1.0f - 1.0f + MOVE, bottom + 1.0f, new Color(25, 25, 25, 150).getRGB());
            RenderUtils.drawESPRect(right + 3.0f + MOVE, top + height * (1.0f - armorPercent) - 1.0f, right + 1.0f + MOVE, bottom, new Color(78, 206, 229).getRGB());
            RenderUtils.drawESPRect(right + 3.0f + MOVE + (float)line, bottom + 1.0f, right + 3.0f + MOVE, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
            RenderUtils.drawESPRect(right + 1.0f + MOVE, bottom + 1.0f, right + 1.0f + MOVE - (float)line, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
            RenderUtils.drawESPRect(right + 1.0f + MOVE, top - 1.0f, right + 3.0f + MOVE, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
            RenderUtils.drawESPRect(right + 1.0f + MOVE, bottom + 1.0f, right + 3.0f + MOVE, bottom, new Color(0, 0, 0, 255).getRGB());
            RenderUtils.renderItemStack(itemStack, (int)(right + 6.0f + MOVE), (int)((double)(bottom + 30.0f) - (double)(i + 1) * h));
            float scale = 1.0f;
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);
            ESP.mc.fontRendererObj.drawStringWithShadow(String.valueOf(itemStack.getMaxDamage() - itemStack.getItemDamage()), (right + 6.0f + MOVE + (16.0f - (float)ESP.mc.fontRendererObj.getStringWidth(String.valueOf(itemStack.getMaxDamage() - itemStack.getItemDamage())) * scale) / 2.0f) / scale, (float)((int)((double)(bottom + 30.0f) - (double)(i + 1) * h) + 16) / scale, -1);
            GlStateManager.popMatrix();
            if (!(-difference > 50.0)) continue;
            for (int j = 1; j < 4; ++j) {
                double dThing = difference / 4.0 * (double)j;
                RenderUtils.rectangle(right + 2.0f, (double)bottom - 0.5 + dThing, (double)right + 6.0, (double)bottom - 0.5 + dThing - 1.0, Colors.getColor(0));
            }
        }
    }

    private void drawHealth(EntityLivingBase entityLivingBase, float left, float top, float right, float bottom) {
        float height = bottom + 1.0f - top;
        float currentHealth = entityLivingBase.getHealth();
        float maxHealth = entityLivingBase.getMaxHealth();
        float healthPercent = currentHealth / maxHealth;
        float MOVE = 2.0f;
        float line = 1.0f;
        String healthStr = "§f" + this.decimalFormat.format(currentHealth) + "§c❤";
        float bottom1 = top + height * (1.0f - healthPercent) - 1.0f;
        float health = entityLivingBase.getHealth();
        float[] fractions = new float[]{0.0f, 0.5f, 1.0f};
        Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN};
        float progress = health / entityLivingBase.getMaxHealth();
        Color customColor = health >= 0.0f ? Colors.blendColors(fractions, colors, progress).brighter() : Color.RED;
        ESP.mc.fontRendererObj.drawStringWithShadow(healthStr, left - 3.0f - MOVE - (float)ESP.mc.fontRendererObj.getStringWidth(healthStr), bottom1, -1);
        RenderUtils.drawESPRect(left - 3.0f - MOVE, bottom, left - 1.0f - MOVE, top - 1.0f, new Color(25, 25, 25, 150).getRGB());
        RenderUtils.drawESPRect(left - 3.0f - MOVE, bottom, left - 1.0f - MOVE, bottom1, customColor.getRGB());
        RenderUtils.drawESPRect(left - 3.0f - MOVE, bottom + 1.0f, left - 3.0f - MOVE - (float)line, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
        RenderUtils.drawESPRect(left - 1.0f - MOVE + (float)line, bottom + 1.0f, left - 1.0f - MOVE, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
        RenderUtils.drawESPRect(left - 3.0f - MOVE, top - 1.0f, left - 1.0f - MOVE, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
        RenderUtils.drawESPRect(left - 3.0f - MOVE, bottom + 1.0f, left - 1.0f - MOVE, bottom, new Color(0, 0, 0, 255).getRGB());
        double difference = (double)(top - bottom) + 0.5;
        if (-difference > 50.0) {
            for (int j = 1; j < 10; ++j) {
                double dThing = difference / 10.0 * (double)j;
                RenderUtils.rectangle((double)left - 5.5, (double)bottom - 0.5 + dThing, (double)left - 2.5, (double)bottom - 0.5 + dThing - 1.0, Colors.getColor(0));
            }
        }
    }

    public Color getColor(Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase entityLivingBase = (EntityLivingBase)entity;
            return colorProp.getValue();
        }
        return new Color(255, 255, 255);
    }

    private static Vec3 WorldToScreen(Vec3 position) {
        FloatBuffer screenPositions = BufferUtils.createFloatBuffer(3);
        boolean result = GLU.gluProject((float)position.xCoord, (float)position.yCoord, (float)position.zCoord, modelView, projection, viewport, screenPositions);
        if (result) {
            return new Vec3(screenPositions.get(0), (float) Display.getHeight() - screenPositions.get(1), screenPositions.get(2));
        }
        return null;
    }

    public void updatePositions(Entity entity) {
        this.positions.clear();
        Vec3 position = ESP.getEntityRenderPosition(entity);
        double x = position.xCoord - entity.posX;
        double y = position.yCoord - entity.posY;
        double z = position.zCoord - entity.posZ;
        double height = entity instanceof EntityItem ? 0.5 : (double)entity.height + 0.1;
        double width = entity instanceof EntityItem ? 0.25 : (Double)this.boxWidth.getValue();
        AxisAlignedBB aabb = new AxisAlignedBB(entity.posX - width + x, entity.posY + y, entity.posZ - width + z, entity.posX + width + x, entity.posY + height + y, entity.posZ + width + z);
        this.positions.add(new Vec3(aabb.minX, aabb.minY, aabb.minZ));
        this.positions.add(new Vec3(aabb.minX, aabb.minY, aabb.maxZ));
        this.positions.add(new Vec3(aabb.minX, aabb.maxY, aabb.minZ));
        this.positions.add(new Vec3(aabb.minX, aabb.maxY, aabb.maxZ));
        this.positions.add(new Vec3(aabb.maxX, aabb.minY, aabb.minZ));
        this.positions.add(new Vec3(aabb.maxX, aabb.minY, aabb.maxZ));
        this.positions.add(new Vec3(aabb.maxX, aabb.maxY, aabb.minZ));
        this.positions.add(new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ));
    }

    private static Vec3 getEntityRenderPosition(Entity entity) {
        return new Vec3(ESP.getEntityRenderX(entity), ESP.getEntityRenderY(entity), ESP.getEntityRenderZ(entity));
    }

    private static double getEntityRenderX(Entity entity) {
        return entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)Minecraft.getMinecraft().timer.renderPartialTicks - RenderManager.renderPosX;
    }

    private static double getEntityRenderY(Entity entity) {
        return entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)Minecraft.getMinecraft().timer.renderPartialTicks - RenderManager.renderPosY;
    }

    private static double getEntityRenderZ(Entity entity) {
        return entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) Minecraft.getMinecraft().timer.renderPartialTicks - RenderManager.renderPosZ;
    }

    private int getColor(EntityLivingBase ent) {
        if (ent.getName().equals(ESP.mc.thePlayer.getName())) {
            return new Color(50, 255, 50).getRGB();
        }
        if (HackerDetector.enabled && HackerDetector.isHacker(ent)) {
            return new Color(255, 0, 0).getRGB();
        }
        if (isTeamMate((EntityPlayer)ent)) {
            return new Color(0, 200, 0).getRGB();
        }
        return new Color(200, 0, 0, 50).getRGB();
    }

    public static boolean isValid(Entity entity) {
        if (entity == ESP.mc.thePlayer && ESP.mc.gameSettings.thirdPersonView == 0) {
            return false;
        }
        if (entity.isInvisible()) {
            return false;
        }
        return entity instanceof EntityPlayer;
    }

    private static void updateView() {
        GL11.glGetFloat(2982, modelView);
        GL11.glGetFloat(2983, projection);
        GL11.glGetInteger(2978, viewport);
    }

    public static boolean isTeamMate(final EntityPlayer entity) {
        final String entName = entity.getDisplayName().getFormattedText();
        final String playerName = mc.thePlayer.getDisplayName().getFormattedText();
        return entName.length() >= 2 && playerName.length() >= 2 && entName.startsWith("§") && playerName.startsWith("§") && entName.charAt(1) == playerName.charAt(1);
    }
}