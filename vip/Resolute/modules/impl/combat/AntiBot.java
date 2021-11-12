package vip.Resolute.modules.impl.combat;

import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.events.impl.EventUpdate;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ModeSetting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class AntiBot extends Module {
    public static ModeSetting mode = new ModeSetting("Mode", "Hypixel", "Mineplex", "Hypixel", "Advanced");

    public BooleanSetting botKiller = new BooleanSetting("Bot Killer", false, () -> mode.is("Hypixel"));

    private final String[] strings = new String[]{"1st Killer - ", "1st Place - ", "You died! Want to play again? Click here!", " - Damage Dealt - ", "1st - ", "Winning Team - ", "Winners: ", "Winner: ", "Winning Team: ", " win the game!", "1st Place: ", "Last team standing!", "Winner #1 (", "Top Survivors", "Winners - "};

    public static List<EntityPlayer> watchdogBots = new ArrayList<>();

    public static ArrayList bots = new ArrayList();
    private static final ArrayList spawnedBots = new ArrayList();

    public static boolean enabled = false;

    @Override
    public void onEnable() {
        super.onEnable();
        enabled = true;
        watchdogBots.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        enabled = false;
        watchdogBots.clear();
    }

    public AntiBot() {
        super("AntiBot", 0, "Removes anticheat bots", Category.COMBAT);
        this.addSettings(mode, botKiller);
    }

    public void onEvent(Event e) {
        this.setSuffix(mode.getMode());

        if(e instanceof EventPacket) {
            if(mode.is("Advanced")) {
                if(((EventPacket) e).getPacket() instanceof S0CPacketSpawnPlayer) {
                    S0CPacketSpawnPlayer packet = (S0CPacketSpawnPlayer)((EventPacket) e).getPacket();
                    double posX = packet.getX() / 32D;
                    double posY = packet.getY() / 32D;
                    double posZ = packet.getZ() / 32D;

                    double diffX = mc.thePlayer.posX - posX;
                    double diffY = mc.thePlayer.posY - posY;
                    double diffZ = mc.thePlayer.posZ - posZ;

                    double dist = MathHelper.sqrt_double(diffX * diffX + diffY * diffY + diffZ * diffZ);

                    if (dist <= 17 && posY > mc.thePlayer.posY + 1 && (posX != mc.thePlayer.posX && posY != mc.thePlayer.posY && posZ != mc.thePlayer.posZ)) {
                        e.setCancelled(true);
                    }
                }
            }
        }

        if (e instanceof EventUpdate && e.isPre()) {
            if(mode.is("Hypixel")) {
                if (mc.thePlayer.ticksExisted <= 500) {
                    for (EntityPlayer entity : mc.theWorld.playerEntities) {
                        if (entity.getDistanceToEntity(mc.thePlayer) <= 17) {
                            if (Math.abs(mc.thePlayer.posY - entity.posY) > 2) {
                                if (!isOnSameTeam(entity) && entity != mc.thePlayer && !watchdogBots.contains(entity) && entity.ticksExisted != 0 && entity.ticksExisted <= 10) {
                                    watchdogBots.add(entity);
                                    Resolute.addChatMessage("Added bot: " + entity.getGameProfile().getName() + ", Distance: " + entity.getDistanceToEntity(mc.thePlayer) + ", Ticks Existed: " + entity.ticksExisted);
                                }
                            }
                        }
                    }
                }

                if(botKiller.isEnabled()) {
                    if(watchdogBots.isEmpty()) return;

                    watchdogBots.forEach(wdBots -> mc.theWorld.removeEntity(wdBots));
                }
            }

            if(mode.is("Hylex")) {
                for (Entity entity : mc.theWorld.loadedEntityList) {
                    if (entity instanceof EntityArmorStand) {
                        this.mc.theWorld.removeEntity(entity);
                    }
                }
            }

            if(mode.is("TabList")) {
                for (EntityPlayer player : mc.theWorld.playerEntities) {
                    if (player == mc.thePlayer)
                        continue;
                    if (!GuiPlayerTabOverlay.getPlayers().contains(player)) bots.add(player);
                }
            }

            if (mode.is("Mineplex")) {
                if (mc.thePlayer.ticksExisted % 50 == 0) {
                    spawnedBots.clear();
                }

                for (Object o : mc.theWorld.loadedEntityList) {
                    Entity en = (Entity) o;
                    if (en instanceof EntityPlayer && !(en instanceof EntityPlayerSP)) {
                        String customname = en.getCustomNameTag();
                        if (customname == "" && !spawnedBots.contains(en))
                            spawnedBots.add(en);
                    }
                }

                spawnedBots.forEach(mineplexBots -> mc.theWorld.removeEntity((Entity) mineplexBots));
            }
        }
    }

    public static boolean isOnSameTeam(EntityLivingBase entity) {
        if (entity.getTeam() != null && mc.thePlayer.getTeam() != null) {
            char c1 = entity.getDisplayName().getFormattedText().charAt(1);
            char c2 = mc.thePlayer.getDisplayName().getFormattedText().charAt(1);
            return c1 == c2;
        } else {
            return false;
        }
    }

    public static boolean isInTablist (EntityLivingBase player){
        if (mc.isSingleplayer()) {
            return true;
        }
        for (Object o : mc.getNetHandler().getPlayerInfoMap()) {
            NetworkPlayerInfo playerInfo = (NetworkPlayerInfo) o;
            if (playerInfo.getGameProfile().getName().equalsIgnoreCase(((EntityPlayer) player).getGameProfile().getName())) {
                return true;
            }
        }
        return false;
    }
}
