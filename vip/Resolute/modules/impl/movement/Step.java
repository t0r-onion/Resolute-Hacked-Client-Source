package vip.Resolute.modules.impl.movement;

import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.*;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.util.misc.TimerUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import vip.Resolute.util.movement.MovementUtils;

public class Step extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "NCP", "NCP");

    public BooleanSetting lessPackets = new BooleanSetting("Less Packets", true, () -> mode.is("NCP"));

    public static boolean cancelStep;
    private final double[] offsets = new double[] { 0.41999998688697815, 0.7531999945640564 };
    private float timerWhenStepping;
    private boolean cancelMorePackets;
    private byte cancelledPackets;

    public Step() {
        super("Step", 0, "Allows for a higher block step", Category.MOVEMENT);
        this.addSettings(mode);
    }

    public void onDisable() {
        mc.timer.timerSpeed = 1;
    }

    public void onEvent(Event e) {
        this.setSuffix(mode.getMode());

        double steppedHeight;
        double[] offsets;
        int length;
        int i = 0;
        double offset;

        if(e instanceof EventStep) {
            EventStep event = (EventStep) e;

            if (!MovementUtils.isInLiquid() && MovementUtils.isOnGround()) {
                if (e.isPre()) {
                    event.setStepHeight(Step.cancelStep ? 0.0f : 1.0f);
                }
                else {
                    steppedHeight = event.getStepHeight();
                    offsets = this.offsets;
                    for (length = 0; i < length; ++i) {
                        offset = offsets[i];
                        mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + offset * steppedHeight, mc.thePlayer.posZ, false));
                    }
                    this.timerWhenStepping = 1.0f / (this.offsets.length + 1);
                    this.cancelMorePackets = true;
                }
            }
        }

        if(e instanceof EventPacket) {
            if (this.lessPackets.isEnabled() && ((EventPacket) e).getPacket() instanceof C03PacketPlayer) {
                if (this.cancelledPackets > 0) {
                    this.cancelMorePackets = false;
                    this.cancelledPackets = 0;
                    mc.timer.timerSpeed = 1.0f;
                }
                if (this.cancelMorePackets) {
                    mc.timer.timerSpeed = this.timerWhenStepping;
                    ++this.cancelledPackets;
                }
            }
        }
    }
}



