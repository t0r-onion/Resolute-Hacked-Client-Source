package vip.Resolute.modules.impl.movement;

import net.minecraft.util.MovementInput;
import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.*;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.ui.notification.Notification;
import vip.Resolute.ui.notification.NotificationType;
import vip.Resolute.util.misc.MathUtils;
import vip.Resolute.util.movement.MovementUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

public class Speed extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "NCP", "NCP", "NCP Low", "Strafe", "Mineplex Safe", "Mineplex", "Mineplex Fast", "Hypixel", "Verus", "Minemen", "Jartex", "Custom", "Watchdog New", "Watchdoge", "Larkus");

    public NumberSetting customHeight = new NumberSetting("Custom Height", 0.42, () -> mode.is("Custom"), 0.0, 0.42, 0.02);

    public ModeSetting motionMode = new ModeSetting("Motion", "Low", this::isMode4Selected, "Low");
    public NumberSetting watchdogSpeed = new NumberSetting("Watchdog Speed", 15.5, this::isMode4Selected, 10.0, 20.0, 0.1);


    public BooleanSetting newStrafe = new BooleanSetting("Watchdog Strafe", false, () -> mode.is("Watchdog New") || mode.is("Hypixel"));

    public ModeSetting verusMode = new ModeSetting("Verus Mode", "Float", this::isModeSelected, "Float", "Hop", "Port");
    public NumberSetting vanillaSpeed = new NumberSetting("Custom Speed", 2.0, this::isMode2Selected, 0.1, 5.0, 0.1);
    public NumberSetting mineplexRegSpeed = new NumberSetting("Mineplex Regular Speed", 4.0, this::isMode3Selected,0.1, 10.0, 0.1);
    public BooleanSetting watchdogStrafeFix = new BooleanSetting("Watchdog Strafe Fix", false);
    public BooleanSetting watchdogTimerBoost = new BooleanSetting("Watchdog Timer", false);

    public BooleanSetting dogeSpoof = new BooleanSetting("Doge Spoof", true, () -> mode.is("Doge"));

    public BooleanSetting disable = new BooleanSetting("Lagback Check", true);

    private double moveSpeed;
    public int stage;
    int ncpStage;
    public boolean reset, doSlow;
    double lastDistance = 0;
    double dist;
    private double nextMotionSpeed;
    double moveSpeed3 = 0.0D;
    float speed = 0.0f;

    public double movementSpeed;
    private int verusStage;
    double lastDist = 0;
    public boolean spoofGround;
    int watchdogStage = 1;
    double mineplexMoveSpeed;

    private boolean wasOnGround;
    public static boolean enabled = false;
    private int stopTicks;
    private float fallDist;
    int jumps = 0;

    public boolean isModeSelected() {
        return this.mode.is("Verus");
    }
    public boolean isMode2Selected() {
        return this.mode.is("Custom");
    }
    public boolean isMode3Selected() {
        return this.mode.is("Mineplex");
    }
    public boolean isMode4Selected() {
        return this.mode.is("Hypixel");
    }

    public Speed() {
        super("Speed", Keyboard.KEY_NONE, "Allows you to move faster", Category.MOVEMENT);
        this.addSettings(mode, customHeight, motionMode, watchdogSpeed, newStrafe, verusMode, vanillaSpeed, mineplexRegSpeed, dogeSpoof, watchdogStrafeFix, watchdogTimerBoost, disable);
    }

    public void onEnable() {
        super.onEnable();
        if (mc.thePlayer != null) {
            moveSpeed = MovementUtils.getSpeed();
        }
        enabled = true;
        movementSpeed = 0.0D;
        ncpStage = 0;
        this.lastDistance = 0;
        watchdogStage = 2;
        this.moveSpeed3 = 0.0D;
        doSlow = false;
        reset = false;
        this.fallDist = mc.thePlayer.fallDistance;
        nextMotionSpeed = 0.0;
        verusStage = 0;
        this.stopTicks = 0;
        mc.thePlayer.speedInAir = 0.02f;
        mc.timer.timerSpeed = 1.0f;
    }

    public void onDisable() {
        super.onDisable();
        speed = 0;
        watchdogStage = 2;
        enabled = false;
        doSlow = false;
        reset = false;
        lastDist = 0;
        mineplexMoveSpeed = 0;
        mc.thePlayer.speedInAir = 0.02f;
        this.stopTicks = 0;
        mc.timer.timerSpeed = 1.0f;
    }

    public void onEvent(Event e) {
        this.setSuffix(mode.getMode());
        if(e instanceof EventPacket) {
            if(((EventPacket) e).getPacket() instanceof S08PacketPlayerPosLook) {
                if(disable.isEnabled()) {
                    Resolute.getNotificationManager().add(new Notification("Flag alert", "Speed was automatically disabled to prevent flags", 4000L, NotificationType.WARNING));
                    enabled = false;
                    doSlow = false;
                    reset = false;
                    lastDist = 0;
                    mc.thePlayer.speedInAir = 0.02f;
                    mc.timer.timerSpeed = 1.0f;
                    this.toggled = false;
                }
            }

            if(((EventPacket) e).getPacket() instanceof C03PacketPlayer.C04PacketPlayerPosition) {
                if(watchdogStrafeFix.isEnabled()) {
                    float yaw = mc.thePlayer.rotationYaw;
                    float pitch = mc.thePlayer.rotationPitch;
                    double offsetX = 0;
                    double offsetZ = 0;
                    if(mc.thePlayer.motionX > 0) {
                        offsetX = mc.thePlayer.motionX / 2;
                        yaw = -90;
                    }
                    if(mc.thePlayer.motionX < 0) {
                        offsetX = mc.thePlayer.motionX / 2;
                        yaw = 90;
                    }
                    if(mc.thePlayer.motionZ > 0) {
                        offsetZ = mc.thePlayer.motionZ / 2;
                        yaw = 0;
                    }
                    if(mc.thePlayer.motionZ < 0) {
                        offsetZ = mc.thePlayer.motionZ / 2;
                        yaw = -170;
                    }
                    
                    ((EventPacket) e).setPacket(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX + offsetX, mc.thePlayer.posY, mc.thePlayer.posZ + offsetZ, yaw, pitch, mc.thePlayer.onGround));
                }
            }
        }

        if(e instanceof EventMove) {
            EventMove event = (EventMove) e;

            if(mode.is("Strafe")) {
                MovementUtils.setStrafeSpeed(event, Math.max(MovementUtils.getSpeed(), MovementUtils.getBaseMoveSpeed() * 0.98));
            }

            if(mode.is("Mineplex")) {
                mc.timer.timerSpeed = 0.9f;
                if (MovementUtils.isMovingOnGround()) {
                    if (mineplexMoveSpeed < 0.5) mineplexMoveSpeed = 0.8;
                    else mineplexMoveSpeed += 0.5;
                    event.setY(mc.thePlayer.motionY = 0.42F);
                    mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42F, mc.thePlayer.posZ, true));
                } else if (!MovementUtils.isMoving()) {
                    mineplexMoveSpeed = 0.45;
                }
                mineplexMoveSpeed = Math.max(mineplexMoveSpeed, 0.4);
                mineplexMoveSpeed -= mineplexMoveSpeed / 44;
                mineplexMoveSpeed = Math.min(mineplexRegSpeed.getValue(), mineplexMoveSpeed);
                if (mc.thePlayer.isCollidedHorizontally || !MovementUtils.isMoving())
                    mineplexMoveSpeed = 0.32;
                MovementUtils.setSpeed(event, mineplexMoveSpeed);
            }

            if(mode.is("Spartan")) {
                if (MovementUtils.isMovingOnGround()) {
                    event.setY(this.mc.thePlayer.motionY = MovementUtils.getJumpBoostModifier(0.41999998688697815D));
                } else if(MovementUtils.isMoving()) {
                    this.movementSpeed = 0.3;
                }

                MovementUtils.setStrafeSpeed(event, this.movementSpeed);
            }

            if(mode.is("Watchdog New")) {
                double baseMoveSpeed = MovementUtils.getBaseSpeedHypixelApplied();
                double motionY;
                if (MovementUtils.isMovingOnGround()) {
                    this.wasOnGround = true;

                    if(Scaffold.enabled || mc.gameSettings.keyBindJump.isKeyDown()) {
                        motionY = MovementUtils.getJumpHeight();
                    } else {
                        motionY = MovementUtils.getJumpBoostModifier(0.4195);
                    }

                    event.setY(mc.thePlayer.motionY = motionY);

                    this.moveSpeed = Math.max(baseMoveSpeed * 1.72, lastDist * 1.72);
                    MovementUtils.setStrafeSpeed(event, this.moveSpeed);

                    mc.timer.timerSpeed = 1.0f;
                    this.doSlow = true;
                } else if(MovementUtils.isMoving()) {
                    if(newStrafe.isEnabled()) {
                        MovementUtils.setStrafeSpeed(event, MovementUtils.getSpeed());
                    }

                    if (this.watchdogTimerBoost.isEnabled()) {
                        mc.timer.timerSpeed = 1.125f;
                    }
                }
            }

            if(mode.is("Hypixel")) {
                double baseMoveSpeed = MovementUtils.getBaseSpeedHypixelAppliedLow();
                double motionY;
                if (MovementUtils.isMovingOnGround()) {
                    this.wasOnGround = true;
                    motionY = MovementUtils.getJumpBoostModifier(0.2);
                    event.setY(mc.thePlayer.motionY = motionY);
                    this.moveSpeed = Math.max(baseMoveSpeed * (watchdogSpeed.getValue() / 10), lastDist * (watchdogSpeed.getValue() / 10));
                    MovementUtils.setStrafeSpeed(event, this.moveSpeed);
                    mc.timer.timerSpeed = 1.0f;
                } else if(wasOnGround) {
                    double difference = (0.6556 + 0.02 * MovementUtils.getJumpBoostModifier()) * (lastDist - baseMoveSpeed);
                    this.moveSpeed = lastDist - difference;
                    wasOnGround = false;
                } else if(MovementUtils.isMoving()) {
                    this.moveSpeed = MovementUtils.getFriction(this.moveSpeed);
                    if(newStrafe.isEnabled() && mc.thePlayer.fallDistance < 1.0f && MovementUtils.isDistFromGround(1)) {
                        MovementUtils.setStrafeSpeed(event, Math.max(MovementUtils.getSpeed(), moveSpeed));
                    }

                    if (this.watchdogTimerBoost.isEnabled()) {
                        if(!(this.mc.thePlayer.getHeldItem() != null && (this.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || this.mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) && this.mc.thePlayer.isEating())) {
                            mc.timer.timerSpeed = 1.115f;
                        } else {
                            mc.timer.timerSpeed = 1.0f;
                        }
                    }
                }
            }

            if(mode.is("Verus")) {
                if(verusMode.is("Float")) {
                    if (MovementUtils.isMovingOnGround()) {
                        this.movementSpeed = 0.612D;
                        event.setY(0.41999998688697815D);
                        this.spoofGround = true;
                        this.verusStage = 0;
                    } else if (this.verusStage <= 5) {
                        this.movementSpeed += 0.1D;
                        event.setY(0.0D);
                        ++this.verusStage;
                    } else {
                        this.movementSpeed = 0.24D;
                        this.spoofGround = false;
                    }

                    this.mc.thePlayer.motionY = event.getY();

                    MovementUtils.setStrafeSpeed(event, this.movementSpeed - 1.0E-4D);
                }

                if(verusMode.is("Hop")) {
                    if (MovementUtils.isMovingOnGround()) {
                        this.movementSpeed = 0.612D;
                        event.setY(this.mc.thePlayer.motionY = 0.41999998688697815D);
                    } else {
                        this.movementSpeed = 0.36D;
                    }

                    MovementUtils.setStrafeSpeed(event, this.movementSpeed - 1.0E-4D);
                }

                if(verusMode.is("Port")) {
                    if (MovementUtils.isMovingOnGround()) {


                        this.movementSpeed = 0.512D;
                        event.setY(this.mc.thePlayer.motionY = 0.41999998688697815D);
                        if (!this.mc.thePlayer.movementInput.jump) {
                            this.spoofGround = true;
                        }
                    } else if (this.spoofGround) {
                        this.movementSpeed = 0.38D;
                        event.setY(this.mc.thePlayer.motionY = 0.0D);
                        this.spoofGround = false;
                    }

                    MovementUtils.setStrafeSpeed(event, this.movementSpeed - 1.0E-4D);
                }
            }

            if(mode.is("Mineplex Safe")) {
                Entity player = mc.thePlayer;
                BlockPos pos = new BlockPos(player.posX, player.posY - 1, player.posZ);
                Block block = mc.theWorld.getBlockState(pos).getBlock();

                mc.timer.timerSpeed = 1.0f;

                if(MovementUtils.isMovingOnGround()) {
                    //mc.timer.timerSpeed = 10f;
                    event.setY(mc.thePlayer.motionY = 0.359);
                    doSlow = true;
                    dist = moveSpeed;
                    moveSpeed = 0;
                } else {
                    mc.timer.timerSpeed = 1.0f;
                    if(doSlow) {
                        moveSpeed = dist + 0.56f;
                        doSlow = false;
                    } else {
                        moveSpeed = lastDistance * (moveSpeed > 2.2 ? 0.975 : moveSpeed >= 1.5 ? 0.98 : 0.985);
                    }

                    event.setY(event.getY() - 1.0e-4);
                }

                double max = 5;

                MovementUtils.setStrafeSpeed(event, Math.max(Math.min(moveSpeed, max), doSlow ? 0 : 0.455));

            }

            if(mode.is("Custom")) {
                if(MovementUtils.isMovingOnGround()) {
                    //0.42
                    ((EventMove) e).setY(mc.thePlayer.motionY = MovementUtils.getJumpBoostModifier(customHeight.getValue()));
                }

                MovementUtils.setStrafeSpeed(event, vanillaSpeed.getValue());
            }

            if(mode.is("NCP")) {
                if(MovementUtils.isMoving()) {
                    MovementUtils.setStrafeSpeed(event, Math.max(MovementUtils.getSpeed(), MovementUtils.getBaseMoveSpeed()));
                }
            }

            if(mode.is("NCP Low")) {
                if(MovementUtils.isMoving()) {
                    MovementUtils.setStrafeSpeed(event, Math.max(MovementUtils.getSpeed(), MovementUtils.getBaseMoveSpeed()));
                }

                if(MovementUtils.isMovingOnGround()) {
                    event.setY(this.mc.thePlayer.motionY = 0.41999998688697815D);
                }
            }

            if(mode.is("Velo")) {
                if(MovementUtils.isMoving() && !MovementUtils.isInLiquid()) {
                    MovementUtils.setStrafeSpeed(event, MovementUtils.getBaseSpeedHypixelApplied());
                }
            }

            double baseMoveSpeed;
            double motionY;
            double difference;

            //i have no clue if this works or not
            if(mode.is("Watchdoge")) {
                if (MovementUtils.isMoving()) {
                    baseMoveSpeed = MovementUtils.getBaseMoveSpeed();

                    if (MathUtils.roundToPlace(mc.thePlayer.posY - (int) mc.thePlayer.posY, 3) == MathUtils.roundToPlace(0.4,
                            3)) {
                        event.setY(mc.thePlayer.motionY = 0.31);
                    } else if (MathUtils.roundToPlace(mc.thePlayer.posY - (int) mc.thePlayer.posY, 3) == MathUtils
                            .roundToPlace(0.71, 3)) {
                        event.setY(mc.thePlayer.motionY = 0.04);
                    } else if (MathUtils.roundToPlace(mc.thePlayer.posY - (int) mc.thePlayer.posY, 3) == MathUtils
                            .roundToPlace(0.75, 3)) {
                        event.setY(mc.thePlayer.motionY = -0.2);
                    } else if (MathUtils.roundToPlace(mc.thePlayer.posY - (int) mc.thePlayer.posY, 3) == MathUtils
                            .roundToPlace(0.55, 3)) {
                        event.setY(mc.thePlayer.motionY = -0.19);
                    } else if (MathUtils.roundToPlace(mc.thePlayer.posY - (int) mc.thePlayer.posY, 3) == MathUtils
                            .roundToPlace(0.4, 3)) {
                        event.setY(mc.thePlayer.motionY = -0.2);
                    }

                    if (MovementUtils.isOnGround() && !this.wasOnGround) {
                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + MathUtils.randomNumber(0.0067, 0.0042), mc.thePlayer.posZ);
                        event.setY(mc.thePlayer.motionY = MovementUtils.getJumpHeight(0.4F));
                        this.moveSpeed = baseMoveSpeed * 1.17;
                        this.wasOnGround = true;
                        if (this.watchdogTimerBoost.isEnabled()) {
                            mc.timer.timerSpeed = 1.0f;
                        }
                    } else if (this.wasOnGround) {
                        this.wasOnGround = false;
                        difference = (0.6556
                                + 0.02 * MovementUtils.getJumpBoostModifier())
                                * (lastDist - baseMoveSpeed);
                        this.moveSpeed = lastDist - difference;
                    } else {
                        this.moveSpeed = MovementUtils.getFriction(this.moveSpeed);
                        if (this.watchdogTimerBoost.isEnabled()) {
                            if(!(this.mc.thePlayer.getHeldItem() != null && (this.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || this.mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) && this.mc.thePlayer.isEating())) {
                                mc.timer.timerSpeed = 1.115f;
                            } else {
                                mc.timer.timerSpeed = 1.0f;
                            }
                        }
                    }

                    MovementUtils.setStrafeSpeed(event, Math.max(MovementUtils.getSpeed(), this.moveSpeed));
                }
            }
        }

        if(e instanceof EventMotion) {
            if (e.isPre()) {
                EventMotion event = (EventMotion) e;

                if(mode.is("Velo")) {
                    if(MovementUtils.isMoving() && !MovementUtils.isInLiquid()) {
                        if(MovementUtils.isOnGround()) {
                            mc.thePlayer.addVelocity(0, 0.39, 0);
                        }
                    }
                }

                if(mode.is("Jartex")) {
                    if (mc.thePlayer.isInWater()) return;
                    if (MovementUtils.isMoving()) {
                        if (mc.thePlayer.onGround) {
                            mc.thePlayer.jump();
                            mc.thePlayer.speedInAir = 0.02028f;
                            mc.timer.timerSpeed = 1.01f;
                        }
                    } else {
                        mc.timer.timerSpeed = 1f;
                    }
                }

                if(mode.is("Strafe")) {
                    if (MovementUtils.isMovingOnGround())
                        mc.gameSettings.keyBindJump.pressed = false;
                }

                if (mode.is("Watchdog New")) {
                    if (mc.thePlayer.isInWater() || mc.thePlayer.isInLava())
                        return;

                    if(newStrafe.isEnabled()) {
                        MovementUtils.bypassOffSet(event);
                    }

                    if (MovementUtils.isMovingOnGround())
                        mc.gameSettings.keyBindJump.pressed = false;

                    if (event.isOnGround()) {
                        event.setY(event.getY() + 0.015625);
                    }

                    if (mc.thePlayer.fallDistance < 1.0f) {
                       mc.thePlayer.motionY += 0.005;
                    }

                    if (MovementUtils.isMovingOnGround()) {
                        mc.gameSettings.keyBindJump.pressed = false;
                    }

                    if (!(this.mc.theWorld.getBlockState(new BlockPos(this.mc.thePlayer.posX, this.mc.thePlayer.posY - 1.0D, this.mc.thePlayer.posZ)).getBlock() instanceof BlockSlab) && this.mc.thePlayer.motionY < 0.1D && this.mc.thePlayer.motionY > -0.25D && (double) this.mc.thePlayer.fallDistance < 0.1) {
                        mc.thePlayer.motionY -= 0.15D;
                    }

                    double x = this.mc.thePlayer.posX - this.mc.thePlayer.prevPosX;
                    double z = this.mc.thePlayer.posZ - this.mc.thePlayer.prevPosZ;
                    this.lastDist = Math.sqrt(x * x + z * z);
                }

                if (mode.is("Hypixel")) {
                    if (mc.thePlayer.isInWater() || mc.thePlayer.isInLava())
                        return;

                    if (MovementUtils.isMovingOnGround()) {
                        mc.gameSettings.keyBindJump.pressed = false;

                        if(newStrafe.isEnabled()) {
                            event.setOnGround(false);
                        }
                    } else {
                        if(newStrafe.isEnabled()) {
                            if(mc.thePlayer.fallDistance < 1.0f && MovementUtils.isDistFromGround(1)) {
                                event.setOnGround(true);
                            }
                        }
                    }

                    if (event.isOnGround()) {
                        event.setY(event.getY() + MathUtils.randomNumber(0.0067, 0.0042));
                    }

                    if (mc.thePlayer.fallDistance < 1.0f && MovementUtils.isMoving()) {
                        mc.thePlayer.motionY += 0.015;
                    }

                    double x = this.mc.thePlayer.posX - this.mc.thePlayer.prevPosX;
                    double z = this.mc.thePlayer.posZ - this.mc.thePlayer.prevPosZ;
                    this.lastDist = Math.sqrt(x * x + z * z);
                }

                if(mode.is("Watchdoge")) {
                    if (mc.thePlayer.isInWater() || mc.thePlayer.isInLava())
                        return;

                    if (MovementUtils.isMovingOnGround())
                        mc.gameSettings.keyBindJump.pressed = false;

                    /*
                    if(MovementUtils.isMoving())
                        MovementUtils.bypassOffSet(event);

                     */

                    double x = this.mc.thePlayer.posX - this.mc.thePlayer.prevPosX;
                    double z = this.mc.thePlayer.posZ - this.mc.thePlayer.prevPosZ;
                    this.lastDist = Math.sqrt(x * x + z * z);
                }
            }

            if(mode.is("Verus")) {
                EventMotion event = (EventMotion) e;

                mc.getNetHandler().sendPacketNoEvent(new C0CPacketInput());
                if(verusMode.is("Float") || verusMode.is("Port")) {
                    event.setOnGround(this.mc.thePlayer.onGround || this.spoofGround);
                }

                if(MovementUtils.isMovingOnGround()) {
                    mc.gameSettings.keyBindJump.pressed = false;
                }
            }

            if(mode.is("NCP")) {
                if(e.isPre()) {
                    if(MovementUtils.isMoving() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                        if(mc.thePlayer.onGround) {
                            mc.thePlayer.jump();
                            mc.timer.timerSpeed = 1.05F;
                            mc.thePlayer.motionX *= 1.053F;
                            mc.thePlayer.motionZ *= 1.053F;
                            mc.thePlayer.motionY *= 0.975f;
                        } else {
                            mc.thePlayer.jumpMovementFactor = 0.0265F;
                        }
                    } else {
                        mc.timer.timerSpeed = 1.0f;
                    }
                }
            }

            if(mode.is("NCP Low")) {
                if (mc.thePlayer.isInWater() || mc.thePlayer.isInLava())
                    return;

                if (MovementUtils.isMovingOnGround())
                    mc.gameSettings.keyBindJump.pressed = false;

                if (!MovementUtils.isOnGround()) {
                    mc.thePlayer.motionY = -0.4;
                }

                double x = this.mc.thePlayer.posX - this.mc.thePlayer.prevPosX;
                double z = this.mc.thePlayer.posZ - this.mc.thePlayer.prevPosZ;
                this.lastDist = Math.sqrt(x * x + z * z);
            }


        }

        if(e instanceof EventUpdate) {
            if(mode.is("Strafe")) {
                if (MovementUtils.isMovingOnGround()) {
                    mc.gameSettings.keyBindJump.pressed = false;
                    mc.thePlayer.jump();
                }
            }

            if(mode.is("Verus") && verusMode.is("Port")) {
                if(MovementUtils.isMovingOnGround()) {
                    mc.gameSettings.keyBindJump.pressed = false;
                }
            }

            if(mode.is("Minemen")) {
                if (MovementUtils.isMovingOnGround()) {
                    mc.gameSettings.keyBindJump.pressed = false;
                    mc.thePlayer.jump();
                }
            }

            if(mode.is("Larkus")) {
                if(mc.thePlayer.onGround) {
                    if(MovementUtils.isMoving()) {
                        mc.gameSettings.keyBindJump.pressed = false;
                        mc.thePlayer.jump();

                        if(mc.thePlayer.isAirBorne) {
                            mc.thePlayer.setSpeed(1);
                        }
                    }
                }
            }
        }
    }
}
