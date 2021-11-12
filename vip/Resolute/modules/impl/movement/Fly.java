package vip.Resolute.modules.impl.movement;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.*;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.misc.TimerUtil;
import vip.Resolute.util.movement.MovementUtils;
import vip.Resolute.util.misc.TimerUtils;
import vip.Resolute.util.render.RenderUtils;
import vip.Resolute.util.world.Vec3d;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class Fly extends Module {

    public ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Motion", "NCP Dev", "Funcraft", "Survivaldub Old", "Verus", "Verus Float", "Verus Damage", "Disabler5", "Packet1", "Collision", "MushMC");

    public BooleanSetting bobbing = new BooleanSetting("View Bobbing", false);
    public BooleanSetting xzcancel = new BooleanSetting("X Z Cancel", true);

    public NumberSetting motionSpeedProp = new NumberSetting("Motion Speed", 5.0, () -> mode.is("Motion"), 0.1, 5.0, 0.1);

    public NumberSetting verusTicks = new NumberSetting("Verus Ticks", 20, this::isModeSelected, 5, 300, 5);
    public NumberSetting verusSpeed = new NumberSetting("Verus Speed",2.5,() -> this.isModeSelected() || this.isMode4Selected(), 0.1, 5.0, 0.1);
    public NumberSetting verusTimer = new NumberSetting("Verus Timer",0.50, this::isModeSelected,0.05, 2.00, 0.05);

    public NumberSetting mushTicks = new NumberSetting("Mush Ticks", 20, this::isMode5Selected, 5, 300, 5);
    public NumberSetting mushSpeed = new NumberSetting("Mush Speed",2.5,() -> this.isMode5Selected() || this.isMode5Selected(), 0.1, 5.0, 0.1);
    public NumberSetting mushTimer = new NumberSetting("Mush Timer",0.50, this::isMode5Selected,0.05, 2.00, 0.05);

    public BooleanSetting timerProp = new BooleanSetting("Timer", true, this::isMode3Selected);
    public NumberSetting timerSpeedProp = new NumberSetting("Timer Speed", 1.7, () -> isMode3Selected() && timerProp.isEnabled(), 1.0, 5.0, 0.1);
    public NumberSetting timerDurationProp = new NumberSetting("Timer Duration", 600, () -> isMode3Selected() && timerProp.isEnabled(), 0, 2000, 10);
    public NumberSetting initialSpeed = new NumberSetting("Initial Speed", 1.0, this::isMode3Selected, 0.0, 1.0, 0.1);
    public NumberSetting funSpeed = new NumberSetting("Funcraft Speed", 1.0, this::isMode3Selected, 0.1, 5.0, 0.1);
    public NumberSetting reductionSpeed = new NumberSetting("Reduction Speed", 0.8, this::isMode3Selected, 0.0, 1.0, 0.1);
    public BooleanSetting funDamage = new BooleanSetting("Funcraft Damage", true, this::isMode3Selected);

    public BooleanSetting damage = new BooleanSetting("Damage", true, () -> mode.is("Survivaldub Old"));
    public NumberSetting reduce = new NumberSetting("Reduce", 1.2D, () -> mode.is("Survivaldub Old"), 1.0D, 2.0D, 0.1D);
    public NumberSetting startSpeed = new NumberSetting("Start Speed", 1.2, () -> mode.is("Survivaldub Old"),1.0, 2.0, 0.1);

    public NumberSetting hylexSpeed = new NumberSetting("Hylex Speed", 2.0, () -> mode.is("Hylex"), 0.1, 4.0, 0.1);

    public NumberSetting disablerHeld = new NumberSetting("Disabler5 Packets", 7, this::isMode2Selected,3, 20, 1);
    public BooleanSetting disabler5C04 = new BooleanSetting("Disabler5 Alt Packets",false, this::isMode2Selected);

    private ArrayList<Packet> packetList = new ArrayList<>();

    public static boolean enabled = false;

    private final TimerUtil funtimer = new TimerUtil();

    TimerUtils timer;
    TimerUtil barTimer = new TimerUtil();
    TimerUtils flyTimer = new TimerUtils();
    int stage;
    private double moveSpeed;
    double lastDist = 0;
    private double mineplexSpeed;
    private boolean hasFallen;
    private int i = (int) verusTicks.getValue();
    private int l = (int) mushTicks.getValue();
    int ticks;
    public static double startX, startY, startZ;
    public static boolean back, done;
    int airTicks;
    boolean hasJumped = false;
    int slot = 0;
    public boolean hypixelDamaged = false;

    public double speed;
    private double launchY;
    public boolean doSlow;
    public boolean damaged;
    public double movementSpeed;
    public boolean spoofGround;
    private int verusStage;

    public boolean isModeSelected() {
        return this.mode.is("Verus Damage");
    }
    public boolean isMode2Selected() {
        return this.mode.is("Disabler5");
    }
    public boolean isMode3Selected() { return this.mode.is("Funcraft"); }
    public boolean isMode4Selected() {
        return this.mode.is("Verus");
    }
    public boolean isMode5Selected() {
        return  this.mode.is("MushMC");
    }

    public Fly() {
        super("Fly", Keyboard.KEY_NONE, "Allows flight", Category.MOVEMENT);
        this.addSettings(mode, bobbing, xzcancel, motionSpeedProp, verusTicks, verusSpeed,
                verusTimer, timerProp, timerSpeedProp, timerDurationProp, initialSpeed,
                funSpeed, reductionSpeed, funDamage, damage, reduce, startSpeed, hylexSpeed, disablerHeld, disabler5C04);
        this.timer = new TimerUtils();
    }

    public void onEnable() {
        super.onEnable();

        this.timer.reset();
        barTimer.reset();
        done = false;
        back = false;
        verusStage = 0;
        launchY = mc.thePlayer.posY;
        startX = mc.thePlayer.posX;
        startY = mc.thePlayer.posY;
        startZ = mc.thePlayer.posZ;
        enabled = true;
        hasFallen = false;
        movementSpeed = 0.0D;
        ticks = 0;
        airTicks = 0;
        mineplexSpeed = 0;
        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;
        i = (int) verusTicks.getValue();
        l = (int) mushTicks.getValue();
        moveSpeed = 0;
        funtimer.reset();
        stage = 0;
        this.damaged = false;

        if(mode.is("AAC5")) {
            flyTimer.reset();
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ);
        }

        if(mode.is("Disabler3")) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionY = 0;
            if(mc.thePlayer.onGround){
                Resolute.addChatMessage("Jump into the air first and toggle");
                toggled = false;
            } else {
                mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x,1.7976931348623157E+308 , z,true));
            }
        }

        if(mode.is("Disabler4")) {
            mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x,1.7976931348623157E+308,z,true));
        }

        if(mode.is("Disabler2")) {
            mc.thePlayer.sendQueue.addToSendQueue(new C18PacketSpectate(mc.thePlayer.getGameProfile().getId()));
        }

        if(mode.is("NCP Dev")) {
            if(mc.thePlayer.onGround) {
                mc.thePlayer.jump();
            }
        }

        if(mode.is("Verus Damage")) {
            if (mc.theWorld.getCollidingBoundingBoxes((Entity)mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, 3.0001D, 0.0D).expand(0.0D, 0.0D, 0.0D)).isEmpty()) {
                mc.thePlayer.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.0001D, mc.thePlayer.posZ, false));
                mc.thePlayer.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                mc.thePlayer.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
            }
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42D, mc.thePlayer.posZ);
        }

        if(mode.is("MushMC")) {

            if (mc.theWorld.getCollidingBoundingBoxes((Entity)mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, 3.0001D, 0.0D).expand(0.0D, 0.0D, 0.0D)).isEmpty()) {
                mc.thePlayer.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.0001D, mc.thePlayer.posZ, false));
                mc.thePlayer.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                mc.thePlayer.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
            }
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42D, mc.thePlayer.posZ);
        }



        if(mode.is("Verus")) {
            if(mc.thePlayer.onGround){
                if(!damaged){
                    if (mc.theWorld.getCollidingBoundingBoxes((Entity)mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, 3.0001D, 0.0D).expand(0.0D, 0.0D, 0.0D)).isEmpty()) {
                        mc.thePlayer.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.0001D, mc.thePlayer.posZ, false));
                        mc.thePlayer.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                        mc.thePlayer.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                    }
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42D, mc.thePlayer.posZ);
                }
            }
        }

        if(mode.is("Survivaldub Old")) {
            if (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically && damage.isEnabled()) {
                MovementUtils.fallDistDamage();
            }
        }

        if(mode.is("Hylex")) {
            if (mc.theWorld.getCollidingBoundingBoxes((Entity)mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, 3.0001D, 0.0D).expand(0.0D, 0.0D, 0.0D)).isEmpty()) {
                mc.thePlayer.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.0001D, mc.thePlayer.posZ, false));
                mc.thePlayer.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                mc.thePlayer.sendQueue.addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
            }
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42D, mc.thePlayer.posZ);
        }
    }

    public void onDisable() {
        super.onDisable();
        this.speed = 0.0D;
        this.timer.reset();
        hasFallen = false;
        this.slot = 0;
        mc.timer.timerSpeed = 1.0f;
        mc.thePlayer.speedInAir = 0.02f;
        mc.thePlayer.capabilities.isFlying = false;
        mc.thePlayer.capabilities.allowFlying = false;
        hasJumped = false;
        mc.thePlayer.noClip = false;
        enabled = false;
        stage = 0;
        hypixelDamaged = false;

        try {
            for (Packet packets : packetList) {
                mc.getNetHandler().sendPacketNoEvent(packets);
            }
            packetList.clear();
        }
        catch (final ConcurrentModificationException e) {
            e.printStackTrace();
        }

        if(xzcancel.isEnabled()) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
        }

        if(mode.is("AAC5")) {
            sendAAC5Packets();
            mc.thePlayer.noClip = false;
        }

        if(mode.is("Disabler5")) {
            sendAAC5Packets();
        }

        if(mode.is("Motion")) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
        }

        if(mode.is("Redesky S")) {
            mc.thePlayer.capabilities.isFlying = false;
        }

        if(mode.is("Vanilla")) {
            mc.thePlayer.capabilities.allowFlying = false;
            mc.thePlayer.capabilities.isFlying = false;
        }
        if(mode.is("Larkus")) {



        }
    }

    public void onEvent(Event e) {
        this.setSuffix(mode.getMode());

        if(e instanceof EventCollide) {
            EventCollide event = (EventCollide) e;
            if(mode.is("Verus Float") || mode.is("Collide")) {
                if(event.getBlock() instanceof BlockAir && event.getY() <= launchY) {
                    event.setBoundingBox(AxisAlignedBB.fromBounds(event.getX(), event.getY(), event.getZ(), event.getX() + 1, launchY, event.getZ() + 1));
                }
            }
        }

        if(e instanceof EventRender2D) {
            EventRender2D event = (EventRender2D) e;
            float x;
            float y;
            float percentage;
            float width;
            float half;

            ScaledResolution resolution = new ScaledResolution(mc);
            x = resolution.getScaledWidth() / 2.0f;
            y = resolution.getScaledHeight() / 2.0f + 15.0f;
            percentage = Math.min(1.0f, (this.barTimer.elapsed() / 20) / 128.0f);
            width = 80.0f;
            half = width / 2.0f;
            Gui.drawRect(x - half - 0.5f, y - 2.0f, x + half + 0.5f, y + 2.0f, 2013265920);
            GL11.glEnable(3089);
            RenderUtils.startScissorBox(resolution, (int)(x - half), (int)y - 2, (int)(width * percentage), 4);
            RenderUtils.drawGradientRect(x - half, y - 1.5f, x - half + width, y + 1.5f, true, -1571930, -16711936);
            GL11.glDisable(3089);
        }

        if(e instanceof EventPacket) {
            if(((EventPacket) e).getPacket() instanceof S08PacketPlayerPosLook) {
                if(mode.is("Disabler5")) {
                    final S08PacketPlayerPosLook packetPlayerPosLook=(S08PacketPlayerPosLook) ((EventPacket) e).getPacket();
                }
            }

            if(((EventPacket) e).getPacket() instanceof C03PacketPlayer) {
                final C03PacketPlayer packetPlayer = (C03PacketPlayer) ((EventPacket) e).getPacket();

                if(mode.is("Disabler5")) {
                    disablerC03List.add(packetPlayer);
                    e.setCancelled(true);
                    if(disablerC03List.size() > disablerHeld.getValue())
                        sendAAC5Packets();
                }
            }


            if(((EventPacket) e).getPacket() instanceof S08PacketPlayerPosLook) {
                if(mode.is("NCP Dev")) {
                    if(mc.thePlayer.ticksExisted % 2 ==0) {
                        e.setCancelled(true);
                    }
                }
            }
        }

        if(e instanceof EventUpdate) {
            double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
            double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
            lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

            if(e.isPre()) {
                if(bobbing.isEnabled())
                    mc.thePlayer.cameraYaw = 0.105F;

                if(mode.is("Packet1")) {
                    mc.getNetHandler().addToSendQueueSilent(new C0CPacketInput());
                    mc.getNetHandler().addToSendQueueSilent(new C0FPacketConfirmTransaction());
                    mc.thePlayer.motionY = 0;
                    if(MovementUtils.isMoving()) {
                        MovementUtils.setSpeed((float) 1.0000024);
                    }
                    if(mc.gameSettings.keyBindSneak.isKeyDown()) {
                        mc.thePlayer.motionY -= 1;
                    }
                    if(mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.thePlayer.motionY += 1;
                    }
                }

                if(mode.is("Verus Damage")) {
                    mc.thePlayer.motionY = 0.0D;

                    mc.thePlayer.onGround = true;
                    mc.thePlayer.fallDistance = 0;
                    mc.thePlayer.isCollidedVertically = true;
                }

                if(mode.is("MushMC")) {
                    mc.thePlayer.motionY = 0.0D;

                    mc.thePlayer.onGround = true;
                    mc.thePlayer.fallDistance = 0;
                    mc.thePlayer.isCollidedVertically = true;
                }

                if(mode.is("Verus")) {
                    mc.thePlayer.motionY = 0.0D;

                    mc.thePlayer.onGround = true;
                    mc.thePlayer.fallDistance = 0;
                    mc.thePlayer.isCollidedVertically = true;
                }
            }
        }

        if(e instanceof EventCollide) {
            EventCollide event = (EventCollide) e;

            if(mode.is("Spartan")) {
                if (event.getBlock() instanceof BlockAir && event.getY() < mc.thePlayer.posY) {
                    event.setBoundingBox(new AxisAlignedBB((double) event.getX(), (double) event.getY(), (double) event.getZ(), event.getX() + 1.0, mc.thePlayer.posY, event.getZ() + 1.0));
                }
            }
        }

        if(e instanceof EventMove) {
            EventMove event = (EventMove) e;

            if(mode.is("Verus Float")) {
                if (mc.thePlayer.onGround) {
                    this.movementSpeed = 0.592D;
                    event.setY(0.44999998688697815D);
                    this.spoofGround = true;
                    this.verusStage = 0;
                } else if (this.verusStage <= 4) {
                    this.movementSpeed += 0.05D;
                    event.setY(0.0D);
                } else {
                    this.movementSpeed = 0.34D;
                    this.spoofGround = false;
                }
                ++this.verusStage;
                this.mc.thePlayer.motionY = event.getY();

                MovementUtils.setStrafeSpeed(event, this.movementSpeed - 1.0E-4D);
            }

            if(mode.is("Hylex")) {
                if(mc.thePlayer.ticksExisted % 2 == 0) MovementUtils.setStrafeSpeed(event, hylexSpeed.getValue()); else MovementUtils.setStrafeSpeed(event, 0);
            }

            if(mode.is("Spartan")) {
                if (!MovementUtils.isMovingOnGround()) {
                    BlockPos blockPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY - 1.0D, mc.thePlayer.posZ);
                    Vec3d vec = new Vec3d(blockPos).addVector(0.4D, 0.4D, 0.4D).mul(0.4F);
                    mc.playerController.onPlayerRightClick3d(mc.thePlayer, mc.theWorld, new ItemStack(Blocks.barrier), blockPos, EnumFacing.UP, vec);
                }

                MovementUtils.setStrafeSpeed(event, 0.3);
            }

            if(mode.is("Verus")) {
                if(mc.thePlayer.hurtTime > 0 && !damaged){
                    damaged = true;
                    mc.timer.timerSpeed = 1.0f;
                }

                if (!this.damaged) {
                    event.setX(0);
                    event.setZ(0);
                }
            }

            if(mode.is("Funcraft")) {
                if (MovementUtils.isMoving()) {
                    double baseMoveSpeed = MovementUtils.getBaseMoveSpeed();
                    switch (stage) {
                        case 0:
                            moveSpeed = baseMoveSpeed;
                            if (MovementUtils.isOnGround()) {
                                event.setY(mc.thePlayer.motionY = MovementUtils.getJumpHeight(MovementUtils.VANILLA_JUMP_HEIGHT));
                                if (funDamage.isEnabled() && MovementUtils.fallDistDamage() || mc.thePlayer.fallDistance >= 3.0F)
                                    moveSpeed = baseMoveSpeed * (MovementUtils.MAX_DIST * initialSpeed.getValue());
                            }
                            break;
                        case 1:
                            moveSpeed *= funSpeed.getValue();
                            double difference = reductionSpeed.getValue() * (moveSpeed - baseMoveSpeed);
                            moveSpeed = moveSpeed - difference;
                            break;
                        case 2:
                            double lastDif = reductionSpeed.getValue() * (lastDist - baseMoveSpeed);
                            moveSpeed = lastDist - lastDif;
                            break;
                        default:
                            moveSpeed = lastDist - lastDist / MovementUtils.BUNNY_DIV_FRICTION;
                            break;
                    }
                    MovementUtils.setStrafeSpeed(event, Math.max(baseMoveSpeed, moveSpeed));
                    stage++;
                }
            }

            if(mode.is("Motion")) {
                MovementUtils.setStrafeSpeed(event, motionSpeedProp.getValue());
            }
        }

        if(e instanceof EventMotion) {
            EventMotion event = (EventMotion) e;

            if(e.isPre()) {
                if(mode.is("Verus Float")) {
                    mc.getNetHandler().getNetworkManager().sendPacket(new C0CPacketInput());
                    event.setOnGround(this.mc.thePlayer.onGround || this.spoofGround);
                }

                if(mode.is("Survivaldub Old")) {
                    if(mc.thePlayer.onGround && ticks > 2) {
                        mc.thePlayer.setSpeed(0);
                        this.toggle();
                    }

                    mc.thePlayer.cameraYaw = 0.07F;
                    mc.thePlayer.motionY = 0;

                    if(MovementUtils.isMovingOnGround())
                        mc.thePlayer.jump();

                    if(MovementUtils.isMoving()) {
                        ticks++;
                        if(!mc.thePlayer.onGround && ticks > 1) {
                            if(moveSpeed < 1.05) {
                                //moveSpeed += 0.01F;
                                moveSpeed += (reduce.getValue() / 100);
                            }
                            mc.timer.timerSpeed = 1.0F;
                            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 1.0E-10, mc.thePlayer.posZ);
                            mc.thePlayer.setSpeed((float) (startSpeed.getValue() - moveSpeed));
                        }
                    } else {
                        MovementUtils.setSpeed(0);
                    }
                }

                if(mode.is("Hylex")) {
                    mc.thePlayer.motionY = 0;
                    ((EventMotion) e).setOnGround(true);
                    ((EventMotion) e).onGround = true;
                }

                if(mode.is("Funcraft")) {
                    if (timerProp.isEnabled()) {
                        if (funtimer.hasElapsed((long) timerDurationProp.getValue()))
                            mc.timer.timerSpeed = 1.0F;
                        else
                            mc.timer.timerSpeed = (float) timerSpeedProp.getValue();
                    }

                    event.setOnGround(true);

                    mc.thePlayer.motionY = 0.0D;
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + (mc.thePlayer.ticksExisted % 2 == 0 ? -0.0003F : 0.0003F), mc.thePlayer.posZ);

                    double xDist = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
                    double zDist = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;

                    lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

                    /*
                    double y;
                    double y1;
                    mc.thePlayer.motionY = 0;
                    if (mc.thePlayer.ticksExisted % 3 == 0) {
                        y = mc.thePlayer.posY - 1.0E-10D;
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, true));
                    }
                    y1 = mc.thePlayer.posY + 1.0E-10D;
                    mc.thePlayer.setPosition(mc.thePlayer.posX, y1, mc.thePlayer.posZ);
                     */
                }

                if(mode.is("NCP Dev")) {
                    MovementUtils.setMotion(MovementUtils.getBaseMoveSpeed() * 1);
                    if (mc.thePlayer.ticksExisted % 2 == 0) {
                        mc.thePlayer.sendQueue.addToSendQueueSilent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.28, mc.thePlayer.posZ, true));
                    } else {
                        mc.thePlayer.sendQueue.addToSendQueueSilent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.28, mc.thePlayer.posZ, false));

                    }
                    mc.thePlayer.sendQueue.addToSendQueueSilent(new C0CPacketInput(0, 0, true, true));
                    mc.thePlayer.motionY = 0;
                }

                if(mode.is("Vanilla")) {
                    mc.thePlayer.capabilities.isFlying = true;
                    mc.thePlayer.capabilities.allowFlying = true;
                }

                if(mode.is("Motion") || mode.is("Disabler5")) {
                    mc.thePlayer.fallDistance = 0.0f;
                    mc.thePlayer.motionX = 0.0;
                    mc.thePlayer.motionY = 0.0;
                    mc.thePlayer.motionZ = 0.0;

                    final EntityPlayerSP entityPlayerSP = mc.thePlayer;
                    entityPlayerSP.posY += 0.1;
                    entityPlayerSP.posY -= 0.1;

                    if(mode.is("Disabler5"))
                        MovementUtils.strafe((float)5);

                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        entityPlayerSP.motionY += 1.5;
                    }
                    if (this.mc.gameSettings.keyBindSneak.isKeyDown()) {
                        entityPlayerSP.motionY -= 1.5;
                    }
                }

                if(mode.is("Collision")) {
                    mc.thePlayer.motionY = 0;
                    MovementUtils.strafe();
                }

                if(mode.is("Verus Damage")) {
                    ((EventMotion) e).setOnGround(true);
                    ((EventMotion) e).onGround = true;
                    if(i > 0) {
                        MovementUtils.setMotion(verusSpeed.getValue());
                    }

                    i--;
                }

                if(mode.is("MushMC")) {
                    ((EventMotion) e).setOnGround(true);
                    ((EventMotion) e).onGround = true;
                    if(l > 0) {
                        MovementUtils.setMotion(verusSpeed.getValue());

                    }

                    l--;
                }

                if(mode.is("Verus")) {
                    event.setOnGround(true);
                    event.onGround = true;
                    MovementUtils.setMotion(verusSpeed.getValue());
                }
            }
        }
    }


    private final ArrayList<C03PacketPlayer> disablerC03List=new ArrayList<>();

    private void sendAAC5Packets(){
        float yaw=mc.thePlayer.rotationYaw;
        float pitch=mc.thePlayer.rotationPitch;
        for(C03PacketPlayer packet : disablerC03List){
            mc.getNetHandler().sendPacketNoEvent(packet);
            if(packet.isMoving()){
                if(packet.getRotating()){
                    yaw = packet.yaw;
                    pitch = packet.pitch;
                }

                if(disabler5C04.isEnabled()) {
                    mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(packet.x,1e+159,packet.z, true));
                    mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(packet.x,packet.y,packet.z, true));
                    System.out.println(true);
                } else {
                    mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(packet.x,1e+159,packet.z, yaw, pitch, true));
                    mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(packet.x,packet.y,packet.z, yaw, pitch, true));
                }

            }
        }
        disablerC03List.clear();
    }
}
