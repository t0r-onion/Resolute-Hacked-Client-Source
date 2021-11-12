package vip.Resolute.modules.impl.combat;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.events.impl.EventRender3D;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.render.Colors;
import vip.Resolute.util.render.RenderUtils;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import java.util.Iterator;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.network.play.client.C0BPacketEntityAction;

import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import java.util.HashMap;
import net.minecraft.util.Vec3;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.entity.player.EntityPlayer;
import vip.Resolute.modules.Module;

public class AimBot extends Module {
    public int ticks;
    public int lookDelay;
    private EntityPlayer target;
    public int buffer = 10;
    private Map playerPositions = new HashMap();
    public static boolean isFiring;

    public BooleanSetting silent = new BooleanSetting("Silent", true);
    public NumberSetting delay = new NumberSetting("Delay", 3.0, 0.1, 10.0, 0.1);
    public BooleanSetting autoFire = new BooleanSetting("Auto Fire", true);
    public NumberSetting fov = new NumberSetting("FOV", 90, 5, 360, 5);
    public NumberSetting recoilSet = new NumberSetting("Recoil", 1.5, 0.1, 3.0, 0.1);

    public AimBot() {
        super("AimBot", 0, "Automatically aims at targets", Category.COMBAT);
        this.addSettings(silent, delay, autoFire, fov, recoilSet);
    }

