package vip.Resolute.modules.impl.player;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.events.impl.EventRender3D;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.render.RenderUtils;
import vip.Resolute.util.misc.TimerUtil;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Blink extends Module {
    private ArrayList<Packet> packetList = new ArrayList<>();
    private List<Vec3> crumbs = new CopyOnWriteArrayList<>();

    private BooleanSetting trail = new BooleanSetting("Trail", true);

    private BooleanSetting blinklag = new BooleanSetting("BlinkLag", false);
    private NumberSetting delay = new NumberSetting("Blink Delay", 5, 2, 30, 1);

    private TimerUtil timer = new TimerUtil();

    public Blink() {
        super("Blink", 0, "Cancels movement packets", Category.PLAYER);
        this.addSettings(trail, blinklag, delay);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        crumbs.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        crumbs.clear();
        try {
            for (Packet packets : packetList) {
                mc.getNetHandler().sendPacketNoEvent(packets);
            }
            packetList.clear();
        }
        catch (final ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    public void onEvent(Event e) {
        if(e instanceof EventPacket) {
            if(((EventPacket) e).getPacket() instanceof C0APacketAnimation || ((EventPacket) e).getPacket() instanceof C03PacketPlayer || ((EventPacket) e).getPacket() instanceof C07PacketPlayerDigging || ((EventPacket) e).getPacket() instanceof C08PacketPlayerBlockPlacement) {
                if(blinklag.isEnabled()) {
                    if (mc.thePlayer.ticksExisted % delay.getValue() == 0) {
                        try {
                            for (Packet packets : packetList) {
                                mc.getNetHandler().sendPacketNoEvent(packets);
                            }
                            packetList.clear();
                            crumbs.clear();
                        }
                        catch (final ConcurrentModificationException exception) {
                            exception.printStackTrace();
                        }
                    } else {
                        e.setCancelled(true);
                        packetList.add(((EventPacket) e).getPacket());
                    }
                } else {
                    e.setCancelled(true);
                    packetList.add(((EventPacket) e).getPacket());
                }
            }
        }

        if(e instanceof EventRender3D) {
            if (timer.hasElapsed(10)) {
                crumbs.add(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
                timer.reset();
            }
            if (!crumbs.isEmpty() && crumbs.size() > 2) {
                for (int i = 1; i < crumbs.size(); i++) {
                    Vec3 vecBegin = crumbs.get(i - 1);
                    Vec3 vecEnd = crumbs.get(i);
                    int color = getColor(255, 255, 255);
                    float beginX = (float) ((float) vecBegin.xCoord - RenderManager.renderPosX);
                    float beginY = (float) ((float) vecBegin.yCoord - RenderManager.renderPosY);
                    float beginZ = (float) ((float) vecBegin.zCoord - RenderManager.renderPosZ);
                    float endX = (float) ((float) vecEnd.xCoord - RenderManager.renderPosX);
                    float endY = (float) ((float) vecEnd.yCoord - RenderManager.renderPosY);
                    float endZ = (float) ((float) vecEnd.zCoord - RenderManager.renderPosZ);
                    final boolean bobbing = mc.gameSettings.viewBobbing;
                    mc.gameSettings.viewBobbing = false;
                    RenderUtils.drawLine3D(beginX, beginY, beginZ, endX, endY, endZ, color);
                    mc.gameSettings.viewBobbing = bobbing;
                }
            }
        }
    }

    public static int getColor(int red, int green, int blue) {
        return getColor(red, green, blue, 255);
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        int color = 0;
        color |= alpha << 24;
        color |= red << 16;
        color |= green << 8;
        color |= blue;
        return color;
    }
}
