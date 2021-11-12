package vip.Resolute.util.player;

import vip.Resolute.events.impl.EventStrafe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;


public class RotationUtils {
    public static float[] getRotationFromPosition(double var0, double var2, double var4) {
        double var6 = var0 - Minecraft.getMinecraft().thePlayer.posX;
        double var8 = var4 - Minecraft.getMinecraft().thePlayer.posZ;
        double var10 = var2 - Minecraft.getMinecraft().thePlayer.posY - 1.2D;
        double var12 = (double) MathHelper.sqrt_double(var6 * var6 + var8 * var8);
        float var14 = (float)(Math.atan2(var8, var6) * 180.0D / 3.141592653589793D) - 90.0F;
        float var15 = (float)(-(Math.atan2(var10, var12) * 180.0D / 3.141592653589793D));
        return new float[]{var14, var15};
    }

    public static float[] faceBlock(BlockPos pos, boolean scaffoldFix, float currentYaw, float currentPitch, float speed) {
        double x = (pos.getX() + (scaffoldFix ? 0.5 : 0.0)) - Minecraft.getMinecraft().thePlayer.posX;
        double y = (pos.getY() - (scaffoldFix ? 1.75 : 0.0F)) - (Minecraft.getMinecraft().thePlayer.posY + Minecraft.getMinecraft().thePlayer.getEyeHeight());
        double z = (pos.getZ() + (scaffoldFix ? 0.5 : 0.0)) - Minecraft.getMinecraft().thePlayer.posZ;

        double calculate = MathHelper.sqrt_double(x * x + z * z);
        float calcYaw = (float) (MathHelper.func_181159_b(z, x) * 180.0D / Math.PI) - 90.0F;
        float calcPitch = (float) -(MathHelper.func_181159_b(y, calculate) * 180.0D / Math.PI);
        float finalPitch = calcPitch >= 90 ? 90 : calcPitch;
        float yaw = updateRotation(currentYaw, calcYaw, speed);
        float pitch = updateRotation(currentPitch, finalPitch, speed);

        float sense = Minecraft.getMinecraft().gameSettings.mouseSensitivity * 0.8F + 0.2F;
        float fix = (float) (Math.pow(sense, 3) * 1.5F);
        yaw -= yaw % fix;
        pitch -= pitch % fix;

        return new float[]{yaw, pitch};
    }

    public static void applyStrafeToPlayer(EventStrafe event)  {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        float dif = ((MathHelper.wrapAngleTo180_float(player.rotationYaw - event.yaw
                - 23.5f - 135)
                + 180) / 45);

        float yaw = event.yaw;

        float strafe = event.strafe;
        float forward = event.forward;
        float friction = event.friction;

        float calcForward = 0f;
        float calcStrafe = 0f;

        switch ((int) dif) {
            case 0: {
                calcForward = forward;
                calcStrafe = strafe;
            }
            case 1: {
                calcForward += forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe += strafe;
            }
            case 2: {
                calcForward = strafe;
                calcStrafe = -forward;
            }
            case 3: {
                calcForward -= forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe -= strafe;
            }
            case 4: {
                calcForward = -forward;
                calcStrafe = -strafe;
            }
            case 5: {
                calcForward -= forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe -= strafe;
            }
            case 6: {
                calcForward = -strafe;
                calcStrafe = forward;
            }
            case 7: {
                calcForward += forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe += strafe;
            }
        }

        if (calcForward > 1f || calcForward < 0.9f && calcForward > 0.3f || calcForward < -1f || calcForward > -0.9f && calcForward < -0.3f) {
            calcForward *= 0.5f;
        }

        if (calcStrafe > 1f || calcStrafe < 0.9f && calcStrafe > 0.3f || calcStrafe < -1f || calcStrafe > -0.9f && calcStrafe < -0.3f) {
            calcStrafe *= 0.5f;
        }

        float d = calcStrafe * calcStrafe + calcForward * calcForward;

        if (d >= 1.0E-4f) {
            d = MathHelper.sqrt_float(d);
            if (d < 1.0f) d = 1.0f;
            d = friction / d;
            calcStrafe *= d;
            calcForward *= d;
            float yawSin = MathHelper.sin((float) (yaw * Math.PI / 180f));
            float yawCos = MathHelper.cos((float) (yaw * Math.PI / 180f));
            player.motionX += calcStrafe * yawCos - calcForward * yawSin;
            player.motionZ += calcForward * yawCos + calcStrafe * yawSin;
        }
    }

