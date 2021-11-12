package vip.Resolute.modules.impl.combat;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.*;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.misc.AStarCustomPathFinder;
import vip.Resolute.util.player.RotationUtils;
import vip.Resolute.util.misc.TimerUtils;
import vip.Resolute.util.render.RenderUtils;
import vip.Resolute.util.world.Vec3;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class TPAura extends Module {

    public NumberSetting cps = new NumberSetting("APS", 5, 1, 20, 1);
    public NumberSetting maxTargets = new NumberSetting("Max Targets", 1, 1, 5, 1);
    public NumberSetting range = new NumberSetting("Range", 300, 50, 300, 10);

    private ArrayList<Vec3> path = new ArrayList<>();
    private List<Vec3>[] test = new ArrayList[50];
    private List<EntityLivingBase> targets = new CopyOnWriteArrayList<>();
    private TimerUtils cpstimer = new TimerUtils();
    public static TimerUtils timer = new TimerUtils();
    public static boolean canReach;
    int ticks = 0;
    double startX;
    double startY;
    double startZ;
    private float lastHealth = 0.0F;

    public TPAura() {
        super("TPAura", 0, "Allows for infinite aura range", Category.COMBAT);
        this.addSettings(cps, range, maxTargets);
    }

    public void onEvent(Event e) {
        if(e instanceof EventMotion) {
            EventMotion event = (EventMotion) e;
            if(e.isPre()) {
                this.targets = getTargets();
                if (this.cpstimer.hasTimeElapsed((long)(1000.0D / cps.getValue()), true) && this.targets.size() > 0) {
                    this.test = (List<Vec3>[])new ArrayList[50];
                    for (int i = 0; i < (targets.size() > 1 ? maxTargets.getValue() : targets.size()); i++) {
                        EntityLivingBase T = this.targets.get(i);
                        if (this.mc.thePlayer.getDistanceToEntity(T) > range.getValue())
                            return;
                        Vec3 topFrom = new Vec3(this.mc.thePlayer.posX, this.mc.thePlayer.posY, this.mc.thePlayer.posZ);
                        Vec3 to = new Vec3(T.posX, T.posY, T.posZ);
                        this.path = computePath(topFrom, to);
                        this.test[i] = this.path;
                        for (Vec3 pathElm : this.path)
                            this.mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(pathElm.getX(), pathElm.getY(), pathElm.getZ(), true));
                        this.mc.thePlayer.swingItem();
                        this.mc.playerController.attackEntity(this.mc.thePlayer, T);
                        Collections.reverse(this.path);
                        for (Vec3 pathElm : this.path)
                            this.mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(pathElm.getX(), pathElm.getY(), pathElm.getZ(), true));

                        float[] rots = RotationUtils.getRotations(T);
                        event.setYaw(rots[0]);
                        event.setPitch(rots[1]);
                    }
                    this.cpstimer.reset();
                }
            }
        }

        if(e instanceof EventRender3D) {
            if(targets.size() > 0){
                for (int i = 0; i < path.size(); i++) {
                    Vec3 pathElm = path.get(i);
                    Vec3 pathOther = path.get(i < path.size() - 1 ? i + 1 : i);
                    double x = pathElm.getX() + (pathElm.getX() - pathElm.getX()) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosX;
                    double y = pathElm.getY() + (pathElm.getY() - pathElm.getY()) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosY;
                    double z = pathElm.getZ() + (pathElm.getZ() - pathElm.getZ()) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosZ;
                    double x1 = pathOther.getX() + (pathOther.getX() - pathOther.getX()) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosX;
                    double y1 = pathOther.getY() + (pathOther.getY() - pathOther.getY()) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosY;
                    double z1 = pathOther.getZ() + (pathOther.getZ() - pathOther.getZ()) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosZ;
                    AxisAlignedBB var12 = new AxisAlignedBB(x1, y1, z1, x, y, z);
                    RenderUtils.glColor(new Color(255, 255, 255).getRGB());
                    RenderUtils.drawOutlinedBoundingBox(var12);
                }
            }
        }
    }


    public void onEnable() {
        super.onEnable();
        this.startX = this.mc.thePlayer.posX;
        this.startY = this.mc.thePlayer.posY;
        this.startZ = this.mc.thePlayer.posZ;
    }

    double dashDistance = 5;

    public boolean canAttack(EntityLivingBase player) {
        return (player != this.mc.thePlayer &&
                !(player instanceof net.minecraft.entity.item.EntityArmorStand)
                && player instanceof EntityPlayer);
    }

    private List<EntityLivingBase> getTargets() {
        List<EntityLivingBase> targets = new ArrayList<>();

        for (Object o : mc.theWorld.getLoadedEntityList()) {
            if (o instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) o;
                if (canAttack(entity)) {
                    targets.add(entity);
                }
            }
        }
        targets.sort((o1, o2) -> (int) (o1.getDistanceToEntity(mc.thePlayer) * 1000 - o2.getDistanceToEntity(mc.thePlayer) * 1000));
        return targets;
    }
    private ArrayList<Vec3> computePath(Vec3 topFrom, Vec3 to) {
        if (!canPassThrow(new BlockPos(topFrom.mc()))) {
            topFrom = topFrom.addVector(0, 1, 0);
        }
        AStarCustomPathFinder pathfinder = new AStarCustomPathFinder(topFrom, to);
        pathfinder.compute();

        int i = 0;
        Vec3 lastLoc = null;
        Vec3 lastDashLoc = null;
        ArrayList<Vec3> path = new ArrayList<Vec3>();
        ArrayList<Vec3> pathFinderPath = pathfinder.getPath();
        for (Vec3 pathElm : pathFinderPath) {
            if (i == 0 || i == pathFinderPath.size() - 1) {
                if (lastLoc != null) {
                    path.add(lastLoc.addVector(0.5, 0, 0.5));
                }
                path.add(pathElm.addVector(0.5, 0, 0.5));
                lastDashLoc = pathElm;
            } else {
                boolean canContinue = true;
                if (pathElm.squareDistanceTo(lastDashLoc) > dashDistance * dashDistance) {
                    canContinue = false;
                } else {
                    double smallX = Math.min(lastDashLoc.getX(), pathElm.getX());
                    double smallY = Math.min(lastDashLoc.getY(), pathElm.getY());
                    double smallZ = Math.min(lastDashLoc.getZ(), pathElm.getZ());
                    double bigX = Math.max(lastDashLoc.getX(), pathElm.getX());
                    double bigY = Math.max(lastDashLoc.getY(), pathElm.getY());
                    double bigZ = Math.max(lastDashLoc.getZ(), pathElm.getZ());
                    cordsLoop:
                    for (int x = (int) smallX; x <= bigX; x++) {
                        for (int y = (int) smallY; y <= bigY; y++) {
                            for (int z = (int) smallZ; z <= bigZ; z++) {
                                if (!AStarCustomPathFinder.checkPositionValidity(x, y, z, false)) {
                                    canContinue = false;
                                    break cordsLoop;
                                }
                            }
                        }
                    }
                }
                if (!canContinue) {
                    path.add(lastLoc.addVector(0.5, 0, 0.5));
                    lastDashLoc = lastLoc;
                }
            }
            lastLoc = pathElm;
            i++;
        }
        return path;
    }

    private boolean canPassThrow(BlockPos pos) {
        Block block = Minecraft.getMinecraft().theWorld.getBlockState(new net.minecraft.util.BlockPos(pos.getX(), pos.getY(), pos.getZ())).getBlock();
        return block.getMaterial() == Material.air || block.getMaterial() == Material.plants || block.getMaterial() == Material.vine || block == Blocks.ladder || block == Blocks.water || block == Blocks.flowing_water || block == Blocks.wall_sign || block == Blocks.standing_sign;
    }
}
