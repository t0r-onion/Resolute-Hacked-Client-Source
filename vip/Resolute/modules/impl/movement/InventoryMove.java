package vip.Resolute.modules.impl.movement;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.ui.click.skeet.SkeetUI;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;

public class InventoryMove extends Module {

    public BooleanSetting cancel = new BooleanSetting("Cancel Packet", false);

    public static boolean enabled = false;

    public InventoryMove() {
        super("InventoryMove", 0, "Allows you to move while in inventory", Category.MOVEMENT);
        this.addSettings(cancel);
    }

    public void onEnable() {
        enabled = true;
    }

    public void onDisable() { enabled = false; }


    public void onEvent(Event e) {
        this.setSuffix("");

        if(e instanceof EventPacket) {
            if(cancel.isEnabled()) {
                if(((EventPacket) e).getPacket() instanceof C16PacketClientStatus || ((EventPacket) e).getPacket() instanceof C0DPacketCloseWindow) {
                    e.setCancelled(true);
                }
            }
        }

        /*
        if (mc.currentScreen instanceof GuiContainer || mc.currentScreen instanceof SkeetUI) {
            mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward);
            mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack);
            mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight);
            mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft);
            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump);
            mc.gameSettings.keyBindSprint.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSprint);
        }

         */
    }

    /*
    public void onDisable() {
        enabled = false;

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindForward) || mc.currentScreen != null)
            mc.gameSettings.keyBindForward.pressed = false;
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindBack) || mc.currentScreen != null)
            mc.gameSettings.keyBindBack.pressed = false;
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight) || mc.currentScreen != null)
            mc.gameSettings.keyBindRight.pressed = false;
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft) || mc.currentScreen != null)
            mc.gameSettings.keyBindLeft.pressed = false;
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindJump) || mc.currentScreen != null)
            mc.gameSettings.keyBindJump.pressed = false;
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSprint) || mc.currentScreen != null)
            mc.gameSettings.keyBindSprint.pressed = false;
        super.onDisable();
    }

     */
}