    public static float getYawToEntity(Entity entity, boolean useOldPos) {
        final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        double xDist = (useOldPos ? entity.prevPosX : entity.posX) -
                (useOldPos ? player.prevPosX : player.posX);
        double zDist = (useOldPos ? entity.prevPosZ : entity.posZ) -
                (useOldPos ? player.prevPosZ : player.posZ);
        float rotationYaw = useOldPos ? Minecraft.getMinecraft().thePlayer.prevRotationYaw : Minecraft.getMinecraft().thePlayer.rotationYaw;
        float var1 = (float) (Math.atan2(zDist, xDist) * 180.0D / Math.PI) - 90.0F;
        return rotationYaw + MathHelper.wrapAngleTo180_float(var1 - rotationYaw);
    }

    public static float getOldYaw(final Entity entity) {
        final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        return getYawBetween(player.prevRotationYaw, player.prevPosX, player.prevPosZ, entity.prevPosX, entity.prevPosZ);
    }

    public static float getYawToEntity(final Entity entity) {
        final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        return getYawBetween(player.rotationYaw, player.posX, player.posZ, entity.posX, entity.posZ);
    }

    public static float getYawBetween(final float yaw, final double srcX, final double srcZ, final double destX, final double destZ) {
        final double xDist = destX - srcX;
        final double zDist = destZ - srcZ;
        final float var1 = (float)(StrictMath.atan2(zDist, xDist) * 180.0 / 3.141592653589793) - 90.0f;
        return yaw + MathHelper.wrapAngleTo180_float(var1 - yaw);
    }

    public static MovingObjectPosition rayCast(final EntityPlayerSP player, final double x, final double y, final double z) {
        final HashSet<Entity> excluded = new HashSet<>();
        excluded.add(player);
        return tracePathD(player.worldObj, player.posX, player.posY + player.getEyeHeight(), player.posZ, x, y, z, 1.0f, excluded);
    }

    private static MovingObjectPosition tracePathD(final World w, final double posX, final double posY, final double posZ, final double v, final double v1, final double v2, final float borderSize, final HashSet<Entity> exclude) {
        return tracePath(w, (float) posX, (float) posY, (float) posZ, (float) v, (float) v1, (float) v2, borderSize, exclude);
    }

