package vip.Resolute.modules.impl.combat;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.movement.MovementUtils;
import vip.Resolute.util.misc.TimerUtils;
import vip.Resolute.util.world.RandomUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;

public class Criticals extends Module {

    private int groundTicks;
    TimerUtils timer = new TimerUtils();
    private final double[] ncpOffsets = {0.06252f, 0.0f};
    private final double[] watchdogOffsets = {0.056f, 0.016f, 0.003f};

    public ModeSetting mode = new ModeSetting("Mode", "Watchdog",  "Watchdog", "Watchdog", "Packet", "Ground", "NoGround");
    public ModeSetting watchdogMode = new ModeSetting("Watchdog Mode", "Packet 1", this::isModeSelected, "Packet 1", "Packet 2", "Packet 3", "Packet 4");
    public NumberSetting delay = new NumberSetting("Delay", 500, 0 ,2000, 10);

    public boolean isModeSelected() {
        return this.mode.is("Watchdog");
    }

    public Criticals() {
        super("Criticals", 0, "Allows for a critical every hit", Category.COMBAT);
        this.addSettings(mode, watchdogMode, delay);
    }

    public void onEvent(Event e) {
        if(mode.is("Watchdog")) {
            this.setSuffix(watchdogMode.getMode());
        } else {
            this.setSuffix(mode.getMode());
        }

        if(e instanceof EventMotion) {
            EventMotion eventMotion = (EventMotion) e;
            if(mode.is("NoGround")) {
                if(!(mc.thePlayer.fallDistance > 3)) {
                    eventMotion.setOnGround(false);
                }
            }
        }

        if(e instanceof EventPacket) {
            if((((EventPacket) e).getPacket() instanceof C03PacketPlayer)) {
                C03PacketPlayer c03PacketPlayer = (C03PacketPlayer) ((EventPacket) e).getPacket();
                if(mode.is("Ground")) {
                    if(mc.thePlayer.fallDistance > 0) {
                        c03PacketPlayer.onGround = true;
                    }

                    if(mc.thePlayer.onGround && KillAura.target != null && ((((EventPacket) e).getPacket() instanceof C03PacketPlayer.C04PacketPlayerPosition) || (((EventPacket) e).getPacket() instanceof C03PacketPlayer.C06PacketPlayerPosLook))) {
                        c03PacketPlayer.onGround = false;
                        mc.thePlayer.onCriticalHit(KillAura.target);
                    }
                }
            }

            if(((EventPacket) e).getPacket() instanceof C0APacketAnimation) {
                if(mode.is("Packet")) {
                    if(timer.hasTimeElapsed((long) delay.getValue(), true)) {
                        if(MovementUtils.isOnGround()) {
                            if(KillAura.target != null) {
                                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + .1625, mc.thePlayer.posZ, false));
                                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 4.0E-6, mc.thePlayer.posZ, false));
                                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0E-6, mc.thePlayer.posZ, false));
                                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer());
                                mc.thePlayer.onCriticalHit(KillAura.target);
                            }
                        }
                    }
                }

                if(mode.is("Watchdog")) {
                    if (!Criticals.mc.thePlayer.onGround || Criticals.mc.thePlayer.isOnLadder() || Criticals.mc.thePlayer.isInWeb || Criticals.mc.thePlayer.isInWater() || Criticals.mc.thePlayer.isInLava() || Criticals.mc.thePlayer.ridingEntity != null) {
                        return;
                    }

                    if(watchdogMode.is("Packet 1")) {
                        if(timer.hasTimeElapsed((long) delay.getValue(), true)) {
                            for(double offset : watchdogOffsets) {
                                if(MovementUtils.isOnGround()) {
                                    if(KillAura.target != null) {
                                        mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + offset + (Math.random() * 0.0003F), mc.thePlayer.posZ, false));
                                        mc.thePlayer.onCriticalHit(KillAura.target);
                                    }
                                }
                            }
                        }
                    }

                    if(watchdogMode.is("Packet 2")) {
                        if(timer.hasTimeElapsed((long) delay.getValue(), true)) {
                            for(double offset : watchdogOffsets) {
                                if(MovementUtils.isOnGround()) {
                                    if(KillAura.target != null) {
                                        mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + offset + 0.045, mc.thePlayer.posZ, false));
                                        mc.thePlayer.onCriticalHit(KillAura.target);
                                    }
                                }
                            }
                        }
                    }

                    if(watchdogMode.is("Packet 3")) {
                        double[] arrayOfDouble;
                        double random = RandomUtil.getRandom(4.0E-7, 4.0E-5);

                        if(timer.hasTimeElapsed((long) delay.getValue(), true)) {
                            for(double value : arrayOfDouble = new double[]{0.007017625 + random, 0.007349825 + random, 0.006102874 + random}) {
                                if(MovementUtils.isOnGround()) {
                                    if(KillAura.target != null) {
                                        mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + value, mc.thePlayer.posZ, false));
                                        mc.thePlayer.onCriticalHit(KillAura.target);
                                    }
                                }
                            }
                        }
                    }

                    if(watchdogMode.is("Packet 4")) {
                        if(timer.hasTimeElapsed((long) delay.getValue(), true)) {
                            if(MovementUtils.isOnGround()) {
                                if (KillAura.target != null) {
                                    for(int i = 0; i <= 2; ++i) {
                                        mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.101 - (double)i * 0.02, mc.thePlayer.posZ, false));
                                        mc.thePlayer.onCriticalHit(KillAura.target);
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
