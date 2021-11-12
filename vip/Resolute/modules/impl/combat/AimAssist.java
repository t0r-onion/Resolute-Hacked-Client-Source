package vip.Resolute.modules.impl.combat;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.ui.click.skeet.SkeetUI;
import vip.Resolute.util.player.RotationUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MathHelper;

import java.util.Random;

public class AimAssist extends Module {

    public ModeSetting mode = new ModeSetting("Mode", "Rotate", "Sensitivity", "Rotate");

    private NumberSetting range = new NumberSetting("Range", 4, this::isModeSelected, 1, 8, 0.1);
    private NumberSetting fov = new NumberSetting("FOV",250, this::isModeSelected,1, 360, 1);
    private NumberSetting aimspeed = new NumberSetting("Aim Speed",5, this::isModeSelected,0.1, 10, 0.1);
    private NumberSetting horizontal = new NumberSetting("Horizontal",9, this::isModeSelected,0.1, 10, 0.1);
    private NumberSetting vertical = new NumberSetting("Vertical",9,this::isModeSelected, 0.1, 10, 0.1);
    private NumberSetting ticksExisted = new NumberSetting("Ticks Existed", 100, this::isModeSelected,0, 1000, 10);

    private NumberSetting horizontalSpeed = new NumberSetting("Horizontal Speed",80, this::isModeSelected, 1, 100, 1);
    private NumberSetting verticalSpeed = new NumberSetting("Vertical Speed",80, this::isModeSelected,1, 100, 1);

    private BooleanSetting onSword = new BooleanSetting("On Sword",true, this::isModeSelected);

    protected Random rand = new Random();

    public static EntityLivingBase target;

    float oldSens;

    public boolean isModeSelected() {
        return this.mode.is("Rotate");
    }

    public AimAssist() {
        super("AimAssist", 0, "Rotates the player to assist with aim", Category.COMBAT);
        this.addSettings(mode, range, fov, aimspeed, horizontal, vertical, ticksExisted, onSword);
        this.oldSens = this.mc.gameSettings.mouseSensitivity;
    }

    public void onEnable() {
        this.oldSens = this.mc.gameSettings.mouseSensitivity;
    }

    public void onEvent(Event e) {
        this.setSuffix(String.valueOf(range.getValue()));
        if(e instanceof EventMotion && e.isPre()) {
            if(mode.is("Sensitivity")) {
                this.mc.gameSettings.mouseSensitivity = this.mc.objectMouseOver.entityHit != null ? 0.1f : this.oldSens;
            }

            if(mode.is("Rotate")) {
                if(onSword.isEnabled() && !isHoldingSword())
                    return;

                target = getClosest(range.getValue());

                if(target != null) {
                    double horizontalSpeed =  horizontal.getValue() * 3.0 + (horizontal.getValue() > 0.0 ? this.rand.nextDouble() : 0.0);
                    double verticalSpeed = vertical.getValue() * 3.0 + (vertical.getValue() > 0.0 ? this.rand.nextDouble() : 0.0);
                    horizontalSpeed *= aimspeed.getValue();
                    verticalSpeed *= aimspeed.getValue();

                    faceTarget(target, 0.0f, (float) verticalSpeed);
                    faceTarget(target, (float) horizontalSpeed, 0.0f);
                }
            }
        }
    }

    private void faceTarget(EntityLivingBase targets, float yawspeed, float pitchspeed) {
        EntityPlayerSP player = this.mc.thePlayer;
        float yaw = getRotations(targets)[0];
        float pitch = getRotations(targets)[1];
        player.rotationYaw = this.getRotation(player.rotationYaw, yaw, yawspeed);
        player.rotationPitch = this.getRotation(player.rotationPitch, pitch, pitchspeed);
    }

    public float[] getRotations(EntityLivingBase e) {
        return RotationUtils.getRotations2(e);
    }

    protected float getRotation(float currentRotation, float targetRotation, float maxIncrement) {
        float deltaAngle = MathHelper.wrapAngleTo180_float(targetRotation - currentRotation);
        if (deltaAngle > maxIncrement) {
            deltaAngle = maxIncrement;
        }
        if (deltaAngle < -maxIncrement) {
            deltaAngle = -maxIncrement;
        }
        return currentRotation + deltaAngle / 2.0f;
    }

    private boolean isHoldingSword() {
        return mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword;
    }

    private EntityLivingBase getClosest(double range) {
        double dist = range;
        EntityLivingBase target = null;
        for (Object object : this.mc.theWorld.loadedEntityList) {
            Entity entity = (Entity)object;
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase player = (EntityLivingBase)entity;
                if (canAttack(player)) {
                    double currentDist = this.mc.thePlayer.getDistanceToEntity((Entity)player);
                    if (currentDist <= dist) {
                        dist = currentDist;
                        target = player;
                    }
                }
            }
        }
        return target;
    }

    public boolean canAttack(EntityLivingBase player) {
        if (player == this.mc.thePlayer)
            return false;
        if (player instanceof EntityPlayer || player instanceof net.minecraft.entity.passive.EntityAnimal || player instanceof net.minecraft.entity.monster.EntityMob || player instanceof net.minecraft.entity.passive.EntityVillager) {
            if (player instanceof EntityPlayer && !(SkeetUI.isPlayers()))
                return false;
            if (player instanceof net.minecraft.entity.passive.EntityAnimal && !(SkeetUI.isAnimals()))
                return false;
            if (player instanceof net.minecraft.entity.monster.EntityMob && !(SkeetUI.isMobs()))
                return false;
            if(!player.isEntityAlive())
                return false;
            if(player instanceof net.minecraft.entity.passive.EntityVillager && !(SkeetUI.isVillagers()))
                return false;
        }
        if(!isInFOV(player, this.fov.getValue()))
            return false;
        if (player instanceof EntityPlayer) {
            if (isTeam((EntityPlayer)this.mc.thePlayer, (EntityPlayer)player) && (SkeetUI.isTeams()))
                return false;
        }
        if (player.isInvisible() && !(SkeetUI.isInvisibles()))
            return false;
        return player.ticksExisted > this.ticksExisted.getValue();
    }

    private boolean isInFOV(EntityLivingBase entity, double angle) {
        angle *= .5D;
        double angleDiff = getAngleDifference(mc.thePlayer.rotationYaw, getRotations(entity.posX, entity.posY, entity.posZ)[0]);
        return (angleDiff > 0 && angleDiff < angle) || (-angle < angleDiff && angleDiff < 0);
    }

    private float getAngleDifference(float dir, float yaw) {
        float f = Math.abs(yaw - dir) % 360F;
        float dist = f > 180F ? 360F - f : f;
        return dist;
    }

    private float[] getRotations(double x, double y, double z) {
        double diffX = x + .5D - mc.thePlayer.posX;
        double diffY = (y + .5D) / 2D - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double diffZ = z + .5D - mc.thePlayer.posZ;

        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float)(Math.atan2(diffZ, diffX) * 180D / Math.PI) - 90F;
        float pitch = (float)-(Math.atan2(diffY, dist) * 180D / Math.PI);

        return new float[] { yaw, pitch };
    }

    public static boolean isTeam(EntityPlayer e, EntityPlayer e2) {
        if (e2.getTeam() != null && e.getTeam() != null) {
            Character target = Character.valueOf(e2.getDisplayName().getFormattedText().charAt(1));
            Character player = Character.valueOf(e.getDisplayName().getFormattedText().charAt(1));
            if (target.equals(player))
                return true;
        } else {
            return true;
        }
        return false;
    }
}