    private static MovingObjectPosition tracePath(final World world, final float x, final float y, final float z, final float tx, final float ty, final float tz, final float borderSize, final HashSet<Entity> excluded) {
        Vec3 startVec = new Vec3(x, y, z);
        Vec3 endVec = new Vec3(tx, ty, tz);
        final float minX = (x < tx) ? x : tx;
        final float minY = (y < ty) ? y : ty;
        final float minZ = (z < tz) ? z : tz;
        final float maxX = (x > tx) ? x : tx;
        final float maxY = (y > ty) ? y : ty;
        final float maxZ = (z > tz) ? z : tz;
        final AxisAlignedBB bb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).expand(borderSize, borderSize, borderSize);
        final ArrayList<Entity> allEntities = (ArrayList<Entity>) world.getEntitiesWithinAABBExcludingEntity(null, bb);
        MovingObjectPosition blockHit = world.rayTraceBlocks(startVec, endVec);
        startVec = new Vec3(x, y, z);
        endVec = new Vec3(tx, ty, tz);
        Entity closestHitEntity = null;
        float closestHit = Float.POSITIVE_INFINITY;
        float currentHit;
        for (final Entity ent : allEntities) {
            if (ent.canBeCollidedWith() && !excluded.contains(ent)) {
                final float entBorder = ent.getCollisionBorderSize();
                AxisAlignedBB entityBb = ent.getEntityBoundingBox();
                if (entityBb == null) {
                    continue;
                }
                entityBb = entityBb.expand(entBorder, entBorder, entBorder);
                final MovingObjectPosition intercept = entityBb.calculateIntercept(startVec, endVec);
                if (intercept == null) {
                    continue;
                }
                currentHit = (float) intercept.hitVec.distanceTo(startVec);
                if (currentHit >= closestHit && currentHit != 0.0f) {
                    continue;
                }
                closestHit = currentHit;
                closestHitEntity = ent;
            }
        }
        if (closestHitEntity != null) {
            blockHit = new MovingObjectPosition(closestHitEntity);
        }
        return blockHit;
    }

    public static float getDistanceBetweenAngles(float angle1, float angle2) {
        float angle = Math.abs(angle1 - angle2) % 360.0f;
        if (angle > 180.0f) {
            angle = 360.0f - angle;
        }
        return angle;
    }

    public static boolean isValid(EntityLivingBase entity, boolean players, boolean monsters, boolean animals, boolean teams, boolean invisibles, boolean passives, double range) {
        if (entity instanceof EntityPlayer && !players) {
            return false;
        }
        if (entity instanceof EntityMob && !monsters) {
            return false;
        }
        if (entity instanceof EntityVillager || entity instanceof EntityGolem && !passives) {
            return false;
        }
        if (entity instanceof EntityAnimal && !animals) {
            return false;
        }
        if (entity == Minecraft.getMinecraft().thePlayer || entity.isDead || entity.getHealth() == 0 || Minecraft.getMinecraft().thePlayer.getDistanceToEntity(entity) > range) {
            return false;
        }
        if (entity.isInvisible() && !invisibles) {
            return false;
        }
        if (isOnSameTeam(entity) && teams) {
            return false;
        }
        if (entity instanceof EntityBat) {
            return false;
        }
        return !(entity instanceof EntityArmorStand);
    }

    public static boolean isOnSameTeam(EntityLivingBase entity) {
        if (entity.getTeam() != null && Minecraft.getMinecraft().thePlayer.getTeam() != null) {
            char c1 = entity.getDisplayName().getFormattedText().charAt(1);
            char c2 = Minecraft.getMinecraft().thePlayer.getDisplayName().getFormattedText().charAt(1);
            return c1 == c2;
        } else {
            return false;
        }
    }

    public static float[] getRotations(EntityLivingBase entityIn, float speed) {
        float yaw = updateRotation(Minecraft.getMinecraft().thePlayer.rotationYaw,
                getNeededRotations(entityIn)[0],
                speed);
        float pitch = updateRotation(Minecraft.getMinecraft().thePlayer.rotationPitch,
                getNeededRotations(entityIn)[1],
                speed);
        return new float[]{yaw, pitch};
    }


    public static float getDistanceToEntity(EntityLivingBase entityLivingBase) {
        return Minecraft.getMinecraft().thePlayer.getDistanceToEntity(entityLivingBase);
    }

    public static float getAngleChange(EntityLivingBase entityIn) {
        float yaw = getNeededRotations(entityIn)[0];
        float pitch = getNeededRotations(entityIn)[1];
        float playerYaw = Minecraft.getMinecraft().thePlayer.rotationYaw;
        float playerPitch = Minecraft.getMinecraft().thePlayer.rotationPitch;
        if (playerYaw < 0)
            playerYaw += 360;
        if (playerPitch < 0)
            playerPitch += 360;
        if (yaw < 0)
            yaw += 360;
        if (pitch < 0)
            pitch += 360;
        float yawChange = Math.max(playerYaw, yaw) - Math.min(playerYaw, yaw);
        float pitchChange = Math.max(playerPitch, pitch) - Math.min(playerPitch, pitch);
        return yawChange + pitchChange;
    }

    public static float[] getNeededRotations(EntityLivingBase entityIn) {
        double d0 = entityIn.posX - Minecraft.getMinecraft().thePlayer.posX;
        double d1 = entityIn.posZ - Minecraft.getMinecraft().thePlayer.posZ;
        double d2 = entityIn.posY + entityIn.getEyeHeight() - (Minecraft.getMinecraft().thePlayer.getEntityBoundingBox().minY + Minecraft.getMinecraft().thePlayer.getEyeHeight());

        double d3 = MathHelper.sqrt_double(d0 * d0 + d1 * d1);
        float f = (float) (MathHelper.func_181159_b(d1, d0) * 180.0D / Math.PI) - 90.0F;
        float f1 = (float) (-(MathHelper.func_181159_b(d2, d3) * 180.0D / Math.PI));
        return new float[]{f, f1};
    }

    public static net.minecraft.util.Vec3 getVectorForRotation(final float[] rotation) {
        float yawCos = MathHelper.cos(-rotation[0] * 0.017453292F - (float) Math.PI);
        float yawSin = MathHelper.sin(-rotation[0] * 0.017453292F - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-rotation[1] * 0.017453292F);
        float pitchSin = MathHelper.sin(-rotation[1] * 0.017453292F);
        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    public static Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
        float f2 = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
        float f3 = -MathHelper.cos(-pitch * 0.017453292F);
        float f4 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3((double)(f2 * f3), (double)f4, (double)(f * f3));
    }


    public static float[] getFaceDirectionToBlockPos(BlockPos pos, float currentYaw, float currentPitch) {
        double x = (pos.getX() + 0.5F) - Minecraft.getMinecraft().thePlayer.posX;
        double y = (pos.getY()) - (Minecraft.getMinecraft().thePlayer.posY + Minecraft.getMinecraft().thePlayer.getEyeHeight());
        double z = (pos.getZ() + 0.5F) - Minecraft.getMinecraft().thePlayer.posZ;

        double calculate = MathHelper.sqrt_double(x * x + z * z);
        float calcYaw = (float) (MathHelper.func_181159_b(z, x) * 180.0D / Math.PI) - 90.0F;
        float calcPitch = (float) -(MathHelper.func_181159_b(y, calculate) * 180.0D / Math.PI);

        float finalPitch = calcPitch >= 90 ? 90 : calcPitch;

        float yaw = updateRotation(currentYaw, calcYaw, 360);
        float pitch = updateRotation(currentPitch, finalPitch, 360);

        return new float[]{yaw, pitch};
    }

    public static float updateRotation(float current, float intended, float speed) {
        float f = MathHelper.wrapAngleTo180_float(intended - current);
        if (f > speed)
            f = speed;
        if (f < -speed)
            f = -speed;
        return current + f;
    }


    public static float[] getRotationsToEntity(Entity entity) {
        final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        double xDist = entity.posX - player.posX;
        double zDist = entity.posZ - player.posZ;

        double entEyeHeight = entity.getEyeHeight();
        double yDist = ((entity.posY + entEyeHeight) - Math.min(Math.max(entity.posY - player.posY, 0), entEyeHeight)) - (player.posY + player.getEyeHeight());
        double fDist = MathHelper.sqrt_double(xDist * xDist + zDist * zDist);
        float rotationYaw = Minecraft.getMinecraft().thePlayer.rotationYaw;
        float var1 = (float) (Math.atan2(zDist, xDist) * 180.0D / Math.PI) - 90.0F;

        float yaw = rotationYaw + MathHelper.wrapAngleTo180_float(var1 - rotationYaw);
        float rotationPitch = Minecraft.getMinecraft().thePlayer.rotationPitch;

        float var2 = (float) (-(Math.atan2(yDist, fDist) * 180.0D / Math.PI));
        float pitch = rotationPitch + MathHelper.wrapAngleTo180_float(var2 - rotationPitch);

        return new float[]{yaw, MathHelper.clamp_float(pitch, -90.0f, 90.0f)};
    }

    public static float[] getRotations(Entity entity) {
        double diffY;
        if (entity == null) {
            return null;
        }
        double diffX = entity.posX - Minecraft.getMinecraft().thePlayer.posX;
        double diffZ = entity.posZ - Minecraft.getMinecraft().thePlayer.posZ;
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase elb = (EntityLivingBase) entity;
            diffY = elb.posY + ((double) elb.getEyeHeight() - 0.4)
                    - (Minecraft.getMinecraft().thePlayer.posY + (double)Minecraft.getMinecraft().thePlayer.getEyeHeight());
        } else {
            diffY = (entity.boundingBox.minY + entity.boundingBox.maxY) / 2.0
                    - (Minecraft.getMinecraft().thePlayer.posY + (double) Minecraft.getMinecraft().thePlayer.getEyeHeight());
        }
        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-Math.atan2(diffY, dist) * 180.0 / Math.PI);
        return new float[]{yaw, pitch};
    }

    public static float[] getRotations2(EntityLivingBase ent) {
        double x = ent.posX;
        double z = ent.posZ;
        double y = ent.posY + ent.getEyeHeight() / 2.0F;
        return getRotationFromPosition2(x, z, y);
    }
    public static float[] getRotationFromPosition2(double x, double z, double y) {
        double xDiff = x - Minecraft.getMinecraft().thePlayer.posX;
        double zDiff = z - Minecraft.getMinecraft().thePlayer.posZ;
        double yDiff = y - Minecraft.getMinecraft().thePlayer.posY - 1.2;

        double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) (Math.atan2(zDiff, xDiff) * 180.0D / 3.141592653589793D) - 90.0F;
        float pitch = (float) -(Math.atan2(yDiff, dist) * 180.0D / 3.141592653589793D);
        return new float[]{yaw, pitch};
    }
}
