package vip.Resolute.modules.impl.movement;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.apache.commons.lang3.RandomUtils;
import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.*;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.misc.MathUtils;
import vip.Resolute.util.misc.TimerUtil;
import vip.Resolute.util.misc.TimerUtils;
import vip.Resolute.util.movement.MovementUtils;
import vip.Resolute.util.player.*;
import vip.Resolute.util.render.RenderUtils;
import vip.Resolute.util.world.RandomUtil;
import vip.Resolute.util.world.SetBlockAndFacingUtils;
import vip.Resolute.util.world.Vec3d;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Scaffold extends Module {
    private static final BlockPos[] BLOCK_POSITIONS = new BlockPos[]{new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(0, 0, -1), new BlockPos(0, 0, 1)};
    private static final EnumFacing[] FACINGS = new EnumFacing[]{EnumFacing.EAST, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH};

    private ModeSetting scaffoldmode = new ModeSetting("Mode", "NCP", "NCP", "Normal", "Hypixel", "Matrix", "Redesky", "Expand");
    private ModeSetting towerMode = new ModeSetting("Tower Mode", "NCP", "NCP");

    private BooleanSetting keeprots = new BooleanSetting("Keep Rots", true);
    private BooleanSetting downwards = new BooleanSetting("Downwards",false, () -> scaffoldmode.is("Normal"));

    private NumberSetting delay = new NumberSetting("Delay", 0, 0, 1000, 10);

    public NumberSetting sneakAfter = new NumberSetting("Sneak After", 0, () -> scaffoldmode.is("Matrix"), 0, 10, 1);
    public BooleanSetting sneak = new BooleanSetting("Sneak", false, () -> scaffoldmode.is("Matrix"));
    public BooleanSetting altRots  = new BooleanSetting("Alt Rotations", false, () -> scaffoldmode.is("Matrix"));
    public NumberSetting slowdownMod = new NumberSetting("Slowdown Modifier", 0.6, () -> scaffoldmode.is("Matrix"), 0.1, 1.0, 0.1);
    public ModeSetting rayCastMode = new ModeSetting("Raycast Mode", "None", () -> scaffoldmode.is("Matrix"), "None", "Fix", "Full");
    private NumberSetting timerSpeed = new NumberSetting("Timer Speed", 4, 1, 10, 1);
    private NumberSetting watchdogStep = new NumberSetting("Watchdog Step",1, () -> scaffoldmode.is("Watchdog"),1, 45, 1);
    private NumberSetting watchdog2Step = new NumberSetting("Watchdog2 Step",1, () -> scaffoldmode.is("Watchdog2"),1, 45, 1);
    private BooleanSetting cancelSpeed = new BooleanSetting("Cancel Speed", true);
    private NumberSetting modifier = new NumberSetting("Modifier", 1.0, () -> scaffoldmode.is("Watchdog2"), 0.8, 1.5, 0.1);
    private NumberSetting expandDistance = new NumberSetting("Expand Value",0.5, () -> scaffoldmode.is("Expand"),0.1, 5, 0.1);
    private NumberSetting blockOverride = new NumberSetting("Block Slot",9, 1, 9, 1);

    public BooleanSetting watchdogBoost = new BooleanSetting("Watchdog Boost", false, () -> scaffoldmode.is("Watchdog2"));
    public NumberSetting watchdogValue = new NumberSetting("Watchdog Value", 1.6, () -> scaffoldmode.is("Watchdog2"),1.0, 5.0, 0.1);
    public ModeSetting sprintMode = new ModeSetting("Sprint Mode", "Cancel", () -> scaffoldmode.is("Watchdog2"), "Cancel", "Full");

    public BooleanSetting ncpStep = new BooleanSetting("NCP Step", false, () -> scaffoldmode.is("NCP"));
    public NumberSetting ncpStepAngle = new NumberSetting("NCP Angle Step", 45, () -> ncpStep.isEnabled() && ncpStep.isAvailable(), 5, 180, 5);
    public BooleanSetting cancelTowerSpeed = new BooleanSetting("Cancel Tower Speed", true, () -> scaffoldmode.is("NCP"));
    public BooleanSetting bypass = new BooleanSetting("Bypass", true, () -> scaffoldmode.is("NCP"));

    public static BooleanSetting sprint = new BooleanSetting("Sprint", true);
    private BooleanSetting timerboost = new BooleanSetting("TimerBoost", false);
    private BooleanSetting safewalk = new BooleanSetting("Safewalk", true);
    private BooleanSetting tower = new BooleanSetting("Tower", false);
    private BooleanSetting towermove = new BooleanSetting("Tower Move", false);
    private BooleanSetting swing = new BooleanSetting("Swing", false);
    private BooleanSetting boolkeepY = new BooleanSetting("KeepY", false);
    private BooleanSetting rayCast = new BooleanSetting("RayCast", false);
    private BooleanSetting spoofSprint = new BooleanSetting("Disable Sprint Packet", false);
    private BooleanSetting esp = new BooleanSetting("Indicator", true);

    private float[] rotations = new float[2];
    private static final Map<Integer, Boolean> glCapMap = new HashMap<>();
    private List<Block> badBlocks = Arrays.asList(Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava, Blocks.flowing_lava, Blocks.enchanting_table, Blocks.carpet, Blocks.glass_pane, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.snow_layer, Blocks.ice, Blocks.packed_ice, Blocks.coal_ore, Blocks.diamond_ore, Blocks.emerald_ore, Blocks.chest, Blocks.trapped_chest, Blocks.torch, Blocks.anvil, Blocks.trapped_chest, Blocks.noteblock, Blocks.jukebox, Blocks.tnt, Blocks.gold_ore, Blocks.iron_ore, Blocks.lapis_ore, Blocks.lit_redstone_ore, Blocks.quartz_ore, Blocks.redstone_ore, Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.tallgrass, Blocks.tripwire, Blocks.tripwire_hook, Blocks.rail, Blocks.waterlily, Blocks.red_flower, Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.vine, Blocks.trapdoor, Blocks.yellow_flower, Blocks.ladder, Blocks.furnace, Blocks.sand, Blocks.cactus, Blocks.dispenser, Blocks.noteblock, Blocks.dropper, Blocks.crafting_table, Blocks.web, Blocks.pumpkin, Blocks.sapling, Blocks.cobblestone_wall, Blocks.oak_fence);
    private BlockData blockData;

    public static boolean isPlaceTick = false;
    public static boolean stopWalk = false;
    private double startY;
    public TimerUtils towerTimer = new TimerUtils();
    private TimerUtils timer = new TimerUtils();
    private TimerUtils slotTimer = new TimerUtils();
    private TimerUtils boostTimer = new TimerUtils();
    private TimerUtils reduceTimer = new TimerUtils();
    private BlockData lastBlockData;
    float yaw = 0;
    float pitch = 0;
    int ticks = 0;
    float x;
    float y;
    float percentage;
    float width;
    float half;
    private int count;
    public static int heldItem = 0;
    int hotBarSlot;

    private static List<Block> invalidBlocks = Arrays.asList(Blocks.sand, Blocks.ladder, Blocks.flower_pot, Blocks.red_flower, Blocks.yellow_flower, Blocks.rail, Blocks.golden_rail, Blocks.activator_rail, Blocks.detector_rail, Blocks.beacon, Blocks.web, Blocks.enchanting_table, Blocks.carpet, Blocks.glass_pane, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava, Blocks.flowing_lava, Blocks.snow_layer, Blocks.chest, Blocks.torch, Blocks.anvil, Blocks.trapped_chest, Blocks.noteblock, Blocks.jukebox, Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.crafting_table, Blocks.furnace, Blocks.stone_slab, Blocks.wooden_slab, Blocks.stone_slab2, Blocks.brown_mushroom, Blocks.red_mushroom, Blocks.flower_pot, Blocks.double_plant);;

    private final BlockPos[] blockPositions = new BlockPos[]{new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(0, 0, -1), new BlockPos(0, 0, 1)};
    private final EnumFacing[] facings = new EnumFacing[]{EnumFacing.EAST, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH};

    private int bestBlockStack;
    private BlockData data;
    private int blockCount;

    private double keepY;

    public boolean isSprinting;
    int usedTicks;
    int placeCounter;
    BlockPos blockUnder;
    boolean override;
    private final TimerUtil clickTimer = new TimerUtil();

    private float[] angles;
    private static List<Block> blacklistedBlocks = Arrays.asList(Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava, Blocks.flowing_lava, Blocks.ender_chest, Blocks.enchanting_table, Blocks.stone_button, Blocks.wooden_button, Blocks.crafting_table, Blocks.beacon, Blocks.furnace, Blocks.chest, Blocks.trapped_chest, Blocks.iron_bars, Blocks.cactus, Blocks.ladder);;
    private final TimerUtil sigmaTimer = new TimerUtil();
    double oldY = 0.0;
    private int sigmaY = 0;
    int i;
    ItemStack stack;
    int blockSlot;
    public BlockPos finalPos;
    public int sneakCount;
    public float aacyaw, aacpitch, speed;

    public static boolean enabled = false;
    TimerUtil timeUtil = new TimerUtil();

    public Scaffold() {
        super("Scaffold", Keyboard.KEY_NONE, "Automatically places blocks under you", Category.MOVEMENT);
        this.addSettings(scaffoldmode, keeprots, downwards, delay,
                watchdogStep, watchdog2Step, cancelSpeed, modifier, sprint, timerboost, expandDistance, blockOverride,
                sprintMode, ncpStep, ncpStepAngle, cancelTowerSpeed, bypass,
                watchdogBoost, watchdogValue, sneakAfter, sneak, altRots, slowdownMod, rayCastMode, timerSpeed, safewalk,
                tower, towermove, swing, boolkeepY, rayCast, spoofSprint,
                esp);
    }

    public void onEnable() {
        timer.reset();
        boostTimer.reset();
        slotTimer.reset();
        reduceTimer.reset();
        ticks = 0;
        lastBlockData = null;
        startY = mc.thePlayer.getEntityBoundingBox().minY - 1;
        enabled = true;
        blockCount = 0;
        data = null;
        this.keepY = mc.thePlayer.posY - 1.0D;

        if(scaffoldmode.is("Redesky")) {
            this.blockCount = 0;
            this.oldY = mc.thePlayer.posY - 1.0D;
            if (this.boolkeepY.isEnabled()) {
                this.sigmaTimer.reset();
            }
        }

        this.usedTicks = 0;
        this.placeCounter = 0;
        if (this.mc.thePlayer != null && this.mc.theWorld != null) {
            if (this.sprintMode.is("Cancel") && this.mc.thePlayer != null && this.mc.theWorld != null) {
                this.mc.thePlayer.setSprinting(false);
            }

            enabled = true;
            if (this.mc.thePlayer != null) {
                this.heldItem = this.mc.thePlayer.inventory.currentItem;
            }

        }

        if (mc.thePlayer != null) {
            heldItem = mc.thePlayer.inventory.currentItem;
        }
    }

    public void onDisable() {
        super.onDisable();
        mc.timer.timerSpeed = 1.0f;
        mc.gameSettings.keyBindSneak.pressed = false;
        mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
        enabled = false;
        this.usedTicks = 0;
        this.mc.gameSettings.keyBindSneak.pressed = false;
        this.mc.thePlayer.movementInput.sneak = false;
        this.mc.thePlayer.inventory.currentItem = this.heldItem;
        this.setSneaking(false);
    }

    public void onEvent(Event e) {
        this.setSuffix("");

        try {
            if(e instanceof EventSafeWalk) {
                if(safewalk.isEnabled()) {
                    e.setCancelled(true);
                }
            }

            if(e instanceof EventRender2D) {
                ScaledResolution resolution;
                float x;
                float y;
                float percentage;
                float width;
                float half;


                if(esp.isEnabled()) {
                    resolution = new ScaledResolution(mc);
                    x = resolution.getScaledWidth() / 2.0f;
                    y = resolution.getScaledHeight() / 2.0f + 15.0f;
                    percentage = Math.min(1.0f, this.blockCount / 128.0f);
                    width = 80.0f;
                    half = width / 2.0f;
                    Gui.drawRect(x - half - 0.5f, y - 2.0f, x + half + 0.5f, y + 2.0f, 2013265920);
                    GL11.glEnable(3089);
                    RenderUtils.startScissorBox(resolution, (int)(x - half), (int)y - 2, (int)(width * percentage), 4);
                    RenderUtils.drawGradientRect(x - half, y - 1.5f, x - half + width, y + 1.5f, true, -1571930, -16711936);
                    GL11.glDisable(3089);
                }
            }

            if(e instanceof EventMotion) {
                this.updateBlockCount();

                if(e.isPre()) {
                    if(spoofSprint.isEnabled()) {
                        NetHandlerPlayClient netHandler = mc.getNetHandler();
                        netHandler.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    }
                }

                if(scaffoldmode.is("Matrix")) {
                    EventMotion event = (EventMotion) e;
                    if(e.isPre()) {
                        event.setYaw(aacyaw);
                        event.setPitch(aacpitch);

                        BlockPos pos = new BlockPos(mc.thePlayer.posX, (mc.thePlayer.getEntityBoundingBox()).minY - 1, mc.thePlayer.posZ);
                        mc.thePlayer.setSprinting(sprint.isEnabled());
                        getBlockPosToPlaceOn(pos);

                        aacpitch = getPitch();

                        if(altRots.isEnabled()) {
                            setYawSimple();
                        } else {
                            setYaw();
                        }

                        if(this.mc.thePlayer.isPotionActive(Potion.moveSpeed) && cancelSpeed.isEnabled()) {
                            mc.thePlayer.motionX *= 0.8180000185966492D;
                            mc.thePlayer.motionZ *= 0.8180000185966492D;
                        }

                        mc.thePlayer.motionX *= slowdownMod.getValue();
                        mc.thePlayer.motionZ *= slowdownMod.getValue();
                    }
                }

                if(scaffoldmode.is("Redesky")) {
                    EventMotion event = (EventMotion)e;
                    final BlockPos underPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
                    final BlockData data = getBlockData(underPos);

                    if (getBlockSlot() == -1) {
                        return;
                    }

                    if (mc.theWorld.getBlockState(underPos).getBlock().getMaterial().isReplaceable() && data != null) {
                        if (e.isPre()) {
                            BlockPos pos = new BlockPos(this.mc.thePlayer.posX, (this.mc.thePlayer.posY - 1), this.mc.thePlayer.posZ);
                            BlockData blockData = getBlockData(pos);
                            this.blockData = blockData;

                            if (data.face == EnumFacing.UP) {
                                mc.timer.timerSpeed = 1;
                                event.setPitch(90f);
                            }

                            float[] facing = getRotationsAAC(blockData.position, blockData.face);
                            float yaw = facing[0];
                            float pitch = facing[1];
                            event.setYaw(yaw);


                        } else if (getBlockSlot() != -1) {
                            mc.thePlayer.inventory.currentItem = getBlockSlot();

                            float[] facing = getRotationsAAC(blockData.position, blockData.face);
                            float yaw = facing[0];

                            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), data.position, data.face, getVec3(data));

                            if (swing.isEnabled()) {
                                mc.thePlayer.swingItem();
                            } else {
                                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                            }
                        }
                    }
                }

                if(scaffoldmode.is("Hypixel")) {
                    EventMotion event = (EventMotion) e;

                    if(this.mc.thePlayer.isPotionActive(Potion.moveSpeed) && cancelSpeed.isEnabled()) {
                        mc.thePlayer.motionX *= 0.8180000185966492D;
                        mc.thePlayer.motionZ *= 0.8180000185966492D;
                    }

                    if(e.isPre()) {
                        data = getBlockData();
                        this.bestBlockStack = findBestBlockStack();
                        if (this.bestBlockStack != -1) {
                            if (this.bestBlockStack < 36 && this.clickTimer.hasElapsed((long) delay.getValue())) {
                                override = true;
                                i = 44;
                                while (i >= 36) {
                                    stack = getStackInSlot(i);
                                    if (!InventoryUtils.isValid(stack)) {
                                        InventoryUtils.windowClick(this.bestBlockStack, i - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                                        this.bestBlockStack = i;
                                        override = false;
                                        break;
                                    } else {
                                        --i;
                                    }
                                }
                                if (override) {
                                    blockSlot = (int) (this.blockOverride.getValue() - 1);
                                    InventoryUtils.windowClick(this.bestBlockStack, blockSlot, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                                    this.bestBlockStack = blockSlot + 36;
                                }
                            }

                            if (data != null  && this.bestBlockStack >= 36) {
                                mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
                                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                                rotations = getScaffoldRotations(data);
                                event.setYaw(rotations[0]);
                                event.setPitch(rotations[1]);
                            }
                        }
                    } else if (this.data != null && this.bestBlockStack != -1 && this.bestBlockStack >= 36) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                        hotBarSlot = this.bestBlockStack - 36;
                        if (mc.thePlayer.inventory.currentItem != hotBarSlot) {
                            mc.thePlayer.inventory.currentItem = hotBarSlot;
                        }

                        event.setOnGround(false);
                        mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), data.position, data.face, PlayerUtil.getVectorForRotation(rotations[0], rotations[1]))) {
                            if (!swing.isEnabled())
                                mc.getNetHandler().getNetworkManager().sendPacket(new C0APacketAnimation());
                            else mc.thePlayer.swingItem();
                            data = null;
                        }
                    }
                }

                if(scaffoldmode.is("NCP")) {
                    EventMotion event = (EventMotion) e;

                    if(this.mc.thePlayer.isPotionActive(Potion.moveSpeed) && cancelSpeed.isEnabled()) {
                        mc.thePlayer.motionX *= 0.8180000185966492D;
                        mc.thePlayer.motionZ *= 0.8180000185966492D;
                    }

                    if(e.isPre()) {
                        this.data = null;
                        this.bestBlockStack = findBestBlockStack();

                        if (this.bestBlockStack != -1) {
                            if (this.bestBlockStack < 36 && this.clickTimer.hasElapsed((long) delay.getValue())) {
                                override = true;
                                i = 44;
                                while (i >= 36) {
                                    stack = getStackInSlot(i);
                                    if (!InventoryUtils.isValid(stack)) {
                                        InventoryUtils.windowClick(this.bestBlockStack, i - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                                        this.bestBlockStack = i;
                                        override = false;
                                        break;
                                    }
                                    else {
                                        --i;
                                    }
                                }
                                if (override) {
                                    blockSlot = (int) (this.blockOverride.getValue() - 1);
                                    InventoryUtils.windowClick(this.bestBlockStack, blockSlot, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                                    this.bestBlockStack = blockSlot + 36;
                                }
                            }
                            boolean isKeepY = boolkeepY.isEnabled();
                            BlockPos blockUnder = getBlockUnder(isKeepY);
                            BlockData data = this.getBlockData2(blockUnder);

                            if (isKeepY && (Math.abs(mc.thePlayer.posY - keepY) > (MovementUtils.isMoving() ? 4.0D : 1.0D) || data == null))
                                keepY = mc.thePlayer.posY - 1.0D;

                            if (data == null) {
                                if (isKeepY)
                                    blockUnder = getBlockUnder(true);
                                data = this.getBlockData2(blockUnder.offset(EnumFacing.DOWN));
                            }
                            if (data != null && this.bestBlockStack >= 36) {
                                if(bypass.isEnabled()) {
                                    mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
                                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                                }

                                if (validateReplaceable(data) && data.hitVec != null) {
                                    if(ncpStep.isEnabled()) {
                                        this.angles = getRotations(event, data.hitVec, (float) ncpStepAngle.getValue());
                                    } else {
                                        this.angles = getRotations(data.hitVec);
                                    }
                                }
                                else {
                                    data = null;
                                }
                            }
                            if (this.angles != null) {
                                event.setYaw(angles[0]);
                                event.setPitch(angles[1]);
                            }
                            this.data = data;
                        }
                    } else if (this.data != null && this.bestBlockStack != -1 && this.bestBlockStack >= 36) {
                        hotBarSlot = this.bestBlockStack - 36;
                        if (mc.thePlayer.inventory.currentItem != hotBarSlot) {
                            mc.thePlayer.inventory.currentItem = hotBarSlot;
                        }
                        if(bypass.isEnabled()) {
                            event.setOnGround(false);
                            mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                        }
                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), this.data.position, this.data.face, this.data.hitVec)) {
                            if (this.tower.isEnabled() && mc.gameSettings.keyBindJump.isKeyDown()) {
                                if(!towermove.isEnabled() && !MovementUtils.isMoving()) {
                                    if(cancelTowerSpeed.isEnabled()) {
                                        mc.thePlayer.motionX = 0.0D;
                                        mc.thePlayer.motionZ = 0.0D;
                                    }
                                    mc.thePlayer.motionY = MovementUtils.getJumpHeight(MovementUtils.VANILLA_JUMP_HEIGHT);

                                    if (towerTimer.hasTimeElapsed(1500, true)) {
                                        mc.thePlayer.motionY = -0.28D;
                                        towerTimer.reset();
                                    }
                                } else if(towermove.isEnabled()) {
                                    if(cancelTowerSpeed.isEnabled()) {
                                        mc.thePlayer.motionX = 0.0D;
                                        mc.thePlayer.motionZ = 0.0D;
                                    }
                                    mc.thePlayer.motionY = MovementUtils.getJumpHeight(MovementUtils.VANILLA_JUMP_HEIGHT);

                                    if (towerTimer.hasTimeElapsed(1500, true)) {
                                        mc.thePlayer.motionY = -0.28D;
                                        towerTimer.reset();
                                    }
                                }
                            }
                            if (this.swing.isEnabled()) {
                                mc.thePlayer.swingItem();
                            }
                            else {
                                mc.getNetHandler().sendPacketNoEvent(new C0APacketAnimation());
                            }
                        }
                    }
                }



                if(scaffoldmode.is("Expand")) {
                    EventMotion event = (EventMotion) e;
                    double addition = expandDistance.getValue();
                    final double x2 = Math.cos(Math.toRadians(mc.thePlayer.rotationYaw + 90.0f));
                    final double z2 = Math.sin(Math.toRadians(mc.thePlayer.rotationYaw + 90.0f));
                    final double xOffset = MovementInput.moveForward * addition * x2 + MovementInput.moveStrafe * addition * z2;
                    final double zOffset = MovementInput.moveForward * addition * z2 - MovementInput.moveStrafe * addition * x2;

                    BlockPos blockBelow = new BlockPos(mc.thePlayer.posX + xOffset, mc.thePlayer.posY - 1, mc.thePlayer.posZ + zOffset);
                    BlockData blockEntry = mc.theWorld.getBlockState(blockBelow).getBlock() == Blocks.air ? blockEntry = getBlockData2(blockBelow) : null;

                    if (blockEntry == null) {
                        if (lastBlockData != null && event.isPre()) {
                            float[] rotations = getRotationsNeeded(lastBlockData);
                            event.setPitch(rotations[1]);
                            event.setYaw(rotations[0]);
                        }
                    }
                    if (blockEntry == null)
                        return;
                    if (event.isPre()) {
                        float[] rotations = getRotationsNeeded(blockEntry);
                        event.setPitch(rotations[1]);
                        event.setYaw(rotations[0]);
                    } else {
                        if (getBlockCount() <= 0) {
                            return;
                        }
                        final int heldItem = mc.thePlayer.inventory.currentItem;
                        boolean hasBlock = false;
                        for (int i = 0; i < 9; ++i) {
                            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
                            if (itemStack != null && itemStack.stackSize != 0 && itemStack.getItem() instanceof ItemBlock && !badBlocks.contains(((ItemBlock) mc.thePlayer.inventory.getStackInSlot(i).getItem()).getBlock())) {
                                mc.thePlayer.sendQueue.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = i));
                                hasBlock = true;
                                break;
                            }
                        }
                        if (!hasBlock) {
                            for (int i = 0; i < 45; ++i) {
                                if (mc.thePlayer.inventory.getStackInSlot(i) != null && mc.thePlayer.inventory.getStackInSlot(i).stackSize != 0
                                        && mc.thePlayer.inventory.getStackInSlot(i).getItem() instanceof ItemBlock
                                        && !badBlocks.contains(
                                        ((ItemBlock) mc.thePlayer.inventory.getStackInSlot(i).getItem()).getBlock())) {
                                    mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, i, 8, 2,
                                            mc.thePlayer);
                                    break;
                                }
                            }
                        }

                        if (tower.isEnabled()) {
                            if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.thePlayer.isPotionActive(Potion.jump)) {
                                if (!MovementUtils.isMoving()) {
                                    mc.thePlayer.motionY = 0.42f;
                                    mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                                } else {
                                    if (mc.thePlayer.onGround && towermove.isEnabled()) {
                                        mc.thePlayer.motionY = 0.42f;
                                    } else if (mc.thePlayer.motionY < 0.17D && mc.thePlayer.motionY > 0.16D && towermove.isEnabled()) {
                                        mc.thePlayer.motionY = -0.01f;
                                    }
                                }
                            }
                        }

                        mc.playerController.onPlayerRightClick3d(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), blockEntry.position.add(0, 0, 0), blockEntry.face, new Vec3d(blockEntry.position.getX(), blockEntry.position.getY(), blockEntry.position.getZ()));
                        lastBlockData = blockEntry;


                        if (!swing.isEnabled()) {
                            mc.thePlayer.sendQueue.sendPacketNoEvent(new C0APacketAnimation());
                        } else {
                            mc.thePlayer.swingItem();
                        }

                        mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = heldItem));
                    }

                } else {
                    if(e.isPre()) {
                        EventMotion event = (EventMotion) e;

                        if(scaffoldmode.is("Normal")) {
                            int slot = this.getSlot();
                            this.stopWalk = (getBlockCount() == 0 || slot == -1);
                            this.isPlaceTick = keeprots.isEnabled() ? blockData != null && slot != -1 : blockData != null && slot != -1 && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).add(0, -1, 0)).getBlock() == Blocks.air;
                            if (slot == -1) {
                                moveBlocksToHotbar();

                                return;
                            }

                            this.blockData = getBlockData();
                            if (this.blockData == null) {
                                return;
                            }

                            if(timerboost.isEnabled()) {
                                if(boostTimer.hasTimeElapsed(2000L, true)) {
                                    mc.timer.timerSpeed = (float) timerSpeed.getValue();
                                    reduceTimer.reset();
                                } else if(reduceTimer.hasTimeElapsed(100L, true)) {
                                    mc.timer.timerSpeed = 1.0f;
                                }
                            } else {
                                // mc.timer.timerSpeed = 1.0f;
                            }



                            if (mc.gameSettings.keyBindJump.isKeyDown() && tower.isEnabled() && (this.towermove.isEnabled() || !MovementUtils.isMoving()) && !mc.thePlayer.isPotionActive(Potion.jump) && !boolkeepY.isEnabled()) {
                                if (towerMode.is("Packet")) {
                                    if (mc.thePlayer.onGround) {
                                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.99, mc.thePlayer.posZ);
                                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.41999998688698, mc.thePlayer.posZ, false));
                                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.7531999805212, mc.thePlayer.posZ, false));
                                    }
                                }

                                if(towerMode.is("NCP")) {
                                    if (!MovementUtils.isOnGround(0.79) || mc.thePlayer.onGround) {
                                        mc.thePlayer.motionY = 0.41985;
                                    }
                                    if(towerTimer.hasTimeElapsed(1500, true)){
                                        mc.thePlayer.motionY = -1;
                                    }
                                }
                            } else {
                                towerTimer.reset();
                            }

                            if (this.isPlaceTick) {
                                Rotation targetRotation = new Rotation(SetBlockAndFacingUtils.BlockUtil.getDirectionToBlock(blockData.getPosition().getX(), blockData.getPosition().getY(), blockData.getPosition().getZ(), blockData.getFacing())[0], 79.44f);
                                Rotation limitedRotation = SetBlockAndFacingUtils.BlockUtil.limitAngleChange(new Rotation(yaw, event.getPitch()), targetRotation, (float) ThreadLocalRandom.current().nextDouble(20, 30));
                                yaw = limitedRotation.getYaw();
                                pitch = limitedRotation.getPitch();
                                event.setYaw(yaw);
                                event.setPitch(80);
                            }
                        }
                    } else {
                        if(scaffoldmode.is("Normal")) {
                            int slot = this.getSlot();
                            BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
                            if (slot != -1 && this.blockData != null) {
                                final int currentSlot = mc.thePlayer.inventory.currentItem;
                                if (pos.getBlock() instanceof BlockAir) {
                                    mc.thePlayer.inventory.currentItem = slot;
                                    if (this.getPlaceBlock(this.blockData.getPosition(), this.blockData.getFacing())) {
                                        mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(currentSlot));
                                    }
                                }else{
                                    MovementUtils.setMotion(MovementUtils.getSpeed() - MovementUtils.getSpeed() / 50);
                                }

                                mc.thePlayer.inventory.currentItem = currentSlot;
                            }
                        }
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static float[] getRotationsNeeded(final BlockData data) {
        final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        final Vec3 hitVec = data.hitVec;

        final double xDist = hitVec.xCoord - player.posX;
        final double zDist = hitVec.zCoord - player.posZ;

        final double yDist = hitVec.yCoord - (player.posY + player.getEyeHeight());
        final double fDist = MathHelper.sqrt_double(xDist * xDist + zDist * zDist);
        final float rotationYaw = Minecraft.getMinecraft().thePlayer.rotationYaw;
        final float var1 = MovementUtils.getMovementDirection() - 180.0F;

        final float yaw = rotationYaw + MathHelper.wrapAngleTo180_float(var1 - rotationYaw);
        final float rotationPitch = Minecraft.getMinecraft().thePlayer.rotationPitch;

        if (data.face != EnumFacing.DOWN && data.face != EnumFacing.UP) {
            final double yDistFeet = hitVec.yCoord - player.posY;
            final double totalAbsDist = Math.abs(xDist * xDist + yDistFeet * yDistFeet + zDist * zDist);

            if (totalAbsDist < 1.0)
                return new float[]{yaw, MathUtils.getRandom(80, 90)};
        }

        final float var2 = (float) (-(StrictMath.atan2(yDist, fDist) * 180.0D / Math.PI));
        final float pitch = rotationPitch + MathHelper.wrapAngleTo180_float(var2 - rotationPitch);

        return new float[]{yaw, MathHelper.clamp_float(pitch, -90.0F, 90.0F)};
    }

    private static float[] getRotations(final Vec3 hitVec) {
        final EntityPlayerSP player = mc.thePlayer;
        final double xDist = hitVec.xCoord - player.posX;
        final double zDist = hitVec.zCoord - player.posZ;
        final double yDist = hitVec.yCoord - (player.posY + player.getEyeHeight());
        final double fDist = MathHelper.sqrt_double(xDist * xDist + zDist * zDist);
        final float rotationYaw = mc.thePlayer.rotationYaw;
        final float var1 = (float)(StrictMath.atan2(zDist, xDist) * 180.0 / 3.141592653589793) - 90.0f;
        final float yaw = rotationYaw + MathHelper.wrapAngleTo180_float(var1 - rotationYaw);
        final float rotationPitch = mc.thePlayer.rotationPitch;
        final float var2 = (float)(-(StrictMath.atan2(yDist, fDist) * 180.0 / 3.141592653589793));
        final float pitch = rotationPitch + MathHelper.wrapAngleTo180_float(var2 - rotationPitch);
        return new float[] { yaw, MathHelper.clamp_float(pitch, -90.0f, 90.0f) };
    }

    private float[] getRotations(EventMotion ev, Vec3 hitVec, float aimSpeed) {
        EntityPlayerSP entity = mc.thePlayer;
        double x = hitVec.xCoord - entity.posX;
        double y = hitVec.yCoord - (entity.posY + (double)entity.getEyeHeight());
        double z = hitVec.zCoord - entity.posZ;
        double fDist = MathHelper.sqrt_double(x * x + z * z);
        float yaw = Scaffold.interpolateRotation(ev.getPrevYaw(), (float)(StrictMath.atan2(z, x) * 180.0 / Math.PI) - 90.0f, aimSpeed);
        float pitch = Scaffold.interpolateRotation(ev.getPrevPitch(), (float)(-(StrictMath.atan2(y, fDist) * 180.0 / Math.PI)), aimSpeed);
        return new float[]{yaw, MathHelper.clamp_float(pitch, -90.0f, 90.0f)};
    }

    private void updateBlockCount() {
        this.blockCount = 0;
        for (int i = 9; i < 45; ++i) {
            final ItemStack stack = Resolute.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock && InventoryUtils.isGoodBlockStack(stack)) {
                this.blockCount += stack.stackSize;
            }
        }
    }

    public ItemStack getStackInSlot(final int index) {
        return mc.thePlayer.inventoryContainer.getSlot(index).getStack();
    }

    private float[] getBlockRotations(BlockPos blockPos, EnumFacing enumFacing) {
        if (blockPos == null && enumFacing == null) {
            return null;
        } else {
            Vec3d positionEyes = this.mc.thePlayer.getPositionEyes2(2.0F);
            Vec3d add = (new Vec3d((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D)).add((new Vec3d(enumFacing.getDirectionVec())).scale(0.49000000953674316D));
            double n = add.xCoord - positionEyes.xCoord;
            double n2 = add.yCoord - positionEyes.yCoord;
            double n3 = add.zCoord - positionEyes.zCoord;

            float yaw = (float) (Math.atan2(n3, n) * 180.0D / 3.141592653589793D - 90.0D);
            float pitch = -((float)(Math.atan2(n2, (double)((float)Math.hypot(n, n3))) * 180.0D / 3.141592653589793D));

            final int inc = (int) watchdog2Step.getValue();
            yaw = (float)(Math.round(yaw / inc) * inc);

            return new float[]{ yaw, pitch };
        }
    }

    public int getBlockSlot() {
        for(int i = 36; i < 45; ++i) {
            if (Minecraft.getMinecraft().thePlayer.inventoryContainer.getSlot(i).getStack() != null && Minecraft.getMinecraft().thePlayer.inventoryContainer.getSlot(i).getStack().getItem() instanceof ItemBlock && !invalidBlocks.contains(((ItemBlock)Minecraft.getMinecraft().thePlayer.inventoryContainer.getSlot(i).getStack().getItem()).getBlock())) {
                return i - 36;
            }
        }

        return -1;
    }

    public void setYawSimple() {
        boolean forward = mc.gameSettings.keyBindForward.isKeyDown();
        boolean left = mc.gameSettings.keyBindLeft.isKeyDown();
        boolean right = mc.gameSettings.keyBindRight.isKeyDown();
        boolean back = mc.gameSettings.keyBindBack.isKeyDown();

        float yaw = 0;

        if (forward && !left && !right && !back)
            yaw = 180;
        if (!forward && left && !right && !back)
            yaw = 90;
        if (!forward && !left && right && !back)
            yaw = -90;
        if (!forward && !left && !right && back)
            yaw = 0;

        if (forward && left && !right && !back)
            yaw = 135;
        if (forward && !left && right && !back)
            yaw = -135;

        if (!forward && left && !right && back)
            yaw = 45;
        if (!forward && !left && right && back)
            yaw = -45;

        this.aacyaw = mc.thePlayer.rotationYaw + yaw;
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

    private static boolean validateReplaceable(BlockData data) {
        BlockPos pos = data.position.offset(data.face);
        World world = mc.theWorld;
        return world.getBlockState(pos)
                .getBlock()
                .isReplaceable(world, pos);
    }

    public float[] look(Vec3 vector) {
        double diffX = vector.xCoord - mc.thePlayer.posX;
        double diffY = vector.yCoord - (mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight());
        double diffZ = vector.zCoord - mc.thePlayer.posZ;
        double distance = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, distance)));
        return new float[]{mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw), mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch)};
    }

    private void tower() {
        if (!MovementUtils.isMoving()) {
            mc.thePlayer.motionX = 0.0;
            mc.thePlayer.motionZ = 0.0;
            if (mc.thePlayer.onGround) {
                mc.thePlayer.setPosition((double)this.down(mc.thePlayer.posX) + 0.5, mc.thePlayer.posY, (double)this.down(mc.thePlayer.posZ) + 0.5);
            }
        }
        if (MovementUtils.isOnGround(0.76) && !MovementUtils.isOnGround(0.75) && mc.thePlayer.motionY > 0.23 && mc.thePlayer.motionY < 0.25) {
            mc.thePlayer.motionY = (double)Math.round(mc.thePlayer.posY) - mc.thePlayer.posY;
        }
        if (MovementUtils.isOnGround(1.0E-4)) {
            mc.thePlayer.motionY = MovementUtils.getJumpBoostModifier(0.41999998688698);
        } else if (mc.thePlayer.posY >= (double)Math.round(mc.thePlayer.posY) - 1.0E-4 && mc.thePlayer.posY <= (double)Math.round(mc.thePlayer.posY) + 1.0E-4 && !mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.thePlayer.motionY = 0.0;
        }
    }

    private int down(double n) {
        int n2 = (int)n;
        try {
            if (n < (double)n2) {
                return n2 - 1;
            }
        }
        catch (IllegalArgumentException illegalArgumentException) {
            illegalArgumentException.printStackTrace();
        }
        return n2;
    }

    private BlockData getBlockData2(final BlockPos pos) {
        final BlockPos[] blockPositions = BLOCK_POSITIONS;
        final EnumFacing[] facings = FACINGS;
        final WorldClient world = Minecraft.getMinecraft().theWorld;

        for (int i = 0; i < blockPositions.length; i++) {
            final BlockPos blockPos = pos.add(blockPositions[i]);
            if (isValidBlock(world.getBlockState(blockPos).getBlock(), false)) {
                final BlockData data = new BlockData(blockPos, facings[i]);
                if (validateBlockRange(data))
                    return data;
            }
        }

        final BlockPos posBelow = pos.add(0, -1, 0);
        if (isValidBlock(world.getBlockState(posBelow).getBlock(), false)) {
            final BlockData data = new BlockData(posBelow, EnumFacing.UP);
            if (validateBlockRange(data))
                return data;
        }

        for (BlockPos blockPosition : blockPositions) {
            final BlockPos blockPos = pos.add(blockPosition);
            for (int i = 0; i < blockPositions.length; i++) {
                final BlockPos blockPos1 = blockPos.add(blockPositions[i]);
                if (isValidBlock(world.getBlockState(blockPos1).getBlock(), false)) {
                    final BlockData data = new BlockData(blockPos1, facings[i]);
                    if (validateBlockRange(data))
                        return data;
                }
            }
        }

        for (final BlockPos blockPosition : blockPositions) {
            final BlockPos blockPos = pos.add(blockPosition);
            for (final BlockPos position : blockPositions) {
                final BlockPos blockPos1 = blockPos.add(position);
                for (int i = 0; i < blockPositions.length; i++) {
                    final BlockPos blockPos2 = blockPos1.add(blockPositions[i]);
                    if (isValidBlock(world.getBlockState(blockPos2).getBlock(), false)) {
                        final BlockData data = new BlockData(blockPos2, facings[i]);
                        if (validateBlockRange(data))
                            return data;
                    }
                }
            }
        }

        for (final BlockPos blackPosition : blockPositions) {
            final BlockPos blockPos = pos.add(blackPosition);
            for (final BlockPos blockPosition : blockPositions) {
                final BlockPos blockPos1 = blockPos.add(blockPosition);
                for (final BlockPos position : blockPositions) {
                    final BlockPos blockPos2 = blockPos1.add(position);
                    for (int i = 0; i < blockPositions.length; i++) {
                        final BlockPos blockPos3 = blockPos2.add(blockPositions[i]);
                        if (isValidBlock(world.getBlockState(blockPos3).getBlock(), false)) {
                            final BlockData data = new BlockData(blockPos3, facings[i]);
                            if (validateBlockRange(data))
                                return data;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static float[] getScaffoldRotations(BlockData data) {
        final Vec3 eyes = mc.thePlayer.getPositionEyes(RandomUtils.nextFloat(2.997f, 3.997f));
        final Vec3 position = new Vec3(data.position.getX() + 0.49, data.position.getY() + 0.49, data.position.getZ() + 0.49).add(new Vec3(data.face.getDirectionVec()).scale(0.489997f));
        final Vec3 resultPosition = position.subtract(eyes);
        float yaw = (float) Math.toDegrees(Math.atan2(resultPosition.zCoord, resultPosition.xCoord)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(resultPosition.yCoord, Math.hypot(resultPosition.xCoord, resultPosition.zCoord)));
        return new float[] {yaw, pitch};
    }

    private static boolean validateBlockRange(final BlockData data) {
        final Vec3 pos = data.hitVec;
        if (pos == null)
            return false;
        final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        final double x = (pos.xCoord - player.posX);
        final double y = (pos.yCoord - (player.posY + player.getEyeHeight()));
        final double z = (pos.zCoord - player.posZ);
        return StrictMath.sqrt(x * x + y * y + z * z) <= 5.0D;
    }

    private float[] getRotationsToBlockData(BlockData data, EventMotion ev) {
        EntityPlayerSP entity = mc.thePlayer;
        Vec3 hitVec = data.getHitVec();
        double x = (hitVec.xCoord - entity.posX);
        double y = (hitVec.yCoord - (entity.posY + entity.getEyeHeight()));
        double z = (hitVec.zCoord - entity.posZ);

        double fDist = MathHelper.sqrt_double(x * x + z * z);

        float yaw = interpolateRotation(ev.getPrevYaw(), (float) (Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F);
        float pitch = interpolateRotation(ev.getPrevPitch(), (float) (-(Math.atan2(y, fDist) * 180.0D / Math.PI)));

        final int inc = (int) watchdogStep.getValue();
        yaw = (float)(Math.round(yaw / inc) * inc);

        return new float[]{yaw, MathHelper.clamp_float(pitch, -90.0f, 90.0f)};
    }

    private float interpolateRotation(float p_70663_1_,
                                      float p_70663_2_) {
        float maxTurn = 45.0F;
        float var4 = MathHelper.wrapAngleTo180_float(p_70663_2_ - p_70663_1_);

        if (var4 > maxTurn) {
            var4 = maxTurn;
        }

        if (var4 < -maxTurn) {
            var4 = -maxTurn;
        }

        return p_70663_1_ + var4;
    }

    private BlockPos getBlockUnder(boolean keepY) {
        return new BlockPos(
                mc.thePlayer.posX,
                keepY ? this.keepY : mc.thePlayer.posY - 1.0D,
                mc.thePlayer.posZ);
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


    private boolean getPlaceBlock(final BlockPos pos, final EnumFacing facing) {
        if (timer.hasTimeElapsed((long) delay.getValue(), true)) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), pos, facing, getVec3(new BlockData(pos, facing)))) {
                if (this.swing.isEnabled()) {
                    mc.thePlayer.swingItem();
                } else {
                    mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                }

                timer.reset();
                return true;
            }


        }
        return false;
    }

    private BlockData getBlockData(BlockPos pos) {
        if (this.isPosSolid(mc.theWorld.getBlockState(pos.add(0, -1, 0)).getBlock())) {
            return new BlockData(pos.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock())) {
            return new BlockData(pos.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(pos.add(-1, 0, 0)).getBlock())) {
            return new BlockData(pos.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(pos.add(1, 0, 0)).getBlock())) {
            return new BlockData(pos.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(pos.add(0, 0, -1)).getBlock())) {
            return new BlockData(pos.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos add = pos.add(0, 0, 0);
        if (this.isPosSolid(mc.theWorld.getBlockState(add.add(-1, 0, 0)).getBlock())) {
            return new BlockData(add.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add.add(1, 0, 0)).getBlock())) {
            return new BlockData(add.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add.add(0, 0, -1)).getBlock())) {
            return new BlockData(add.add(0, 0, -1), EnumFacing.SOUTH);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add.add(0, 0, 1)).getBlock())) {
            return new BlockData(add.add(0, 0, 1), EnumFacing.NORTH);
        }
        BlockPos add2 = pos.add(1, 0, 0);
        if (this.isPosSolid(mc.theWorld.getBlockState(add2.add(-1, 0, 0)).getBlock())) {
            return new BlockData(add2.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add2.add(1, 0, 0)).getBlock())) {
            return new BlockData(add2.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add2.add(0, 0, -1)).getBlock())) {
            return new BlockData(add2.add(0, 0, -1), EnumFacing.SOUTH);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add2.add(0, 0, 1)).getBlock())) {
            return new BlockData(add2.add(0, 0, 1), EnumFacing.NORTH);
        }
        BlockPos add3 = pos.add(0, 0, -1);
        if (this.isPosSolid(mc.theWorld.getBlockState(add3.add(-1, 0, 0)).getBlock())) {
            return new BlockData(add3.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add3.add(1, 0, 0)).getBlock())) {
            return new BlockData(add3.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add3.add(0, 0, -1)).getBlock())) {
            return new BlockData(add3.add(0, 0, -1), EnumFacing.SOUTH);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add3.add(0, 0, 1)).getBlock())) {
            return new BlockData(add3.add(0, 0, 1), EnumFacing.NORTH);
        }
        BlockPos add4 = pos.add(0, 0, 1);
        if (this.isPosSolid(mc.theWorld.getBlockState(add4.add(-1, 0, 0)).getBlock())) {
            return new BlockData(add4.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add4.add(1, 0, 0)).getBlock())) {
            return new BlockData(add4.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add4.add(0, 0, -1)).getBlock())) {
            return new BlockData(add4.add(0, 0, -1), EnumFacing.SOUTH);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add4.add(0, 0, 1)).getBlock())) {
            return new BlockData(add4.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add.add(1, 1, 0)).getBlock())) {
            return new BlockData(add.add(1, 1, 0), EnumFacing.DOWN);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add.add(-1, 2, -1)).getBlock())) {
            return new BlockData(add.add(-1, 2, -1), EnumFacing.DOWN);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add2.add(-2, 1, 0)).getBlock())) {
            return new BlockData(add2.add(-2, 1, 0), EnumFacing.DOWN);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add2.add(0, 2, 1)).getBlock())) {
            return new BlockData(add2.add(0, 2, 1), EnumFacing.DOWN);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add3.add(0, 1, 2)).getBlock())) {
            return new BlockData(add3.add(0, 1, 2), EnumFacing.DOWN);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add3.add(1, 2, 0)).getBlock())) {
            return new BlockData(add3.add(1, 2, 0), EnumFacing.DOWN);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add4.add(0, 1, -2)).getBlock())) {
            return new BlockData(add4.add(0, 1, -2), EnumFacing.DOWN);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add4.add(-1, 2, 0)).getBlock())) {
            return new BlockData(add4.add(-1, 2, 0), EnumFacing.DOWN);
        }
        return null;
    }

    private boolean isPosSolid(Block block) {
        return !blacklistedBlocks.contains(block) && (block.getMaterial().isSolid() || !block.isTranslucent() || block.isVisuallyOpaque() || block instanceof BlockLadder || block instanceof BlockCarpet || block instanceof BlockSnow || block instanceof BlockSkull) && !block.getMaterial().isLiquid() && !(block instanceof BlockContainer);
    }


    private static float interpolateRotation(float prev, float now, float maxTurn) {
        float var4 = MathHelper.wrapAngleTo180_float(now - prev);
        if (var4 > maxTurn) {
            var4 = maxTurn;
        }
        if (var4 < -maxTurn) {
            var4 = -maxTurn;
        }
        return prev + var4;
    }

    private Vec3 getVec3(BlockData data) {
        BlockPos pos = data.getPosition();
        EnumFacing face = data.getFacing();
        double x = (double) pos.getX() + 0.5D;
        double y = (double) pos.getY() + 0.5D;
        double z = (double) pos.getZ() + 0.5D;
        x += (double) face.getFrontOffsetX() / 2.0D;
        z += (double) face.getFrontOffsetZ() / 2.0D;
        y += (double) face.getFrontOffsetY() / 2.0D;
        if (face != EnumFacing.UP && face != EnumFacing.DOWN) {
            y += this.randomNumber(0.49D, 0.5D);
        } else {
            x += this.randomNumber(0.3D, -0.3D);
            z += this.randomNumber(0.3D, -0.3D);
        }

        if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
            z += this.randomNumber(0.3D, -0.3D);
        }

        if (face == EnumFacing.SOUTH || face == EnumFacing.NORTH) {
            x += this.randomNumber(0.3D, -0.3D);
        }

        return new Vec3(x, y, z);
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

    private double randomNumber(double max, double min) {
        return Math.random() * (max - min) + min;
    }

    static Random rng = new Random();

    public static int getRandom(final int floor, final int cap) {
        return floor + rng.nextInt(cap - floor + 1);
    }

    private void setSneaking(boolean b) {
        mc.gameSettings.keyBindSneak.pressed = b;
    }

    public BlockData getBlockData() {
        final EnumFacing[] invert = {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.WEST};
        double yValue = 0;
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()) && !mc.gameSettings.keyBindJump.isKeyDown() && downwards.isEnabled()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            yValue -= 0.6;
        }
        BlockPos aa = new BlockPos(mc.thePlayer.getPositionVector()).offset(EnumFacing.DOWN).add(0, yValue, 0);
        BlockPos playerpos = aa;

        boolean tower = !this.towermove.isEnabled() && this.tower.isEnabled() && !MovementUtils.isMoving();
        if (!this.downwards.isEnabled() && this.boolkeepY.isEnabled() && !tower) {
            playerpos = new BlockPos(new Vec3(mc.thePlayer.getPositionVector().xCoord, this.startY, mc.thePlayer.getPositionVector().zCoord)).offset(EnumFacing.DOWN);
        } else {
            this.startY = mc.thePlayer.posY;
        }

        for (EnumFacing facing : EnumFacing.values()) {
            if (playerpos.offset(facing).getBlock().getMaterial() != Material.air) {
                return new BlockData(playerpos.offset(facing), invert[facing.ordinal()]);
            }
        }
        final BlockPos[] addons = {new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(0, 0, -1), new BlockPos(0, 0, 1)};

        for (int length2 = addons.length, j = 0; j < length2; ++j) {
            final BlockPos offsetPos = playerpos.add(addons[j].getX(), 0, addons[j].getZ());
            if (mc.theWorld.getBlockState(offsetPos).getBlock() instanceof BlockAir) {
                for (int k = 0; k < EnumFacing.values().length; ++k) {
                    if (mc.theWorld.getBlockState(offsetPos.offset(EnumFacing.values()[k])).getBlock().getMaterial() != Material.air) {

                        return new BlockData(offsetPos.offset(EnumFacing.values()[k]), invert[EnumFacing.values()[k].ordinal()]);
                    }
                }
            }
        }

        return null;
    }

    int slotIndex = 0;

    private int findBestBlockStack() {
        int bestSlot = -1;
        int blockCount = -1;

        for (int i = InventoryUtils.END - 1; i >= InventoryUtils.EXCLUDE_ARMOR_BEGIN; --i) {
            ItemStack stack = getStackInSlot(i);

            if (stack != null &&
                    stack.getItem() instanceof ItemBlock &&
                    InventoryUtils.isGoodBlockStack(stack)) {
                if (stack.stackSize > blockCount) {
                    bestSlot = i;
                    blockCount = stack.stackSize;
                }
            }
        }

        return bestSlot;
    }


    private int getSlot() {
        ArrayList<Integer> slots = new ArrayList<>();
        for (int k = 0; k < 9; ++k) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[k];
            if (itemStack != null && this.isValid(itemStack) && itemStack.stackSize >= 1) {
                slots.add(k);
            }
        }
        if (slots.isEmpty()) {
            return -1;
        }
        if (slotTimer.hasReached(150)) {
            if (slotIndex >= slots.size() || slotIndex == slots.size() - 1) {
                slotIndex = 0;
            } else {
                slotIndex++;
            }
            slotTimer.reset();
        }
        return slots.get(slotIndex);
    }

    private boolean isValid(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemBlock) {
            boolean isBad = false;

            ItemBlock block = (ItemBlock) itemStack.getItem();
            for (int i = 0; i < this.badBlocks.size(); i++) {
                if (block.getBlock().equals(this.badBlocks.get(i))) {
                    isBad = true;
                }
            }

            return !isBad;
        }
        return false;
    }

    private int getBlockCount() {
        int count = 0;
        for (int k = 0; k < mc.thePlayer.inventory.mainInventory.length; ++k) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[k];
            if (itemStack != null && this.isValid(itemStack) && itemStack.stackSize >= 1) {
                count += itemStack.stackSize;
            }
        }
        return count;
    }

    public static class BlockData {
        public BlockPos position;
        public EnumFacing face;
        public Vec3 hitVec;

        public BlockData(BlockPos position, EnumFacing face) {
            this.position = position;
            this.face = face;
            this.hitVec = getHitVec();
        }

        public EnumFacing getFacing() {
            return this.face;
        }

        public BlockPos getPosition() {
            return this.position;
        }

        private Vec3 getHitVec() {
            final Vec3i directionVec = face.getDirectionVec();
            double x = directionVec.getX() * 0.5D;
            double z = directionVec.getZ() * 0.5D;

            if (face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                x = -x;
                z = -z;
            }

            final Vec3 hitVec = new Vec3(position).addVector(x + z, directionVec.getY() * 0.5D, x + z);

            final Vec3 src = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0F);
            final MovingObjectPosition obj = Minecraft.getMinecraft().theWorld.rayTraceBlocks(src,
                    hitVec,
                    false,
                    false,
                    true);

            if (obj == null || obj.hitVec == null || obj.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
                return null;

            switch (face.getAxis()) {
                case Z:
                    obj.hitVec = new Vec3(obj.hitVec.xCoord, obj.hitVec.yCoord, Math.round(obj.hitVec.zCoord));
                    break;
                case X:
                    obj.hitVec = new Vec3(Math.round(obj.hitVec.xCoord), obj.hitVec.yCoord, obj.hitVec.zCoord);
                    break;
            }

            if (face != EnumFacing.DOWN && face != EnumFacing.UP) {
                final IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(obj.getBlockPos());
                final Block blockAtPos = blockState.getBlock();

                double blockFaceOffset;

                if (blockAtPos instanceof BlockSlab && !((BlockSlab) blockAtPos).isDouble()) {
                    final BlockSlab.EnumBlockHalf half = blockState.getValue(BlockSlab.HALF);

                    blockFaceOffset = org.apache.commons.lang3.RandomUtils.nextDouble(0.1, 0.4);

                    if (half == BlockSlab.EnumBlockHalf.TOP) {
                        blockFaceOffset += 0.5;
                    }
                } else {
                    blockFaceOffset = org.apache.commons.lang3.RandomUtils.nextDouble(0.1, 0.9);
                }

                obj.hitVec = obj.hitVec.addVector(0.0D, -blockFaceOffset, 0.0D);
            }

            return obj.hitVec;
        }
    }

    public void setYaw() {
        float[] rotations = faceBlock(finalPos, true, aacyaw, aacpitch, speed);
        aacyaw = rotations[0];
    }

    public float getPitch() {
        return faceBlock(finalPos, true, aacyaw, aacpitch, speed)[1];
    }

    public void placeBlock(BlockPos pos, EnumFacing face) {
        int lastItem;
        int silentItem = 0;
        finalPos = pos;
        if (mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBlock && !badBlocks.contains(mc.thePlayer.getCurrentEquippedItem().getItem())) {
            silentItem = mc.thePlayer.inventory.currentItem;
        } else {
            for (int i = 0; i < 9; i++) {
                if (mc.thePlayer.inventory.getStackInSlot(i) != null && mc.thePlayer.inventory.getStackInSlot(i).getItem() instanceof ItemBlock) {
                    ItemBlock itemBlock = (ItemBlock) mc.thePlayer.inventory.getStackInSlot(i).getItem();
                    if (badBlocks.contains(itemBlock.getBlock()))
                        continue;
                    silentItem = i;
                    break;
                }
            }
        }
        MovingObjectPosition fixRayTrace = RayCastUtil.rayTrace(mc.thePlayer, 4.0, 0, aacyaw, aacpitch);
        if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0D, mc.thePlayer.posZ)).getBlock() instanceof BlockAir) {
            if (!rayCastMode.is("Fix") || fixRayTrace.getBlockPos() != null) {
                if (sneakCount >= sneakAfter.getValue() && sneak.isEnabled())
                    mc.gameSettings.keyBindSneak.pressed = true;
                if(!altRots.isEnabled()) {
                    setYaw();
                }

                lastItem = mc.thePlayer.inventory.currentItem;
                mc.thePlayer.inventory.currentItem = silentItem;

                if(swing.isEnabled()) {
                    mc.thePlayer.swingItem();
                } else {
                    mc.getNetHandler().sendPacketNoEvent(new C0APacketAnimation());
                }

                this.speed = 360;
                if (mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBlock && !badBlocks.contains(mc.thePlayer.getCurrentEquippedItem().getItem())) {
                    if (rayCastMode.is("Full")) {
                        MovingObjectPosition rayTrace = RayCastUtil.getMouseOver(mc.thePlayer, aacyaw, aacpitch, 4.0D);
                        mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), rayTrace.getBlockPos(), rayTrace.sideHit, rayTrace.hitVec);
                        sneakCount++;
                    } else {
                        double vec = RandomUtil.getDouble(0.2, 0.8);
                        if (rayCastMode.is("Fix"))
                            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), fixRayTrace.getBlockPos(), fixRayTrace.sideHit, fixRayTrace.hitVec);
                        else
                            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), pos, face, new Vec3(vec, vec, vec));
                        sneakCount++;
                    }
                    timeUtil.reset();
                }

                mc.thePlayer.inventory.currentItem = lastItem;

                if (sneakCount > sneakAfter.getValue())
                    sneakCount = 0;
            } else {
                timeUtil.reset();
            }
        } else {
            timeUtil.reset();
            mc.gameSettings.keyBindSneak.pressed = false;

            if(!altRots.isEnabled()) {
                setYaw();
            }
        }
    }

    public static float[] faceBlock(BlockPos pos, boolean scaffoldFix, float currentYaw, float currentPitch, float speed) {
        double x = (pos.getX() + (scaffoldFix ? 0.5 : 0.0)) - mc.thePlayer.posX;
        double y = (pos.getY() - (scaffoldFix ? 1.75 : 0.0F)) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double z = (pos.getZ() + (scaffoldFix ? 0.5 : 0.0)) - mc.thePlayer.posZ;

        double calculate = MathHelper.sqrt_double(x * x + z * z);
        float calcYaw = (float) (MathHelper.func_181159_b(z, x) * 180.0D / Math.PI) - 90.0F;
        float calcPitch = (float) -(MathHelper.func_181159_b(y, calculate) * 180.0D / Math.PI);
        float finalPitch = calcPitch >= 90 ? 90 : calcPitch;
        float yaw = updateRotation(currentYaw, calcYaw, speed);
        float pitch = updateRotation(currentPitch, finalPitch, speed);

        float sense = mc.gameSettings.mouseSensitivity * 0.8F + 0.2F;
        float fix = (float) (Math.pow(sense, 3) * 1.5F);
        yaw -= yaw % fix;
        pitch -= pitch % fix;

        return new float[]{yaw, pitch};
    }

    public static float updateRotation(float current, float intended, float factor) {
        float f = MathHelper.wrapAngleTo180_float(intended - current);
        if (f > factor)
            f = factor;
        if (f < -factor)
            f = -factor;
        return current + f;
    }

    public void getBlockPosToPlaceOn(BlockPos pos) {
        BlockPos blockPos1 = pos.add(-1, 0, 0);
        BlockPos blockPos2 = pos.add(1, 0, 0);
        BlockPos blockPos3 = pos.add(0, 0, -1);
        BlockPos blockPos4 = pos.add(0, 0, 1);
        if (mc.theWorld.getBlockState(pos.add(0, -1, 0)).getBlock() != Blocks.air) {
            placeBlock(pos.add(0, -1, 0), EnumFacing.UP);
        } else if (mc.theWorld.getBlockState(pos.add(-1, 0, 0)).getBlock() != Blocks.air) {
            placeBlock(pos.add(-1, 0, 0), EnumFacing.EAST);
        } else if (mc.theWorld.getBlockState(pos.add(1, 0, 0)).getBlock() != Blocks.air) {
            placeBlock(pos.add(1, 0, 0), EnumFacing.WEST);
        } else if (mc.theWorld.getBlockState(pos.add(0, 0, -1)).getBlock() != Blocks.air) {
            placeBlock(pos.add(0, 0, -1), EnumFacing.SOUTH);
        } else if (mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock() != Blocks.air) {
            placeBlock(pos.add(0, 0, 1), EnumFacing.NORTH);
        } else if (mc.theWorld.getBlockState(blockPos1.add(0, -1, 0)).getBlock() != Blocks.air) {
            placeBlock(blockPos1.add(0, -1, 0), EnumFacing.UP);
        } else if (mc.theWorld.getBlockState(blockPos1.add(-1, 0, 0)).getBlock() != Blocks.air) {
            placeBlock(blockPos1.add(-1, 0, 0), EnumFacing.EAST);
        } else if (mc.theWorld.getBlockState(blockPos1.add(1, 0, 0)).getBlock() != Blocks.air) {
            placeBlock(blockPos1.add(1, 0, 0), EnumFacing.WEST);
        } else if (mc.theWorld.getBlockState(blockPos1.add(0, 0, -1)).getBlock() != Blocks.air) {
            placeBlock(blockPos1.add(0, 0, -1), EnumFacing.SOUTH);
        } else if (mc.theWorld.getBlockState(blockPos1.add(0, 0, 1)).getBlock() != Blocks.air) {
            placeBlock(blockPos1.add(0, 0, 1), EnumFacing.NORTH);
        } else if (mc.theWorld.getBlockState(blockPos2.add(0, -1, 0)).getBlock() != Blocks.air) {
            placeBlock(blockPos2.add(0, -1, 0), EnumFacing.UP);
        } else if (mc.theWorld.getBlockState(blockPos2.add(-1, 0, 0)).getBlock() != Blocks.air) {
            placeBlock(blockPos2.add(-1, 0, 0), EnumFacing.EAST);
        } else if (mc.theWorld.getBlockState(blockPos2.add(1, 0, 0)).getBlock() != Blocks.air) {
            placeBlock(blockPos2.add(1, 0, 0), EnumFacing.WEST);
        } else if (mc.theWorld.getBlockState(blockPos2.add(0, 0, -1)).getBlock() != Blocks.air) {
            placeBlock(blockPos2.add(0, 0, -1), EnumFacing.SOUTH);
        } else if (mc.theWorld.getBlockState(blockPos2.add(0, 0, 1)).getBlock() != Blocks.air) {
            placeBlock(blockPos2.add(0, 0, 1), EnumFacing.NORTH);
        } else if (mc.theWorld.getBlockState(blockPos3.add(0, -1, 0)).getBlock() != Blocks.air) {
            placeBlock(blockPos3.add(0, -1, 0), EnumFacing.UP);
        } else if (mc.theWorld.getBlockState(blockPos3.add(-1, 0, 0)).getBlock() != Blocks.air) {
            placeBlock(blockPos3.add(-1, 0, 0), EnumFacing.EAST);
        } else if (mc.theWorld.getBlockState(blockPos3.add(1, 0, 0)).getBlock() != Blocks.air) {
            placeBlock(blockPos3.add(1, 0, 0), EnumFacing.WEST);
        } else if (mc.theWorld.getBlockState(blockPos3.add(0, 0, -1)).getBlock() != Blocks.air) {
            placeBlock(blockPos3.add(0, 0, -1), EnumFacing.SOUTH);
        } else if (mc.theWorld.getBlockState(blockPos3.add(0, 0, 1)).getBlock() != Blocks.air) {
            placeBlock(blockPos3.add(0, 0, 1), EnumFacing.NORTH);
        } else if (mc.theWorld.getBlockState(blockPos4.add(0, -1, 0)).getBlock() != Blocks.air) {
            placeBlock(blockPos4.add(0, -1, 0), EnumFacing.UP);
        } else if (mc.theWorld.getBlockState(blockPos4.add(-1, 0, 0)).getBlock() != Blocks.air) {
            placeBlock(blockPos4.add(-1, 0, 0), EnumFacing.EAST);
        } else if (mc.theWorld.getBlockState(blockPos4.add(1, 0, 0)).getBlock() != Blocks.air) {
            placeBlock(blockPos4.add(1, 0, 0), EnumFacing.WEST);
        } else if (mc.theWorld.getBlockState(blockPos4.add(0, 0, -1)).getBlock() != Blocks.air) {
            placeBlock(blockPos4.add(0, 0, -1), EnumFacing.SOUTH);
        } else if (mc.theWorld.getBlockState(blockPos4.add(0, 0, 1)).getBlock() != Blocks.air) {
            placeBlock(blockPos4.add(0, 0, 1), EnumFacing.NORTH);
        }
    }

    private void moveBlocksToHotbar() {
        boolean added = false;
        if (!isHotbarFull()) {
            for (int k = 0; k < mc.thePlayer.inventory.mainInventory.length; ++k) {
                if (k > 8 && !added) {
                    final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[k];
                    if (itemStack != null && this.isValid(itemStack)) {
                        shiftClick(k);
                        added = true;
                    }
                }
            }
        }
    }

    public boolean isHotbarFull() {
        int count = 0;
        for (int k = 0; k < 9; ++k) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[k];
            if (itemStack != null) {
                count++;
            }
        }
        return count == 8;
    }


    public static void shiftClick(int slot) {
        Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().thePlayer.inventoryContainer.windowId, slot, 0, 1, Minecraft.getMinecraft().thePlayer);
    }
}

