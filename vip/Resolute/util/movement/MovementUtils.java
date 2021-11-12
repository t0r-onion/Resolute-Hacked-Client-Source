package vip.Resolute.util.movement;

import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventMove;
import vip.Resolute.modules.impl.combat.KillAura;
import vip.Resolute.modules.impl.combat.TargetStrafe;
import vip.Resolute.util.player.RotationUtils;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class MovementUtils {
    protected static Minecraft mc = Minecraft.getMinecraft();


    public static final double BUNNY_SLOPE = 0.66;
    public static final double WATCHDOG_BUNNY_SLOPE = BUNNY_SLOPE * 0.96;
    public static final double SPRINTING_MOD = 1.3;
    public static final double SNEAK_MOD = 0.3;
    public static final double ICE_MOD = 2.5;
    public static final double VANILLA_JUMP_HEIGHT = 0.42F;
    public static final double WALK_SPEED = 0.221;
    private static final List<Double> frictionValues = new ArrayList<>();
    private static final double MIN_DIF = 1.0E-2;
    public static final double MAX_DIST = 2.15 - MIN_DIF;
    public static final double BUNNY_DIV_FRICTION = 160.0D - MIN_DIF;
    private static final double SWIM_MOD = 0.115D / WALK_SPEED;
    private static final double[] DEPTH_STRIDER_VALUES = {
            1.0,
            0.1645 / SWIM_MOD / WALK_SPEED,
            0.1995 / SWIM_MOD / WALK_SPEED,
            1.0 / SWIM_MOD,
    };
    private static final double AIR_FRICTION = 0.98;
    private static final double WATER_FRICTION = 0.89;
    private static final double LAVA_FRICTION = 0.535;



    public static double getBlockHeight() {
        return mc.thePlayer.posY - (int)mc.thePlayer.posY;
    }
    public static double getJumpHeight() {
        final double baseJumpHeight = 0.41999998688697815;
        if (isInLiquid()) {
            return 0.13500000163912773;
        }
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            return baseJumpHeight + (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1.0f) * 0.1f;
        }
        return baseJumpHeight;
    }



    public static void fakeJump() {
        mc.thePlayer.isAirBorne = true;
        mc.thePlayer.triggerAchievement(StatList.jumpStat);
    }

    public static float getSensitivityMultiplier() {
        float f = Minecraft.getMinecraft().gameSettings.mouseSensitivity * 0.6F + 0.2F;
        return f * f * f * 8.0F * 0.15F;
    }

    public static double getMotion(final EntityPlayerSP player) {
        return Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
    }

    public static boolean isInsideBlock() {
        for (int x = MathHelper.floor_double(Minecraft.getMinecraft().thePlayer.getEntityBoundingBox().minX); x < MathHelper.floor_double(Minecraft.getMinecraft().thePlayer.getEntityBoundingBox().maxX) + 1; x++) {
            for (int y = MathHelper.floor_double(Minecraft.getMinecraft().thePlayer.getEntityBoundingBox().minY); y < MathHelper.floor_double(Minecraft.getMinecraft().thePlayer.getEntityBoundingBox().maxY) + 1; y++) {
                for (int z = MathHelper.floor_double(Minecraft.getMinecraft().thePlayer.getEntityBoundingBox().minZ); z < MathHelper.floor_double(Minecraft.getMinecraft().thePlayer.getEntityBoundingBox().maxZ) + 1; z++) {
                    Block block = Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block != null && !(block instanceof BlockAir)) {
                        AxisAlignedBB boundingBox = block.getCollisionBoundingBox(Minecraft.getMinecraft().theWorld, new BlockPos(x, y, z), Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(x, y, z)));
                        if (block instanceof BlockHopper)
                            boundingBox = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
                        if (boundingBox != null && Minecraft.getMinecraft().thePlayer.getEntityBoundingBox().intersectsWith(boundingBox))
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public static double[] yawPos(double value) {
        double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
        return new double[] {-Math.sin(yaw) * value, Math.cos(yaw) * value};
    }

    public static float applyCustomFriction(float speed, float friction) {
        float value = speed / 100.0F * friction;
        return value;
    }

    public static boolean fallDistDamage() {
        if (!isOnGround() || isBlockAbove()) return false;

        final EntityPlayerSP player = mc.thePlayer;
        final double randomOffset = Math.random() * 0.0003F;
        final double jumpHeight = 0.0625D - 1.0E-2D - randomOffset;
        final int packets = (int) ((getMinFallDist() / (jumpHeight - randomOffset)) + 1);

        for (int i = 0; i < packets; i++) {
            mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + jumpHeight, player.posZ, false));
            mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + randomOffset, player.posZ, false));
        }

        mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer(true));
        return true;
    }

    public static float getMinFallDist() {
        float minDist = 3.0F;
        if (mc.thePlayer.isPotionActive(Potion.jump))
            minDist += mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1.0F;
        return minDist;
    }

    public static double getBaseSpeedHypixel() {
        double baseSpeed = 0.2873D;

        return baseSpeed;
    }

    public static void bypassOffSet(EventMotion event) {
        if (MovementUtils.isMoving()) {
            List<Double> BypassOffset = Arrays.asList(0.125, 0.25, 0.375, 0.625, 0.75, 0.015625, 0.5, 0.0625, 0.875, 0.1875);
            double d3 = event.getY() % 1.0;
            BypassOffset.sort(Comparator.comparingDouble(PreY -> Math.abs(PreY - d3)));
            double acc = event.getY() - d3 + BypassOffset.get(0);
            if (Math.abs(BypassOffset.get(0) - d3) < 0.005) {
                event.setY(acc);
                event.setOnGround(true);
            } else {
                List<Double> BypassOffset2 = Arrays.asList(0.715, 0.945, 0.09, 0.155, 0.14, 0.045, 0.63, 0.31);
                double d3_ = event.getY() % 1.0;
                BypassOffset2.sort(Comparator.comparingDouble(PreY -> Math.abs(PreY - d3_)));
                acc = event.getY() - d3_ + BypassOffset2.get(0);
                if (Math.abs(BypassOffset2.get(0) - d3_) < 0.005) {
                    event.setY(acc);
                }
            }
        }
    }

    public static int getSpeedEffect() {
        return mc.thePlayer.isPotionActive(Potion.moveSpeed) ? mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 : 0;
    }

    public static double getBaseSpeedHypixelAppliedLow() {
        double baseSpeed = 0.2873D;

        if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
            baseSpeed *= 1.0D + 0.2D * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 0.3);


        return baseSpeed;
    }

    public static double getBaseSpeedHypixelApplied() {
        double baseSpeed = 0.2873D;

        if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
            baseSpeed *= 1.0D + 0.2D * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1.0);


        return baseSpeed;
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.2875D;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
            baseSpeed *= 1.0D + 0.2D * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        return baseSpeed;
    }

    public static double getBaseMoveSpeed(double basespeed) {
        double baseSpeed = basespeed;
        if (MovementUtils.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            baseSpeed *= 1.0 + 0.2 * (double)(MovementUtils.mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }
        return baseSpeed;
    }

    public static boolean canSprint(final boolean omni) {
        if (omni) {
            if (!isMovingEnoughForSprint()) {
                return false;
            }
        }
        else if (mc.thePlayer.movementInput.moveForward < 0.8f) {
            return false;
        }
        if (!mc.thePlayer.isCollidedHorizontally && (mc.thePlayer.getFoodStats().getFoodLevel() > 6 || mc.thePlayer.capabilities.allowFlying) && !mc.thePlayer.isSneaking()) {
            return true;
        }
        return false;
    }

    private static boolean isMovingEnoughForSprint() {
        final MovementInput movementInput = mc.thePlayer.movementInput;
        return movementInput.moveForward > 0.8f || movementInput.moveForward < -0.8f || movementInput.moveStrafe > 0.8f || movementInput.moveStrafe < -0.8f;
    }


    public static double getSpeed(EntityPlayer player) {
        return Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
    }

    public static boolean isInLiquid(Entity e) {
        for(int x = MathHelper.floor_double(e.boundingBox.minY); x < MathHelper.floor_double(e.boundingBox.maxX) + 1; ++x) {
            for(int z = MathHelper.floor_double(e.boundingBox.minZ); z < MathHelper.floor_double(e.boundingBox.maxZ) + 1; ++z) {
                BlockPos pos = new BlockPos(x, (int)e.boundingBox.minY, z);
                Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
                if(block != null && !(block instanceof BlockAir))
                    return block instanceof BlockLiquid;
            }
        }
        return false;
    }

    public static double getFriction(final double moveSpeed) {
        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        double lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

        return MovementUtils.calculateFriction(moveSpeed, lastDist, MovementUtils.getBaseMoveSpeed());
    }

    public static boolean isDistFromGround(double dist) {
        return Minecraft.getMinecraft().theWorld.checkBlockCollision(Minecraft.getMinecraft().thePlayer.getEntityBoundingBox().addCoord(0.0D, -dist, 0.0D));
    }

    public static Block getBlockUnder() {
        final EntityPlayerSP player = mc.thePlayer;
        return mc.theWorld.getBlockState(new BlockPos(player.posX, StrictMath.floor(player.getEntityBoundingBox().minY) - 1.0, player.posZ)).getBlock();
    }


    public static boolean isBlockAbove() {
        for (double height = 0.0D; height <= 1.0D; height += 0.5D) {
            List<AxisAlignedBB> collidingList = mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0, height, 0));
            if (!collidingList.isEmpty())
                return true;
        }

        return false;
    }

    public static boolean isOverVoid() {
        for (double posY = mc.thePlayer.posY; posY > 0.0; posY--) {
            if (!(mc.theWorld.getBlockState(
                    new BlockPos(mc.thePlayer.posX, posY, mc.thePlayer.posZ)).getBlock() instanceof BlockAir))
                return false;
        }

        return true;
    }

    public static void actualSetSpeed(double moveSpeed) {
        setSpeed(moveSpeed, mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveStrafe, mc.thePlayer.movementInput.moveForward);
    }

    public static float getBaseSpeed() {
        return 0.30F;
    }

    public static double getBaseSpeedVerus() {
        double baseSpeed = 0.24D;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amp = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1;
            baseSpeed *= 1.0D + (0.05D) * (double)amp;
        }

        return baseSpeed;
    }

    public static boolean isBlockUnderneath(BlockPos pos) {
        for (int k = 0; k < pos.getY() + 1; k++) {
            if (mc.theWorld.getBlockState(new BlockPos(pos.getX(), k, pos.getZ())).getBlock().getMaterial() != Material.air) {
                return true;
            }
        }
        return false;
    }

    public static void move(final float speed) {
        if(!isMoving())
            return;

        final double yaw = getDirection();
        mc.thePlayer.motionX += -Math.sin(yaw) * speed;
        mc.thePlayer.motionZ += Math.cos(yaw) * speed;
    }

    public static void limitSpeed(final float speed){
        final double yaw = getDirection();
        final double maxXSpeed=-Math.sin(yaw) * speed;
        final double maxZSpeed=Math.cos(yaw) * speed;

        if(mc.thePlayer.motionX>maxZSpeed){
            mc.thePlayer.motionX=maxXSpeed;
        }
        if(mc.thePlayer.motionZ>maxZSpeed){
            mc.thePlayer.motionZ=maxZSpeed;
        }
    }

    public static void setMotionWithValues(EventMove em, double speed, float yaw, double forward, double strafe) {
        if (forward == 0.0 && strafe == 0.0) {
            if (em != null) {
                em.setX(0);
                em.setZ(0);
            } else {
                mc.thePlayer.motionX = 0.0;
                mc.thePlayer.motionZ = 0.0;
            }
        }
        else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += ((forward > 0.0) ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += ((forward > 0.0) ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            if (em != null) {
                em.setX(forward * speed * Math.cos(Math.toRadians(yaw + 90f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90f)));
                em.setZ(forward * speed * Math.sin(Math.toRadians(yaw + 90f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90f)));
            } else {
                mc.thePlayer.motionX = forward * speed * Math.cos(Math.toRadians(yaw + 90f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90f));
                mc.thePlayer.motionZ = forward * speed * Math.sin(Math.toRadians(yaw + 90f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90f));
            }
        }
    }


    public static void setSpeed(EventMotion e, double speed, float forward, float strafing, float yaw) {
        boolean reversed = forward < 0.0f;
        float strafingYaw = 90.0f *
                (forward > 0.0f ? 0.5f : reversed ? -0.5f : 1.0f);

        if (reversed)
            yaw += 180.0f;
        if (strafing > 0.0f)
            yaw -= strafingYaw;
        else if (strafing < 0.0f)
            yaw += strafingYaw;

        double x = Math.cos(Math.toRadians(yaw + 90.0f));
        double z = Math.cos(Math.toRadians(yaw));

        e.setX(x * speed);
        e.setZ(z * speed);
    }


    public static void setSpeed(final EventMove moveEvent, final double moveSpeed) {
        setSpeed(moveEvent, moveSpeed, mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveStrafe, mc.thePlayer.movementInput.moveForward);
    }


    public static float getMovementDirection() {
        final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        final float forward = player.moveForward;
        final float strafe = player.moveStrafing;
        float direction = 0.0f;
        if (forward < 0.0f) {
            direction += 180.0f;
            if (strafe > 0.0f) {
                direction += 45.0f;
            }
            else if (strafe < 0.0f) {
                direction -= 45.0f;
            }
        }
        else if (forward > 0.0f) {
            if (strafe > 0.0f) {
                direction -= 45.0f;
            }
            else if (strafe < 0.0f) {
                direction += 45.0f;
            }
        }
        else if (strafe > 0.0f) {
            direction -= 90.0f;
        }
        else if (strafe < 0.0f) {
            direction += 90.0f;
        }
        direction += player.rotationYaw;
        return MathHelper.wrapAngleTo180_float(direction);
    }

    public static boolean isOnGround() {
//        List<AxisAlignedBB> collidingList = Wrapper.getWorld().getCollidingBoundingBoxes(Wrapper.getPlayer(), Wrapper.getPlayer().getEntityBoundingBox().offset(0.0, -(0.01 - MIN_DIF), 0.0));
//        return collidingList.size() > 0;
        return mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically;
    }

    public static boolean isMovingOnGround(){
        return isMoving() && mc.thePlayer.onGround;
    }

    public static double getJumpHeight(double baseJumpHeight) {
        if (isInLiquid()) {
            return WALK_SPEED * SWIM_MOD + 0.02F;
        } else if (mc.thePlayer.isPotionActive(Potion.jump)) {
            return baseJumpHeight + (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1.0F) * 0.1F;
        }
        return baseJumpHeight;
    }

    public static double fallPacket() {
        double i;
        for (i = mc.thePlayer.posY; i > getGroundLevel(); i -= 8.0) {
            if (i < getGroundLevel()) {
                i = getGroundLevel();
            }
            mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(
                    mc.thePlayer.posX, i, mc.thePlayer.posZ, true));
        }
        return i;
    }

    public static void ascendPacket() {
        for (double i = getGroundLevel(); i < mc.thePlayer.posY; i += 8.0) {
            if (i > mc.thePlayer.posY) {
                i = mc.thePlayer.posY;
            }
            mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(
                    mc.thePlayer.posX, i, mc.thePlayer.posZ, true));
        }
    }

    public static double getGroundLevel() {
        for (int i = (int) Math.round(mc.thePlayer.posY); i > 0; --i) {
            final AxisAlignedBB box = mc.thePlayer.boundingBox.addCoord(0.0, 0.0, 0.0);
            box.minY = i - 1;
            box.maxY = i;
            if (!isColliding(box) || !(box.minY <= mc.thePlayer.posY)) {
                continue;
            }
            return i;
        }
        return 0.0;
    }

    public static boolean isColliding(final AxisAlignedBB box) {
        return mc.theWorld.checkBlockCollision(box);
    }

    public static boolean isInLiquid() {
        return mc.thePlayer.isInWater() || mc.thePlayer.isInLava();
    }

    public static void sendPosition(double x, double y, double z, boolean ground, boolean moving) {
        if (!moving) {
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + y, mc.thePlayer.posZ, ground));
        } else {
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z, ground));
        }
    }

    public static double calculateFriction(double moveSpeed, double lastDist, double baseMoveSpeedRef) {
        frictionValues.clear();
        frictionValues.add(lastDist - (lastDist / BUNNY_DIV_FRICTION));
        frictionValues.add(lastDist - ((moveSpeed - lastDist) / 33.3));
        double materialFriction =
                mc.thePlayer.isInWater() ?
                        WATER_FRICTION :
                        mc.thePlayer.isInLava() ?
                                LAVA_FRICTION :
                                AIR_FRICTION;
        frictionValues.add(lastDist - (baseMoveSpeedRef * (1.0 - materialFriction)));
        return Collections.min(frictionValues);
    }

    public static void setStrafeSpeed(final EventMove e, double speed) {
        final EntityPlayerSP player = mc.thePlayer;
        if (TargetStrafe.enabled && (!TargetStrafe.onSpace.isEnabled() || Keyboard.isKeyDown(57))) {
            if (KillAura.target != null) {
                float dist = mc.thePlayer.getDistanceToEntity(KillAura.target);
                double radius = TargetStrafe.range.getValue();

                if(TargetStrafe.behind.isEnabled()) {
                    TargetStrafe.setSpeed(e, speed);
                } else {
                    setTargetStrafeSpeed(e, speed, 1, dist <= radius + 1.0D ? TargetStrafe.direction : 0, RotationUtils.getYawToEntity(KillAura.target, false));
                }

                return;
            }
        }

        setTargetStrafeSpeed(e, speed, player.moveForward, player.moveStrafing, player.rotationYaw);
    }

    public static void setTargetStrafeSpeed(final EventMove e, final double speed, float forward, float strafing, float yaw) {
        if ((forward == 0.0D) && (strafing == 0.0D)) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
        } else {
            if (forward != 0.0D) {
                if (strafing > 0.0D) {
                    yaw += (forward > 0.0D ? -45 : 45);
                } else if (strafing < 0.0D) {
                    yaw += (forward > 0.0D ? 45 : -45);
                }
                strafing = 0.0F;
                if (forward > 0.0D) {
                    forward = 1;
                } else if (forward < 0.0D) {
                    forward = -1;
                }
            }

            e.setX(mc.thePlayer.motionX = forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafing * speed * Math.sin(Math.toRadians(yaw + 90.0F)));
            e.setZ(mc.thePlayer.motionZ = forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafing * speed * Math.cos(Math.toRadians(yaw + 90.0F)));
        }
    }

    public static void setSpeed(final EventMove moveEvent, final double moveSpeed, final float pseudoYaw, final double pseudoStrafe, final double pseudoForward) {
        double forward = pseudoForward;
        double strafe = pseudoStrafe;
        float yaw = pseudoYaw;

        if (forward != 0.0) {
            if (strafe > 0.0) {
                yaw += ((forward > 0.0) ? -45 : 45);
            } else if (strafe < 0.0) {
                yaw += ((forward > 0.0) ? 45 : -45);
            }
            strafe = 0.0F;
            if (forward > 0.0) {
                forward = 1F;
            } else if (forward < 0.0) {
                forward = -1F;
            }
        }

        if (strafe > 0.0) {
            strafe = 1F;
        } else if (strafe < 0.0) {
            strafe = -1F;
        }
        double mx = Math.cos(Math.toRadians((yaw + 90.0F)));
        double mz = Math.sin(Math.toRadians((yaw + 90.0F)));
        moveEvent.x = (forward * moveSpeed * mx + strafe * moveSpeed * mz);
        moveEvent.z = (forward * moveSpeed * mz - strafe * moveSpeed * mx);

    }



    public static boolean isMoving() {
        return mc.thePlayer.movementInput.moveForward != 0.0F || mc.thePlayer.movementInput.moveStrafe != 0.0F;
    }


    public static boolean isOnGround(double height) {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, -height, 0.0D)).isEmpty();
    }


    public static int getJumpBoostModifier() {
        PotionEffect effect = mc.thePlayer.getActivePotionEffect(Potion.jump.id);
        if (effect != null)
            return effect.getAmplifier() + 1;
        return 0;
    }

    public static double getJumpBoostModifier(double baseJumpHeight, boolean potionJumpHeight) {
        if (MovementUtils.mc.thePlayer.isPotionActive(Potion.jump) && potionJumpHeight) {
            int amplifier = MovementUtils.mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier();
            baseJumpHeight += (double)((float)(amplifier + 1) * 0.1f);
        }
        return baseJumpHeight;
    }

    public static double getJumpBoostModifier(double baseJumpHeight) {
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier();
            baseJumpHeight += (double) ((float) (amplifier + 1) * 0.1F);
        }

        return baseJumpHeight;
    }

    public static void setSpeed(double moveSpeed, float yaw, double strafe, double forward) {

        double fforward = forward;
        double sstrafe = strafe;
        float yyaw = yaw;
        if (forward != 0.0D) {
            if (strafe > 0.0D) {
                yaw += ((forward > 0.0D) ? -45 : 45);
            } else if (strafe < 0.0D) {
                yaw += ((forward > 0.0D) ? 45 : -45);
            }
            strafe = 0.0D;
            if (forward > 0.0D) {
                forward = 1.0D;
            } else if (forward < 0.0D) {
                forward = -1.0D;
            }
        }
        if (strafe > 0.0D) {
            strafe = 1.0D;
        } else if (strafe < 0.0D) {
            strafe = -1.0D;
        }
        double mx = Math.cos(Math.toRadians((yaw + 90.0F)));
        double mz = Math.sin(Math.toRadians((yaw + 90.0F)));
        mc.thePlayer.motionX = forward * moveSpeed * mx + strafe * moveSpeed * mz;
        mc.thePlayer.motionZ = forward * moveSpeed * mz - strafe * moveSpeed * mx;


    }

    public static float getSpeed() {
        return (float) Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
    }

    public static void setSpeed(double moveSpeed) {
        setSpeed(moveSpeed, mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveStrafe, mc.thePlayer.movementInput.moveForward);
    }

    public double getTickDist() {
        double xDist = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;
        return Math.sqrt(Math.pow(xDist, 2) + Math.pow(zDist, 2));
    }

    public static void strafe() {
        strafe(getSpeed());
    }

    public static void strafe(final float speed) {
        if(!isMoving())
            return;

        final double yaw = getDirection();
        mc.thePlayer.motionX = -Math.sin(yaw) * speed;
        mc.thePlayer.motionZ = Math.cos(yaw) * speed;
    }

    public static double getDirection() {
        final EntityPlayerSP thePlayer = MovementUtils.mc.thePlayer;
        Float rotationYaw = thePlayer.rotationYaw;
        if (thePlayer.moveForward < 0.0f) {
            rotationYaw += 180.0f;
        }
        Float forward = 1.0f;
        if (thePlayer.moveForward < 0.0f) {
            forward = -0.5f;
        }
        else if (thePlayer.moveForward > 0.0f) {
            forward = 0.5f;
        }
        if (thePlayer.moveStrafing > 0.0f) {
            rotationYaw -= 90.0f * forward;
        }
        if (thePlayer.moveStrafing < 0.0f) {
            rotationYaw += 90.0f * forward;
        }
        return Math.toRadians(rotationYaw);
    }


    public static void setMotion(EventMove event, double speed) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        float yaw = mc.thePlayer.rotationYaw;
        if ((forward == 0.0D) && (strafe == 0.0D)) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
        } else {
            if (forward != 0.0D) {
                if (strafe > 0.0D) {
                    yaw += (forward > 0.0D ? -45 : 45);
                } else if (strafe < 0.0D) {
                    yaw += (forward > 0.0D ? 45 : -45);
                }
                strafe = 0.0D;
                if (forward > 0.0D) {
                    forward = 1;
                } else if (forward < 0.0D) {
                    forward = -1;
                }
            }

            event.setX(mc.thePlayer.motionX = forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F)));
            event.setZ(mc.thePlayer.motionZ = forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F)));
        }
    }

    public static void setMotion(double speed) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        float yaw = mc.thePlayer.rotationYaw;
        if ((forward == 0.0D) && (strafe == 0.0D)) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
        } else {
            if (forward != 0.0D) {
                if (strafe > 0.0D) {
                    yaw += (forward > 0.0D ? -45 : 45);
                } else if (strafe < 0.0D) {
                    yaw += (forward > 0.0D ? 45 : -45);
                }
                strafe = 0.0D;
                if (forward > 0.0D) {
                    forward = 1;
                } else if (forward < 0.0D) {
                    forward = -1;
                }
            }
            mc.thePlayer.motionX = forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F));
            mc.thePlayer.motionZ = forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F));
        }
    }

    public static boolean canStep(double height) {

        if (!mc.thePlayer.isCollidedHorizontally || !isOnGround(0.001))
            return false;

        if ((!mc.theWorld
                .getCollidingBoundingBoxes(mc.thePlayer,
                        mc.thePlayer.getEntityBoundingBox().expand(0.1, 0, 0).offset(0.0D, height - 0.1, 0.0D))
                .isEmpty()
                && mc.theWorld
                .getCollidingBoundingBoxes(mc.thePlayer,
                        mc.thePlayer.getEntityBoundingBox().expand(0.1, 0, 0).offset(0.0D, height + 0.1, 0.0D))
                .isEmpty())
                || (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().expand(-0.1, 0, 0).offset(0.0D, height - 0.1, 0.0D))
                .isEmpty()
                && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().expand(-0.1, 0, 0).offset(0.0D, height + 0.1,
                        0.0D))
                .isEmpty())
                || (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().expand(0, 0, 0.1).offset(0.0D, height - 0.1, 0.0D))
                .isEmpty()
                && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().expand(0, 0, 0.1).offset(0.0D, height + 0.1, 0.0D))
                .isEmpty())
                || (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().expand(0, 0, -0.1).offset(0.0D, height - 0.1, 0.0D))
                .isEmpty()
                && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox()
                .expand(0, 0, -0.1).offset(0.0D, height + 0.1, 0.0D)).isEmpty())) {
            return true;
        } else {
            return false;
        }
    }
}
