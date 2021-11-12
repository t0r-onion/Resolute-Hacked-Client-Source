package vip.Resolute.modules.impl.movement;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventSlow;
import vip.Resolute.events.impl.EventUpdate;
import vip.Resolute.modules.Module;
import vip.Resolute.modules.impl.combat.KillAura;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.util.misc.TimerUtils;
import vip.Resolute.util.movement.MovementUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;

public class NoSlowdown extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "NCP", "NCP", "AAC5", "Vanilla");

    private static final C07PacketPlayerDigging PLAYER_DIGGING;
    private static final C08PacketPlayerBlockPlacement BLOCK_PLACEMENT;

    public static boolean enabled = false;

    TimerUtils timer = new TimerUtils();

    public NoSlowdown() {
        super("NoSlow", Keyboard.KEY_NONE, "Removes item slowdown", Category.MOVEMENT);
        this.addSettings(mode);
    }

    static {
        PLAYER_DIGGING = new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
        BLOCK_PLACEMENT = new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f);
    }

    public void onEnable() {
        mc.timer.timerSpeed = 1.0f;
        mc.thePlayer.speedInAir = 0.02f;
        enabled = true;
        super.onEnable();
    }

    public void onDisable() {
        mc.timer.timerSpeed = 1.0f;
        enabled = false;
        mc.thePlayer.speedInAir = 0.02f;
        super.onDisable();
    }

    public void onEvent(Event e) {
        this.setSuffix(mode.getMode());

        if(e instanceof EventSlow) {
            e.setCancelled(true);
        }

        if(e instanceof EventMotion) {
            EventMotion event = (EventMotion) e;

            if(mode.is("AAC5")) {
                ItemStack heldItem = mc.thePlayer.getHeldItem();
                if(mc.thePlayer.isUsingItem() && heldItem != null && !(heldItem.getItem() instanceof ItemSword)){
                    if(e.isPre()) {
                        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                    }
                } else {
                    if (!mc.thePlayer.isBlocking() && !KillAura.blocking) {
                        return;
                    }

                    sendPacket(event,true,true,true,200,false, false);
                }
            }


            if(mode.is("NCP")) {
                if(MovementUtils.isMoving() && !KillAura.blocking && mc.thePlayer.isBlocking()) {
                    if (e.isPre()) {
                        mc.getNetHandler().sendPacketNoEvent(NoSlowdown.PLAYER_DIGGING);
                    }
                    else {
                        mc.getNetHandler().sendPacketNoEvent(NoSlowdown.BLOCK_PLACEMENT);
                    }
                }
            }
        }

        if(e instanceof EventUpdate) {
            if(mode.is("Vanilla")) {
                if((mc.thePlayer.isBlocking() || mc.thePlayer.isEating())) {}
            }
        }
    }

    private void sendPacket(EventMotion event, boolean sendC07, boolean sendC08, boolean delay, long delayValue, boolean onGround, boolean watchDog) {
        C07PacketPlayerDigging digging = new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(-1,-1,-1), EnumFacing.DOWN);
        C08PacketPlayerBlockPlacement blockPlace = new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem());
        C08PacketPlayerBlockPlacement blockMent = new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0f, 0f, 0f);
        if(onGround && !mc.thePlayer.onGround) {
            return;
        }
        if(sendC07 && event.isPre()) {
            if(delay && timer.hasTimeElapsed(delayValue, true)) {
                mc.getNetHandler().addToSendQueue(digging);
            } else if(!delay) {
                mc.getNetHandler().addToSendQueue(digging);
            }
        }
        if(sendC08 && !event.isPre()) {
            if(delay && timer.hasTimeElapsed(delayValue, true) && !watchDog) {
                mc.getNetHandler().addToSendQueue(blockPlace);
            } else if(!delay && !watchDog) {
                mc.getNetHandler().addToSendQueue(blockPlace);
            } else if(watchDog) {
                mc.getNetHandler().addToSendQueue(blockMent);
            }
        }
    }

    private boolean isBlocking() {
        return isHoldingSword() && mc.thePlayer.isBlocking();
    }

    public static boolean isHoldingSword() {
        return mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword;
    }
}
