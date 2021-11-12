package vip.Resolute;

import vip.Resolute.command.CommandManager;
import vip.Resolute.config.ConfigManager;
import vip.Resolute.config.SaveLoad;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventChat;
import vip.Resolute.events.impl.EventKey;
import vip.Resolute.modules.Module;
import vip.Resolute.modules.impl.combat.*;
import vip.Resolute.modules.impl.exploit.*;
import vip.Resolute.modules.impl.movement.*;
import vip.Resolute.modules.impl.player.*;
import vip.Resolute.modules.impl.render.*;

import vip.Resolute.ui.login.system.AccountManager;
import vip.Resolute.ui.notification.NotificationManager;
import vip.Resolute.util.misc.TimerUtil;
import vip.Resolute.util.misc.TimerUtils;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.font.MinecraftFontRenderer;
import vip.Resolute.util.rpc.DiscordRP;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Session;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Resolute {
    public static Minecraft mc = Minecraft.getMinecraft();
    public static String selectedCategory = String.valueOf(Module.Category.COMBAT);
    public static String name = "Resolute", build = "211026";
    public static String fullname = name + " " + build;

    public static String loginName;

    public static int color = 0xF00EEFF;

    public static float posX, posY, boolTranslation;

    public boolean ebicauth;

    public static String APIKey = "";

    private static File directory;

    public static CopyOnWriteArrayList<Module> modules = new CopyOnWriteArrayList<Module>();



    private static final TimerUtils inventoryTimer = new TimerUtils();
    public static CommandManager commandManager = new CommandManager();

    public static Resolute instance = new Resolute();

    public static SaveLoad saveLoad;

    public static AccountManager accountManager;

    public String username;
    public String uuid;

    public static boolean authorized;
    public static MinecraftFontRenderer fontRenderer = null;

    public static NotificationManager notificationManager;
    public static DiscordRP discordRP = new DiscordRP();
    public static ConfigManager configManager = new ConfigManager();
    public static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static TimerUtil sessionTime = new TimerUtil();

    private static String alt;
    private Proxy proxy = Proxy.NO_PROXY;
    public String api = "";

    public static void startup() {
        Display.setTitle(fullname);

        directory = new File(Minecraft.getMinecraft().mcDataDir, "Resolute");
        if (!directory.exists()) {
            directory.mkdir();
        }
        discordRP.start();
        sessionTime.reset();
        notificationManager = new NotificationManager();
        authorized = false;

        accountManager = new AccountManager(directory);

        saveLoad = new SaveLoad("");

        FontUtil.bootstrap();


        //COMBAT
        modules.add(new AntiBot());
        modules.add(new Velocity());
        modules.add(new AutoPot());
        modules.add(new KillAura());
        modules.add(new Criticals());
        modules.add(new AimAssist());
        modules.add(new AutoClicker());
        modules.add(new TPAura());
        modules.add(new Reach());
        modules.add(new DelayRemover());
        modules.add(new Hitboxes());
        modules.add(new AntiAim());
        modules.add(new AimBot());

        //MOVEMENT
        modules.add(new Fly());
        modules.add(new Sprint());
        modules.add(new Speed());
        modules.add(new Step());
        modules.add(new NoSlowdown());
        modules.add(new Strafe());
        modules.add(new Phase());
        modules.add(new InventoryMove());
        modules.add(new ScaffoldOld());
        modules.add(new LongJump());
        modules.add(new HighJump());
        modules.add(new Scaffold());
        modules.add(new TargetStrafe());
        modules.add(new Jesus());
        modules.add(new Graph());

        //PLAYER
        modules.add(new NoFall());
        modules.add(new NoRotate());
        modules.add(new Breaker());
        modules.add(new FastPlace());
        modules.add(new Timer());
        modules.add(new FastBreak());
        modules.add(new AutoServer());
        modules.add(new AutoTool());
        modules.add(new ChestStealer());
        modules.add(new InventoryManager());
        modules.add(new Blink());
        modules.add(new FastEat());
        modules.add(new Regen());
        modules.add(new Safewalk());
        modules.add(new Eagle());
        modules.add(new AutoGapple());
        modules.add(new StreamerMode());
        modules.add(new AutoSoup());
        modules.add(new Emote());

        //RENDER
        modules.add(new ClickGUI());
        modules.add(new Overlay());
        modules.add(new Chams());
        modules.add(new Camera());
        modules.add(new Animation());
        modules.add(new Trajectories());
        modules.add(new Ambience());
        modules.add(new ChestESP());
        modules.add(new BlockOverlay());
        modules.add(new Glint());
        modules.add(new ChinaHat());
        modules.add(new ESP());
        modules.add(new Crosshair());
        modules.add(new FullBright());
        modules.add(new MotionPredict());
        modules.add(new HitMarkers());
        modules.add(new OffscreenESP());
        modules.add(new FakeFPS());

        //EXPLOIT
        modules.add(new Freecam());
        modules.add(new Disabler());
        modules.add(new AntiVoid());
        modules.add(new Crasher());
        modules.add(new AntiTabComplete());
        modules.add(new LightningDetector());
        modules.add(new StaffDetector());
        modules.add(new MineplexDisabler());
        modules.add(new HackerDetector());
        modules.add(new PluginViewer());

        //MISC
        modules.add(new KillSults());
        modules.add(new TimerSlowdown());
        modules.add(new AntiLixo());
        modules.add(new TargetSpammer());
        modules.add(new LunarSpoofer());
    }

    public static void shutdown() {
        if(saveLoad != null) {
            saveLoad.save();
        }
        if (Resolute.getConfigManager().saveConfig("shutdownConfig")) {
            System.out.println("Saved Config");
        }
        discordRP.shutdown();
        accountManager.save();
    }

    public static void keyPress(int key) {
        Resolute.onEvent(new EventKey(key));

        for (Module m : modules) {
            if (m.getKey() == key) {
                m.toggle();
            }
        }
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public static MinecraftFontRenderer getFontRenderer() {
        if(Overlay.cfont.is("Client")) {
            fontRenderer = FontUtil.clientfont;
        }

        if(Overlay.cfont.is("Moon")) {
            fontRenderer = FontUtil.moon;
        }

        if(Overlay.cfont.is("SF Large")) {
            fontRenderer = FontUtil.summer;
        }

        if(Overlay.cfont.is("SF")) {
            fontRenderer = FontUtil.sf;
        }

        if(Overlay.cfont.is("Tahoma")) {
            fontRenderer = FontUtil.tahoma;
        }

        if(Overlay.cfont.is("Roboto")) {
            fontRenderer = FontUtil.robo;
        }

        return fontRenderer;
    }

    public DiscordRP getDiscordRP() {
        return discordRP;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public void setResoluteName(String newname) {
        this.username = newname;
    }

    public static ExecutorService executorService() {
        return executorService;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public static AccountManager getAccountManager() {
        return accountManager;
    }

    public static Resolute getInstance() {
        return instance;
    }

    public static NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public void setAPI(String api){ this.api = api; }

    public String[] getSpectatorAlt() {
        return alt == null ? null : alt.split(":");
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public Proxy getProxy() {
        return proxy;
    }



    public static void onEvent(Event e) {
        try {
            if (e instanceof EventChat) {
                commandManager.handleChat((EventChat) e);
            }

            for (Module m : modules) {
                if (!m.isToggled()) {
                    continue;
                }

                try {
                    m.onEvent(e);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

            }
        } catch (NullPointerException e1) {
            e1.printStackTrace();
        }
    }

    public static void addChatMessage(String message) {
        //message = "\2479" + name + "\2477 >> " + message;
        message = "§4[§cR§4]§8 "+ message;

        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
    }

    public static void printChat(String text) {
        text = "§4[§cR§4]§8 "+ text;

        mc.thePlayer.addChatComponentMessage(new ChatComponentText(text));
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static java.util.List<Module> getModulesByCategory(Module.Category c){
        List<Module> modules = new ArrayList<Module>();

        for(Module m : Resolute.modules) {
            if(m.category == c)
                modules.add(m);
        }

        return modules;
    }

    public static void setPlayerName(String string) {
        Minecraft.getMinecraft().session = new Session(string, "", "0", "");
    }


    public static String getVersion() {
        return build;
    }

    public File getDirectory() {
        return directory;
    }

    public static List<EntityLivingBase> getLivingEntities() {
        return mc.theWorld.getLoadedEntityList()
                .stream()
                .filter(e -> e instanceof EntityLivingBase)
                .map(e -> (EntityLivingBase) e)
                .collect(Collectors.toList());
    }

    public static List<EntityLivingBase> getLivingEntities(final Predicate<EntityLivingBase> validator) {
        final List<EntityLivingBase> entities = new ArrayList<EntityLivingBase>();
        for (final Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityLivingBase) {
                final EntityLivingBase e = (EntityLivingBase)entity;
                if (!validator.test(e)) {
                    continue;
                }
                entities.add(e);
            }
        }
        return entities;
    }

    public static ItemStack getStackInSlot(final int index) {
        return mc.thePlayer.inventoryContainer.getSlot(index).getStack();
    }
}
