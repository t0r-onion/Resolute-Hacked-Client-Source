package vip.Resolute.modules.impl.movement;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventSafeWalk;
import vip.Resolute.events.impl.EventUpdate;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.movement.MovementUtils;
import vip.Resolute.util.player.RotationUtils;
import vip.Resolute.util.misc.TimerUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class ScaffoldOld extends Module {

    public static ModeSetting mode = new ModeSetting("Mode", "NCP", "NCP", "AAC4.4.2", "AAC5", "Snap");
    public static ModeSetting towerMode = new ModeSetting("Tower", "NCP", "NCP", "None");
    public NumberSetting timerBoost = new NumberSetting("Timer Boost", 1.0, 1.0, 2.0, 0.1);
    public BooleanSetting safeWalk = new BooleanSetting("SafeWalk", true);
    public BooleanSetting swing = new BooleanSetting("Swing", true);
    public BooleanSetting silent = new BooleanSetting("Silent", true);
    public BooleanSetting sprint = new BooleanSetting("Sprint", true);

    public static TimerUtils timer = new TimerUtils();
    public static int heldItem = 0;

    public float pitch;
    public float yaw;

    BlockData blockData;

    private TimerUtils towerStopwatch = new TimerUtils();
    private int slot;

    public ScaffoldOld() {
        super("ScaffoldOld", Keyboard.KEY_NONE, "Automatically places blocks under you", Category.MOVEMENT);
        this.addSettings(mode, towerMode, safeWalk, silent, swing);
        this.towerStopwatch = new TimerUtils();
    }

    public void onEnable() {
        super.onEnable();
        if (mc.thePlayer != null) {
            heldItem = mc.thePlayer.inventory.currentItem;
        }
        this.towerStopwatch.reset();
        yaw = mc.thePlayer.rotationYaw;
        this.slot = mc.thePlayer.inventory.currentItem;

        this.blockData = null;
    }

    public void onDisable() {
        super.onDisable();
        mc.gameSettings.keyBindSneak.pressed = false;
        mc.thePlayer.movementInput.sneak = false;
        mc.thePlayer.inventory.currentItem = heldItem;
        mc.thePlayer.inventory.currentItem = this.slot;
        mc.timer.timerSpeed = 1.0f;
        this.blockData = null;
    }

    public void onEvent(Event e) {
        this.setSuffix(mode.getMode());
        if(e instanceof EventSafeWalk) {
            if(safeWalk.isEnabled()) {
                e.setCancelled(true);
            }
        }
        /*
        if(e instanceof EventRender2D) {
            ScaledResolution sr = new ScaledResolution(mc);
            int blockCount = 0;
            for (int i = 0; i < 45; ++i) {
                if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack())
                    continue;
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();
                if (!(is.getItem() instanceof ItemBlock) || !isValidBlock(((ItemBlock) item).getBlock(), false))
                    continue;
                blockCount += is.stackSize;
            }

            x = sr.getScaledWidth() / 2.0f;
            y = sr.getScaledHeight() / 2.0f + 15.0f;
            percentage = Math.min(1.0f, blockCount / 128.0f);
            width = 80.0f;
            half = width / 2.0f;

            GL11.glEnable(3089);
            RenderUtils.startScissorBox(sr, (int)(x - half), (int)y - 2, (int)(width * percentage), 4);
            RenderUtils.drawGradientRect(x - half, y - 1.5f, x - half + width, y + 1.5f, true, 0xFF718deb, 0xFF718deb);
            GL11.glDisable(3089);
        }

         */

        if(e instanceof EventUpdate) {
            BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY - 1, mc.thePlayer.posZ);
            getYaw(pos);
        }

        if (e instanceof EventMotion) {

            if(e.isPre())
                mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));

            EventMotion event = (EventMotion)e;
            //System.out.println(event.getYaw());
            mc.thePlayer.setSprinting(false);
            double x = mc.thePlayer.posX;
            double z = mc.thePlayer.posZ;

            if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)).getBlock().getMaterial().isReplaceable()) {
                x = mc.thePlayer.posX;
                z = mc.thePlayer.posZ;
            }
            final BlockPos underPos = new BlockPos(x, mc.thePlayer.posY - 1, z);
            final BlockData data = getBlockData(underPos);

            if (getBlockSlot() == -1) {
                return;
            }


            //mc.timer.timerSpeed = (float) timerBoost.getValue();

            if (e.isPre() && getBlockSlot() != -1 && mc.gameSettings.keyBindJump.isPressed() && !MovementUtils.isMoving() && towerMode.is("NCP")) {
                MovementUtils.setSpeed(0);
                if (mc.thePlayer.onGround) {
                    if (MovementUtils.isOnGround(0.76) && !MovementUtils.isOnGround(0.75) && mc.thePlayer.motionY > 0.23 && mc.thePlayer.motionY < 0.25) {
                        mc.thePlayer.motionY = Math.round(mc.thePlayer.posY) - mc.thePlayer.posY;
                    }
                    if (MovementUtils.isOnGround(1.0E-4)) {
                        mc.thePlayer.motionY = 0.41999998688697815;
                        if (timer.hasTimeElapsed(1500, false)) {
                            mc.thePlayer.motionY = -0.28;
                            timer.reset();
                        }
                    } else if (mc.thePlayer.posY >= Math.round(mc.thePlayer.posY) - 1.0E-4 && mc.thePlayer.posY <= Math.round(mc.thePlayer.posY) + 1.0E-4) {
                        mc.thePlayer.motionY = 0.0;
                    }
                } else if (mc.theWorld.getBlockState(underPos).getBlock().getMaterial().isReplaceable() && data != null) {
                    mc.thePlayer.motionY = 0.41955;
                }
            }

            if (mc.theWorld.getBlockState(underPos).getBlock().getMaterial().isReplaceable() && data != null) {

                if (e.isPre()) {
                    BlockPos pos = new BlockPos(this.mc.thePlayer.posX, (this.mc.thePlayer.posY - 1), this.mc.thePlayer.posZ);
                    BlockData blockData = getBlockData(pos);
                    this.blockData = blockData;


                    if(mode.is("AAC4.4.2") || mode.is("AAC5")) {
                        event.setPitch(72);
                        event.setYaw(yaw);

                        //mc.thePlayer.rotationYawHead = mc.thePlayer.rotationYaw + 180;
                        //mc.thePlayer.renderYawOffset = mc.thePlayer.rotationYaw + 180;
                    }


                    if(mode.is("NCP")) {
                        //event.setYaw(mc.thePlayer.rotationYaw + RandomUtils.nextFloat(178, 179));
                        //event.setPitch(70f);



                        event.setYaw(yaw);
                        event.setPitch(72);

                        //mc.thePlayer.rotationYawHead = mc.thePlayer.rotationYaw + RandomUtils.nextFloat(178, 179);
                    }

                    if (data.face == EnumFacing.UP) {
                        mc.timer.timerSpeed = 1;
                        event.setPitch(90f);
                    }

                    if(!silent.isEnabled()) {

                    }



                    if(mode.is("Snap")) {
                        float[] facing = getRotationsAAC(blockData.position, blockData.face);



                        float yaw = facing[0];
                        float pitch = facing[1];

                        event.setYaw(yaw);
                        event.setPitch(pitch);

                        //mc.thePlayer.rotationYawHead = yaw;
                        //mc.thePlayer.renderYawOffset = yaw;
                    }


                } else if (getBlockSlot() != -1) {
                    mc.thePlayer.inventory.currentItem = getBlockSlot();

                    if(mode.is("Snap")) {
                        float[] facing = getRotationsAAC(blockData.position, blockData.face);

                        float yaw = facing[0];
                        float pitch = facing[1];

                        event.setYaw(yaw);
                        event.setPitch(pitch);

                        //mc.thePlayer.rotationYawHead = yaw;
                        //mc.thePlayer.renderYawOffset = yaw;
                    }

                    if(mode.is("AAC5")) {
                        if(mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0, -0.5, 0).expand(-0.1, 0, -0.1)).isEmpty()) {
                            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), data.position, data.face, getVec3(data.position, data.face));

                            if (swing.isEnabled()) {
                                mc.thePlayer.swingItem();
                            } else {
                                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                            }
                        }
                    } else {
                        mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), data.position, data.face, getVec3(data.position, data.face));

                        if (swing.isEnabled()) {
                            mc.thePlayer.swingItem();
                        } else {
                            mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                        }
                    }
                }
            } else {
                if(mode.is("AAC4.4.2") || mode.is("AAC5")) {
                    event.setYaw(mc.thePlayer.rotationYaw + 180);
                    event.setPitch(80);

                    //mc.thePlayer.rotationYawHead = mc.thePlayer.rotationYaw + 180;
                    //mc.thePlayer.renderYawOffset = mc.thePlayer.rotationYaw + 180;
                }

                if(mode.is("NCP")) {
                    event.setYaw(yaw);
                    event.setPitch(72);

                    //mc.thePlayer.rotationYawHead = mc.thePlayer.rotationYaw + RandomUtils.nextFloat(178, 179);
                    //mc.thePlayer.renderYawOffset = mc.thePlayer.rotationYaw + RandomUtils.nextFloat(178, 179);
                }

                if(mode.is("Snap")) {
                    //event.setYaw(prevYaw);
                    //event.setPitch(prevPitch);

                   // mc.thePlayer.rotationYawHead = prevYaw;
                   // mc.thePlayer.renderYawOffset = prevYaw;
                }


                if(!silent.isEnabled()) {

                }
            }
        }
    }

    private class BlockData {
        public BlockPos position;
        public EnumFacing face;

        private BlockData(final BlockPos position, final EnumFacing face, final BlockData blockData) {
            this.position = position;
            this.face = face;
        }
    }

    public static boolean isValidBlock(Block block, boolean toPlace) {
        if (block instanceof BlockContainer)
            return false;
        if (toPlace) {
            return !(block instanceof BlockFalling) && block.isFullBlock() && block.isFullCube();
        } else {
            final Material material = block.getMaterial();
            return !material.isReplaceable() && !material.isLiquid();
        }
    }

    public static float[] getRotations(BlockPos block, EnumFacing face) {
        double x = block.getX() + 0.5D - mc.thePlayer.posX + face.getFrontOffsetX() / 2.0D;
        double z = block.getZ() + 0.5D - mc.thePlayer.posZ + face.getFrontOffsetZ() / 2.0D;
        double y = block.getY() + 0.5D + (face.getFrontOffsetZ() / 2);
        double d1 = mc.thePlayer.posY + mc.thePlayer.getEyeHeight() - y;
        double d3 = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float)(Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float)(Math.atan2(d1, d3) * 180.0D / Math.PI);
        if (yaw < 0.0F)
            yaw += 360.0F;
        return new float[] { yaw, pitch };
    }

    private static List<Block> invalidBlocks = Arrays.asList(Blocks.enchanting_table, Blocks.carpet, Blocks.glass_pane,
            Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava,
            Blocks.flowing_lava, Blocks.snow_layer, Blocks.chest, Blocks.torch, Blocks.anvil, Blocks.trapped_chest,
            Blocks.noteblock, Blocks.jukebox, Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate,
            Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.stone_button,
            Blocks.wooden_button, Blocks.lever, Blocks.crafting_table, Blocks.furnace, Blocks.stone_slab,
            Blocks.wooden_slab, Blocks.stone_slab2, Blocks.brown_mushroom, Blocks.red_mushroom, Blocks.red_flower,
            Blocks.yellow_flower, Blocks.flower_pot);

    private int getBlockSlot() {
        int item = -1;
        int stacksize = 0;
        for (int i = 36; i < 45; ++i) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getStack() != null && mc.thePlayer.inventoryContainer.getSlot(i).getStack().getItem() instanceof ItemBlock && !invalidBlocks.contains(((ItemBlock) mc.thePlayer.inventoryContainer.getSlot(i).getStack().getItem()).getBlock()) && mc.thePlayer.inventoryContainer.getSlot(i).getStack().stackSize >= stacksize) {
                item = i - 36;
                stacksize = mc.thePlayer.inventoryContainer.getSlot(i).getStack().stackSize;
            }
        }
        return item;
    }

    public Vec3 getVec3(final BlockPos pos, final EnumFacing face) {
        double x = pos.getX() + 0.500;
        double y = pos.getY() + 0.500;
        double z = pos.getZ() + 0.500;
        x += face.getFrontOffsetX() / 2.0;
        z += face.getFrontOffsetZ() / 2.0;
        y += face.getFrontOffsetY() / 2.0;
        if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
            x += (new Random().nextDouble() / 2) - 0.25;
            z += (new Random().nextDouble() / 2) - 0.25;
        } else {
            y += (new Random().nextDouble() / 2) - 0.25;
        }
        if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
            z += (new Random().nextDouble() / 2) - 0.25;
        }
        if (face == EnumFacing.SOUTH || face == EnumFacing.NORTH) {
            x += (new Random().nextDouble() / 2) - 0.25;
        }
        return new Vec3(x, y, z);
    }


    private boolean isValidItem(Item item) {
        if (item instanceof ItemBlock) {
            ItemBlock iBlock = (ItemBlock)item;
            Block block = iBlock.getBlock();
            return !this.invalidBlocks.contains(block);
        } else {
            return false;
        }
    }



    public static boolean isPosSolid(BlockPos pos) {
        Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
        if ((block.getMaterial().isSolid() || !block.isTranslucent() || block instanceof BlockLadder || block instanceof BlockCarpet
                || block instanceof BlockSnow || block instanceof BlockSkull)
                && !block.getMaterial().isLiquid() && !(block instanceof BlockContainer)) {
            return true;
        }
        return false;
    }

    public float[] getRotationsAAC(BlockPos block, EnumFacing face) {
        double x = block.getX() + 0.5D - this.mc.thePlayer.posX + face.getFrontOffsetX() / 2.0D;
        double z = block.getZ() + 0.5D - this.mc.thePlayer.posZ + face.getFrontOffsetZ() / 2.0D;
        double y = block.getY() + 0.5D + (face.getFrontOffsetZ() / 2);
        y += 0.5D;
        double d1 = this.mc.thePlayer.posY + this.mc.thePlayer.getEyeHeight() - y;
        double d3 = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float)(Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float)(Math.atan2(d1, d3) * 180.0D / Math.PI);
        if (yaw < 0.0F)
            yaw += 360.0F;
        return new float[] { yaw, pitch };
    }

    private BlockData getBlockData(final BlockPos pos) {
        if (isPosSolid(pos.add(0, -1, 0))) {
            return new BlockData(pos.add(0, -1, 0), EnumFacing.UP, (BlockData) null);
        }
        if (isPosSolid(pos.add(-1, 0, 0))) {
            return new BlockData(pos.add(-1, 0, 0), EnumFacing.EAST, (BlockData) null);
        }
        if (isPosSolid(pos.add(1, 0, 0))) {
            return new BlockData(pos.add(1, 0, 0), EnumFacing.WEST, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, 1))) {
            return new BlockData(pos.add(0, 0, 1), EnumFacing.NORTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, -1))) {
            return new BlockData(pos.add(0, 0, -1), EnumFacing.SOUTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(-1, 0, 0).add(0, -1, 0))) {
            return new BlockData(pos.add(-1, 0, 0).add(0, -1, 0), EnumFacing.UP, (BlockData) null);
        }
        if (isPosSolid(pos.add(-1, 0, 0).add(-1, 0, 0))) {
            return new BlockData(pos.add(-1, 0, 0).add(-1, 0, 0), EnumFacing.EAST, (BlockData) null);
        }
        if (isPosSolid(pos.add(-1, 0, 0).add(1, 0, 0))) {
            return new BlockData(pos.add(-1, 0, 0).add(1, 0, 0), EnumFacing.WEST, (BlockData) null);
        }
        if (isPosSolid(pos.add(-1, 0, 0).add(0, 0, 1))) {
            return new BlockData(pos.add(-1, 0, 0).add(0, 0, 1), EnumFacing.NORTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(-1, 0, 0).add(0, 0, -1))) {
            return new BlockData(pos.add(-1, 0, 0).add(0, 0, -1), EnumFacing.SOUTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(1, 0, 0).add(0, -1, 0))) {
            return new BlockData(pos.add(1, 0, 0).add(0, -1, 0), EnumFacing.UP, (BlockData) null);
        }
        if (isPosSolid(pos.add(1, 0, 0).add(-1, 0, 0))) {
            return new BlockData(pos.add(1, 0, 0).add(-1, 0, 0), EnumFacing.EAST, (BlockData) null);
        }
        if (isPosSolid(pos.add(1, 0, 0).add(1, 0, 0))) {
            return new BlockData(pos.add(1, 0, 0).add(1, 0, 0), EnumFacing.WEST, (BlockData) null);
        }
        if (isPosSolid(pos.add(1, 0, 0).add(0, 0, 1))) {
            return new BlockData(pos.add(1, 0, 0).add(0, 0, 1), EnumFacing.NORTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(1, 0, 0).add(0, 0, -1))) {
            return new BlockData(pos.add(1, 0, 0).add(0, 0, -1), EnumFacing.SOUTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, 1).add(0, -1, 0))) {
            return new BlockData(pos.add(0, 0, 1).add(0, -1, 0), EnumFacing.UP, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, 1).add(-1, 0, 0))) {
            return new BlockData(pos.add(0, 0, 1).add(-1, 0, 0), EnumFacing.EAST, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, 1).add(1, 0, 0))) {
            return new BlockData(pos.add(0, 0, 1).add(1, 0, 0), EnumFacing.WEST, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, 1).add(0, 0, 1))) {
            return new BlockData(pos.add(0, 0, 1).add(0, 0, 1), EnumFacing.NORTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, 1).add(0, 0, -1))) {
            return new BlockData(pos.add(0, 0, 1).add(0, 0, -1), EnumFacing.SOUTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, -1).add(0, -1, 0))) {
            return new BlockData(pos.add(0, 0, -1).add(0, -1, 0), EnumFacing.UP, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, -1).add(-1, 0, 0))) {
            return new BlockData(pos.add(0, 0, -1).add(-1, 0, 0), EnumFacing.EAST, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, -1).add(1, 0, 0))) {
            return new BlockData(pos.add(0, 0, -1).add(1, 0, 0), EnumFacing.WEST, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, -1).add(0, 0, 1))) {
            return new BlockData(pos.add(0, 0, -1).add(0, 0, 1), EnumFacing.NORTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, -1).add(0, 0, -1))) {
            return new BlockData(pos.add(0, 0, -1).add(0, 0, -1), EnumFacing.SOUTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(-1, 0, 0).add(0, -1, 0))) {
            return new BlockData(pos.add(-1, 0, 0).add(0, -1, 0), EnumFacing.UP, (BlockData) null);
        }
        if (isPosSolid(pos.add(-1, 0, 0).add(-1, 0, 0))) {
            return new BlockData(pos.add(-1, 0, 0).add(-1, 0, 0), EnumFacing.EAST, (BlockData) null);
        }
        if (isPosSolid(pos.add(-1, 0, 0).add(1, 0, 0))) {
            return new BlockData(pos.add(-1, 0, 0).add(1, 0, 0), EnumFacing.WEST, (BlockData) null);
        }
        if (isPosSolid(pos.add(-1, 0, 0).add(0, 0, 1))) {
            return new BlockData(pos.add(-1, 0, 0).add(0, 0, 1), EnumFacing.NORTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(-1, 0, 0).add(0, 0, -1))) {
            return new BlockData(pos.add(-1, 0, 0).add(0, 0, -1), EnumFacing.SOUTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(1, 0, 0).add(0, -1, 0))) {
            return new BlockData(pos.add(1, 0, 0).add(0, -1, 0), EnumFacing.UP, (BlockData) null);
        }
        if (isPosSolid(pos.add(1, 0, 0).add(-1, 0, 0))) {
            return new BlockData(pos.add(1, 0, 0).add(-1, 0, 0), EnumFacing.EAST, (BlockData) null);
        }
        if (isPosSolid(pos.add(1, 0, 0).add(1, 0, 0))) {
            return new BlockData(pos.add(1, 0, 0).add(1, 0, 0), EnumFacing.WEST, (BlockData) null);
        }
        if (isPosSolid(pos.add(1, 0, 0).add(0, 0, 1))) {
            return new BlockData(pos.add(1, 0, 0).add(0, 0, 1), EnumFacing.NORTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(1, 0, 0).add(0, 0, -1))) {
            return new BlockData(pos.add(1, 0, 0).add(0, 0, -1), EnumFacing.SOUTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, 1).add(0, -1, 0))) {
            return new BlockData(pos.add(0, 0, 1).add(0, -1, 0), EnumFacing.UP, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, 1).add(-1, 0, 0))) {
            return new BlockData(pos.add(0, 0, 1).add(-1, 0, 0), EnumFacing.EAST, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, 1).add(1, 0, 0))) {
            return new BlockData(pos.add(0, 0, 1).add(1, 0, 0), EnumFacing.WEST, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, 1).add(0, 0, 1))) {
            return new BlockData(pos.add(0, 0, 1).add(0, 0, 1), EnumFacing.NORTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, 1).add(0, 0, -1))) {
            return new BlockData(pos.add(0, 0, 1).add(0, 0, -1), EnumFacing.SOUTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, -1).add(0, -1, 0))) {
            return new BlockData(pos.add(0, 0, -1).add(0, -1, 0), EnumFacing.UP, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, -1).add(-1, 0, 0))) {
            return new BlockData(pos.add(0, 0, -1).add(-1, 0, 0), EnumFacing.EAST, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, -1).add(1, 0, 0))) {
            return new BlockData(pos.add(0, 0, -1).add(1, 0, 0), EnumFacing.WEST, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, -1).add(0, 0, 1))) {
            return new BlockData(pos.add(0, 0, -1).add(0, 0, 1), EnumFacing.NORTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, 0, -1).add(0, 0, -1))) {
            return new BlockData(pos.add(0, 0, -1).add(0, 0, -1), EnumFacing.SOUTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, -1, 0).add(0, -1, 0))) {
            return new BlockData(pos.add(0, -1, 0).add(0, -1, 0), EnumFacing.UP, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, -1, 0).add(-1, 0, 0))) {
            return new BlockData(pos.add(0, -1, 0).add(-1, 0, 0), EnumFacing.EAST, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, -1, 0).add(1, 0, 0))) {
            return new BlockData(pos.add(0, -1, 0).add(1, 0, 0), EnumFacing.WEST, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, -1, 0).add(0, 0, 1))) {
            return new BlockData(pos.add(0, -1, 0).add(0, 0, 1), EnumFacing.NORTH, (BlockData) null);
        }
        if (isPosSolid(pos.add(0, -1, 0).add(0, 0, -1))) {
            return new BlockData(pos.add(0, -1, 0).add(0, 0, -1), EnumFacing.SOUTH, (BlockData) null);
        }
        final BlockPos pos2 = pos.add(0, -1, 0).add(1, 0, 0);
        final BlockPos pos3 = pos.add(0, -1, 0).add(0, 0, 1);
        final BlockPos pos4 = pos.add(0, -1, 0).add(-1, 0, 0);
        final BlockPos pos5 = pos.add(0, -1, 0).add(0, 0, -1);
        if (isPosSolid(pos2.add(0, -1, 0))) {
            return new BlockData(pos2.add(0, -1, 0), EnumFacing.UP, (BlockData) null);
        }
        if (isPosSolid(pos2.add(-1, 0, 0))) {
            return new BlockData(pos2.add(-1, 0, 0), EnumFacing.EAST, (BlockData) null);
        }
        if (isPosSolid(pos2.add(1, 0, 0))) {
            return new BlockData(pos2.add(1, 0, 0), EnumFacing.WEST, (BlockData) null);
        }
        if (isPosSolid(pos2.add(0, 0, 1))) {
            return new BlockData(pos2.add(0, 0, 1), EnumFacing.NORTH, (BlockData) null);
        }
        if (isPosSolid(pos2.add(0, 0, -1))) {
            return new BlockData(pos2.add(0, 0, -1), EnumFacing.SOUTH, (BlockData) null);
        }
        if (isPosSolid(pos4.add(0, -1, 0))) {
            return new BlockData(pos4.add(0, -1, 0), EnumFacing.UP, (BlockData) null);
        }
        if (isPosSolid(pos4.add(-1, 0, 0))) {
            return new BlockData(pos4.add(-1, 0, 0), EnumFacing.EAST, (BlockData) null);
        }
        if (isPosSolid(pos4.add(1, 0, 0))) {
            return new BlockData(pos4.add(1, 0, 0), EnumFacing.WEST, (BlockData) null);
        }
        if (isPosSolid(pos4.add(0, 0, 1))) {
            return new BlockData(pos4.add(0, 0, 1), EnumFacing.NORTH, (BlockData) null);
        }
        if (isPosSolid(pos4.add(0, 0, -1))) {
            return new BlockData(pos4.add(0, 0, -1), EnumFacing.SOUTH, (BlockData) null);
        }
        if (isPosSolid(pos3.add(0, -1, 0))) {
            return new BlockData(pos3.add(0, -1, 0), EnumFacing.UP, (BlockData) null);
        }
        if (isPosSolid(pos3.add(-1, 0, 0))) {
            return new BlockData(pos3.add(-1, 0, 0), EnumFacing.EAST, (BlockData) null);
        }
        if (isPosSolid(pos3.add(1, 0, 0))) {
            return new BlockData(pos3.add(1, 0, 0), EnumFacing.WEST, (BlockData) null);
        }
        if (isPosSolid(pos3.add(0, 0, 1))) {
            return new BlockData(pos3.add(0, 0, 1), EnumFacing.NORTH, (BlockData) null);
        }
        if (isPosSolid(pos3.add(0, 0, -1))) {
            return new BlockData(pos3.add(0, 0, -1), EnumFacing.SOUTH, (BlockData) null);
        }
        if (isPosSolid(pos5.add(0, -1, 0))) {
            return new BlockData(pos5.add(0, -1, 0), EnumFacing.UP, (BlockData) null);
        }
        if (isPosSolid(pos5.add(-1, 0, 0))) {
            return new BlockData(pos5.add(-1, 0, 0), EnumFacing.EAST, (BlockData) null);
        }
        if (isPosSolid(pos5.add(1, 0, 0))) {
            return new BlockData(pos5.add(1, 0, 0), EnumFacing.WEST, (BlockData) null);
        }
        if (isPosSolid(pos5.add(0, 0, 1))) {
            return new BlockData(pos5.add(0, 0, 1), EnumFacing.NORTH, (BlockData) null);
        }
        if (isPosSolid(pos5.add(0, 0, -1))) {
            return new BlockData(pos5.add(0, 0, -1), EnumFacing.SOUTH, (BlockData) null);
        }
        return null;
    }

    public void getYaw(BlockPos pos) {
        float[] rotations = RotationUtils.getFaceDirectionToBlockPos(pos, this.yaw, this.pitch);
        float yaw = 0;
            if (mc.gameSettings.keyBindForward.isKeyDown())
                yaw = mc.thePlayer.rotationYaw + 180;
            if (mc.gameSettings.keyBindLeft.isKeyDown())
                yaw = mc.thePlayer.rotationYaw + 90;
            if (mc.gameSettings.keyBindRight.isKeyDown())
                yaw = mc.thePlayer.rotationYaw - 90;
            if (mc.gameSettings.keyBindBack.isKeyDown())
                yaw = mc.thePlayer.rotationYaw;

            if (mc.gameSettings.keyBindForward.isKeyDown() && mc.gameSettings.keyBindLeft.isKeyDown())
                yaw = mc.thePlayer.rotationYaw + 90 + (180 - 90) / 2;
            if (mc.gameSettings.keyBindForward.isKeyDown() && mc.gameSettings.keyBindRight.isKeyDown())
                yaw = mc.thePlayer.rotationYaw - 90 - (180 - 90) / 2;

            if (mc.gameSettings.keyBindBack.isKeyDown() && mc.gameSettings.keyBindLeft.isKeyDown())
                yaw = mc.thePlayer.rotationYaw + 90 - (180 - 90) / 2;
            if (mc.gameSettings.keyBindBack.isKeyDown() && mc.gameSettings.keyBindRight.isKeyDown())
                yaw = mc.thePlayer.rotationYaw - 90 + (180 - 90) / 2;

            if (!MovementUtils.isMoving())
                yaw = mc.thePlayer.rotationYaw + 180;

        this.yaw = yaw;
    }

    public void getPitch(BlockPos pos) {
        float pitch;
        float[] rotations = RotationUtils.getFaceDirectionToBlockPos(pos, this.yaw, this.pitch);
        pitch = rotations[1];

        this.pitch = pitch;
    }
}