    public void onEvent(Event e) {
        if (isFiring) {
            isFiring = false;
        }

        if (e instanceof EventMotion && mc.thePlayer.isEntityAlive()) {
            final EventMotion em = (EventMotion) e;

            if (em.isPre()) {
                double targetWeight = Double.NEGATIVE_INFINITY;
                this.target = null;
                Iterator var5 = mc.theWorld.getLoadedEntityList().iterator();

                Object o;
                EntityPlayer p;
                while(var5.hasNext()) {
                    o = var5.next();
                    if (o instanceof EntityPlayer) {
                        p = (EntityPlayer)o;
                        if (p != mc.thePlayer && !isTeam(mc.thePlayer, p) && mc.thePlayer.canEntityBeSeen(p) && this.isVisibleFOV(mc.thePlayer, p, (float) fov.getValue())) {
                            if (this.target == null) {
                                this.target = p;
                                targetWeight = this.getTargetWeight(p);
                            } else if (this.getTargetWeight(p) > targetWeight) {
                                this.target = p;
                                targetWeight = this.getTargetWeight(p);
                            }
                        }
                    }
                }

                Object[] var13 = this.playerPositions.keySet().toArray();
                int var15 = var13.length;

                for(int var17 = 0; var17 < var15; ++var17) {
                    o = var13[var17];
                    EntityPlayer player = (EntityPlayer)o;
                    if (!mc.theWorld.playerEntities.contains(player) || !checkPing(player)) {
                        this.playerPositions.remove(player);
                    }
                }

                var5 = mc.theWorld.playerEntities.iterator();

                while(true) {
                    List previousPositions;
                    do {
                        if (!var5.hasNext()) {
                            if (this.target != null) {
                                EntityLivingBase simulated = (EntityLivingBase)this.predictPlayerMovement(this.target);
                                float[] rotations = getRotationsToEnt(predict(target), mc.thePlayer);
                                em.setYaw(rotations[0]);
                                em.setPitch(rotations[1]);

                                ++this.lookDelay;
                                if ((float)this.lookDelay >= delay.getValue()) {
                                    isFiring = true;
                                    boolean nospread = true;
                                    if (nospread) {
                                        mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
                                    }

                                    if (autoFire.isEnabled() && mc.thePlayer.inventory.getCurrentItem() != null) {
                                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem());
                                    }

                                    if (nospread) {
                                        mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                                    }

                                    this.lookDelay = 0;
                                    return;
                                }
                            } else {
                                --this.ticks;
                                if (this.ticks <= 0) {
                                    this.ticks = 0;
                                    return;
                                }
                            }

                            return;
                        }

                        o = var5.next();
                        p = (EntityPlayer)o;
                        this.playerPositions.putIfAbsent(p, new ArrayList());
                        previousPositions = (List)this.playerPositions.get(p);
                        previousPositions.add(new Vec3(p.posX, p.posY, p.posZ));
                    } while(previousPositions.size() <= this.buffer);

                    int i = 0;

                    for(Iterator var10 = (new ArrayList(previousPositions)).iterator(); var10.hasNext(); ++i) {
                        Vec3 position = (Vec3)var10.next();
                        if (i < previousPositions.size() - this.buffer) {
                            previousPositions.remove(previousPositions.get(i));
                        }
                    }
                }
            }
        } else if(e instanceof EventPacket) {
            if(((EventPacket) e).getPacket() instanceof C08PacketPlayerBlockPlacement) {
                ++this.ticks;
            }
        } else if(e instanceof EventRender3D) {
            EventRender3D er = (EventRender3D) e;

            if (target != null) {
                double[] p = this.getPredPos(target);
                double x = mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * (double)er.getPartialTicks() - RenderManager.renderPosX;
                double y = mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * (double)er.getPartialTicks() - RenderManager.renderPosY;
                double z = mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * (double)er.getPartialTicks() - RenderManager.renderPosZ;
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                RenderUtils.filledBox(new AxisAlignedBB(p[0] - 0.5D, p[1], p[2] - 0.5D, p[0] + 0.5D, p[1] + 2.0D, p[2] + 0.5D), Colors.getColor(255, 0, 0, 100), true);
                GlStateManager.popMatrix();
            }
        }
    }

    private float[] getRotationsToEnt(Vec3 ent, EntityPlayerSP playerSP) {
        final double differenceX = ent.xCoord - playerSP.posX;
        final double differenceY = (ent.yCoord + target.height) - (playerSP.posY + playerSP.height);
        final double differenceZ = ent.zCoord - playerSP.posZ;
        final float rotationYaw = (float) (Math.atan2(differenceZ, differenceX) * 180.0D / Math.PI) - 90.0f;
        final float rotationPitch = (float) (Math.atan2(differenceY, playerSP.getDistanceToEntity(target)) * 180.0D / Math.PI);
        final float finishedYaw = playerSP.rotationYaw + MathHelper.wrapAngleTo180_float(rotationYaw - playerSP.rotationYaw);
        final float finishedPitch = playerSP.rotationPitch + MathHelper.wrapAngleTo180_float(rotationPitch - playerSP.rotationPitch);
        return new float[]{finishedYaw, -finishedPitch};
    }

    public static boolean checkPing(final EntityPlayer entity) {
        final NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(entity.getUniqueID());
        return info != null && info.getResponseTime() == 1;
    }

    private double[] getPredPos(Entity entity) {
        double xDelta = entity.posX - entity.lastTickPosX;
        double zDelta = entity.posZ - entity.lastTickPosZ;
        double yDelta = entity.posY - entity.lastTickPosY;
        double d = (double)mc.thePlayer.getDistanceToEntity(entity);
        d -= d % 0.8D;
        double xMulti = 1.0D;
        double zMulti = 1.0D;
        boolean sprint = entity.isSprinting();
        xMulti = d / 0.8D * xDelta * (sprint ? 1.2D : 1.1D);
        zMulti = d / 0.8D * zDelta * (sprint ? 1.2D : 1.1D);
        double yMulti = d / 0.8D * yDelta * 0.1D;
        double x = entity.posX + xMulti - mc.thePlayer.posX;
        double z = entity.posZ + zMulti - mc.thePlayer.posZ;
        double y = entity.posY + yMulti - mc.thePlayer.posY;
        return new double[]{x, y, z};
    }

    private Vec3 predict(EntityPlayer player) {
        int pingTicks = (int) Math.ceil(mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime() / 50D) + 1;
        return predictPos(player, pingTicks);
    }

    private static Vec3 lerp(Vec3 pos, Vec3 prev, float time) {
        double x = pos.xCoord + ((pos.xCoord - prev.xCoord) * time);
        double y = pos.yCoord + ((pos.yCoord - prev.yCoord) * time);
        double z = pos.zCoord + ((pos.zCoord - prev.zCoord) * time);
        return new Vec3(x, y, z);
    }

    public static Vec3 predictPos(Entity entity, float time) {
        return lerp(new Vec3(entity.posX, entity.posY, entity.posZ), new Vec3(entity.prevPosX, entity.prevPosY, entity.prevPosZ), time);
    }

    public static boolean isTeam(EntityPlayer e, EntityPlayer e2) {
        return e.getDisplayName().getFormattedText().contains("§" + isTeam(e)) && e2.getDisplayName().getFormattedText().contains("§" + isTeam(e));
    }

    private static String isTeam(EntityPlayer player) {
        Matcher m = Pattern.compile("§(.).*§r").matcher(player.getDisplayName().getFormattedText());
        return m.find() ? m.group(1) : "f";
    }

    public boolean isVisibleFOV(EntityLivingBase e, EntityLivingBase e2, float fov) {
        return (Math.abs(getRotations(e)[0] - e2.rotationYaw) % 360.0F > 180.0F ? 360.0F - Math.abs(getRotations(e)[0] - e2.rotationYaw) % 360.0F : Math.abs(getRotations(e)[0] - e2.rotationYaw) % 360.0F) <= fov;
    }

    public static float[] getRotations(EntityLivingBase ent) {
        double x = ent.posX;
        double z = ent.posZ;
        double y = ent.posY + (double)(ent.getEyeHeight() / 2.0F);
        return getRotationFromPosition(x, z, y);
    }

    public static float[] getRotationFromPosition(double x, double z, double y) {
        double xDiff = x - mc.thePlayer.posX;
        double zDiff = z - mc.thePlayer.posZ;
        double yDiff = y - mc.thePlayer.posY - 1.2D;
        double dist = (double) MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float)(Math.atan2(zDiff, xDiff) * 180.0D / 3.141592653589793D) - 90.0F;
        float pitch = (float)(-(Math.atan2(yDiff, dist) * 180.0D / 3.141592653589793D));
        return new float[]{yaw, pitch};
    }

    public double getTargetWeight(EntityPlayer p) {
        double weight = (double)(-mc.thePlayer.getDistanceToEntity(p));
        if (p.lastTickPosX == p.posX && p.lastTickPosY == p.posY && p.lastTickPosZ == p.posZ) {
            weight += 200.0D;
        }

        weight -= (double)(p.getDistanceToEntity(mc.thePlayer) / 5.0F);
        return weight;
    }

    private Entity predictPlayerMovement(EntityPlayer target) {
        int pingTicks = 0;

        try {
            pingTicks = (int)Math.ceil((double)mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime() / 50.0D);
        } catch (Exception var4) {
            ;
        }

        return this.predictPlayerLocation(target, pingTicks);
    }

    public Entity predictPlayerLocation(EntityPlayer player, int ticks) {
        if (this.playerPositions.containsKey(player)) {
            List previousPositions = (List)this.playerPositions.get(player);
            if (previousPositions.size() > 1) {
                Vec3 origin = (Vec3)previousPositions.get(0);
                List deltas = new ArrayList();
                Vec3 previous = origin;

                Vec3 position;
                for(Iterator var7 = previousPositions.iterator(); var7.hasNext(); previous = position) {
                    position = (Vec3)var7.next();
                    deltas.add(new Vec3(position.xCoord - previous.xCoord, position.yCoord - previous.yCoord, position.zCoord - previous.zCoord));
                }

                double x = 0.0D;
                double y = 0.0D;
                double z = 0.0D;

                Vec3 delta;
                for(Iterator var13 = deltas.iterator(); var13.hasNext(); z += delta.zCoord * 1.5D) {
                    delta = (Vec3)var13.next();
                    x += delta.xCoord * 1.5D;
                    y += delta.yCoord;
                }

                x /= (double)deltas.size();
                y /= (double)deltas.size();
                z /= (double)deltas.size();
                EntityPlayer simulated = new EntityOtherPlayerMP(mc.theWorld, player.getGameProfile());
                simulated.noClip = false;
                simulated.setPosition(player.posX, player.posY + 0.5D, player.posZ);

                for(int i = 0; i < ticks; ++i) {
                    simulated.moveEntity(x, y, z);
                }

                return simulated;
            }
        }

        return player;
    }
}
