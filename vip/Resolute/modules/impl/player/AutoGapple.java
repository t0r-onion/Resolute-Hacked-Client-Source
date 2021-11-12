package vip.Resolute.modules.impl.player;

import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.misc.TimerUtil;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

public class AutoGapple extends Module {

    public NumberSetting delay = new NumberSetting("Delay", 150, 0, 1000, 5);
    public NumberSetting health = new NumberSetting("Health", 5, 1, 9, 1);

    public ModeSetting eatMode = new ModeSetting("Consume Mode", "Instant", "Instant");

    public TimerUtil timer = new TimerUtil();

    public AutoGapple() {
        super("AutoGapple", 0, "Automatically eats gapples", Category.PLAYER);
        this.addSettings(delay, health, eatMode);
    }

    public void onEvent(Event e) {
        if(e instanceof EventMotion) {
            if (!timer.hasElapsed((long) delay.getValue()))
                return;
            if (mc.thePlayer.getHealth() <= health.getValue() * 2.0F){
                doEat(false);
                timer.reset();
            }
        }
    }

    private void doEat(boolean warn) {
        int gappleInHotbar = findItem(36, 54, Items.golden_apple);
        if(gappleInHotbar != -1 ){
            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(gappleInHotbar - 36));
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
            for(int i = 0; i <= 35; i++) {
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer(mc.thePlayer.onGround));
            }
            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            Resolute.addChatMessage("Ate gapple");
        } else {
            Resolute.addChatMessage("No gapples in hotbar");
        }
    }

    public static int findItem(final int startSlot, final int endSlot, final Item item) {
        for(int i = startSlot; i < endSlot; i++) {
            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if(stack != null && stack.getItem() == item)
                return i;
        }
        return -1;
    }
}
