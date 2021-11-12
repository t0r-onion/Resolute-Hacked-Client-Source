package vip.Resolute.modules.impl.player;

import org.lwjgl.input.Mouse;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.modules.Module;
import vip.Resolute.modules.impl.combat.KillAura;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.player.InventoryUtils;
import vip.Resolute.util.misc.TimerUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChestStealer extends Module {
    public NumberSetting clickDelay = new NumberSetting("Click Delay", 150.0, 0.0, 500.0, 10.0);
    public NumberSetting closeDelay = new NumberSetting("Close Delay", 150.0, 0.0, 500.0, 10.0);

    public static BooleanSetting silent = new BooleanSetting("Silent", false);
    public BooleanSetting aura = new BooleanSetting("Aura", false);
    public NumberSetting auraRange = new NumberSetting("Aura Range", 5.0, this::isBoolEnabled, 1.0, 6.0, 0.1);
    public BooleanSetting check = new BooleanSetting("Check", true);
    public BooleanSetting mouseFix = new BooleanSetting("Mouse Fix", false);

    TimerUtil timer = new TimerUtil();
    TimerUtil auraTimer = new TimerUtil();
    public final Set openedChests = new HashSet();;
    GuiChest chest;
    IInventory lowerChestInv;
    int i;
    Slot slot;

    public boolean isBoolEnabled() {
        return this.aura.isEnabled();
    }

    public ChestStealer() {
        super("ChestStealer", 0, "Steals from chests on a delay", Category.PLAYER);
        this.addSettings(clickDelay, closeDelay, silent, aura, auraRange, check, mouseFix);
    }

    public void onEvent(Event e) {
        this.setSuffix("");
        if(e instanceof EventPacket) {
            if(((EventPacket) e).getPacket() instanceof S2DPacketOpenWindow) {
                timer.reset();
            }
        }

        if(e instanceof EventMotion) {
            if(e.isPre() && this.mc.currentScreen == null && aura.isEnabled() && KillAura.target == null) {
                List loadedTileEntityList = this.mc.theWorld.loadedTileEntityList;
                index = 0;

                for(int loadedTileEntityListSize = loadedTileEntityList.size(); index < loadedTileEntityListSize; ++index) {
                    TileEntity tile = (TileEntity)loadedTileEntityList.get(index);
                    BlockPos pos = tile.getPos();
                    if (tile instanceof TileEntityChest && this.getDistToPos(pos) < (Double)this.auraRange.getValue() && !this.openedChests.contains(tile) && this.auraTimer.hasElapsed((long) 500) && this.mc.playerController.onPlayerRightClick(mc.thePlayer, this.mc.theWorld, mc.thePlayer.getHeldItem(), pos, EnumFacing.DOWN, this.getVec3(tile.getPos()))) {
                        mc.getNetHandler().sendPacketNoEvent(new C0APacketAnimation());
                        this.set(this.openedChests, tile);
                        this.auraTimer.reset();
                        return;
                    }
                }
            }

            if(e.isPre() && mc.currentScreen instanceof GuiChest) {
                chest = (GuiChest)mc.currentScreen;
                lowerChestInv = chest.getLowerChestInventory();

                if (lowerChestInv.getDisplayName().getUnformattedText().contains("Chest") || !this.check.isEnabled()) {
                    if(silent.isEnabled()) {
                        if (!Mouse.isGrabbed()) {
                            mc.inGameHasFocus = true;
                            mc.mouseHelper.grabMouseCursor();
                        }
                    }
                    if (this.isInventoryFull() || InventoryUtils.isInventoryEmpty(lowerChestInv)) {
                        if (this.timer.hasElapsed((long) this.closeDelay.getValue())) {
                            mc.thePlayer.closeScreen();
                        }
                    } else {
                        i = 0;
                        while (i < lowerChestInv.getSizeInventory()) {
                            if (this.timer.hasElapsed((long) this.clickDelay.getValue()) && InventoryUtils.isValid(lowerChestInv.getStackInSlot(i))) {

                                slot = mc.thePlayer.openContainer.getSlot(i);

                                if(mouseFix.isEnabled()) {
                                    InventoryUtils.legitClick(slot, chest.inventorySlots.windowId, i, 0, InventoryUtils.ClickType.SHIFT_CLICK);
                                } else {
                                    InventoryUtils.windowClick(chest.inventorySlots.windowId, i, 0, InventoryUtils.ClickType.SHIFT_CLICK);
                                }


                                this.timer.reset();
                            }
                            else {
                                ++i;
                            }
                        }
                    }
                }
            }
        }
    }

    public void set(Set set, TileEntity chest) {
        if (set.size() > 128) {
            set.clear();
        }

        set.add(chest);
    }

    public Vec3 getVec3(BlockPos pos) {
        return new Vec3((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
    }

    public double getDistToPos(BlockPos pos) {
        return this.mc.thePlayer.getDistance((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
    }


    private boolean isInventoryFull() {
        for (int i = 9; i < 45; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                return false;
            }
        }
        return true;
    }
}
/*

 */