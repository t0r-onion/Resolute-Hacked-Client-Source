package vip.Resolute.modules.impl.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemArmor;
import org.lwjgl.util.vector.Vector2f;
import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.*;
import vip.Resolute.modules.Module;
import vip.Resolute.modules.impl.movement.Scaffold;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ColorSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.ui.click.skeet.SkeetUI;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.misc.TimerUtil;
import vip.Resolute.util.player.RotationUtils;
import vip.Resolute.util.render.Render2DUtils;
import vip.Resolute.util.render.RenderUtils;
import vip.Resolute.util.render.Translate;
import vip.Resolute.util.world.RandomUtil;

import me.tojatta.api.utilities.angle.Angle;
import me.tojatta.api.utilities.angle.AngleUtility;
import me.tojatta.api.utilities.vector.impl.Vector3;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Keyboard;

import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class KillAura extends Module {
    private static final C07PacketPlayerDigging PLAYER_DIGGING;
    private static final C08PacketPlayerBlockPlacement BLOCK_PLACEMENT;
    public static int waitTicks;
    private double x;
    private double y;
    private float lastHealth = 0.0F;
    private final TimerUtil attackTimer = new TimerUtil();
    public static EntityLivingBase target;
    public static boolean blocking;
    double healthBarWidth;
    private boolean entityInBlockRange;
    private Vector2f lastAngles = new Vector2f(0.0F, 0.0F);
    private final Map<EntityLivingBase, Double> entityDamageMap = new HashMap<EntityLivingBase, Double>();
    private final Map<EntityLivingBase, Integer> entityArmorCache = new HashMap<>();
    private boolean hasEnemyBeenHit = false;

    public ModeSetting auraModeProp = new ModeSetting("Aura Mode", "Single", "Single", "HVH5");
    public ModeSetting rotationModeProp = new ModeSetting("Rotation Mode", "Normal", "Normal", "NCP", "Stepped", "Smooth", "None");
    public ModeSetting sortMethodProp = new ModeSetting("Sorting Mode", "Distance", "Hurt Time", "Distance", "Health", "Angle", "Combined");
    public ModeSetting attackMethodProp = new ModeSetting("Attack Mode", "PRE", "PRE", "POST");
    public NumberSetting minApsProp = new NumberSetting("Min APS", 10, 1, 20, 1);
    public NumberSetting maxApsProp = new NumberSetting("Max APS", 12, 1, 20, 1);
    public NumberSetting rangeProp = new NumberSetting("Range", 4.0, 3.0, 6.0, 0.1);
    public BooleanSetting randomize = new BooleanSetting("Randomize", true);
    public NumberSetting randFactor = new NumberSetting("RAND Factor", 2.0, randomize::isEnabled, 0.1, 30.0, 0.1);
    public BooleanSetting customPitchProp = new BooleanSetting("Custom Pitch", false);
    public NumberSetting customPitchValueProp = new NumberSetting("Custom Pitch Value", 90, customPitchProp::isEnabled, -90, 90, 1);
    public BooleanSetting autoblockProp = new BooleanSetting("Autoblock", true);
    public NumberSetting autoBlockRangeProp = new NumberSetting("Block Range", 4.0, 3.0, 6.0, 0.1);
    public static ModeSetting autoblockModeProp = new ModeSetting("Autoblock Mode", "NCP", "NCP", "Watchdog", "Vanilla", "Fake");
    public NumberSetting maxAngleProp = new NumberSetting("NCP Step", 45.0, () -> rotationModeProp.is("NCP"), 1.0, 180.0, 1.0);
    public BooleanSetting lockViewProp = new BooleanSetting("Lock View", false);
    public BooleanSetting throughWallsProp = new BooleanSetting("Through Walls", true);
    public BooleanSetting keepSprintProp = new BooleanSetting("Keep Sprint", true);
    public BooleanSetting raytraceProp = new BooleanSetting("Ray Trace", false);
    public BooleanSetting packetUpdateProp = new BooleanSetting("Packet Update", false);
    public BooleanSetting indicatorProp = new BooleanSetting("Indicator", true);
    public ModeSetting indMode = new ModeSetting("Indicator Mode", "Box", indicatorProp::isEnabled, "Platform", "Box", "Diamond");
    public BooleanSetting targetHudProp = new BooleanSetting("TargetHUD", true);
    public ModeSetting targetHudModeProp = new ModeSetting("TargetHUD Mode", "Radium", "Exhibition", "Radium", "Radium New");
    public ColorSetting firstColorProp = new ColorSetting("First Color", new Color(0, 255, 98), () -> (targetHudModeProp.is("Radium") || targetHudModeProp.is("Exhibition") || targetHudModeProp.is("Radium New")) && targetHudProp.isEnabled());
    public ColorSetting secondColorProp = new ColorSetting("Second Color", new Color(136, 0, 255), () -> (targetHudModeProp.is("Radium") || targetHudModeProp.is("Exhibition") || targetHudModeProp.is("Radium New")) && targetHudProp.isEnabled());

    static {
        PLAYER_DIGGING = new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
        BLOCK_PLACEMENT = new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f);
    }

    public KillAura() {
        super("KillAura", Keyboard.KEY_NONE, "Automatically attacks a target in range", Category.COMBAT);
        this.addSettings(auraModeProp, rotationModeProp, sortMethodProp, attackMethodProp,
                minApsProp, maxApsProp, rangeProp, randomize, randFactor, customPitchProp, customPitchValueProp, autoblockProp, autoBlockRangeProp,
                autoblockModeProp, maxAngleProp, lockViewProp, throughWallsProp, keepSprintProp, raytraceProp,
                packetUpdateProp, indicatorProp, indMode, targetHudProp, targetHudModeProp, firstColorProp, secondColorProp);
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer != null) {
            this.lastAngles.x = mc.thePlayer.rotationYaw;
        }
    }

    @Override
    public void onDisable() {
        if (this.blocking) {
            this.blocking = false;
            mc.getNetHandler().getNetworkManager().sendPacket(KillAura.PLAYER_DIGGING);
        }
        if (mc.thePlayer != null) {
            this.lastAngles.x = mc.thePlayer.rotationYaw;
        }
        this.target = null;
        this.entityInBlockRange = false;
    }

    public void onEvent(Event e) {
        this.setSuffix(auraModeProp.getMode());

        if(e instanceof EventPacket) {
            if(((EventPacket) e).getPacket() instanceof C0APacketAnimation) {
                this.attackTimer.reset();
            }
        }

        if(e instanceof EventEntityDamage) {
            this.entityDamageMap.put((EntityLivingBase) ((EventEntityDamage) e).getEntity(), ((EventEntityDamage) e).getDamage());
        }

        EntityLivingBase optimalTarget;
        List<EntityLivingBase> entities;
        final Iterator<EntityLivingBase> iterator;
        EntityLivingBase entity;
        float dist;
        float[] rotations;
        float yaw;
        float pitch;

        if(e instanceof EventMotion) {
            EventMotion event = (EventMotion) e;

            if(e.isPre()) {
                this.entityInBlockRange = false;
                optimalTarget = null;
                entities = Resolute.getLivingEntities(this::isValid);
                if (entities.size() > 1) {
                    switch (sortMethodProp.getMode()) {
                        case "Hurt Time": {
                            entities.sort(SortingMethod.HURT_TIME.getSorter());
                            break;
                        }
                        case "Distance": {
                            entities.sort(SortingMethod.DISTANCE.getSorter());
                            break;
                        }
                        case "Health": {
                            entities.sort(SortingMethod.HEALTH.getSorter());
                            break;
                        }
                        case "Angle": {
                            entities.sort(SortingMethod.ANGLE.getSorter());
                            break;
                        }
                        case "Combined": {
                            entities.sort(SortingMethod.COMBINED.getSorter());
                            break;
                        }
                    }
                }
                iterator = entities.iterator();
                while (iterator.hasNext()) {
                    entity = iterator.next();
                    dist = mc.thePlayer.getDistanceToEntity(entity);
                    if (!this.entityInBlockRange && dist < this.autoBlockRangeProp.getValue()) {
                        this.entityInBlockRange = true;
                    }
                    if (dist < this.rangeProp.getValue()) {
                        optimalTarget = entity;
                        break;
                    }
                }

                this.target = optimalTarget;
                if (this.blocking) {
                    this.blocking = false;
                    if (this.isHoldingSword()) {
                        unblock();
                    }
                }

                if (!this.isOccupied()) {
                    if (optimalTarget != null) {
                        switch (rotationModeProp.getMode()) {
                            case "Normal": {
                                rotations = RotationUtils.getRotationsToEntity(optimalTarget);
                                yaw = rotations[0];
                                pitch = rotations[1];
                                break;
                            }
                            case "NCP": {
                                rotations = getRotations(optimalTarget, event.getPrevYaw(), event.getPrevPitch(), (float) this.maxAngleProp.getValue());
                                yaw = rotations[0];
                                pitch = rotations[1];
                                break;
                            }
                            case "Stepped": {
                                rotations = getRotations(this.target);
                                yaw = rotations[0];
                                pitch = rotations[1];
                                break;
                            }
                            case "Smooth": {
                                final AngleUtility angleUtil = new AngleUtility(10, 190, 10, 10);
                                Vector3<Double> enemyCoords = new Vector3<>(target.posX, target.posY, target.posZ);
                                Vector3<Double> myCoords = new Vector3<>(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                                Angle dstAngle = angleUtil.calculateAngle(enemyCoords, myCoords);
                                yaw = dstAngle.getYaw();
                                pitch = dstAngle.getPitch();
                                break;
                            }
                            case "None": {
                                yaw = mc.thePlayer.rotationYaw;
                                pitch = mc.thePlayer.rotationPitch;
                                break;
                            }
                            default: {
                                yaw = mc.thePlayer.rotationYaw;
                                pitch = mc.thePlayer.rotationPitch;
                            }
                        }

                        if (randomize.isEnabled()) {
                            if (pitch > 0.0F)
                                pitch += Math.random() * randFactor.getValue();
                            else
                                pitch -= Math.random() * randFactor.getValue();
                            final double yawRandom = Math.random() * randFactor.getValue();
                            if (yawRandom > yawRandom / 2)
                                yaw += yawRandom;
                            else
                                yaw -= yawRandom;
                        }

                        if(pitch > 90) {
                            pitch = 90;
                        } else if(pitch < -90) {
                            pitch = -90;
                        }

                        event.setYaw(yaw);

                        if(customPitchProp.isEnabled()) {
                            event.setPitch((float) customPitchValueProp.getValue());
                        } else {
                            event.setPitch(pitch);
                        }

                        if (this.lockViewProp.isEnabled()) {
                            mc.thePlayer.rotationYaw = yaw;
                            mc.thePlayer.rotationPitch = pitch;
                        }
                        if (this.packetUpdateProp.isEnabled()) {
                            mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(event.getX(), event.getY(), event.getZ(), event.getYaw(), event.getPitch(), event.isOnGround()));
                        }
                        if (this.attackMethodProp.is("PRE") && this.checkWaitTicks()) {
                            this.tryAttack(event);
                        }
                    } else {
                        hasEnemyBeenHit = false;
                    }
                }
            } else if (!this.isOccupied()) {
                if (this.target != null && this.attackMethodProp.is("POST") && this.checkWaitTicks()) {
                    this.tryAttack(event);
                }
                if (this.entityInBlockRange && this.autoblockProp.isEnabled() && this.isHoldingSword()) {
                    setItemInUse();

                    if (!this.blocking) {
                        block();
                        this.blocking = true;
                    }
                }
            }
        }

        if(e instanceof EventRender2D) {
            ScaledResolution sr;
            if(target != null) {
                if(targetHudProp.isEnabled()) {
                    if(targetHudModeProp.is("Radium")) {
                        ScaledResolution lr;
                        FontRenderer fontRenderer;
                        float sWidth;
                        float sHeight;
                        float middleX;
                        float middleY;
                        float top;
                        float xOffset;
                        String name;
                        float modelWidth;
                        float nameYOffset;
                        float nameHeight;
                        float width;
                        float height;
                        float half;
                        float left;
                        float right;
                        float bottom;
                        float textLeft;
                        float healthTextY;
                        float health;
                        String healthText;
                        float scale;
                        float healthTextHeight;
                        float healthPercentage;
                        int fadeColor = 0;
                        float downScale;
                        float healthBarY;
                        float healthBarHeight;
                        float healthBarRight;
                        float dif;
                        Double lastDamage;
                        float healthWidth;
                        float healthBarEnd;
                        float damage;
                        float damageWidth;

                        if(target instanceof EntityPlayer) {
                            lr = new ScaledResolution(mc);
                            fontRenderer = mc.fontRendererObj;
                            sWidth = (float)lr.getScaledWidth();
                            sHeight = (float)lr.getScaledHeight();
                            middleX = sWidth / 2.0f;
                            middleY = sHeight / 2.0f;
                            top = middleY + 20.0f;
                            xOffset = 0.0f;
                            if (target instanceof EntityPlayer) {
                                name = ((EntityPlayer)target).getGameProfile().getName();
                            }
                            else {
                                name = target.getDisplayName().getUnformattedText();
                            }
                            modelWidth = 30.0f;
                            nameYOffset = 4.0f;
                            nameHeight = fontRenderer.FONT_HEIGHT;
                            width = Math.max(120.0f, modelWidth + 4.0f + fontRenderer.getStringWidth(name) + 2.0f);
                            height = 50.0f;
                            half = width / 2.0f;
                            left = middleX - half + xOffset;
                            right = middleX + half + xOffset;
                            bottom = top + height;
                            Gui.drawRect(left, top, right, bottom, Integer.MIN_VALUE);
                            GL11.glDisable(3553);
                            GL11.glLineWidth(0.5f);
                            GL11.glColor3f(0.0f, 0.0f, 0.0f);
                            GL11.glBegin(2);
                            GL11.glVertex2f(left, top);
                            GL11.glVertex2f(left, bottom);
                            GL11.glVertex2f(right, bottom);
                            GL11.glVertex2f(right, top);
                            GL11.glEnd();
                            GL11.glEnable(3553);
                            textLeft = left + modelWidth;
                            fontRenderer.drawStringWithShadow(name, textLeft, top + nameYOffset, -1);
                            healthTextY = top + nameHeight + nameYOffset;
                            health = target.getHealth();
                            healthText = String.format("%.1f", health);
                            scale = 2.0f;
                            healthTextHeight = fontRenderer.FONT_HEIGHT * scale;
                            healthPercentage = health / target.getMaxHealth();
                            fadeColor = RenderUtils.fadeBetween(this.firstColorProp.getColor(), this.secondColorProp.getColor(), System.currentTimeMillis() % 3000L / 1500.0f);
                            downScale = 1.0f / scale;
                            GL11.glScalef(scale, scale, 1.0f);
                            fontRenderer.drawStringWithShadow(healthText, textLeft / scale, healthTextY / scale + 2.0f, fadeColor);
                            GL11.glScalef(downScale, downScale, 1.0f);
                            healthBarY = healthTextY + healthTextHeight + 2.0f;
                            healthBarHeight = 8.0f;
                            healthBarRight = right - 4.0f;
                            dif = healthBarRight - textLeft;
                            Gui.drawRect(textLeft, healthBarY, healthBarRight, healthBarY + healthBarHeight, 1342177280);
                            target.healthProgressX = (float)RenderUtils.progressiveAnimation(target.healthProgressX, healthPercentage, 1.0);
                            lastDamage = this.entityDamageMap.get(target);
                            healthWidth = dif * target.healthProgressX;
                            healthBarEnd = textLeft + healthWidth;
                            if (lastDamage != null && lastDamage > 0.0) {
                                damage = lastDamage.floatValue();
                                damageWidth = dif * (damage / target.getMaxHealth());
                                Gui.drawRect(healthBarEnd, healthBarY, Math.min(healthBarEnd + damageWidth, healthBarRight), healthBarY + healthBarHeight, RenderUtils.darker(fadeColor, 0.49f));
                            }
                            Gui.drawRect(textLeft, healthBarY, healthBarEnd, healthBarY + healthBarHeight, fadeColor);
                            GL11.glColor3f(1.0f, 1.0f, 1.0f);
                            GuiInventory.drawEntityOnScreen((int)(left + modelWidth / 2.0f), (int)bottom - 2, 23, 0.0f, 0.0f, target);
                        }
                    }

                    if(targetHudModeProp.is("Astolfo")) {
                        int n2;
                        int n3;
                        float health;
                        float healthPercentage;
                        float scaledWidth;
                        float scaledHeight;
                        int x1;
                        int i;
                        int x;
                        int yAdd;

                        ScaledResolution scaledResolution = new ScaledResolution(mc);

                        if(target instanceof EntityPlayer) {
                            GlStateManager.pushMatrix();
                            GlStateManager.translate(this.x, this.y, 0.0D);
                            n2 = scaledResolution.getScaledWidth() / 2 + 300;
                            n3 = scaledResolution.getScaledHeight() / 2 + 200;
                            RenderUtils.drawRoundedRect2((float)n2 - 70.0F, (float)n3 + 35.0F, (float)n2 + 88.0F, (float)n3 + 89.0F, 6.0F, (new Color(0, 0, 0, 190)).getRGB());
                            health = ((EntityLivingBase)target).getHealth();
                            healthPercentage = health / ((EntityLivingBase)target).getMaxHealth();
                            scaledWidth = 0.0F;
                            if (healthPercentage != this.lastHealth) {
                                scaledHeight = healthPercentage - this.lastHealth;
                                scaledWidth = this.lastHealth;
                                this.lastHealth += scaledHeight / 8.0F;
                            }

                            RenderUtils.drawRoundedRect2((double)((float)n2 - 36.0F), (double)((float)n3 + 78.0F), (double)((float)n2 - 36.0F) + 120.0D, (double)((float)n3 + 85.0F), 6.0D, RenderUtils.pulseBrightness(new Color(14, 60, 190), 2, 2).getRGB());
                            if (!(healthPercentage * 100.0F > 75.0F)) {
                                RenderUtils.drawRoundedRect2((float)n2 - 36.0F, (float)n3 + 78.0F, (float)n2 - 36.0F + 126.0F * scaledWidth, (float)n3 + 85.0F, 6.0F, RenderUtils.pulseBrightness(new Color(13, 108, 244), 2, 2).getRGB());
                                RenderUtils.drawRoundedRect2((float)n2 - 36.0F, (float)n3 + 78.0F, (float)n2 - 36.0F + 120.0F * scaledWidth, (float)n3 + 85.0F, 6.0F, RenderUtils.pulseBrightness(new Color(13, 108, 214), 2, 2).getRGB());
                            } else {
                                RenderUtils.drawRoundedRect2((float)n2 - 36.0F, (float)n3 + 78.0F, (float)n2 - 36.0F + 120.0F * scaledWidth, (float)n3 + 85.0F, 6.0F, RenderUtils.pulseBrightness(new Color(13, 108, 214), 2, 2).getRGB());
                            }

                            x1 = n2 - 50;
                            i = n3 + 32;
                            GL11.glPushMatrix();
                            GlStateManager.translate((float)x1, (float)i, 1.0F);
                            GL11.glScaled(1.1D, 1.1D, 1.1D);
                            GlStateManager.translate((float)(-x1), (float)(-i), 1.0F);
                            mc.fontRendererObj.drawStringWithShadow(((EntityPlayer)target).getGameProfile().getName(), (float)x1 + 13.5F, (float)i + 7.5F, -1);
                            GL11.glPopMatrix();
                            x = n2 - 64;
                            yAdd = n3 + 40;
                            GL11.glPushMatrix();
                            GlStateManager.translate((float)x, (float)yAdd, 1.0F);
                            GL11.glScalef(2.0F, 2.0F, 2.0F);
                            GlStateManager.translate((float)(-x), (float)(-yAdd), 1.0F);
                            this.mc.fontRendererObj.drawStringWithShadow(String.format("%.1f", ((EntityLivingBase)target).getHealth() / 2.0F) + " ❤", (float)x + 13.5F, (float)yAdd + 7.5F, RenderUtils.pulseBrightness(new Color(13, 108, 214), 2, 2).getRGB());
                            GL11.glPopMatrix();
                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                            drawEntityOnScreen(n2 - 53, n3 + 85, 24, 25.0F, 25.0F, (EntityLivingBase)target);
                            GlStateManager.popMatrix();
                        }
                    }

                    if(targetHudModeProp.is("Radium New")) {
                        double damageAsHealthBarWidth;

                        if(target instanceof EntityPlayer) {
                            ScaledResolution lr = new ScaledResolution(mc);
                            FontRenderer fontRenderer = this.mc.fontRendererObj;
                            int sWidth = lr.getScaledWidth();
                            int sHeight = lr.getScaledHeight();
                            int middleX = sWidth / 2;
                            int middleY = sHeight / 2 + 10;
                            String name1 = ((EntityPlayer)target).getGameProfile().getName();

                            int width = Math.max(100, 32 + (int)Math.ceil((double)(fontRenderer.getStringWidth(name1) / 2)) + 4);
                            int half = width / 2;
                            int left = middleX - half;
                            int right = middleX + half;
                            int top = middleY + 20;
                            int bottom = top + 32;
                            float maxHealth1;

                            RenderUtils.enableBlending();
                            AbstractClientPlayer clientPlayer = (AbstractClientPlayer)target;
                            GL11.glEnable(3553);
                            mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
                            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.6F);
                            maxHealth1 = 0.125F;
                            GL11.glBegin(7);
                            GL11.glTexCoord2f(0.125F, 0.125F);
                            GL11.glVertex2i(left, top);
                            GL11.glTexCoord2f(0.125F, 0.25F);
                            GL11.glVertex2i(left, bottom);
                            GL11.glTexCoord2f(0.25F, 0.25F);
                            GL11.glVertex2i(left + 32, bottom);
                            GL11.glTexCoord2f(0.25F, 0.125F);
                            GL11.glVertex2i(left + 32, top);
                            GL11.glEnd();
                            GL11.glDisable(3042);

                            float health1 = (target).getHealth();
                            maxHealth1 = (target).getMaxHealth();
                            float healthPercentage1 = health1 / maxHealth1;
                            int fadeColor = RenderUtils.fadeBetween(this.firstColorProp.getColor(), this.secondColorProp.getColor());

                            fadeColor = RenderUtils.fadeBetween(fadeColor, RenderUtils.darker(fadeColor));
                            int alphaInt = alphaToInt(0.6F, 0);
                            int textAlpha = alphaToInt(0.6F, 70);
                            fadeColor += textAlpha << 24;
                            int backgroundColor = alphaInt << 24;
                            Gui.drawRect(left + 32, top, right, bottom, backgroundColor);
                            float infoLeft = (float)(left + 32 + 2);
                            float infoTop = (float)(top + 2);
                            float scale = 0.5F;
                            GL11.glScalef(0.5F, 0.5F, 1.0F);
                            float infoypos = infoTop / 0.5F;
                            fontRenderer.drawStringWithShadow(name1, infoLeft / 0.5F, infoypos, 16777215 + (textAlpha << 24));
                            infoypos += (float)fontRenderer.FONT_HEIGHT;
                            String healthText = String.format("§FHP: §R%.1f", (double)health1 / 2.0D);
                            fontRenderer.drawStringWithShadow(healthText, infoLeft / 0.5F, infoypos, fadeColor);
                            infoypos += (float)fontRenderer.FONT_HEIGHT;
                            EntityPlayer player = (EntityPlayer)target;

                            int targetArmor = this.getOrCacheArmor(player);
                            int localArmor = this.getOrCacheArmor(this.mc.thePlayer);
                            char prefix;
                            if (targetArmor > localArmor) {
                                prefix = '4';
                            } else if (targetArmor < localArmor) {
                                prefix = 'A';
                            } else {
                                prefix = 'F';
                            }

                            String armorText = String.format("§FArmor: §R%s%% §F/ §%s%s%%", targetArmor, prefix, Math.abs(targetArmor - localArmor));
                            fontRenderer.drawStringWithShadow(armorText, infoLeft / 0.5F, infoypos, 5308415 + (textAlpha << 24));

                            GL11.glScalef(2.0F, 2.0F, 1.0F);
                            (target).healthProgressX = (float)RenderUtils.linearAnimation((target).healthProgressX, (double)healthPercentage1, 0.02D);
                            float healthBarRight = (float)(right - 2);
                            float xDif = healthBarRight - infoLeft;
                            float healthBarThickness = 4.0F;
                            float healthBarEnd = infoLeft + xDif * (target).healthProgressX;
                            float healthBarBottom = (float)(bottom - 2);
                            float healthBarTop = healthBarBottom - 4.0F;
                            Gui.drawRect((double)infoLeft, (double)healthBarTop, (double)healthBarRight, (double)healthBarBottom, backgroundColor);
                            if (this.entityDamageMap.containsKey(target)) {
                                double lastDamage = (Double)this.entityDamageMap.get(target);
                                if (lastDamage > 0.0D) {
                                    damageAsHealthBarWidth = (double)xDif * (lastDamage / (double)maxHealth1);
                                    Gui.drawRect((double)healthBarEnd, (double)healthBarTop, Math.min((double)healthBarEnd + damageAsHealthBarWidth, (double)healthBarRight), (double)healthBarBottom, RenderUtils.darker(fadeColor));
                                }
                            }

                            Gui.drawRect((double)infoLeft, (double)healthBarTop, (double)healthBarEnd, (double)healthBarBottom, fadeColor);
                        }
                    }

                    if(targetHudModeProp.is("Exhibition")) {
                        if(target instanceof EntityPlayer) {
                            float startX = 20;
                            sr = new ScaledResolution(mc);
                            final float x = sr.getScaledWidth() / 2.0f + 30.0f;
                            final float y = sr.getScaledHeight() / 2.0f + 30.0f;
                            final float healthRender = this.target.getHealth();
                            double hpPercentage = healthRender / this.target.getMaxHealth();
                            hpPercentage = MathHelper.clamp_double(hpPercentage, 0.0, 1.0);
                            final double hpWidth = 60.0 * hpPercentage;
                            final String healthStr = String.valueOf((int) this.target.getHealth() / 1.0f);
                            int xAdd = 0;
                            double multiplier = 0.85;
                            GlStateManager.pushMatrix();
                            GlStateManager.scale(multiplier, multiplier, multiplier);
                            if (target.getCurrentArmor(3) != null) {
                                mc.getRenderItem().renderItemAndEffectIntoGUI(target.getCurrentArmor(3), (int) ((((sr.getScaledWidth() / 2) + startX + 33) + xAdd) / multiplier), (int) (((sr.getScaledHeight() / 2) + 56) / multiplier));
                                xAdd += 15;
                            }
                            if (target.getCurrentArmor(2) != null) {
                                mc.getRenderItem().renderItemAndEffectIntoGUI(target.getCurrentArmor(2), (int) ((((sr.getScaledWidth() / 2) + startX + 33) + xAdd) / multiplier), (int) (((sr.getScaledHeight() / 2) + 56) / multiplier));
                                xAdd += 15;
                            }
                            if (target.getCurrentArmor(1) != null) {
                                mc.getRenderItem().renderItemAndEffectIntoGUI(target.getCurrentArmor(1), (int) ((((sr.getScaledWidth() / 2) + startX + 33) + xAdd) / multiplier), (int) (((sr.getScaledHeight() / 2) + 56) / multiplier));
                                xAdd += 15;
                            }
                            if (target.getCurrentArmor(0) != null) {
                                mc.getRenderItem().renderItemAndEffectIntoGUI(target.getCurrentArmor(0), (int) ((((sr.getScaledWidth() / 2) + startX + 33) + xAdd) / multiplier), (int) (((sr.getScaledHeight() / 2) + 56) / multiplier));
                                xAdd += 15;
                            }
                            if (target.getHeldItem() != null) {
                                mc.getRenderItem().renderItemAndEffectIntoGUI(target.getHeldItem(), (int) ((((sr.getScaledWidth() / 2) + startX + 33) + xAdd) / multiplier), (int) (((sr.getScaledHeight() / 2) + 56) / multiplier));
                            }

                            GlStateManager.popMatrix();
                            this.healthBarWidth = Translate.animate(hpWidth, this.healthBarWidth, 0.1);
                            Gui.drawGradientRect(x - 23.5, y - 3.5, x + 105.5f, y + 42.4f, new Color(10, 10, 10, 255).getRGB(), new Color(10, 10, 10, 255).getRGB());
                            Gui.drawGradientRect(x - 23, y - 3.2, x + 104.8f, y + 41.8f, new Color(40, 40, 40, 255).getRGB(), new Color(40, 40, 40, 255).getRGB());
                            Gui.drawGradientRect(x - 21.4, y - 1.5, x + 103.5f, y + 40.5f, new Color(74, 74, 74, 255).getRGB(), new Color(74, 74, 74, 255).getRGB());
                            Gui.drawGradientRect(x - 21, y - 1, x + 103.0f, y + 40.0f, new Color(32, 32, 32, 255).getRGB(), new Color(10, 10, 10, 255).getRGB());
                            Gui.drawRect(x + 25.0f, y + 11.0f, x + 87f, y + 14.29f, new Color(105, 105, 105, 40).getRGB());
                            Gui.drawRect(x + 25.0f, y + 11.0f, x + 27f + this.healthBarWidth, y + 14.29f, RenderUtils.getColorFromPercentage(this.target.getHealth(), this.target.getMaxHealth()));
                            mc.fontRendererObj.drawStringWithShadow(this.target.getName(), x + 24.8f, y + 1.9f, new Color(255, 255, 255).getRGB());
                            mc.fontRendererObj.drawStringWithShadow("l   " + "l   " + "l   " + "l   " + "l   " + "l   ", x + 27.50f, y + 10.2f, new Color(50, 50, 50).getRGB());
                            mc.fontRendererObj.drawStringWithShadow("HP:" + healthStr, x - 11.2f + 44.0f - mc.fontRendererObj.getStringWidth(healthStr) / 2.0f, y + 17.0f, -1);
                            Render2DUtils.drawFace((int) (x - 20), (int) (y), 8, 8, 8, 8, 40, 40, 64, 64, (AbstractClientPlayer) this.target);
                        }
                    }
                }
            } else {
                this.healthBarWidth = 92.0;
            }
        }

        if(e instanceof EventRender3D) {
            if (target != null && indicatorProp.isEnabled() && indMode.is("Platform")) {
                RenderUtils.drawAuraMark(target, target.hurtTime > 3 ? new Color(0, 255, 88, 75) : new Color(235, 40, 40, 75));
            }

            if (target != null && indicatorProp.isEnabled() && indMode.is("Diamond")) {
                RenderUtils.drawExhi(target, (EventRender3D) e);
            }

            if (target != null && indicatorProp.isEnabled() && indMode.is("Box")) {
                RenderUtils.drawPlatform(target, target.hurtTime > 3 ? new Color(0, 255, 88, 75) : new Color(235, 40, 40, 75));
            }
        }
    }

    public void setItemInUse() {
        if(autoblockModeProp.is("NCP")) {
            mc.thePlayer.setItemInUse(mc.thePlayer.getCurrentEquippedItem(), mc.thePlayer.getHeldItem().getMaxItemUseDuration());
        }

        if(autoblockModeProp.is("Watchdog")) {
            mc.thePlayer.setItemInUse(mc.thePlayer.getHeldItem(), 8100);
        }

        if(autoblockModeProp.is("Vanilla")) {
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
        }
    }

    public void block() {
        if(autoblockModeProp.is("NCP")) {
            mc.getNetHandler().getNetworkManager().sendPacket(KillAura.BLOCK_PLACEMENT);
        }

        if(autoblockModeProp.is("Watchdog")) {
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, target.getPositionVector()));
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
            mc.getNetHandler().getNetworkManager().sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
        }
    }

    public void unblock() {
        if(autoblockModeProp.is("NCP")) {
            mc.getNetHandler().getNetworkManager().sendPacket(KillAura.PLAYER_DIGGING);
        }

        if(autoblockModeProp.is("Watchdog")) {
            mc.getNetHandler().getNetworkManager().sendPacket(KillAura.PLAYER_DIGGING);
        }
    }

    private boolean isValid(final EntityLivingBase entity) {
        if (!entity.isEntityAlive()) {
            return false;
        }
        if (entity.isInvisible() && !SkeetUI.isInvisibles()) {
            return false;
        }
        if (!entity.canEntityBeSeen(mc.thePlayer) && !throughWallsProp.isEnabled()) {
            return false;
        }
        if (entity instanceof EntityOtherPlayerMP) {
            final EntityPlayer player = (EntityPlayer)entity;
            if (!SkeetUI.isPlayers()) {
                return false;
            }
            if(AntiBot.enabled && AntiBot.watchdogBots.contains(player)) {
                return false;
            }
            if (!SkeetUI.isTeams() && isTeamMate(player)) {
                return false;
            }
        }
        else if(entity instanceof EntityVillager) {
            if(!SkeetUI.isVillagers()) {
                return false;
            }
        }
        else if (entity instanceof EntityMob) {
            if (!SkeetUI.isMobs()) {
                return false;
            }
        }
        else {
            if (!(entity instanceof EntityAnimal)) {
                return false;
            }
            if (!SkeetUI.isAnimals()) {
                return false;
            }
        }
        return mc.thePlayer.getDistanceToEntity(entity) < Math.max(this.autoBlockRangeProp.getValue(), this.rangeProp.getValue());
    }

    private static float[] getRotations(final Entity entity) {
        final EntityPlayerSP player = mc.thePlayer;
        final double xDist = entity.posX - player.posX;
        final double zDist = entity.posZ - player.posZ;
        double yDist = entity.posY - player.posY;
        final double dist = StrictMath.sqrt(xDist * xDist + zDist * zDist);
        final AxisAlignedBB entityBB = entity.getEntityBoundingBox().expand(0.10000000149011612, 0.10000000149011612, 0.10000000149011612);
        final double playerEyePos = player.posY + player.getEyeHeight();
        final boolean close = dist < 2.0 && Math.abs(yDist) < 2.0;
        float pitch;
        if (close && playerEyePos > entityBB.minY) {
            pitch = 60.0f;
        }
        else {
            yDist = ((playerEyePos > entityBB.maxY) ? (entityBB.maxY - playerEyePos) : ((playerEyePos < entityBB.minY) ? (entityBB.minY - playerEyePos) : 0.0));
            pitch = (float)(-(StrictMath.atan2(yDist, dist) * 57.29577951308232));
        }
        float yaw = (float)(StrictMath.atan2(zDist, xDist) * 57.29577951308232) - 90.0f;
        if (close) {
            final int inc = (dist < 1.0) ? 180 : 90;
            yaw = (float)(Math.round(yaw / inc) * inc);
        }
        return new float[] { yaw, pitch };
    }

    private static float[] getRotations(final Entity entity, final float prevYaw, final float prevPitch, final float aimSpeed) {
        final EntityPlayerSP player = mc.thePlayer;
        final double xDist = entity.posX - player.posX;
        final double zDist = entity.posZ - player.posZ;
        final AxisAlignedBB entityBB = entity.getEntityBoundingBox().expand(0.10000000149011612, 0.10000000149011612, 0.10000000149011612);
        final double playerEyePos = player.posY + player.getEyeHeight();
        final double yDist = (playerEyePos > entityBB.maxY) ? (entityBB.maxY - playerEyePos) : ((playerEyePos < entityBB.minY) ? (entityBB.minY - playerEyePos) : 0.0);
        final double fDist = MathHelper.sqrt_double(xDist * xDist + zDist * zDist);
        final float yaw = interpolateRotation(prevYaw, (float)(StrictMath.atan2(zDist, xDist) * 57.29577951308232) - 90.0f, aimSpeed);
        final float pitch = interpolateRotation(prevPitch, (float)(-(StrictMath.atan2(yDist, fDist) * 57.29577951308232)), aimSpeed);
        return new float[] { yaw, pitch };
    }

    private static float interpolateRotation(final float prev, final float now, final float maxTurn) {
        float var4 = MathHelper.wrapAngleTo180_float(now - prev);
        if (var4 > maxTurn) {
            var4 = maxTurn;
        }
        if (var4 < -maxTurn) {
            var4 = -maxTurn;
        }
        return prev + var4;
    }

    private boolean isInMenu() {
        final GuiScreen currentScreen = mc.currentScreen;
        return currentScreen != null && !(currentScreen instanceof SkeetUI);
    }

    private boolean isOccupied() {
        return this.isInMenu() || Scaffold.enabled;
    }

    private boolean checkWaitTicks() {
        if (KillAura.waitTicks > 0) {
            --KillAura.waitTicks;
            return false;
        }
        return true;
    }


    private void tryAttack(final EventMotion event) {
        if (this.isUsingItem()) {
            return;
        }

        final double min = minApsProp.getValue();
        final double max = minApsProp.getValue();
        final double cps;
        if (min == max)
            cps = min;
        else
            cps = RandomUtil.getRandomInRange(minApsProp.getValue(), maxApsProp.getValue());

        if(auraModeProp.is("Single")) {
            if (attackTimer.hasElapsed(1000L / (long) cps) && (!raytraceProp.isEnabled() || isLookingAtEntity(event.getYaw(), event.getPitch(), target, rangeProp.getValue(), raytraceProp.isEnabled()))) {
                mc.thePlayer.swingItem();
                mc.getNetHandler().sendPacketNoEvent(new C02PacketUseEntity(this.target, C02PacketUseEntity.Action.ATTACK));
                if (!this.keepSprintProp.isEnabled() && mc.thePlayer.isSprinting()) {
                    mc.thePlayer.motionX *= 0.6;
                    mc.thePlayer.motionZ *= 0.6;
                    mc.thePlayer.setSprinting(false);
                }
            }
        }

        if(auraModeProp.is("HVH5")) {
            if(target.hurtTime > 1) {
                hasEnemyBeenHit = true;
            } else {
                hasEnemyBeenHit = false;
            }

            if (attackTimer.hasElapsed(480L) || (!hasEnemyBeenHit && attackTimer.hasElapsed(1000L / (long) cps)) && (!raytraceProp.isEnabled() || isLookingAtEntity(event.getYaw(), event.getPitch(), target, rangeProp.getValue(), raytraceProp.isEnabled()))) {
                mc.thePlayer.swingItem();
                mc.getNetHandler().sendPacketNoEvent(new C02PacketUseEntity(this.target, C02PacketUseEntity.Action.ATTACK));
                if (!this.keepSprintProp.isEnabled() && mc.thePlayer.isSprinting()) {
                    mc.thePlayer.motionX *= 0.6;
                    mc.thePlayer.motionZ *= 0.6;
                    mc.thePlayer.setSprinting(false);
                }
            }
        }
    }

    private boolean isUsingItem() {
        return mc.thePlayer.isUsingItem() && !this.isHoldingSword();
    }

    private boolean isHoldingSword() {
        return mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword;
    }

    private static boolean isLookingAtEntity(final float yaw, final float pitch, final Entity entity, final double range, final boolean rayTrace) {
        final EntityPlayer player = mc.thePlayer;
        final Vec3 src = mc.thePlayer.getPositionEyes(1.0f);
        final Vec3 rotationVec = Entity.getVectorForRotation(pitch, yaw);
        final Vec3 dest = src.addVector(rotationVec.xCoord * range, rotationVec.yCoord * range, rotationVec.zCoord * range);
        final MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(src, dest, false, false, true);
        if (obj == null) {
            return false;
        }
        if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (rayTrace) {
                return false;
            }
            if (player.getDistanceToEntity(entity) > 3.0) {
                return false;
            }
        }
        return entity.getEntityBoundingBox().expand(0.10000000149011612, 0.10000000149011612, 0.10000000149011612).calculateIntercept(src, dest) != null;
    }

    public static double getEffectiveHealth(final EntityLivingBase entity) {
        return entity.getHealth() * (20.0 / entity.getTotalArmorValue());
    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
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

    private int getOrCacheArmor(EntityPlayer player) {
        Integer cachedTargetArmor = (Integer)this.entityArmorCache.get(player);
        if (cachedTargetArmor == null) {
            int targetArmor = (int)Math.ceil(getTotalArmorProtection(player) / 20.0D * 100.0D);
            this.entityArmorCache.put(player, targetArmor);
            return targetArmor;
        } else {
            return cachedTargetArmor;
        }
    }

    public static double getTotalArmorProtection(EntityPlayer player) {
        double totalArmor = 0.0D;

        for(int i = 0; i < 4; ++i) {
            ItemStack armorStack = player.getCurrentArmor(i);
            if (armorStack != null && armorStack.getItem() instanceof ItemArmor) {
                totalArmor += getDamageReduction(armorStack);
            }
        }

        return totalArmor;
    }

    public static double getDamageReduction(ItemStack stack) {
        double reduction = 0.0D;
        ItemArmor armor = (ItemArmor)stack.getItem();
        reduction += (double)armor.damageReduceAmount;
        if (stack.isItemEnchanted()) {
            reduction += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.25D;
        }

        return reduction;
    }

    private static int alphaToInt(float alpha, int offset) {
        return Math.min(255, (int)Math.ceil((double)(alpha * 255.0F)) + offset);
    }

    public static boolean isTeamMate(final EntityPlayer entity) {
        final String entName = entity.getDisplayName().getFormattedText();
        final String playerName = mc.thePlayer.getDisplayName().getFormattedText();
        return entName.length() >= 2 && playerName.length() >= 2 && entName.startsWith("§") && playerName.startsWith("§") && entName.charAt(1) == playerName.charAt(1);
    }

    private enum SortingMethod
    {
        DISTANCE((Comparator<EntityLivingBase>)new DistanceSorting()),
        HEALTH((Comparator<EntityLivingBase>)new HealthSorting()),
        HURT_TIME((Comparator<EntityLivingBase>)new HurtTimeSorting()),
        ANGLE((Comparator<EntityLivingBase>)new AngleSorting()),
        COMBINED((Comparator<EntityLivingBase>)new CombinedSorting());

        private final Comparator<EntityLivingBase> sorter;

        private SortingMethod(final Comparator<EntityLivingBase> sorter) {
            this.sorter = sorter;
        }

        public Comparator<EntityLivingBase> getSorter() {
            return this.sorter;
        }
    }

    private static class AngleSorting implements Comparator<EntityLivingBase>
    {
        @Override
        public int compare(final EntityLivingBase o1, final EntityLivingBase o2) {
            final float yaw = mc.thePlayer.currentEvent.getYaw();
            return Double.compare(Math.abs(RotationUtils.getYawToEntity(o1) - yaw), Math.abs(RotationUtils.getYawToEntity(o2) - yaw));
        }
    }

    private static class CombinedSorting implements Comparator<EntityLivingBase>
    {
        @Override
        public int compare(final EntityLivingBase o1, final EntityLivingBase o2) {
            int t1 = 0;
            SortingMethod[] values;
            for (int length = (values = SortingMethod.values()).length, i = 0; i < length; ++i) {
                final SortingMethod sortingMethod = values[i];
                final Comparator<EntityLivingBase> sorter = sortingMethod.getSorter();
                if (sorter != this) {
                    t1 += sorter.compare(o1, o2);
                }
            }
            return t1;
        }
    }

    private static class DistanceSorting implements Comparator<EntityLivingBase>
    {
        @Override
        public int compare(final EntityLivingBase o1, final EntityLivingBase o2) {
            return Double.compare(o1.getDistanceToEntity(mc.thePlayer), o2.getDistanceToEntity(mc.thePlayer));
        }
    }

    private static class HealthSorting implements Comparator<EntityLivingBase>
    {
        @Override
        public int compare(final EntityLivingBase o1, final EntityLivingBase o2) {
            return Double.compare(KillAura.getEffectiveHealth(o1), KillAura.getEffectiveHealth(o2));
        }
    }

    private static class HurtTimeSorting implements Comparator<EntityLivingBase>
    {
        @Override
        public int compare(final EntityLivingBase o1, final EntityLivingBase o2) {
            return Integer.compare(20 - o2.hurtResistantTime, 20 - o1.hurtResistantTime);
        }
    }
}
/*
public ModeSetting auraMode = new ModeSetting("Aura Mode", "Sort", "Sort");
    public ModeSetting rotationMode = new ModeSetting("Rotation Mode", "Normal", "Normal", "Angle", "Origin", "Smooth", "None");
    public ModeSetting attackMode = new ModeSetting("Attack Mode", "PRE", "PRE", "POST");
    public ModeSetting sortingMode = new ModeSetting("Sorting Mode", "Distance", "Distance", "Health", "Angle", "Hurt Time", "Multi");

    public NumberSetting minAps = new NumberSetting("Min APS", 10.0, 1.0, 20.0, 1.0);
    public NumberSetting maxAps = new NumberSetting("Max APS", 12.0,1.0, 20.0, 1.0);

    public NumberSetting attackRange = new NumberSetting("Attack Range", 4.0, 3.0, 7.0, 0.1);

    public NumberSetting hitChance = new NumberSetting("Hit Chance", 100, 1, 100, 1);

    public static BooleanSetting autoBlockProperty = new BooleanSetting("AutoBlock", true);
    public static NumberSetting blockRangeProp = new NumberSetting("Block Range", 4.0, autoBlockProperty::isEnabled, 3.0, 7.0, 0.1);
    public static ModeSetting autoBlockMode = new ModeSetting("AutoBlock Mode", "NCP", autoBlockProperty::isEnabled, "NCP", "Watchdog", "Redesky", "Vanilla", "Fake");

    public NumberSetting angleStep = new NumberSetting("Angle Step", 45.0, () -> rotationMode.is("Angle"), 1.0, 180.0, 1.0);

    public BooleanSetting randomize = new BooleanSetting("Randomize", false);
    public NumberSetting randFactor = new NumberSetting("RAND Factor", 2.0, randomize::isEnabled, 0.1, 30.0, 0.1);

    public BooleanSetting indicator = new BooleanSetting("Indicator", true);
    public ModeSetting indMode = new ModeSetting("Indicator Mode", "Square", "Square", "Exhibition");
    public ColorSetting indColor = new ColorSetting("Indicator Color", new Color(0, 255, 88, 75), () -> indicator.isEnabled() && indMode.is("Square"));

    public BooleanSetting strafeFix = new BooleanSetting("Strafe Fix", false);
    public BooleanSetting lockView = new BooleanSetting("Lock View", false);
    public BooleanSetting keepSprint = new BooleanSetting("Keep Sprint", true);
    public BooleanSetting packetUpdate = new BooleanSetting("Packet Update", false);
    public BooleanSetting rayCast = new BooleanSetting("Ray Cast", false);

    public BooleanSetting throughWalls = new BooleanSetting("Through Walls", true);
    public ModeSetting targetHudMode = new ModeSetting("TargetHUD", "Exhibition", "Exhibition", "Novoline Old");

    public BooleanSetting rangeCircle = new BooleanSetting("Range Circle", false);
    public ColorSetting firstColor = new ColorSetting("First Color", new Color(0, 130, 255), rangeCircle::isEnabled);
    public ColorSetting secondColor = new ColorSetting("Second Color", new Color(131, 0, 0), rangeCircle::isEnabled);

    private static final C07PacketPlayerDigging PLAYER_DIGGING = new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
    private static final C08PacketPlayerBlockPlacement BLOCK_PLACEMENT = new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f);

    private final TimerUtil attackTimer = new TimerUtil();
    public static int waitTicks;
    public static EntityLivingBase target;
    public static boolean blocking;
    private boolean entityNearby;
    public static KillAura instance;
    double healthBarWidth;
    private float lastHealth = 0.0F;
    private float fixedYaw = 0.0f;
    private boolean hasEnemyBeenHit = false;

    public KillAura() {
        super("KillAura", Keyboard.KEY_V, "Automatically attacks a target in range", Category.COMBAT);
        this.addSettings(auraMode, rotationMode, attackMode, sortingMode, minAps, maxAps,
                attackRange, hitChance, autoBlockProperty, blockRangeProp, autoBlockMode, angleStep,
                randomize, randFactor, indicator, indMode, indColor, strafeFix, lockView, keepSprint,
                packetUpdate, rayCast, throughWalls, targetHudMode, rangeCircle, firstColor, secondColor);
    }

    @Override
    public void onDisable() {
        if (blocking) {
            blocking = false;
            mc.playerController.syncCurrentPlayItem();
            mc.getNetHandler().getNetworkManager().sendPacket(PLAYER_DIGGING);
        }
        target = null;
        entityNearby = false;
    }

    public void onEvent(Event e) {
        this.setSuffix(auraMode.getMode());
        if(e instanceof EventPacket) {
            if(((EventPacket) e).getPacket() instanceof C0APacketAnimation) {
                attackTimer.reset();
            }
        }

        if(e instanceof EventRender3D) {
            EventRender3D event = (EventRender3D) e;

            if(rangeCircle.isEnabled()) {
                RenderUtils.drawRadius(mc.thePlayer, attackRange.getValue(), event.getPartialTicks(), (int) 100, (float) 0.5, firstColor.getColor());
                RenderUtils.drawRadius(mc.thePlayer, blockRangeProp.getValue() - 0.1, event.getPartialTicks(), (int) 100, (float) 0.5, secondColor.getColor());
            }
        }

        if(e instanceof EventMotion) {
            EventMotion event = (EventMotion) e;

            if(e.isPre()) {
                if (waitTicks > 0) {
                    --waitTicks;
                    return;
                }
                entityNearby = false;
                final float range = (float) attackRange.getValue();
                final float blockRange = (float) blockRangeProp.getValue();
                EntityLivingBase optimalTarget = null;

                if(target == null)
                    hasEnemyBeenHit = false;

                List<EntityLivingBase> entities = Resolute.getLivingEntities();

                if(sortingMode.is("Distance"))
                    entities.sort(SortingMethod.DISTANCE.sorter);
                if(sortingMode.is("Health"))
                    entities.sort(SortingMethod.HEALTH.sorter);
                if(sortingMode.is("Angle"))
                    entities.sort(SortingMethod.ANGLE.sorter);
                if(sortingMode.is("Hurt Time"))
                    entities.sort(SortingMethod.HURT_TIME.sorter);
                if(sortingMode.is("Multi"))
                    entities.sort(SortingMethod.COMBINED.sorter);


                for (EntityLivingBase entity : entities) {
                    if (isValid(entity)) {
                        float dist = mc.thePlayer.getDistanceToEntity(entity);

                        if (!entityNearby && dist < blockRange)
                            entityNearby = true;

                        if (dist <= range) {
                            optimalTarget = entity;
                            break;
                        }
                    }
                }

                target = optimalTarget;

                if (optimalTarget != null) {
                    float[] rotations;
                    float yaw;
                    float pitch;

                    if(rotationMode.is("Normal")) {
                        rotations = RotationUtils.getRotationsToEntity(optimalTarget);
                        yaw = rotations[0];
                        pitch = rotations[1];
                        fixedYaw = rotations[0];
                    } else if(rotationMode.is("Angle")) {
                        rotations = getSmoothedRotationsToEntity(optimalTarget, lockView.isEnabled() ? mc.thePlayer.rotationYaw : event.getPrevYaw(), lockView.isEnabled() ? mc.thePlayer.rotationPitch : event.getPrevPitch());
                        yaw = rotations[0];
                        pitch = rotations[1];
                        fixedYaw = rotations[0];
                    } else if(rotationMode.is("Origin")) {
                        rotations = getRotations(optimalTarget);
                        yaw = rotations[0];
                        pitch = rotations[1];
                        fixedYaw = rotations[0];
                    } else if(rotationMode.is("Smooth")) {
                        final AngleUtility angleUtil = new AngleUtility(10, 190, 10, 10);
                        Vector3<Double> enemyCoords = new Vector3<>(target.posX, target.posY, target.posZ);
                        Vector3<Double> myCoords = new Vector3<>(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                        Angle dstAngle = angleUtil.calculateAngle(enemyCoords, myCoords);

                        yaw = dstAngle.getYaw();
                        pitch = dstAngle.getPitch();
                        fixedYaw = dstAngle.getYaw();
                    } else {
                        yaw = event.getYaw();
                        pitch = event.getPitch();
                        fixedYaw = event.getYaw();
                    }

                    if (randomize.isEnabled()) {
                        if (pitch > 0.0F)
                            pitch += Math.random() * randFactor.getValue();
                        else
                            pitch -= Math.random() * randFactor.getValue();
                        final double yawRandom = Math.random() * randFactor.getValue();
                        if (yawRandom > yawRandom / 2)
                            yaw += yawRandom;
                        else
                            yaw -= yawRandom;
                    }

                    if(pitch > 90) {
                        pitch = 90;
                    } else if(pitch < -90) {
                        pitch = -90;
                    }

                    event.setYaw(yaw);
                    event.setPitch(pitch);

                    if (lockView.isEnabled()) {
                        mc.thePlayer.rotationYaw = yaw;
                        mc.thePlayer.rotationPitch = pitch;
                    }

                    if (packetUpdate.isEnabled())
                        mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(event.getX(), event.getY(), event.getZ(), event.getYaw(), event.getPitch(), event.isOnGround()));

                    if (attackMode.is("PRE"))
                        tryAttack(event);
                }

                if (blocking) {
                    blocking = false;
                    if (isHoldingSword()) {
                        if(autoBlockMode.is("NCP")) {
                            mc.getNetHandler().getNetworkManager().sendPacket(PLAYER_DIGGING);
                        }
                        if(autoBlockMode.is("Vanilla")) {
                            mc.getNetHandler().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        }
                        if(autoBlockMode.is("Redesky")) {
                            mc.playerController.syncCurrentPlayItem();
                        }
                        if(autoBlockMode.is("Watchdog")) {
                            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(1, 1, 1), EnumFacing.DOWN));
                        }
                    }
                }
            } else if (waitTicks <= 0) {
                if (target != null && attackMode.is("POST"))
                    tryAttack(event);

                if (entityNearby && autoBlockProperty.isEnabled() && isHoldingSword()) {
                    if(autoBlockMode.is("NCP") || autoBlockMode.is("Redesky")) {
                        mc.thePlayer.setItemInUse(mc.thePlayer.getCurrentEquippedItem(), mc.thePlayer.getCurrentEquippedItem().getMaxItemUseDuration());
                    }
                    if(autoBlockMode.is("Vanilla")) {
                        mc.thePlayer.setItemInUse(mc.thePlayer.getCurrentEquippedItem(), 71999);
                    }
                    if(autoBlockMode.is("Watchdog")) {
                        mc.thePlayer.setItemInUse(mc.thePlayer.getCurrentEquippedItem(), 71999);
                    }
                    if (!blocking) {
                        if(autoBlockMode.is("NCP")) {
                            mc.getNetHandler().getNetworkManager().sendPacket(BLOCK_PLACEMENT);
                        }
                        if(autoBlockMode.is("Redesky")) {
                            mc.getNetHandler().getNetworkManager().sendPacket( new C02PacketUseEntity ( target, new Vec3( RandomUtils.nextInt ( 50, 50 ) / 100.0, RandomUtils.nextInt ( 0, 200 ) / 100.0, RandomUtils.nextInt ( 50, 50 ) / 100.0 ) ) );
                            mc.getNetHandler().getNetworkManager().sendPacket( new C02PacketUseEntity ( target, C02PacketUseEntity.Action.INTERACT ) );
                        }
                        if(autoBlockMode.is("Vanilla")) {
                            mc.getNetHandler().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(RandomUtils.nextFloat(0, 1), RandomUtils.nextFloat(0, 1), RandomUtils.nextFloat(0, 1)), 255, mc.thePlayer.getHeldItem(), 0, 0, 0));
                        }
                        if(autoBlockMode.is("Watchdog")) {
                            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
                            mc.getNetHandler().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                        }
                        blocking = true;
                    }
                }
            }
        }

        if(e instanceof EventRender2D) {
            ScaledResolution sr;
            float width;
            float height;
            float health;
            float healthPercentage;

            if(target != null) {
                if(targetHudMode.is("Novoline Old")) {
                    sr = new ScaledResolution(mc);
                    if(target instanceof EntityPlayer) {
                        GlStateManager.pushMatrix();
                        width = (float) (sr.getScaledWidth() / 2.0D + 100.0D);
                        height = (float) (sr.getScaledHeight() / 2.0D);
                        Gui.drawRect((width - 70.0F), (height + 50.0F), (width + 60.0F), (height + 105.0F), (new Color(0, 0, 0, 180)).getRGB());


                        health = target.getHealth();
                        healthPercentage = health / target.getMaxHealth();
                        float targetHealthPercentage = 0.0F;
                        if (healthPercentage != this.lastHealth) {
                            float diff = healthPercentage - this.lastHealth;
                            targetHealthPercentage = this.lastHealth;
                            this.lastHealth += diff / 8.0F;
                        }
                        Color healthcolor = Color.WHITE;
                        if (healthPercentage * 100.0F > 75.0F) {
                            healthcolor = Color.GREEN;
                        } else if (healthPercentage * 100.0F > 50.0F && healthPercentage * 100.0F < 75.0F) {
                            healthcolor = Color.YELLOW;
                        } else if (healthPercentage * 100.0F < 50.0F && healthPercentage * 100.0F > 25.0F) {
                            healthcolor = Color.ORANGE;
                        } else if (healthPercentage * 100.0F < 25.0F) {
                            healthcolor = Color.RED;
                        }

                        Gui.drawRect((width - 70.0F), (height + 104.0F), (width - 70.0F + 130.0F * targetHealthPercentage), (height + 106.0F), healthcolor.getRGB());

                        mc.fontRendererObj.drawString(((EntityPlayer) target).getName(), (int) (width - 30), (int) (height + 60), -1);

                        for (int i = 3; i > -1; i--) {
                            ItemStack itemstack = ((EntityPlayer) target).inventory.armorInventory[i];
                            int yAdd = i * 15;
                            GL11.glPushMatrix();
                            RenderHelper.enableGUIStandardItemLighting();
                            mc.getRenderItem().renderItemAndEffectIntoGUI(itemstack, (int) (width + 10) - yAdd, (int) (height + 80));
                            GL11.glPopMatrix();
                        }
                        GL11.glPushMatrix();
                        RenderHelper.enableGUIStandardItemLighting();
                        mc.getRenderItem().renderItemAndEffectIntoGUI(((EntityPlayer) target).getCurrentEquippedItem(), (int) (int) (width + 34), (int) (height + 80));
                        GL11.glPopMatrix();

                        GlStateManager.popMatrix();
                        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                        RenderUtils.drawEntityOnScreen((int) (width - 50), (int) (height + 95), 20, 25, 25, target);
                    }
                }

                if(target instanceof EntityPlayer) {
                    if(targetHudMode.is("Exhibition")) {
                        float startX = 20;
                        sr = new ScaledResolution(mc);
                        final float x = sr.getScaledWidth() / 2.0f + 30.0f;
                        final float y = sr.getScaledHeight() / 2.0f + 30.0f;
                        final float healthRender = this.target.getHealth();
                        double hpPercentage = healthRender / this.target.getMaxHealth();
                        hpPercentage = MathHelper.clamp_double(hpPercentage, 0.0, 1.0);
                        final double hpWidth = 60.0 * hpPercentage;
                        final String healthStr = String.valueOf((int) this.target.getHealth() / 1.0f);
                        int xAdd = 0;
                        double multiplier = 0.85;
                        GlStateManager.pushMatrix();
                        GlStateManager.scale(multiplier, multiplier, multiplier);
                        if (target.getCurrentArmor(3) != null) {
                            mc.getRenderItem().renderItemAndEffectIntoGUI(target.getCurrentArmor(3), (int) ((((sr.getScaledWidth() / 2) + startX + 33) + xAdd) / multiplier), (int) (((sr.getScaledHeight() / 2) + 56) / multiplier));
                            xAdd += 15;
                        }
                        if (target.getCurrentArmor(2) != null) {
                            mc.getRenderItem().renderItemAndEffectIntoGUI(target.getCurrentArmor(2), (int) ((((sr.getScaledWidth() / 2) + startX + 33) + xAdd) / multiplier), (int) (((sr.getScaledHeight() / 2) + 56) / multiplier));
                            xAdd += 15;
                        }
                        if (target.getCurrentArmor(1) != null) {
                            mc.getRenderItem().renderItemAndEffectIntoGUI(target.getCurrentArmor(1), (int) ((((sr.getScaledWidth() / 2) + startX + 33) + xAdd) / multiplier), (int) (((sr.getScaledHeight() / 2) + 56) / multiplier));
                            xAdd += 15;
                        }
                        if (target.getCurrentArmor(0) != null) {
                            mc.getRenderItem().renderItemAndEffectIntoGUI(target.getCurrentArmor(0), (int) ((((sr.getScaledWidth() / 2) + startX + 33) + xAdd) / multiplier), (int) (((sr.getScaledHeight() / 2) + 56) / multiplier));
                            xAdd += 15;
                        }
                        if (target.getHeldItem() != null) {
                            mc.getRenderItem().renderItemAndEffectIntoGUI(target.getHeldItem(), (int) ((((sr.getScaledWidth() / 2) + startX + 33) + xAdd) / multiplier), (int) (((sr.getScaledHeight() / 2) + 56) / multiplier));
                        }

                        GlStateManager.popMatrix();
                        this.healthBarWidth = Translate.animate(hpWidth, this.healthBarWidth, 0.1);
                        Gui.drawGradientRect(x - 23.5, y - 3.5, x + 105.5f, y + 42.4f, new Color(10, 10, 10, 255).getRGB(), new Color(10, 10, 10, 255).getRGB());
                        Gui.drawGradientRect(x - 23, y - 3.2, x + 104.8f, y + 41.8f, new Color(40, 40, 40, 255).getRGB(), new Color(40, 40, 40, 255).getRGB());
                        Gui.drawGradientRect(x - 21.4, y - 1.5, x + 103.5f, y + 40.5f, new Color(74, 74, 74, 255).getRGB(), new Color(74, 74, 74, 255).getRGB());
                        Gui.drawGradientRect(x - 21, y - 1, x + 103.0f, y + 40.0f, new Color(32, 32, 32, 255).getRGB(), new Color(10, 10, 10, 255).getRGB());
                        Gui.drawRect(x + 25.0f, y + 11.0f, x + 87f, y + 14.29f, new Color(105, 105, 105, 40).getRGB());
                        Gui.drawRect(x + 25.0f, y + 11.0f, x + 27f + this.healthBarWidth, y + 14.29f, RenderUtils.getColorFromPercentage(this.target.getHealth(), this.target.getMaxHealth()));
                        mc.fontRendererObj.drawStringWithShadow(this.target.getName(), x + 24.8f, y + 1.9f, new Color(255, 255, 255).getRGB());
                        mc.fontRendererObj.drawStringWithShadow("HP:" + healthStr, x - 11.2f + 44.0f - mc.fontRendererObj.getStringWidth(healthStr) / 2.0f, y + 17.0f, -1);
                        Render2DUtils.drawFace((int) (x - 20), (int) (y), 8, 8, 8, 8, 40, 40, 64, 64, (AbstractClientPlayer) this.target);
                    }
                }
            } else {
                this.healthBarWidth = 92.0;
            }
        }

        if(e instanceof EventRender3D) {
            if (getTarget() != null && indicator.isEnabled() && indMode.is("Square")) {
                RenderUtils.drawAuraMark(this.getTarget(), this.getTarget().hurtTime > 3 ? new Color(indColor.getValue().getRed(), indColor.getValue().getGreen(), indColor.getValue().getBlue(), 75) : new Color(235, 40, 40, 75));
            }

            if (getTarget() != null && indicator.isEnabled() && indMode.is("Exhibition")) {
                RenderUtils.drawExhi(getTarget(), (EventRender3D) e);
            }
        }

        if(e instanceof StrafeEvent) {
            StrafeEvent strafeevent = (StrafeEvent) e;

            if(!strafeFix.isEnabled())
                return;

            if (target == null)
                return;

            e.setCancelled(true);
            float strafe = strafeevent.getStrafe();
            float forward = strafeevent.getForward();
            float friction = strafeevent.getFriction();
            float f = strafe * strafe + forward * forward;

            if (f >= 1.0E-4F) {
                f = MathHelper.sqrt_float(f);
                if (f < 1.0F)
                    f = 1.0F;
                f = friction / f;
                strafe *= f;
                forward *= f;
                float f1 = MathHelper.sin(this.fixedYaw * 3.1415927F / 180.0F);
                float f2 = MathHelper.cos(this.fixedYaw * 3.1415927F / 180.0F);
                this.mc.thePlayer.motionX += (strafe * f2 - forward * f1);
                this.mc.thePlayer.motionZ += (forward * f2 + strafe * f1);
            }


        }
    }

    private void tryAttack(EventMotion event) {
        final double min = minAps.getValue();
        final double max = maxAps.getValue();
        final double cps;
        if (min == max)
            cps = min;
        else
            cps = RandomUtil.getRandomInRange(minAps.getValue(), maxAps.getValue());

        if(auraMode.is("Sort")) {
            if (attackTimer.hasElapsed(1000L / (long) cps) && (!rayCast.isEnabled() || isLookingAtEntity(event.getYaw(), event.getPitch(), target, rayCast.isEnabled()))) {
                attack(target);

                if (!keepSprint.isEnabled() && mc.thePlayer.isSprinting()) {
                    mc.thePlayer.motionX *= 0.6D;
                    mc.thePlayer.motionZ *= 0.6D;
                    mc.thePlayer.setSprinting(false);
                }
            }
        }
        if(auraMode.is("HVH5")) {
            if(target.hurtTime > 0) {
                hasEnemyBeenHit = true;
            } else {
                hasEnemyBeenHit = false;
            }

            if (attackTimer.hasElapsed(480L) || (!hasEnemyBeenHit && attackTimer.hasElapsed(1000L / (long) cps)) && (!rayCast.isEnabled() || isLookingAtEntity(event.getYaw(), event.getPitch(), target, rayCast.isEnabled()))) {
                attack(target);

                if (!keepSprint.isEnabled() && mc.thePlayer.isSprinting()) {
                    mc.thePlayer.motionX *= 0.6D;
                    mc.thePlayer.motionZ *= 0.6D;
                    mc.thePlayer.setSprinting(false);
                }
            }
        }
    }

    private boolean isLookingAtEntity(float yaw, float pitch, Entity entity, boolean rayTrace) {
        double range = attackRange.getValue();
        Vec3 src = mc.thePlayer.getPositionEyes(1.0F);
        Vec3 rotationVec = Entity.getVectorForRotation(pitch, yaw);
        Vec3 dest = src.addVector(rotationVec.xCoord * range, rotationVec.yCoord * range, rotationVec.zCoord * range);
        AxisAlignedBB entBB = entity.getEntityBoundingBox().expand(0.1F, 0.1F, 0.1F);

        if (rayTrace) {
            MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(src, dest,
                    false, false, false);

            if (obj != null) {
                if (obj.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY)
                    return false;
                if (obj.hitVec.distanceTo(src) > range)
                    return false;
            }
        }

        return entBB.calculateIntercept(src, dest) != null;
    }

    private boolean isHoldingSword() {
        return mc.thePlayer.getCurrentEquippedItem() != null &&
                mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword;
    }


    private void attack(EntityLivingBase e) {
        mc.thePlayer.swingItem();

        if (ThreadLocalRandom.current().nextInt(0, 100) <= hitChance.getValue()) {
            mc.getNetHandler().getNetworkManager().sendPacket(new C02PacketUseEntity(e, C02PacketUseEntity.Action.ATTACK));
        }
    }

    private float[] getSmoothedRotationsToEntity(Entity entity,
                                                 float prevYaw,
                                                 float prevPitch) {
        final EntityPlayerSP player = mc.thePlayer;
        double xDist = entity.posX - player.posX;
        double zDist = entity.posZ - player.posZ;

        double entEyeHeight = entity.getEyeHeight();
        double yDist = ((entity.posY + entEyeHeight) - Math.min(Math.max(entity.posY - player.posY, 0), entEyeHeight)) -
                (player.posY + player.getEyeHeight());

        double fDist = MathHelper.sqrt_double(xDist * xDist + zDist * zDist);

        float yaw = interpolateRotation(prevYaw, (float) (Math.atan2(zDist, xDist) * 180.0D / Math.PI) - 90.0F);
        float pitch = interpolateRotation(prevPitch, (float) (-(Math.atan2(yDist, fDist) * 180.0D / Math.PI)));
        return new float[]{yaw, pitch};
    }

    private float interpolateRotation(float p_70663_1_,
                                      float p_70663_2_) {
        float maxTurn = (float) (angleStep.getValue() / 5.0F);
        float var4 = MathHelper.wrapAngleTo180_float(p_70663_2_ - p_70663_1_);

        if (var4 > maxTurn) {
            var4 = maxTurn;
        }

        if (var4 < -maxTurn) {
            var4 = -maxTurn;
        }

        return p_70663_1_ + var4;
    }

    private static float[] getRotations(final Entity entity) {
        final EntityPlayerSP player = mc.thePlayer;
        final double xDist = entity.posX - player.posX;
        final double zDist = entity.posZ - player.posZ;
        double yDist = entity.posY - player.posY;
        final double dist = StrictMath.sqrt(xDist * xDist + zDist * zDist);
        final AxisAlignedBB entityBB = entity.getEntityBoundingBox().expand(0.10000000149011612, 0.10000000149011612, 0.10000000149011612);
        final double playerEyePos = player.posY + player.getEyeHeight();
        final boolean close = dist < 2.0 && Math.abs(yDist) < 2.0;
        float pitch;
        if (close && playerEyePos > entityBB.minY) {
            pitch = 60.0f;
        }
        else {
            yDist = ((playerEyePos > entityBB.maxY) ? (entityBB.maxY - playerEyePos) : ((playerEyePos < entityBB.minY) ? (entityBB.minY - playerEyePos) : 0.0));
            pitch = (float)(-(StrictMath.atan2(yDist, dist) * 57.29577951308232));
        }
        float yaw = (float)(StrictMath.atan2(zDist, xDist) * 57.29577951308232) - 90.0f;
        if (close) {
            final int inc = (dist < 1.0) ? 180 : 90;
            yaw = (float)(Math.round(yaw / inc) * inc);
        }
        return new float[] { yaw, pitch };
    }

    private boolean isValid(final EntityLivingBase entity) {
        if (!entity.isEntityAlive()) {
            return false;
        }
        if (entity.isInvisible() && !SkeetUI.isInvisibles()) {
            return false;
        }


        if (entity instanceof EntityOtherPlayerMP) {
            final EntityPlayer player = (EntityPlayer)entity;
            if (!SkeetUI.isPlayers()) {
                return false;
            }
            if (AntiBot.enabled && AntiBot.mode.is("Ping") && !ping(player)) {
                return false;
            }
            if(AntiBot.enabled && AntiBot.mode.is("Hypixel") && AntiBot.watchdogBots.contains(player)) {
                return false;
            }
            if (!SkeetUI.isTeams() && isTeamMate(player)) {
                return false;
            }
        }
        else if(entity instanceof EntityVillager) {
            if(!SkeetUI.isVillagers()) {
                return false;
            }
        }
        else if ((entity instanceof EntityMob || entity instanceof EntityAmbientCreature || entity instanceof EntityWaterMob)) {
            if (!SkeetUI.isMobs()) {
                return false;
            }
        } else {
            if (!(entity instanceof EntityAnimal)) {
                return false;
            }
            if (!SkeetUI.isAnimals()) {
                return false;
            }
        }

        return mc.thePlayer.getDistanceToEntity(entity) < Math.max(this.blockRangeProp.getValue(), this.attackRange.getValue());
    }

    public EntityLivingBase getTarget() {
        return target;
    }

    public static boolean ping(final EntityPlayer entity) {
        final NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(entity.getUniqueID());
        return info != null && info.getResponseTime() == 1;
    }

    public static boolean isTeamMate(final EntityPlayer entity) {
        final String entName = entity.getDisplayName().getFormattedText();
        final String playerName = mc.thePlayer.getDisplayName().getFormattedText();
        return entName.length() >= 2 && playerName.length() >= 2 && entName.startsWith("§") && playerName.startsWith("§") && entName.charAt(1) == playerName.charAt(1);
    }

    public static double getEffectiveHealth(final EntityLivingBase entity) {
        return entity.getHealth() * (20.0 / entity.getTotalArmorValue());
    }

    private static class AngleSorting implements Comparator<EntityLivingBase>
    {
        @Override
        public int compare(final EntityLivingBase o1, final EntityLivingBase o2) {
            final float yaw = mc.thePlayer.currentEvent.getYaw();
            return Double.compare(Math.abs(RotationUtils.getYawToEntity(o1) - yaw), Math.abs(RotationUtils.getYawToEntity(o2) - yaw));
        }
    }

    private static class CombinedSorting implements Comparator<EntityLivingBase>
    {
        @Override
        public int compare(final EntityLivingBase o1, final EntityLivingBase o2) {
            int t1 = 0;
            SortingMethod[] values;
            for (int length = (values = SortingMethod.values()).length, i = 0; i < length; ++i) {
                final SortingMethod sortingMethod = values[i];
                final Comparator<EntityLivingBase> sorter = sortingMethod.getSorter();
                if (sorter != this) {
                    t1 += sorter.compare(o1, o2);
                }
            }
            return t1;
        }
    }

    private enum SortingMethod
    {
        DISTANCE((Comparator<EntityLivingBase>)new DistanceSorting()),
        HEALTH((Comparator<EntityLivingBase>)new HealthSorting()),
        HURT_TIME((Comparator<EntityLivingBase>)new HurtTimeSorting()),
        ANGLE((Comparator<EntityLivingBase>)new AngleSorting()),
        COMBINED((Comparator<EntityLivingBase>)new CombinedSorting());

        private final Comparator<EntityLivingBase> sorter;

        private SortingMethod(final Comparator<EntityLivingBase> sorter) {
            this.sorter = sorter;
        }

        public Comparator<EntityLivingBase> getSorter() {
            return this.sorter;
        }
    }

    private static class DistanceSorting implements Comparator<EntityLivingBase>
    {
        @Override
        public int compare(final EntityLivingBase o1, final EntityLivingBase o2) {
            return Double.compare(o1.getDistanceToEntity(mc.thePlayer), o2.getDistanceToEntity(mc.thePlayer));
        }
    }

    private static class HealthSorting implements Comparator<EntityLivingBase>
    {
        @Override
        public int compare(final EntityLivingBase o1, final EntityLivingBase o2) {
            return Double.compare(KillAura.getEffectiveHealth(o1), KillAura.getEffectiveHealth(o2));
        }
    }

    private static class HurtTimeSorting implements Comparator<EntityLivingBase>
    {
        @Override
        public int compare(final EntityLivingBase o1, final EntityLivingBase o2) {
            return Integer.compare(20 - o2.hurtResistantTime, 20 - o1.hurtResistantTime);
        }
    }
 */