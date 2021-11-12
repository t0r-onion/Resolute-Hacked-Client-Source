package vip.Resolute.modules.impl.combat;

import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.events.impl.EventUpdate;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.misc.TimerUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

public class Velocity extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Packet", "Packet", "Reduce", "Matrix", "Stack", "AACP");

    public NumberSetting horizontal = new NumberSetting("Horizontal", 0.0f, this::isModeSelected, 0.0f, 100.0f, 1.0f);
    public NumberSetting vertical = new NumberSetting("Vertical",0.0f, this::isModeSelected, 0.0f, 100.0f, 1.0f);

    public NumberSetting reduce = new NumberSetting("Reduce",0.7, this::isReduceSelected,0.1, 1.0, 0.05);

    TimerUtils timer = new TimerUtils();

    private int hitTimes = 0;

    private boolean hurt = false;

    public boolean isModeSelected() {
        return this.mode.is("Percentage");
    }

    public boolean isReduceSelected() {
        return this.mode.is("Reduce");
    }

    public Velocity() {
        super("Velocity", 0, "Adjusts player velocity", Category.COMBAT);
        this.addSettings(mode, horizontal, vertical, reduce);
    }

    public void onEnable() {
        hitTimes = 0;
    }

    public void onDisable() {
        mc.timer.timerSpeed = 1;
        mc.thePlayer.speedInAir = 0.02f;
    }

    public void onEvent(Event e) {
        this.setSuffix(mode.getMode());


        if(e instanceof EventUpdate) {
            if(mode.is("AACP")) {
                if(!mc.thePlayer.onGround) {
                    if(mc.thePlayer.hurtTime != 0) {
                        if(hurt) {
                            mc.thePlayer.speedInAir = 0.02f;
                            mc.thePlayer.motionX *= 0.7f;
                            mc.thePlayer.motionZ *= 0.7f;
                        }
                    }
                } else {
                    if(timer.hasReached(80L)) {
                        hurt = false;
                        mc.thePlayer.speedInAir = 0.02f;
                    }
                }
            }

            if(mode.is("Reduce")) {
                if(mc.thePlayer.hurtTime > 4 && mc.thePlayer.hurtTime < 9) {
                    mc.thePlayer.motionX *= reduce.getValue();
                    mc.thePlayer.motionZ *= reduce.getValue();
                }
            }
        }

        if(e instanceof EventMotion) {
            if(mode.is("Matrix")) {
                if (mc.thePlayer.hurtTime > 0) {
                    if (mc.thePlayer.onGround) {
                        if (mc.thePlayer.hurtTime <= 6) {
                            mc.thePlayer.motionX *= 0.70;
                            mc.thePlayer.motionZ *= 0.70;
                        }
                        if (mc.thePlayer.hurtTime <= 5) {
                            mc.thePlayer.motionX *= 0.80;
                            mc.thePlayer.motionZ *= 0.80;
                        }
                    } else if (mc.thePlayer.hurtTime <= 10) {
                        mc.thePlayer.motionX *= 0.60;
                        mc.thePlayer.motionZ *= 0.60;
                    }
                }
            }
        }

        if(e instanceof EventPacket) {
            if(mode.is("Packet")) {
                if(((EventPacket) e).getPacket() instanceof S12PacketEntityVelocity) {
                    S12PacketEntityVelocity packet = ((EventPacket) e).getPacket();

                    if(packet.getEntityID() == mc.thePlayer.getEntityId()) {

                        double horiz = horizontal.getValue();
                        double vert = vertical.getValue();

                        if(horiz == 0 && vert == 0) {
                            e.setCancelled(true);
                        } else {
                            packet.motionX *= horiz / 100.0;
                            packet.motionY *= vert / 100.0;
                            packet.motionZ *= horiz / 100.0;
                        }
                    }
                }
            }

            if(((EventPacket) e).getPacket() instanceof S12PacketEntityVelocity) {
                if(mode.is("AAC5.2.0")) {
                        e.setCancelled(true);
                        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 1.7976931348623157E+308, mc.thePlayer.posZ, true));
                }
            }

            if(((EventPacket) e).getPacket() instanceof S12PacketEntityVelocity) {
                if(mode.is("AACP")) {
                    if(mc.thePlayer == null) {
                        return;
                    }

                    timer.reset();
                    hurt = true;
                }
            }

            if(((EventPacket) e).getPacket() instanceof S12PacketEntityVelocity) {
                if(mode.is("Stack")) {
                    S12PacketEntityVelocity packet = ((EventPacket) e).getPacket();

                    if(packet.getEntityID() == mc.thePlayer.getEntityId()) {
                        if(!(hitTimes == 2)) {
                            e.setCancelled(true);
                            hitTimes++;
                        } else {
                            hitTimes = 0;
                        }
                    }
                }
            }

            if(((EventPacket) e).getPacket() instanceof S27PacketExplosion) {
                e.setCancelled(true);
            }
        }
    }
}
