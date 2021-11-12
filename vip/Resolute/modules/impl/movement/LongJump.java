package vip.Resolute.modules.impl.movement;

import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.*;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.ui.notification.Notification;
import vip.Resolute.ui.notification.NotificationType;
import vip.Resolute.util.movement.MovementUtils;
import vip.Resolute.util.misc.TimerUtil;
import vip.Resolute.util.misc.TimerUtils;
import vip.Resolute.util.player.InventoryUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LongJump extends Module {

    public ModeSetting mode = new ModeSetting("Mode", "Watchdog", "Watchdog", "Watchdog Damage", "Watchdog Bow", "Verus", "AAC", "Experimental", "Funcraft", "SurvivalDub", "Jartex", "AGC");

    public NumberSetting jartexTicks = new NumberSetting("Jartex Ticks", 5.0D, this::isModeSelected, 1.0D, 25.0D, 1D);

    public NumberSetting agcSpeed = new NumberSetting("AGC Speed", 5, this::isMode2Selected,1, 10, 1);
    public NumberSetting agcTimer = new NumberSetting("AGC Timer", 0.2, this::isMode2Selected, 0.1, 1.0, 0.1);

    public NumberSetting bowSpeed = new NumberSetting("Bow Speed", 1.0, this::isMode3Selected,0.1, 1.0, 0.1);
    public NumberSetting bowY = new NumberSetting("Bow Y",0.2, this::isMode3Selected, 0.1, 10.0, 0.1);
    public NumberSetting bowTimer = new NumberSetting("Bow Timer",1.0, this::isMode3Selected,0.1, 2.0, 0.1);
    public BooleanSetting yMotionReduce = new BooleanSetting("Y Motion Reduce", false, this::isMode3Selected);

    public NumberSetting speedMult = new NumberSetting("Speed Multiplier", 1.82D, () -> mode.is("Experimental"), 1.00D, 2.50D, 0.01D);
    public NumberSetting heightMult = new NumberSetting("Height Multiplier", 1.53D, () -> mode.is("Experimental"), 1.00D, 2.00D, 0.01D);

    public BooleanSetting candyStop = new BooleanSetting("Full Stop", false, () -> mode.is("Candy Bar"));

    public BooleanSetting reduce = new BooleanSetting("Reduce", false, () -> mode.is("Experimental"));
    public BooleanSetting visualY = new BooleanSetting("Visual Y", false, () -> mode.is("Experimental"));

    public NumberSetting funBoost = new NumberSetting("Funcraft Boost", 3, () -> mode.is("Funcraft"), 0.1, 10.0, 0.1);
    public BooleanSetting funReduce = new BooleanSetting("Funcraft Reduce", false, () -> mode.is("Funcraft"));

    public BooleanSetting boost = new BooleanSetting("Boost", true, this::isMode4Selected);
    public BooleanSetting blink = new BooleanSetting("Blink", true, this::isMode4Selected);
    public BooleanSetting timerBoost = new BooleanSetting("Timer", true, this::isMode4Selected);
    public BooleanSetting indicate = new BooleanSetting("Display Ticks", true, this::isMode4Selected);
    public NumberSetting flightSpeedNum = new NumberSetting("Flight Speed", 1.5, this::isMode4Selected, 0.1, 1.7, 0.1);
    public NumberSetting bowFlightSpeed = new NumberSetting("Bow Flight Speed", 1.5, this::isMode4Selected,0.1, 1.7, 0.1);
    public NumberSetting timerSpeed = new NumberSetting("Timer Speed", 1.5, this::isMode4Selected,1, 2.0, 0.1);
    public NumberSetting timerEndSpeed = new NumberSetting("Timer End Speed", 0.1, this::isMode4Selected, 0.1, 2.0, 0.1);
    public NumberSetting blinkDelay = new NumberSetting("Blink Delay", 150, this::isMode4Selected,10, 500, 5);
    public NumberSetting slowdown = new NumberSetting("Slowdown", 145, this::isMode4Selected,10, 160, 5);
    public BooleanSetting staffWarn = new BooleanSetting("Staff Warn", true, this::isMode4Selected);

    public static BooleanSetting disable = new BooleanSetting("AutoDisable", true);

    public boolean hypixelDamaged = false;
    private int ticks;
    private int hypixelstage = 0;
    int prevSlot2;

    int i = 0;
    boolean isRiding;
    boolean hasJumped;

    public double moveSpeed, air, motionY;
    private int delay = 0;
    private double lastDist;

    public boolean collided, half;
    public int stage, groundTicks;
    public double lastDistance;
    public double movementSpeed;

    double x = 0;
    double y = 0;
    double z = 0;
    boolean shouldBlink;
    int survivalstage = 0;
    int candystage = 0;
    float timer = 0;
    int landingTicks;
    float dmgTimer = 0;
    float flightSpeed = 0;
    boolean damage;
    boolean hasBow = false;
    boolean shoot = false;
    protected boolean boosted = false, doneBow = false;
    public double speed;

    private ArrayList<Packet> packetList = new ArrayList<>();
    private List<Vec3> crumbs = new CopyOnWriteArrayList<>();

    private double launchY;

    private TimerUtil timercrumb = new TimerUtil();
    TimerUtil ljTimer = new TimerUtil();
    TimerUtil blinkTimer = new TimerUtil();
    TimerUtil bowTimerSD = new TimerUtil();
    private boolean onGroundLastTick = false;
    private double distance = 0.0D;
    double baseMoveSpeed;
    float prevPitch;

    private static final C07PacketPlayerDigging PLAYER_DIGGING = new C07PacketPlayerDigging(
            C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
    private static final C08PacketPlayerBlockPlacement BLOCK_PLACEMENT = new C08PacketPlayerBlockPlacement(
            new BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f);


    private boolean damaged;

    private boolean isCharging;
    private int chargedTicks;
    private int bowSlot;

    private double startY;
    private double oldPosY;
    private double yPos;

    public boolean isModeSelected() {
        return this.mode.is("Jartex");
    }

    public boolean isMode2Selected() {
        return this.mode.is("AGC");
    }

    public boolean isMode3Selected() {
        return this.mode.is("Watchdog Bow");
    }

    public boolean isMode4Selected() {
        return this.mode.is("SurvivalDub");
    }

    public LongJump() {
        super("LongJump", Keyboard.KEY_NONE, "Makes the player jump farther", Category.MOVEMENT);
        this.addSettings(mode, disable, jartexTicks, agcSpeed, agcTimer, bowSpeed, bowY, bowTimer, yMotionReduce, speedMult, heightMult, candyStop, reduce, visualY, funBoost, funReduce, boost, blink, timerBoost, indicate, flightSpeedNum, bowFlightSpeed, timerSpeed, timerEndSpeed, slowdown, staffWarn);
    }

    public void onEnable() {
        this.i = 0;
        this.mc.timer.timerSpeed = 1.0f;
        isRiding = false;
        hasJumped = false;
        lastDistance = movementSpeed = 0.0D;
        stage = groundTicks = 0;
        this.groundTicks = 0;
        shouldBlink = false;
        startY = mc.thePlayer.getEyeHeight();
        crumbs.clear();
        delay = 0;
        prevPitch = mc.thePlayer.rotationPitch;
        hypixelstage = 0;
        ticks = 0;
        prevSlot2 = mc.thePlayer.inventory.currentItem;
        collided = false;
        this.damaged = false;
        this.isCharging = false;
        this.chargedTicks = 0;
        launchY = mc.thePlayer.posY;
        doneBow = false;
        shoot = false;
        hasBow = false;
        timer = 0;
        landingTicks = 0;
        dmgTimer = 0;
        flightSpeed = 0;
        candystage = 0;
        survivalstage = 0;
        this.onGroundLastTick = false;
        this.distance = 0.0D;
        damage = false;
        x = mc.thePlayer.posX;
        y = mc.thePlayer.posY;
        z = mc.thePlayer.posZ;

        oldPosY = mc.thePlayer.posY;
        yPos = mc.thePlayer.getEyeHeight();

        bowTimerSD.reset();

        blinkTimer.reset();
    }

    public void onDisable() {
        yPos = 0;
        oldPosY = 0;

        this.i = 0;
        this.mc.timer.timerSpeed = 1.0f;
        crumbs.clear();
        this.motionY = 0.0D;
        isRiding = false;
        hasJumped = false;
        this.boosted = false;
        this.hypixelDamaged = false;
        ticks = 0;
        mc.thePlayer.speedInAir = 0.02F;
        speed = 0.0D;
        shouldBlink = false;
        x = mc.thePlayer.posX;
        y = mc.thePlayer.posY;
        z = mc.thePlayer.posZ;
        if(mode.is("SurvivalDub") || mode.is("Candy Bar"))
            mc.thePlayer.setPosition(x, y + 1.0E-12, z);

        try {
            for (Packet packets : packetList) {
                mc.getNetHandler().sendPacketNoEvent(packets);
            }
            packetList.clear();
        } catch (final ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    public void onEvent(Event e) {
        this.setSuffix(mode.getMode());

        if(e instanceof EventCameraPosition) {
            EventCameraPosition event = (EventCameraPosition) e;

            if(mode.is("Experimental")) {
                if(visualY.isEnabled()) {
                    e.setCancelled(true);
                    event.setY(mc.thePlayer.prevPosY - oldPosY);
                }
            }
        }

        if(e instanceof EventMove) {
            EventMove event = (EventMove) e;

            if(mode.is("Mineplex Low Hop")) {
                event.setY(mc.thePlayer.motionY = mc.thePlayer.movementInput.jump ? 0.42F : 0);
            }

            if(mode.is("Experimental")) {
                if(mc.thePlayer.hurtTime > 0 && !damaged){
                    damaged = true;
                }

                if (!this.damaged) {
                    event.setX(0);
                    event.setZ(0);
                    return;
                }

                if (MovementUtils.isMoving() && damaged && mc.thePlayer.ticksExisted > 8) {
                    final double baseMoveSpeed = MovementUtils.getBaseSpeedHypixel();
                    switch (stage) {
                        case 0:
                            if (MovementUtils.isMovingOnGround()) {
                                this.moveSpeed = baseMoveSpeed * speedMult.getValue();
                                event.setY(this.mc.thePlayer.motionY = MovementUtils.getJumpBoostModifier(0.42F) * heightMult.getValue());
                            }
                            break;
                        case 1:
                            moveSpeed -= 0.18D * (moveSpeed - MovementUtils.getSpeed());
                            break;
                        default:
                            this.moveSpeed = MovementUtils.calculateFriction(moveSpeed, lastDist, baseMoveSpeed);
                            break;
                    }

                    MovementUtils.setSpeed(event, Math.max(this.moveSpeed, baseMoveSpeed));
                    this.stage++;
                }
            }

            if(mode.is("Watchdog")) {
                if (MovementUtils.isMoving()) {
                    double baseMoveSpeed = MovementUtils.getBaseMoveSpeed();
                    switch (stage) {
                        case 0:
                            if (MovementUtils.isOnGround()) {
                                moveSpeed = baseMoveSpeed * MovementUtils.MAX_DIST;
                                event.setY(mc.thePlayer.motionY = MovementUtils.getJumpHeight(MovementUtils.VANILLA_JUMP_HEIGHT));
                            }
                            break;
                        case 1:
                            moveSpeed *= 1.0D;
                        case 2:
                            double difference = (MovementUtils.WATCHDOG_BUNNY_SLOPE + MovementUtils.getJumpBoostModifier() * 0.2D)
                                    * (moveSpeed - baseMoveSpeed);
                            moveSpeed = moveSpeed - difference;
                            break;
                        default:
                            moveSpeed = MovementUtils.calculateFriction(moveSpeed, lastDist, baseMoveSpeed);
                            break;
                    }

                    MovementUtils.setSpeed(event, Math.max(moveSpeed, baseMoveSpeed));
                    stage++;
                }
            }

            if(mode.is("13367")) {
                if (MovementUtils.isMoving()) {
                    double baseMoveSpeed = MovementUtils.getBaseMoveSpeed();
                    switch (stage) {
                        case 0:
                            if (MovementUtils.isOnGround()) {
                                moveSpeed = baseMoveSpeed * MovementUtils.MAX_DIST;
                                event.setY(mc.thePlayer.motionY = MovementUtils.getJumpHeight(MovementUtils.VANILLA_JUMP_HEIGHT));
                            }
                            break;
                        case 1:
                            moveSpeed *= 2.0D;
                        case 2:
                            double difference = (MovementUtils.WATCHDOG_BUNNY_SLOPE + MovementUtils.getJumpBoostModifier() * 0.2D) * (moveSpeed - baseMoveSpeed);
                            moveSpeed = moveSpeed - difference;
                            break;
                        default:
                            moveSpeed = MovementUtils.calculateFriction(moveSpeed, lastDist, baseMoveSpeed);
                            break;
                    }

                    MovementUtils.setSpeed(event, Math.max(moveSpeed, baseMoveSpeed));
                    stage++;
                }
            }

            if(mode.is("Watchdog Damage")) {
                if (MovementUtils.isMoving()) {
                    double baseMoveSpeed = MovementUtils.getBaseMoveSpeed();
                    switch (stage) {
                        case 0:
                            if (MovementUtils.isOnGround() && MovementUtils.fallDistDamage()) {
                                moveSpeed = baseMoveSpeed * MovementUtils.MAX_DIST;
                                event.setY(mc.thePlayer.motionY = MovementUtils.getJumpHeight(MovementUtils.VANILLA_JUMP_HEIGHT));
                                damage = true;
                            }
                            break;
                        case 1:
                            if(damage)
                                event.setY(mc.thePlayer.motionY = MovementUtils.getJumpHeight(MovementUtils.VANILLA_JUMP_HEIGHT));
                        case 2:
                            double difference = (MovementUtils.WATCHDOG_BUNNY_SLOPE + MovementUtils.getJumpBoostModifier() * 0.2D) * (moveSpeed - baseMoveSpeed);
                            moveSpeed = moveSpeed - difference;

                            if(damage)
                                event.setY(mc.thePlayer.motionY = MovementUtils.getJumpHeight(MovementUtils.VANILLA_JUMP_HEIGHT));
                            break;
                        default:
                            moveSpeed = MovementUtils.calculateFriction(moveSpeed, lastDist, baseMoveSpeed);
                            break;
                    }

                    MovementUtils.setSpeed(event, Math.max(moveSpeed, baseMoveSpeed));
                    stage++;
                }
            }

            if(mode.is("Candy Bar")) {
                if (MovementUtils.isMoving() && candystage == 1) {
                    double baseMoveSpeed = MovementUtils.getBaseMoveSpeed();
                    switch (stage) {
                        case 0:
                            if (MovementUtils.isOnGround()) {
                                moveSpeed = baseMoveSpeed * MovementUtils.MAX_DIST;
                                event.setY(mc.thePlayer.motionY = MovementUtils.getJumpHeight(MovementUtils.VANILLA_JUMP_HEIGHT));
                            }
                            break;
                        case 1:
                            moveSpeed *= 2.0D;
                        case 2:
                            double difference = (MovementUtils.WATCHDOG_BUNNY_SLOPE + MovementUtils.getJumpBoostModifier() * 0.2D) * (moveSpeed - baseMoveSpeed);
                            moveSpeed = moveSpeed - difference;
                            break;
                        default:
                            moveSpeed = MovementUtils.calculateFriction(moveSpeed, lastDist, baseMoveSpeed);
                            break;
                    }

                    MovementUtils.setSpeed(event, Math.max(moveSpeed, baseMoveSpeed));
                    stage++;
                }

                if(candystage == 0 && candyStop.isEnabled()) {
                    event.setX(0);
                    event.setZ(0);
                }
            }

            if(mode.is("Funcraft")) {
                if (mc.thePlayer.moveForward == 0.0F && mc.thePlayer.moveStrafing == 0.0F || mc.theWorld == null) {
                    this.speed = 0.27999999999999997D;
                    return;
                }

                if (mc.thePlayer.onGround) {
                    if (!this.onGroundLastTick && mc.thePlayer.motionY >= -0.3D) {
                        this.speed = funBoost.getValue() * 0.27999999999999997D;
                    } else {
                        this.speed *= 2.15D - 1.0D / Math.pow(10.0D, 5.0D);
                        event.setY(mc.thePlayer.motionY = 0.41999998688697815D);
                        mc.thePlayer.onGround = true;
                    }
                } else if (this.onGroundLastTick) {
                    if (this.distance < 2.147D) {
                        this.distance = 2.147D;
                    }

                    this.speed = this.distance - 0.66D * (this.distance - 0.27999999999999997D);
                } else {
                    this.speed = this.distance - this.distance / 159.0D;
                }

                if(funReduce.isEnabled()) mc.thePlayer.motionY = -0.02;

                this.onGroundLastTick = mc.thePlayer.onGround;
                this.speed = Math.max(this.speed, 0.27999999999999997D);
                event.setX(-(Math.sin((double)mc.thePlayer.getDirection()) * this.speed));
                event.setZ(Math.cos((double)mc.thePlayer.getDirection()) * this.speed);
            }
        }

        if(e instanceof EventMotion) {
            EventMotion event = (EventMotion) e;
            if(e.isPre()) {
                if(mode.is("Funcraft")) {
                    this.distance = Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ);

                    if (disable.isEnabled()) {
                        if (!this.onGroundLastTick && mc.thePlayer.isCollidedVertically && this.isEnabled()) {
                            this.toggle();
                        }
                    }
                }

                if(mode.is("Watchdog Damage")) {
                    if (MovementUtils.isMoving() && MovementUtils.isOnGround() && ++this.groundTicks > 1) {
                        this.toggle();
                    }
                    if (mc.thePlayer.fallDistance < 1.0f) {
                        mc.thePlayer.motionY += 0.005;
                    }

                    final EntityPlayerSP player = mc.thePlayer;
                    double xDist = player.posX - player.lastTickPosX;
                    double zDist = player.posZ - player.lastTickPosZ;

                    lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                }

                if(mode.is("Experimental")) {
                    if (!this.damaged) {
                        final int bowSlot = InventoryUtils.findInHotBar(stack -> stack != null && stack.getItem() instanceof ItemBow);

                        if (bowSlot == -1) {
                            return;
                        }

                        if (!isCharging) {
                            if (MovementUtils.isOnGround()) {
                                final boolean needSwitch = this.mc.thePlayer.inventory.currentItem != bowSlot;

                                if (needSwitch)
                                    mc.getNetHandler().sendPacketNoEvent(new C09PacketHeldItemChange(bowSlot));

                                this.bowSlot = bowSlot;
                                mc.getNetHandler().sendPacketNoEvent(BLOCK_PLACEMENT);
                                this.isCharging = true;
                                this.chargedTicks = 0;
                            }
                        } else {
                            ++this.chargedTicks;

                            if (bowSlot != this.bowSlot) {
                                toggle();
                                return;
                            }

                            if (this.chargedTicks == 3) {
                                final int physicalHeldItem = this.mc.thePlayer.inventory.currentItem;
                                mc.getNetHandler().sendPacketNoEvent(PLAYER_DIGGING);
                                if (this.bowSlot != physicalHeldItem)
                                    mc.getNetHandler().sendPacketNoEvent(new C09PacketHeldItemChange(physicalHeldItem));
                            } else if (this.chargedTicks == 2) {
                                event.setPitch(-90.0F);
                            }
                        }

                        return;
                    }

                    final EntityPlayer player = this.mc.thePlayer;

                    if (reduce.isEnabled() && mc.thePlayer.motionY < 0.15 && mc.thePlayer.motionY > -0.3 && mc.thePlayer.fallDistance < 0.05 && !mc.thePlayer.isInWater()) {
                        mc.thePlayer.motionY += 0.0834D / 2.0D;
                    }

                    if (MovementUtils.isMoving() &&
                            MovementUtils.isOnGround() &&
                            ++this.groundTicks >= 1)
                        toggle();
                }

                if(mode.is("Experimental")) {
                    EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                    double xDif = player.posX - player.lastTickPosX;
                    double zDif = player.posZ - player.lastTickPosZ;
                    lastDist = StrictMath.sqrt(xDif * xDif + zDif * zDif);
                }

                if(mode.is("Verus")) {
                    mc.gameSettings.keyBindJump.pressed=false;
                    if(mc.thePlayer.onGround&&MovementUtils.isMoving()) {
                        mc.thePlayer.jump();
                        MovementUtils.strafe(0.48F);
                    }else MovementUtils.strafe();
                }

                if(mode.is("AGC")) {
                    if(mc.thePlayer.hurtTime > 0) {
                        mc.gameSettings.keyBindForward.pressed = true;
                        this.mc.timer.timerSpeed = (float) agcTimer.getValue();
                        if(!mc.thePlayer.onGround) {
                            if (mc.gameSettings.keyBindForward.isKeyDown()) {
                                mc.thePlayer.setSpeed((float) agcSpeed.getValue());
                            }
                        }
                    }
                }

                if(mode.is("Candy Bar")) {
                    x = mc.thePlayer.posX;
                    y = mc.thePlayer.posY;
                    z = mc.thePlayer.posZ;

                    if (MovementUtils.isMoving() && MovementUtils.isOnGround() && candystage == 1) {
                        this.toggle();
                    }
                    if (mc.thePlayer.fallDistance < 1.0f) {
                        mc.thePlayer.motionY += 0.005;
                    }

                    final EntityPlayerSP player = mc.thePlayer;
                    double xDist = player.posX - player.lastTickPosX;
                    double zDist = player.posZ - player.lastTickPosZ;

                    lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

                    if (candystage == 0 && mc.thePlayer.hurtTime == 0) {
                        if (dmgTimer <= 7 && mc.thePlayer.onGround) {
                            mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.42, z, false));
                            mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                            event.onGround = false;
                        } else if (dmgTimer > 120) {
                            mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, true));
                        }

                        dmgTimer += 1;
                    }
                }

                if(mode.is("Candy Bar")) {
                    if(candystage == 0) {
                        if(mc.thePlayer.hurtTime > 0) {
                            mc.thePlayer.setPosition(x, y + 0.5 - 0.00610, z);
                            candystage = 1;
                        }
                    }

                    if(candystage == 0 && !candyStop.isEnabled()) {
                        MovementUtils.setSpeed(0);
                    }
                }

                if (mode.is("SurvivalDub")) {
                    x = mc.thePlayer.posX;
                    y = mc.thePlayer.posY;
                    z = mc.thePlayer.posZ;

                    if (mc.thePlayer.getCurrentEquippedItem() != null) {
                        for (int i = 0; i < 9; i++) {
                            if (mc.thePlayer.inventory.getStackInSlot(i) == null)
                                continue;
                            if (mc.thePlayer.inventory.getStackInSlot(i).getItem() instanceof ItemBow) {
                                hasBow = true;
                            }
                        }
                    }

                    if(!hasBow) {
                        if (survivalstage > 0) {
                            mc.thePlayer.motionY = 0;
                            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 1.0E-10, mc.thePlayer.posZ);
                        } else if (survivalstage == 0 && mc.thePlayer.hurtTime == 0) {
                            if (dmgTimer <= 7 && mc.thePlayer.onGround) {
                                mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.42, z, false));
                                mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                                event.onGround = false;
                            } else if (dmgTimer > 120) {
                                mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, true));
                            }

                            dmgTimer += 1;
                        }
                    } else {
                        if (survivalstage > 0) {
                            mc.thePlayer.motionY = 0;
                            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 1.0E-10, mc.thePlayer.posZ);
                            //mc.thePlayer.fallDistance = (float) (mc.thePlayer.fallDistance - 1.0E-12);
                        }
                        int slot = this.getSlotWithBow();
                        if (this.getSlotWithBow() == -1) {
                            return;
                        }

                        mc.thePlayer.inventory.currentItem = slot;

                        if (mc.thePlayer.getCurrentEquippedItem() != null) {
                            if (mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBow && survivalstage == 0 && mc.thePlayer.hurtTime == 0 && !shoot) {

                                event.setPitch(-90);

                                MovementUtils.strafe();
                                mc.thePlayer.motionX = 0;
                                mc.thePlayer.motionY = 0;
                                mc.thePlayer.motionZ = 0;
                                mc.thePlayer.jumpMovementFactor = 0;
                                mc.thePlayer.onGround = false;

                                mc.gameSettings.keyBindUseItem.pressed = true;
                                if(bowTimerSD.hasElapsed(165)) {
                                    shoot = true;
                                    bowTimerSD.reset();
                                }

                            } else if(shoot ) {
                                MovementUtils.strafe();
                                mc.thePlayer.motionX = 0;
                                mc.thePlayer.motionY = 0;
                                mc.thePlayer.motionZ = 0;
                                mc.thePlayer.jumpMovementFactor = 0;
                                mc.thePlayer.onGround = false;

                                mc.gameSettings.keyBindUseItem.pressed = false;
                                mc.thePlayer.stopUsingItem();
                                mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                                mc.thePlayer.inventory.currentItem = prevSlot2;
                            }
                        }
                    }
                }
            }

            if(mode.is("SurvivalDub")) {
                mc.getNetHandler().sendPacketNoEvent(new C0CPacketInput(flightSpeed, flightSpeed, false, false));

                if(staffWarn.isEnabled()) {
                    for(int i = 0; i < mc.theWorld.playerEntities.size(); i++) {
                        String name = mc.theWorld.playerEntities.get(i).getName();

                        if(name.contains("XMati") || name.contains("M1au_") || name.contains("JennyPixu") || name.contains("iChessman7w7") || name.contains("DashoDM") || name.contains("Thyonne") || name.contains("zLuisaz") || name.contains("conlAlfon") || name.contains("Jenn") || name.contains("David") || name.contains("ReachBoy")) {
                            String string = "There is a staff member in your game";
                            Resolute.getNotificationManager().add(new Notification("Staff Alert", string, 4000L, NotificationType.WARNING));
                        }
                    }
                }

                if(survivalstage == 0) {
                    if(mc.thePlayer.hurtTime > 0) {
                        if(blink.isEnabled()) {
                            shouldBlink = true;
                        } else {
                            shouldBlink = false;
                        }
                        mc.getNetHandler().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                        damage = true;
                        if(!hasBow)
                            mc.thePlayer.setPosition(x, y + 0.5 - 0.00610, z);
                        survivalstage = 1;
                        timer = 0;
                    }
                }

                if(survivalstage == 1) {
                    if(timer == 0) {
                        flightSpeed = 0.46f;
                    }

                    if(timer == 1) {
                        flightSpeed = 0.75f;
                    }

                    if(timer == 2) {
                        if(hasBow) {
                            flightSpeed = (float) bowFlightSpeed.getValue();
                        } else {
                            flightSpeed = (float) flightSpeedNum.getValue();
                        }
                        survivalstage = 2;
                    }
                }

                if(survivalstage == 2) {
                    if(timerBoost.isEnabled()) {
                        if(flightSpeed > (hasBow ? bowFlightSpeed.getValue() : flightSpeedNum.getValue()) - timerEndSpeed.getValue()) {
                            mc.timer.timerSpeed = (float) timerSpeed.getValue();
                        } else {
                            mc.timer.timerSpeed = 1.0f;
                        }
                    }

                    if(flightSpeed > 0.25) {
                        flightSpeed -= flightSpeed / slowdown.getValue();
                    }
                }

                if(mc.thePlayer.isCollidedHorizontally || !MovementUtils.isMoving()) {
                    flightSpeed = 0.25f;
                }

                if(survivalstage != 0) {
                    MovementUtils.setSpeed(flightSpeed);
                } else {
                    MovementUtils.setSpeed(0);
                }

                timer += 1;

                if(survivalstage > 0) {
                    landingTicks += 1;
                }
            }

            if(mode.is("Watchdog Bow")) {
                if (!hypixelDamaged) {
                    if (mc.thePlayer.getCurrentEquippedItem() != null) {
                        for (int i = 0; i < 9; i++) {
                            if (mc.thePlayer.inventory.getStackInSlot(i) == null)
                                continue;
                            if (mc.thePlayer.inventory.getStackInSlot(i).getItem() instanceof ItemBow) {
                                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = i));
                            }
                        }
                    }
                }
                if (mc.thePlayer.getCurrentEquippedItem() != null) {
                    if (mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBow && !this.hypixelDamaged) {
                        if(ticks < 7)
                            event.setPitch(-90);

                        MovementUtils.strafe();
                        mc.thePlayer.motionX = 0;
                        mc.thePlayer.motionY = 0;
                        mc.thePlayer.motionZ = 0;
                        mc.thePlayer.jumpMovementFactor = 0;
                        mc.thePlayer.onGround = false;

                        if(ticks != 50)
                            ++this.ticks;

                        if (ticks >= 7 && ticks != 50) {
                            mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                            ticks = 50;
                        } else if (ticks == 1) {
                            mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                        }
                    }
                }
                if (ticks == 50 && mc.thePlayer.hurtTime > 0) {
                    this.hypixelDamaged = true;
                    if (collided != true) {
                        this.mc.thePlayer.jump();
                        mc.thePlayer.motionY = bowY.getValue() / 10;
                        hypixelstage++;
                    }
                    if (hypixelstage == 1) {
                        this.collided = true;
                    }
                }
                if (this.hypixelDamaged) {
                    this.mc.timer.timerSpeed = (float) bowTimer.getValue();
                    if(!mc.thePlayer.onGround) {
                        if(mc.gameSettings.keyBindForward.isKeyDown()) {
                            mc.thePlayer.setSpeed((float) this.bowSpeed.getValue());
                        }
                        if(yMotionReduce.isEnabled()) {
                            if (mc.thePlayer.fallDistance < 1.0F) {
                                if (mc.thePlayer.motionY < 0.0D)
                                    mc.thePlayer.motionY *= 0.85D;

                                mc.thePlayer.motionY += 0.001D;
                            }
                        }
                    } else {
                        if(mc.thePlayer.hurtTime == 0) {
                            Resolute.getNotificationManager().add(new Notification("Flag alert", "Wait until this notification disappears before toggling again", 10000L, NotificationType.WARNING));
                            this.i = 0;
                            this.mc.timer.timerSpeed = 1.0f;
                            this.motionY = 0.0D;
                            isRiding = false;
                            hasJumped = false;
                            this.boosted = false;
                            this.hypixelDamaged = false;
                            ticks = 0;
                            mc.thePlayer.speedInAir = 0.02F;
                            toggled = false;
                        }
                    }
                }
            }

            if(mode.is("13367")) {
                if (e.isPre()) {
                    final EntityPlayerSP player = mc.thePlayer;
                    double xDist = player.posX - player.lastTickPosX;
                    double zDist = player.posZ - player.lastTickPosZ;

                    lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

                    if (MovementUtils.isOnGround() && !mc.thePlayer.isOnLadder() && ++groundTicks >= 1)
                        toggle();

                    if (player.fallDistance < 1.0F) {
                        if (player.motionY < 0.0D)
                            player.motionY *= 0.75D;

                        player.motionY += 0.001D;
                    }
                }
            }

            if(mode.is("AAC")) {
                if (!mc.thePlayer.onGround && !mc.thePlayer.isCollided) {
                    mc.timer.timerSpeed = 0.6f;
                    if (mc.thePlayer.motionY < 0 && delay > 0) {
                        delay--;
                        mc.timer.timerSpeed = 0.95f;
                    } else {
                        delay = 0;
                        mc.thePlayer.motionY = mc.thePlayer.motionY / 0.9800000190734863;
                        mc.thePlayer.motionY += 0.03;
                        mc.thePlayer.motionY *= 0.9800000190734863;
                        mc.thePlayer.jumpMovementFactor = 0.03625f;
                    }
                } else {
                    mc.timer.timerSpeed = 1.0f;
                    delay = 2;
                }
            }

            if(mode.is("Watchdog")) {
                if (e.isPre()) {
                    final EntityPlayerSP player = mc.thePlayer;
                    double xDist = player.posX - player.lastTickPosX;
                    double zDist = player.posZ - player.lastTickPosZ;

                    lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

                    if (MovementUtils.isOnGround() && !mc.thePlayer.isOnLadder() && ++groundTicks >= 1)
                        toggle();

                    if (player.fallDistance < 1.0F) {
                        if (player.motionY < 0.0D)
                            player.motionY *= 0.75D;

                        player.motionY += 0.001D;
                    }
                }
            }
        }

        if(e instanceof EventCollide) {
            EventCollide event = (EventCollide) e;
            if(mode.is("Verus")) {
                if(event.getBlock() instanceof BlockAir && event.getY() <= launchY) {
                    event.setBoundingBox(AxisAlignedBB.fromBounds(event.getX(), event.getY(), event.getZ(), event.getX() + 1, launchY, event.getZ() + 1));
                }
            }
        }

        if(e instanceof EventUpdate && e.isPre()) {
            if (mode.is("Jartex")) {
                if (mc.thePlayer.isRiding()) {
                    isRiding = true;
                    mc.gameSettings.keyBindSneak.pressed = true;
                } else if (isRiding == true) {
                    mc.thePlayer.jump();
                    isRiding = false;
                    hasJumped = true;
                    mc.gameSettings.keyBindSneak.pressed = false;
                }

                if (hasJumped == true) {
                    if (this.i < jartexTicks.getValue()) {
                        mc.thePlayer.motionY += 1.5f;
                        mc.thePlayer.jumpMovementFactor = 0.1f;
                        i++;
                    } else {
                        if (mc.thePlayer.onGround) {
                            if (disable.isEnabled()) {
                                toggled = false;
                                mc.timer.timerSpeed = 1.0f;
                            } else {
                                this.i = 0;
                                isRiding = false;
                                hasJumped = false;
                            }
                        }
                    }
                }
            }
        }

        if(e instanceof EventPacket) {
            if (((EventPacket) e).getPacket() instanceof C0APacketAnimation || ((EventPacket) e).getPacket() instanceof C03PacketPlayer || ((EventPacket) e).getPacket() instanceof C07PacketPlayerDigging || ((EventPacket) e).getPacket() instanceof C08PacketPlayerBlockPlacement) {
                if (shouldBlink && blink.isEnabled()) {
                    if (blinkTimer.hasElapsed((long) blinkDelay.getValue())) {
                        try {
                            for (Packet packets : packetList) {
                                mc.getNetHandler().sendPacketNoEvent(packets);
                            }
                            packetList.clear();
                            crumbs.clear();
                        } catch (final ConcurrentModificationException exception) {
                            exception.printStackTrace();
                        }
                        blinkTimer.reset();
                    } else {
                        e.setCancelled(true);
                        packetList.add(((EventPacket) e).getPacket());
                    }
                }
            }
        }
    }

    private int getSlotWithBow() {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack == null || !(itemStack.getItem() instanceof ItemBow)) continue;
            return i;
        }
        return -1;
    }


    public static int getColor(int red, int green, int blue) {
        return getColor(red, green, blue, 255);
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        int color = 0;
        color |= alpha << 24;
        color |= red << 16;
        color |= green << 8;
        color |= blue;
        return color;
    }

    public void selfBow() {
        TimerUtils fuck = new TimerUtils();
        fuck.reset();
        int oldSlot = mc.thePlayer.inventory.currentItem;

        mc.gameSettings.keyBindBack.pressed = false;
        mc.gameSettings.keyBindForward.pressed = false;
        mc.gameSettings.keyBindRight.pressed = false;
        mc.gameSettings.keyBindLeft.pressed = false;
        Thread thread = new Thread(){
            public void run(){
                int oldSlot = mc.thePlayer.inventory.currentItem;
                ItemStack block = mc.thePlayer.getCurrentEquippedItem();

                if (block != null) {
                    block = null;
                }
                int slot = mc.thePlayer.inventory.currentItem;
                for (short g = 0; g < 9; g++) {

                    if (mc.thePlayer.inventoryContainer.getSlot(g + 36).getHasStack()
                            && mc.thePlayer.inventoryContainer.getSlot(g + 36).getStack().getItem() instanceof ItemBow
                            && mc.thePlayer.inventoryContainer.getSlot(g + 36).getStack().stackSize != 0
                            && (block == null
                            || (block.getItem() instanceof ItemBow))) {

                        slot = g;
                        block = mc.thePlayer.inventoryContainer.getSlot(g + 36).getStack();

                    }

                }

                mc.getNetHandler().sendPacketNoEvent(new C09PacketHeldItemChange(slot));
                mc.thePlayer.inventory.currentItem = slot;

                float oldPitch = mc.thePlayer.rotationPitch;

                mc.thePlayer.rotationPitch = -90;
                mc.gameSettings.keyBindUseItem.pressed = true;
                try {
                    Thread.sleep(160);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C05PacketPlayerLook(mc.thePlayer.rotationYaw, -90, true));

                mc.gameSettings.keyBindUseItem.pressed = false;
                mc.thePlayer.rotationPitch = oldPitch;
                try {
                    Thread.sleep(180);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                doneBow = true;

                mc.getNetHandler().sendPacketNoEvent(new C09PacketHeldItemChange(oldSlot));
                ljTimer.reset();
                mc.thePlayer.inventory.currentItem = oldSlot;
            }
        };

        thread.start();

        mc.gameSettings.keyBindBack.pressed = false;
        mc.gameSettings.keyBindForward.pressed = false;
        mc.gameSettings.keyBindRight.pressed = false;
        mc.gameSettings.keyBindLeft.pressed = false;

        mc.getNetHandler().sendPacketNoEvent(new C09PacketHeldItemChange(oldSlot));
        mc.thePlayer.inventory.currentItem = oldSlot;
    }
}

