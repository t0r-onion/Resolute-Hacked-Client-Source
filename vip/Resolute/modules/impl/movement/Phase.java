package vip.Resolute.modules.impl.movement;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.*;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.util.misc.TimerUtil;
import vip.Resolute.util.movement.MovementUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockHopper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class Phase extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Skip", "AAC 4.4.2", "Skip", "Watchdog", "Full", "Pos", "Aris");

    private double distance;
    private int delay;
    boolean shouldSpeed = false;
    double rot1, rot2;
    float yaw, pitch;

    private TimerUtil timer = new TimerUtil();

    public Phase() {
        super("Phase", 0, "Allows you to phase through blocks", Category.MOVEMENT);
        this.addSettings(mode);
    }

    public void onEnable() {
        mc.timer.timerSpeed = 1.0f;
        shouldSpeed = isInsideBlock();
        Step.cancelStep = true;
        distance = 1.2;
    }
    public void onDisable() {
        Step.cancelStep = false;
        mc.timer.timerSpeed = 1.0f;
        delay = 0;
    }

    public void onEvent(Event e) {
        if(e instanceof EventTick) {
            if(mode.is("Pos")) {
                if (mc.thePlayer.isCollidedHorizontally && mc.gameSettings.keyBindSprint.isKeyDown()) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.05, mc.thePlayer.posZ, true));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.05, mc.thePlayer.posZ, true));
                }
            }
        }

        if(e instanceof EventPacket) {
            if(((EventPacket) e).getPacket() instanceof C03PacketPlayer) {
                if(mode.is("Pos")) {
                    if(!MovementUtils.isMoving() && mc.thePlayer.posY == mc.thePlayer.lastTickPosY) {
                        e.setCancelled(true);
                    }
                }
            }
        }

        if(e instanceof EventMotion) {
            EventMotion eventMotion = (EventMotion) e;
            if(e.isPre()) {
                if(mode.is("Full")) {
                    if(shouldSpeed || isInsideBlock()){
                        if(!mc.thePlayer.isSneaking())
                            mc.thePlayer.lastReportedPosY = 0;
                        mc.thePlayer.lastReportedPitch = 999;
                        mc.thePlayer.onGround = false;
                        mc.thePlayer.noClip = true;
                        mc.thePlayer.motionX = 0;
                        mc.thePlayer.motionZ = 0;
                        if(mc.gameSettings.keyBindJump.isKeyDown() &&  mc.thePlayer.posY == (int)mc.thePlayer.posY)
                            mc.thePlayer.jump();

                        mc.thePlayer.jumpMovementFactor = 0;
                    }

                    rot1 ++;
                    if(rot1 < 3){
                        if(rot1 == 1){
                            pitch += 15;
                        }else{
                            pitch -= 15;
                        }
                    }
                    if(mc.gameSettings.keyBindSneak.isKeyDown()){
                        mc.thePlayer.lastReportedPitch = 999;
                        double X = mc.thePlayer.posX; double Y = mc.thePlayer.posY; double Z = mc.thePlayer.posZ;
                        if(!MovementUtils.isMoving())
                            if(MovementUtils.isOnGround(0.001) && !isInsideBlock()){
                                mc.thePlayer.lastReportedPosY = -99;
                                eventMotion.setY(Y-1);
                                mc.thePlayer.setPosition(X, Y-1, Z);
                                timer.reset();
                                mc.thePlayer.motionY = 0;
                            }else if(timer.hasElapsed(100) && mc.thePlayer.posY == (int)mc.thePlayer.posY){
                                mc.thePlayer.setPosition(X, Y - 0.3, Z);
                            }

                    }
                    if(isInsideBlock() && rot1 >= 3){
                        if(shouldSpeed){
                            teleport(0.617);

                            float sin = (float)Math.sin(rot2) * 0.1f;
                            float cos = (float)Math.cos(rot2) * 0.1f;
                            mc.thePlayer.rotationYaw += sin;
                            mc.thePlayer.rotationPitch += cos;
                            rot2 ++;
                        }else{
                            teleport(0.031);
                        }
                    }
                }
            }

            if(e.isPre()) {
                if(mode.is("Aris")) {
                    if(mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround && MovementUtils.isMoving()) {
                        final float yaw = eventMotion.getYaw();
                        mc.thePlayer.boundingBox.offsetAndUpdate(distance * Math.cos(Math.toRadians(yaw + 90.0f)), 0.0, distance * Math.sin(Math.toRadians(yaw + 90.0f)));
                    }
                }
            }
        }

        if(e instanceof EventCollide) {
            if(mode.is("Watchdog")) e.setCancelled(true);
        }

        if(e instanceof EventMove) {
            EventMove eventMove = (EventMove) e;
            if(mode.is("Pos")) {
                if (isInsideBlock()) {
                    if (mc.gameSettings.keyBindJump.isKeyDown())
                        eventMove.setY(mc.thePlayer.motionY += 0.09f);
                    else if (mc.gameSettings.keyBindSneak.isKeyDown())
                        eventMove.setY(mc.thePlayer.motionY -= 0.00);
                    else
                        eventMove.setY(mc.thePlayer.motionY = 0.0f);
                    setMoveSpeed(eventMove, 0.3);
                }
            }

            if(mode.is("Watchdog")) {
                if (mc.thePlayer.isCollidedHorizontally) {
                    for(int i = 0; i < 4; i++) {
                        double[] push = MovementUtils.yawPos(0.05);
                        mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + push[0], mc.thePlayer.posY, mc.thePlayer.posZ + push[1], mc.thePlayer.onGround));
                    }
                }
                if(MovementUtils.isInsideBlock()) {
                    double[] push = MovementUtils.yawPos(0.74);
                    eventMove.setY(mc.thePlayer.motionY = 1.1E-2 * 1.3);
                    mc.thePlayer.getEntityBoundingBox().offsetAndUpdate(push[0], 0, push[1]);
                }
            }

            if(mode.is("Skip")) {
                if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround && MovementUtils.isMoving()) {
                    if (mc.timer.timerSpeed == 0.2F) {
                        final float var2 = getDirection();
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + (mc.thePlayer.motionX * 0.3925), mc.thePlayer.posY, mc.thePlayer.posZ + (mc.thePlayer.motionZ * 0.3925), mc.thePlayer.onGround));
                        mc.thePlayer.setPosition(mc.thePlayer.posX + 0.8420 * Math.cos(Math.toRadians(var2 + 90.0f)), mc.thePlayer.posY, mc.thePlayer.posZ + 0.8420 * Math.sin(Math.toRadians(var2 + 90.0f)));
                        for (int i = 0; i < 2; i++) {
                            if (isInsideBlock()) {
                                mc.timer.timerSpeed = 1F;
                                mc.thePlayer.setPosition(mc.thePlayer.posX + 0.3520 * Math.cos(Math.toRadians(var2 + 90.0f)), mc.thePlayer.posY, mc.thePlayer.posZ + 0.3520 * Math.sin(Math.toRadians(var2 + 90.0f)));
                            }
                        }
                        eventMove.setY(0);
                    } else {
                        mc.thePlayer.moveForward *= 0.2F;
                        mc.thePlayer.moveStrafing *= 0.2F;
                        mc.timer.timerSpeed = 0.2F;
                        mc.thePlayer.cameraPitch += 18;
                        mc.thePlayer.cameraYaw += 1;
                    }
                } else {
                    mc.timer.timerSpeed = 1F;
                }
            }
        }

        if(e instanceof EventUpdate) {


            if(mode.is("Pos")) {
                double multiplier = 0.3;
                double mx = -Math.sin(Math.toRadians(mc.thePlayer.rotationYaw));
                double mz = Math.cos(Math.toRadians(mc.thePlayer.rotationYaw));
                double x = mc.thePlayer.movementInput.moveForward * multiplier * mx + mc.thePlayer.movementInput.moveStrafe * multiplier * mz;
                double z = mc.thePlayer.movementInput.moveForward * multiplier * mz - mc.thePlayer.movementInput.moveStrafe * multiplier * mx;
                if (mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isOnLadder()) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z, false));
                    for (int i = 1; i < 10; ++i) {
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 8.988465674311579E307, mc.thePlayer.posZ, false));
                    }
                    mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
                }

                final float dist = 2.0f;
                if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.moveForward != 0.0f) {
                    ++this.delay;
                    final String lowerCase;
                    switch (lowerCase = mc.getRenderViewEntity().getHorizontalFacing().name().toLowerCase()) {
                        case "east": {
                            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                                    mc.thePlayer.posX + 9.999999747378752E-6, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                            break;
                        }
                        case "west": {
                            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                                    mc.thePlayer.posX - 9.999999747378752E-6, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                            break;
                        }
                        case "north": {
                            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,
                                    mc.thePlayer.posY, mc.thePlayer.posZ - 9.999999747378752E-6, false));
                            break;
                        }
                        case "south": {
                            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,
                                    mc.thePlayer.posY, mc.thePlayer.posZ + 9.999999747378752E-6, false));
                            break;
                        }
                        default:
                            break;
                    }
                    if (this.delay >= 1) {
                        final String lowerCase2;
                        switch (lowerCase2 = mc.getRenderViewEntity().getHorizontalFacing().name().toLowerCase()) {
                            case "east": {
                                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                                        mc.thePlayer.posX + 2.0, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                                break;
                            }
                            case "west": {
                                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                                        mc.thePlayer.posX - 2.0, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                                break;
                            }
                            case "north": {
                                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,
                                        mc.thePlayer.posY, mc.thePlayer.posZ - 2.0, false));
                                break;
                            }
                            case "south": {
                                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,
                                        mc.thePlayer.posY, mc.thePlayer.posZ + 2.0, false));
                                break;
                            }
                            default:
                                break;
                        }
                        this.delay = 0;
                    }
                }
            }

            if(e.isPre()) {
                if(mode.is("AAC 4.4.2")) {
                    if(mc.thePlayer.isCollidedHorizontally) {
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY - 1.0E-8D, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY - 1.0E-6D, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                    }
                }

                if(mode.is("Dev")) {
                    mc.thePlayer.setSneaking(true);
                    if (mc.thePlayer.isCollidedHorizontally) {
                        mc.thePlayer.setPosition(mc.thePlayer.posX + -Math.sin(Math.toRadians((Minecraft.getMinecraft()).thePlayer.rotationYaw)) * 0.01, mc.thePlayer.posY, mc.thePlayer.posZ + Math.cos(Math.toRadians((Minecraft.getMinecraft()).thePlayer.rotationYaw)) * 0.01);
                    } else if (isInsideBlock()) {
                        mc.thePlayer.setPosition(mc.thePlayer.posX + -Math.sin(Math.toRadians((Minecraft.getMinecraft()).thePlayer.rotationYaw)) * 0.3, mc.thePlayer.posY, mc.thePlayer.posZ + Math.cos(Math.toRadians((Minecraft.getMinecraft()).thePlayer.rotationYaw)) * 0.3);
                        MovementUtils.setSpeed(0);
                    }
                }
            }
        }
    }

    private void setMoveSpeed(EventMove event, double speed) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        float yaw = mc.thePlayer.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
        } else {
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
            event.setX(
                    forward * speed * -Math.sin(Math.toRadians(yaw)) + strafe * speed * Math.cos(Math.toRadians(yaw)));
            event.setZ(
                    forward * speed * Math.cos(Math.toRadians(yaw)) - strafe * speed * -Math.sin(Math.toRadians(yaw)));
        }
    }

    private void teleport(double dist) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        float yaw = mc.thePlayer.rotationYaw;
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
        double x = mc.thePlayer.posX; double y = mc.thePlayer.posY; double z = mc.thePlayer.posZ;
        double xspeed = forward * dist * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * dist * Math.sin(Math.toRadians(yaw + 90.0F));
        double zspeed = forward * dist * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * dist * Math.cos(Math.toRadians(yaw + 90.0F));
        mc.thePlayer.setPosition(x + xspeed, y,  z + zspeed);

    }


    private float getDirection() {
        float direction = mc.thePlayer.rotationYaw;
        boolean back =mc.gameSettings.keyBindBack.isKeyDown() && !mc.gameSettings.keyBindForward.isKeyDown();
        boolean forward =!mc.gameSettings.keyBindBack.isKeyDown() && mc.gameSettings.keyBindForward.isKeyDown();
        if (mc.gameSettings.keyBindLeft.isKeyDown()) {
            direction -= (back ? 135 : (forward ? 45 : 90));
        } if (mc.gameSettings.keyBindRight.isKeyDown()) {
            direction += (back ? 135 : (forward ? 45 : 90));
        }
        if (back && direction == mc.thePlayer.rotationYaw) {
            direction += 180F;
        }
        return direction;
    }


    public boolean isInsideBlock() {
        for (int x = MathHelper.floor_double(Minecraft.getMinecraft().thePlayer.boundingBox.minX); x < MathHelper.floor_double(Minecraft.getMinecraft().thePlayer.boundingBox.maxX) + 1; x++) {
            for (int y = MathHelper.floor_double(Minecraft.getMinecraft().thePlayer.boundingBox.minY); y < MathHelper.floor_double(Minecraft.getMinecraft().thePlayer.boundingBox.maxY) + 1; y++) {
                for (int z = MathHelper.floor_double(Minecraft.getMinecraft().thePlayer.boundingBox.minZ); z < MathHelper.floor_double(Minecraft.getMinecraft().thePlayer.boundingBox.maxZ) + 1; z++) {
                    Block block = Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block != null && !(block instanceof BlockAir)) {
                        AxisAlignedBB boundingBox = block.getCollisionBoundingBox(Minecraft.getMinecraft().theWorld, new BlockPos(x, y, z), Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(x, y, z)));
                        if (block instanceof BlockHopper)
                            boundingBox = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
                        if (boundingBox != null && Minecraft.getMinecraft().thePlayer.boundingBox.intersectsWith(boundingBox))
                            return true;
                    }
                }
            }
        }
        return false;
    }
}
