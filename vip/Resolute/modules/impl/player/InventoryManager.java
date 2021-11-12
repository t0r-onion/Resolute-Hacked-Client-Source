package vip.Resolute.modules.impl.player;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.events.impl.EventWindowClick;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.misc.TimerUtil;
import vip.Resolute.util.player.PlayerUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class InventoryManager extends Module {
    public NumberSetting clickDelayProp = new NumberSetting("Click Delay", 150, 0, 1000, 10);
    public BooleanSetting combatCheck = new BooleanSetting("While Fighting", false);
    public BooleanSetting autoArmor = new BooleanSetting("Auto Armor", true);
    public BooleanSetting sortHotbar = new BooleanSetting("Sort Hotbar", true);
    public BooleanSetting sortTools = new BooleanSetting("Sort Tools", true);
    public BooleanSetting spoof = new BooleanSetting("Spoof", true);
    public BooleanSetting onOpen = new BooleanSetting("Open Inv", false);

    private int bestSwordSlot;
    private int bestBowSlot;
    private boolean openInventory;
    private final int[] bestArmorPieces = new int[4];
    private final int[] bestToolSlots = new int[3];

    private final TimerUtil interactionsTimer = new TimerUtil();

    private final List<Integer> trash = new ArrayList<>();
    private final List<Integer> duplicateSwords = new ArrayList<>();
    private final List<Integer> gappleStackSlots = new ArrayList<>();

    public InventoryManager() {
        super("InvManager", Keyboard.KEY_U, "Sorts out your inventory", Category.PLAYER);
        this.addSettings(clickDelayProp, combatCheck, autoArmor, sortHotbar, sortTools, spoof, onOpen);
    }

    @Override
    public void onEnable() {
        this.openInventory = (mc.currentScreen instanceof GuiInventory);
        this.interactionsTimer.reset();
    }

    @Override
    public void onDisable() {
        this.close();
    }

    public void onEvent(Event e) {
        this.setSuffix("");

        try {
            if(e instanceof EventPacket) {
                if(this.openInventory) {
                    if(((EventPacket) e).getPacket() instanceof C16PacketClientStatus) {
                        C16PacketClientStatus status = ((EventPacket) e).getPacket();
                        if(status.getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT && !(mc.currentScreen instanceof GuiChest))
                            e.setCancelled(true);
                    }
                    if(((EventPacket) e).getPacket() instanceof C0DPacketCloseWindow) {
                        e.setCancelled(true);
                    }

                    if(((EventPacket) e).getPacket() instanceof S2DPacketOpenWindow) {
                        this.close();
                    }
                    if(((EventPacket) e).getPacket() instanceof S2EPacketCloseWindow) {
                        e.setCancelled(true);
                    }
                }
            }
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }


        if(e instanceof EventWindowClick) {
            this.interactionsTimer.reset();
        }

        GuiScreen currentScreen;

        if(e instanceof EventMotion) {
            if(e.isPre()) {
                currentScreen = mc.currentScreen;

                if ((currentScreen == null && !onOpen.isEnabled()) || currentScreen instanceof GuiInventory) {
                    if(!interactionsTimer.hasElapsed((long) this.clickDelayProp.getValue())) return;
                    reset();
                    boolean foundSword = false;
                    boolean foundBow = false;
                    boolean foundGapple = false;
                    // Find and save slots
                    for(int slot = PlayerUtil.INCLUDE_ARMOR_BEGIN; slot < PlayerUtil.END; slot++) {
                        final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();
                        if(stack != null) {
                            // find best swords (trash duplicates)
                            if(stack.getItem() instanceof ItemSword && PlayerUtil.isBestSword(stack)) {
                                if(foundSword)
                                    duplicateSwords.add(slot);
                                else if(slot != bestSwordSlot) {
                                    foundSword = true;
                                    bestSwordSlot = slot;
                                }
                                // find best bows (trash duplicates)
                            } else if(stack.getItem() instanceof ItemBow && PlayerUtil.isBestBow(stack)) {
                                if(slot != bestBowSlot)
                                    bestBowSlot = slot;
                                // find best tools
                            } else if(stack.getItem() instanceof ItemTool && PlayerUtil.isBestTool(stack)) {
                                final int toolType = PlayerUtil.getToolType(stack);
                                if (toolType != -1 && slot != bestToolSlots[toolType])
                                    bestToolSlots[toolType] = slot;
                                // find best armor
                            } else if(stack.getItem() instanceof ItemArmor && PlayerUtil.isBestArmor(stack) && autoArmor.isEnabled()) {
                                final ItemArmor armor = (ItemArmor) stack.getItem();
                                final int bestSlot = bestArmorPieces[armor.armorType];
                                if(bestSlot == -1 || slot != bestSlot)
                                    bestArmorPieces[armor.armorType] = slot;
                            } else if(stack.getItem() instanceof ItemAppleGold) {
                                if(!foundGapple) {
                                    if(stack.stackSize == 64) {
                                        foundGapple = true;
                                        gappleStackSlots.add(slot);
                                    }
                                } else trash.add(slot);
                            } else if(!trash.contains(slot) && !isValidStack(stack))
                                trash.add(slot);
                        }
                    }

                    for (int i = 0; i < bestArmorPieces.length; i++) {
                        final int piece = bestArmorPieces[i];
                        if (piece != -1) {
                            final int armorPieceSlot = i + PlayerUtil.INCLUDE_ARMOR_BEGIN;
                            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(armorPieceSlot).getStack();
                            if (stack != null)
                                continue;
                            open();
                            PlayerUtil.windowClick(piece, 0, PlayerUtil.ClickType.SHIFT_CLICK);
                            close();
                            return;
                        }
                    }

                    // increment when slot is sorted
                    int currentSlot = PlayerUtil.ONLY_HOT_BAR_BEGIN;

                    if (!combatCheck.isEnabled() && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && mc.objectMouseOver.entityHit.hurtResistantTime >= 10) {
                        interactionsTimer.reset();
                        return;
                    }

                    // purge duplicate swords
                    if(purgeList(duplicateSwords)) return;

                    // sort hotbar
                    if (sortHotbar.isEnabled()) {
                        // get best sword slot and swap it to 36 (0 in hotbar)
                        if(bestSwordSlot != -1) {
                            if(bestSwordSlot != currentSlot) {
                                putItemInSlot(ItemType.SWORD, currentSlot);
                                return;
                            }
                            currentSlot++;
                        }
                    }

                    // purge trash
                    if (purgeList(trash)) return;

                    // sort rest of hotbar
                    if (sortHotbar.isEnabled()) {
                        // get best bow slot and swap it to 37 (1 in hotbar)
                        if (bestBowSlot != -1) {
                            if (bestBowSlot != currentSlot) {
                                putItemInSlot(ItemType.BOW, currentSlot);
                                return;
                            }
                            currentSlot++;
                        }
                        if (!gappleStackSlots.isEmpty()) {
                            open();
                            gappleStackSlots.sort((s1, s2) -> mc.thePlayer.inventoryContainer.getSlot(s2).getStack().stackSize - mc.thePlayer.inventoryContainer.getSlot(s2).getStack().stackSize);
                            // get the biggest gapple slot
                            int bestSlot = gappleStackSlots.get(0);
                            // set the gapple slot to the currentSlot
                            if(bestSlot != currentSlot) {
                                PlayerUtil.windowClick(bestSlot, currentSlot - PlayerUtil.ONLY_HOT_BAR_BEGIN, PlayerUtil.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                                return;
                            }
                            currentSlot++;
                        }
                    }

                    // sort tools
                    if (sortTools.isEnabled()) {
                        // next 3 slots after last incremented slot
                        final int[] toolSlots = {currentSlot, currentSlot + 1, currentSlot + 2};
                        for(int bestSlot : bestToolSlots) {
                            if(bestSlot != -1) {
                                int type = PlayerUtil.getToolType(mc.thePlayer.inventoryContainer.getSlot(bestSlot).getStack());
                                if(type != -1) {
                                    if(bestSlot != toolSlots[type]) {
                                        putToolsInSlot(type, toolSlots);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void reset() {
        trash.clear();
        bestBowSlot = -1;
        bestSwordSlot = -1;
        duplicateSwords.clear();
        gappleStackSlots.clear();
        Arrays.fill(bestArmorPieces, -1);
        Arrays.fill(bestToolSlots, -1);
    }

    private void open() {
        if (!openInventory) {
            interactionsTimer.reset();
            if (spoof.isEnabled())
                PlayerUtil.openInventory();
            openInventory = true;
        }
    }

    private void close() {
        if (openInventory) {
            if (spoof.isEnabled())
                PlayerUtil.closeInventory();
            openInventory = false;
        }
    }

    private boolean isValidStack(ItemStack stack) {
        if (stack.getItem() instanceof ItemBlock && PlayerUtil.isGoodBlockStack(stack))
            return true;
        else if (stack.getItem().getUnlocalizedName().equals("item.arrow"))
            return true;
        else if (stack.getItem() instanceof ItemEnderPearl)
            return true;
        else if (stack.getItem() instanceof ItemPotion && PlayerUtil.isBuffPotion(stack))
            return true;
        else return stack.getItem() instanceof ItemFood && PlayerUtil.isGoodFood(stack);
    }

    private boolean purgeList(List<Integer> listOfSlots) {
        if (!listOfSlots.isEmpty()) {
            open();
            int slot = listOfSlots.remove(0);
            PlayerUtil.windowClick(slot, 1, PlayerUtil.ClickType.DROP_ITEM);
            if (listOfSlots.isEmpty())
                close();
            return true;
        }
        return false;
    }

    private enum ItemType {
        SWORD, BOW
    }

    private void putItemInSlot(ItemType type, int slot) {
        open();
        switch (type) {
            case SWORD:
                PlayerUtil.windowClick(bestSwordSlot, slot - PlayerUtil.ONLY_HOT_BAR_BEGIN, PlayerUtil.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                break;
            case BOW:
                PlayerUtil.windowClick(bestBowSlot, slot - PlayerUtil.ONLY_HOT_BAR_BEGIN, PlayerUtil.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                break;
        }
        close();
    }

    private void putToolsInSlot(int tool, int[] toolSlots) {
        open();
        int toolSlot = toolSlots[tool];
        PlayerUtil.windowClick(bestToolSlots[tool], toolSlot - PlayerUtil.ONLY_HOT_BAR_BEGIN, PlayerUtil.ClickType.SWAP_WITH_HOT_BAR_SLOT);
        bestToolSlots[tool] = toolSlot;
        close();
    }
}
/*
public NumberSetting clickDelay = new NumberSetting("Click Delay", 150, 10, 300, 10);
    public NumberSetting blockCap = new NumberSetting("Block Cap", 128, 64, 640, 32);
    public NumberSetting openDelay = new NumberSetting("Open Delay", 150, 0, 1000, 10);
    public BooleanSetting combatCheck = new BooleanSetting("Combat Check", true);
    public BooleanSetting autoArmor = new BooleanSetting("Auto Armor", true);
    public BooleanSetting sortHotbar = new BooleanSetting("Sort Hotbar", true);
    public BooleanSetting sortTools = new BooleanSetting("Sort Tools", true);
    public BooleanSetting spoof = new BooleanSetting("Spoof", true);
    public BooleanSetting onOpen = new BooleanSetting("Open Inv", false);
    public BooleanSetting mouseFix = new BooleanSetting("Mouse Fix", false);

    private TimerUtils interactionsTimer = new TimerUtils();
    private TimerUtil openTimer = new TimerUtil();
    private final int[] bestArmorPieces;
    private final List<Integer> trash;
    private final List<Integer> duplicateSwords;
    private final int[] bestToolSlots;
    private final List<Integer> gappleStackSlots;
    private int bestSwordSlot;
    private int bestBowSlot;
    private boolean openInventory;
    private boolean hasOpened = false;
    private boolean hasDelayed = false;
    Slot invSlot;

    public InventoryManager() {
        super("InvManager", Keyboard.KEY_U, "Sorts out your inventory", Category.PLAYER);
        this.addSettings(clickDelay, blockCap, openDelay, combatCheck, autoArmor, sortHotbar, sortTools, spoof, onOpen, mouseFix);

        this.bestArmorPieces = new int[4];
        this.trash = new ArrayList<>();
        this.duplicateSwords = new ArrayList<>();
        this.bestToolSlots = new int[3];
        this.gappleStackSlots = new ArrayList<>();
    }

    @Override
    public void onEnable() {
        this.openInventory = (mc.currentScreen instanceof GuiInventory);
        this.interactionsTimer.reset();
        openTimer.reset();
        hasOpened = false;
        hasDelayed = false;
    }

    @Override
    public void onDisable() {
        this.close();
        hasOpened = false;
        hasDelayed = false;
    }

    public void onEvent(Event e) {
        this.setSuffix(clickDelay.getValue() + "");

        if(e instanceof EventPacket) {
            if(this.openInventory) {
                if(((EventPacket) e).getPacket() instanceof C16PacketClientStatus) {
                    C16PacketClientStatus packet = ((EventPacket) e).getPacket();

                    if(packet.getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                        e.setCancelled(true);
                    }
                } else if(((EventPacket) e).getPacket() instanceof C0DPacketCloseWindow) {
                    e.setCancelled(true);
                }

                if(((EventPacket) e).getPacket() instanceof S2DPacketOpenWindow) {
                    this.close();
                } else if(((EventPacket) e).getPacket() instanceof S2EPacketCloseWindow) {
                    e.setCancelled(true);
                }
            }
        }

        GuiScreen currentScreen;
        long clickDelay;
        boolean foundSword;
        int slot3;
        ItemStack stack;
        int toolType;
        ItemArmor armor;
        int pieceSlot;
        int i;
        int piece;
        int armorPieceSlot;
        ItemStack stack2;
        int currentSlot;
        int slot4;
        int bestGappleSlot;
        int[] toolSlots;
        int[] bestToolSlots;
        int length;
        int j = 0;
        int toolSlot;
        int type;

        if(e instanceof EventMotion) {
            if(e.isPre()) {
                currentScreen = mc.currentScreen;

                if(currentScreen instanceof GuiInventory && onOpen.isEnabled()) {
                    if(!hasOpened) {
                        openTimer.reset();
                        hasOpened = true;
                    } else if(openTimer.hasElapsed((long) openDelay.getValue())) {
                        hasDelayed = true;
                    } else {
                        hasDelayed = false;
                    }
                } else {
                    hasOpened = false;
                    openTimer.reset();
                }

                if ((currentScreen == null && !onOpen.isEnabled()) || currentScreen instanceof GuiInventory && hasDelayed) {
                    clickDelay = (long) this.clickDelay.getValue();
                    if (this.interactionsTimer.hasTimeElapsed(clickDelay, true)) {
                        this.clear();
                        foundSword = false;
                        for (slot3 = 5; slot3 < 45; ++slot3) {
                            stack = vip.Resolute.getStackInSlot(slot3);
                            if (stack != null) {
                                if (stack.getItem() instanceof ItemSword && InventoryUtils.isBestSword(stack)) {
                                    if (foundSword) {
                                        this.duplicateSwords.add(slot3);
                                    }
                                    else if (slot3 != this.bestSwordSlot) {
                                        foundSword = true;
                                        this.bestSwordSlot = slot3;
                                    }
                                }
                                else if (stack.getItem() instanceof ItemTool && InventoryUtils.isBestTool(stack)) {
                                    toolType = InventoryUtils.getToolType(stack);
                                    if (toolType != -1 && slot3 != this.bestToolSlots[toolType]) {
                                        this.bestToolSlots[toolType] = slot3;
                                    }
                                }
                                else if (stack.getItem() instanceof ItemArmor && InventoryUtils.isBestArmor(stack) && autoArmor.isEnabled()) {
                                    armor = (ItemArmor)stack.getItem();
                                    pieceSlot = this.bestArmorPieces[armor.armorType];
                                    if (pieceSlot == -1 || slot3 != pieceSlot) {
                                        this.bestArmorPieces[armor.armorType] = slot3;
                                    }
                                }
                                else if(stack.getItem() instanceof ItemBow && InventoryUtils.isBestBow(stack)) {
                                    if (slot3 != this.bestBowSlot) {
                                        this.bestBowSlot = slot3;
                                    }
                                }
                                else if (stack.getItem() instanceof ItemAppleGold) {
                                    this.gappleStackSlots.add(slot3);
                                }
                                else if (!this.trash.contains(slot3) && !this.isValidStack(stack)) {
                                    this.trash.add(slot3);
                                }
                            }
                        }
                        if(autoArmor.isEnabled()) {
                            for (i = 0; i < this.bestArmorPieces.length; ++i) {
                                piece = this.bestArmorPieces[i];
                                if (piece != -1) {
                                    armorPieceSlot = i + 5;
                                    stack2 = vip.Resolute.getStackInSlot(armorPieceSlot);
                                    if (stack2 == null) {
                                        this.open();

                                        invSlot = mc.thePlayer.openContainer.getSlot(piece);

                                        if(mouseFix.isEnabled()) {
                                            InventoryUtils.legitClick(invSlot, piece, 0, InventoryUtils.ClickType.SHIFT_CLICK);
                                        } else {
                                            InventoryUtils.windowClick(piece, 0, InventoryUtils.ClickType.SHIFT_CLICK);
                                        }

                                        this.close();
                                        return;
                                    }
                                }
                            }
                        }
                        if (!this.combatCheck.isEnabled()) {
                            this.interactionsTimer.reset();
                        }
                        else if (!this.purgeList(this.duplicateSwords)) {
                            currentSlot = 36;
                            if (this.bestSwordSlot != -1) {
                                if (this.bestSwordSlot != currentSlot) {
                                    this.putSwordInSlot(currentSlot);
                                    return;
                                }
                                else {
                                    ++currentSlot;
                                }
                            }
                            if (!this.purgeList(this.trash)) {
                                if (!this.trash.isEmpty()) {
                                    this.open();
                                    slot4 = this.trash.remove(0);

                                    invSlot = mc.thePlayer.openContainer.getSlot(slot4);

                                    if(mouseFix.isEnabled()) {
                                        InventoryUtils.legitClick(invSlot, slot4, 1, InventoryUtils.ClickType.DROP_ITEM);
                                    } else {
                                        InventoryUtils.windowClick(slot4, 1, InventoryUtils.ClickType.DROP_ITEM);
                                    }

                                    if (this.trash.isEmpty()) {
                                        this.close();
                                    }
                                }
                                else if (this.sortHotbar.isEnabled()) {
                                    if (this.bestBowSlot != -1) {
                                        if (this.bestBowSlot != currentSlot) {
                                            this.putBowInSlot(currentSlot);
                                            return;
                                        }
                                        else {
                                            ++currentSlot;
                                        }
                                    }
                                    if (!this.gappleStackSlots.isEmpty()) {
                                        this.gappleStackSlots.sort((slot1, slot2) -> vip.Resolute.getStackInSlot(slot2).stackSize - vip.Resolute.getStackInSlot(slot1).stackSize);
                                        bestGappleSlot = this.gappleStackSlots.get(0);
                                        if (bestGappleSlot != currentSlot) {
                                            this.putGappleInSlot(currentSlot, bestGappleSlot);
                                            this.gappleStackSlots.set(0, currentSlot);
                                            return;
                                        }
                                        else {
                                            ++currentSlot;
                                        }
                                    }
                                    if (this.sortTools.isEnabled()) {
                                        toolSlots = new int[] { currentSlot, currentSlot + 1, currentSlot + 2 };
                                        bestToolSlots = this.bestToolSlots;
                                        for (length = bestToolSlots.length; j < length; ++j) {
                                            toolSlot = bestToolSlots[j];
                                            if (toolSlot != -1) {
                                                type = InventoryUtils.getToolType(vip.Resolute.getStackInSlot(toolSlot));
                                                if (type != -1 && toolSlot != toolSlots[type]) {
                                                    this.putToolsInSlot(type, toolSlots);
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isValidStack(final ItemStack stack) {
        return (stack.hasDisplayName()) || (stack.getItem() instanceof ItemBlock && InventoryUtils.isGoodBlockStack(stack) && (getBlockCount() <= blockCap.getValue())) || (stack.getItem() instanceof ItemPotion && InventoryUtils.isBuffPotion(stack)) || (stack.getItem() instanceof ItemFood && InventoryUtils.isGoodFood(stack)) || (stack.getItem() instanceof ItemEnderPearl) || (isGood(stack.getItem()));
    }

    private boolean purgeList(final List<Integer> listOfSlots) {
        if (!listOfSlots.isEmpty()) {
            this.open();
            final int slot = listOfSlots.remove(0);

            invSlot = mc.thePlayer.openContainer.getSlot(slot);

            if(mouseFix.isEnabled()) {
                InventoryUtils.legitClick(invSlot, slot, 1, InventoryUtils.ClickType.DROP_ITEM);
            } else {
                InventoryUtils.windowClick(slot, 1, InventoryUtils.ClickType.DROP_ITEM);
            }

            if (listOfSlots.isEmpty()) {
                this.close();
            }
            return true;
        }
        return false;
    }

    private int getBlockCount() {
        int blockCount = 0;
        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();
                if (is.getItem() instanceof ItemBlock) {
                    blockCount += is.stackSize;
                }
            }
        }
        return blockCount;
    }

    private void clear() {
        this.trash.clear();
        this.bestBowSlot = -1;
        this.bestSwordSlot = -1;
        this.gappleStackSlots.clear();
        Arrays.fill(this.bestArmorPieces, -1);
        Arrays.fill(this.bestToolSlots, -1);
        this.duplicateSwords.clear();
    }

    private void putSwordInSlot(final int swordSlot) {
        this.open();

        invSlot = mc.thePlayer.openContainer.getSlot(swordSlot);

        if(mouseFix.isEnabled()) {
            InventoryUtils.legitClick(invSlot, mc.thePlayer.inventoryContainer.windowId, this.bestSwordSlot, swordSlot - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
        } else {
            InventoryUtils.windowClick(mc.thePlayer.inventoryContainer.windowId, this.bestSwordSlot, swordSlot - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
        }

        this.bestSwordSlot = swordSlot;

        this.close();
    }

    private void putBowInSlot(final int bowSlot) {
        this.open();


        invSlot = mc.thePlayer.openContainer.getSlot(bowSlot);

        if(mouseFix.isEnabled()) {
            InventoryUtils.legitClick(invSlot, mc.thePlayer.inventoryContainer.windowId, this.bestSwordSlot, bowSlot - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
        } else {
            InventoryUtils.windowClick(mc.thePlayer.inventoryContainer.windowId, this.bestSwordSlot, bowSlot - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
        }

        this.bestBowSlot = bowSlot;

        this.close();
    }

    private void putGappleInSlot(final int gappleSlot, final int slotIn) {
        this.open();

        invSlot = mc.thePlayer.openContainer.getSlot(gappleSlot);

        if(mouseFix.isEnabled()) {
            InventoryUtils.legitClick(invSlot, mc.thePlayer.inventoryContainer.windowId, slotIn, gappleSlot - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
        } else {
            InventoryUtils.windowClick(mc.thePlayer.inventoryContainer.windowId, slotIn, gappleSlot - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
        }



        this.close();
    }

    private void putToolsInSlot(final int tool, final int[] toolSlots) {
        this.open();
        final int toolSlot = toolSlots[tool];

        invSlot = mc.thePlayer.openContainer.getSlot(toolSlot);

        if(mouseFix.isEnabled()) {
            InventoryUtils.legitClick(invSlot, mc.thePlayer.inventoryContainer.windowId, this.bestToolSlots[tool], toolSlot - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
        } else {
            InventoryUtils.windowClick(mc.thePlayer.inventoryContainer.windowId, this.bestToolSlots[tool], toolSlot - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
        }



        this.bestToolSlots[tool] = toolSlot;
        this.close();
    }

    private void open() {
        if (!this.openInventory) {
            this.interactionsTimer.reset();
            if (this.spoof.isEnabled()) {
                InventoryUtils.openInventory();
            }
            this.openInventory = true;
        }
    }

    private void close() {
        if (this.openInventory) {
            if (this.spoof.isEnabled()) {
                InventoryUtils.closeInventory();
            }
            this.openInventory = false;
        }
    }

    private boolean isGood(Item i) {
        return i.getUnlocalizedName().contains("arrow");
    }

 */
