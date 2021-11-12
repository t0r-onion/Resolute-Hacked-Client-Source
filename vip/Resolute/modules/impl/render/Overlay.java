package vip.Resolute.modules.impl.render;


import net.minecraft.util.MathHelper;
import vip.Resolute.Resolute;
import vip.Resolute.auth.Authentication;
import vip.Resolute.command.impl.Clientname;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.events.impl.EventRender2D;
import vip.Resolute.events.impl.EventUpdate;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ColorSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.font.MinecraftFontRenderer;
import vip.Resolute.util.misc.MathUtils;
import vip.Resolute.util.misc.TimerUtil;
import vip.Resolute.util.render.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class Overlay extends Module {
    public ModeSetting theme = new ModeSetting("Theme", "Normal", "Normal");
    public static ModeSetting cfont = new ModeSetting("Font", "Roboto", "Default", "Client", "Moon", "Tahoma", "Roboto", "SF", "SF Large");
    public BooleanSetting watermark = new BooleanSetting("Watermark", true);
    public ModeSetting displayMode = new ModeSetting("Watermark Mode", "LABEL", this::isModeSelected, "ONETAP", "Minesense", "Neverlose", "LABEL", "Sixsense");
    public ModeSetting waterMode = new ModeSetting("Watermark Color Mode","Fade", this::isModeSelected, "Fade", "Rainbow", "Static");
    public ColorSetting waterColor = new ColorSetting("Watermark Color", new Color(125, 0, 235), this::isModeSelected);
    public BooleanSetting arraylist = new BooleanSetting("ArrayList", true);
    public BooleanSetting armorStatus = new BooleanSetting("Armor Status", true);
    public NumberSetting colorSpeed = new NumberSetting("Speed", 15 , this::isMode3Selected,1 ,25, 1);
    public NumberSetting fadeSpeed = new NumberSetting("Fade Speed",1.0, this::isModeSelected,0.1, 5.0, 0.1);
    public ModeSetting colormode = new ModeSetting("Color Mode","Blend", this::isModeSelected,"Rainbow", "Blend", "Astolfo", "Static", "Fade");
    public NumberSetting spacing = new NumberSetting("Spacing", 9, this::isModeSelected, 5, 15, 1);
    public ColorSetting arrayColor = new ColorSetting("First Color", new Color(125, 0, 235), this::isModeSelected);
    public ColorSetting secondColor = new ColorSetting("Second Color", new Color(0, 235, 4), this::isMode2Selected);
    public BooleanSetting background = new BooleanSetting("Background", true);
    public NumberSetting transparency = new NumberSetting("Transparency", 3, 1, 10, 1);
    public static BooleanSetting notif = new BooleanSetting("Notifications", true);
    public static BooleanSetting potion = new BooleanSetting("Potion", true);
    public BooleanSetting bps = new BooleanSetting("BPS", true);
    public BooleanSetting line = new BooleanSetting("Line", false);
    public BooleanSetting arraybar = new BooleanSetting("Left Bar", true);
    public BooleanSetting info = new BooleanSetting("Display Info", true);
    public BooleanSetting displayHealth = new BooleanSetting("Display Health", false);
    public BooleanSetting displayCompass = new BooleanSetting("Display Compass", false);
    public BooleanSetting displayStatistics = new BooleanSetting("Statistics", false);
    public ModeSetting sortingMode = new ModeSetting("Sorting Mode","Length", this::isModeSelected,"Length", "Alphabetical", "Reversed", "Alpha Reversed");
    public static ModeSetting arrayPos = new ModeSetting("Array Position", "Top", "Top", "Left", "Bottom");
    public static BooleanSetting scoreboard = new BooleanSetting("Scoreboard", true);
    public static NumberSetting scoreboardX = new NumberSetting("Scoreboard X", 2, -1080, 50, 1), scoreboardY = new NumberSetting("Scoreboard Y", 170, -100, 300, 1);
    public NumberSetting yadd = new NumberSetting("Y Pos", -60, -60, 180, 1);

    private List<Module> moduleCache;
    private MinecraftFontRenderer fontRenderer;
    private int width;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.ENGLISH));
    private static final int MODULE_SPACING = 9;
    int y;
    boolean topArrayList;

    private TimerUtil timer = new TimerUtil();
    private TimerUtil hasUnLaggedShit = new TimerUtil();

    private ArrayList<Double> speedList;
    private float lastTick = -1;
    private double lastSpeed = 0.01;

    double lastDist;
    double xDist;
    double zDist;

    int wins = 0;
    int lost = 0;
    public static int kills = 0;
    int flags = 0;
    private long lastMS;
    private float hue = 0.0F;

    public boolean isModeSelected() {return this.theme.is("Normal");}
    public boolean isMode2Selected() {
        return this.colormode.is("Blend");
    }
    public boolean isMode3Selected() {return this.theme.is("Exhibition");}

    public Overlay() {
        super("Overlay", 0, "Ingame HUD", Category.RENDER);
        this.addSettings(theme, cfont, watermark, displayMode, waterColor, arraylist, fadeSpeed, colorSpeed, colormode, spacing, arrayColor, secondColor, background
                , transparency, notif, potion, bps, info, line, arraybar, displayHealth, displayCompass, displayStatistics, sortingMode,
                arrayPos, scoreboard, scoreboardX, scoreboardY);

        toggled = true;
    }

    public void onEvent(Event e) {
        setSuffix(displayMode.getMode());

        if(e instanceof EventMotion) {
            xDist = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
            zDist = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;
            this.lastDist = StrictMath.sqrt(xDist * xDist + zDist * zDist);
        }

        if(e instanceof EventUpdate) {
            if(!cfont.is("Default")) {
                if (sortingMode.is("Length")) {
                    moduleCache.sort(SortingMode.LENGTH.getSorter());
                }

                if (sortingMode.is("Alphabetical")) {
                    moduleCache.sort(SortingMode.ALPHABETICAL.getSorter());
                }

                if (sortingMode.is("Reversed")) {
                    moduleCache.sort(SortingMode.LENGTH.getSorter().reversed());
                }

                if (sortingMode.is("Alpha Reversed")) {
                    moduleCache.sort(SortingMode.ALPHABETICAL.getSorter().reversed());
                }
            } else {
                if (sortingMode.is("Length")) {
                    moduleCache.sort(SortingMode.DEFLENGTH.getSorter());
                }

                if (sortingMode.is("Alphabetical")) {
                    moduleCache.sort(SortingMode.DEFALPHA.getSorter());
                }

                if (sortingMode.is("Reversed")) {
                    moduleCache.sort(SortingMode.DEFLENGTH.getSorter().reversed());
                }

                if (sortingMode.is("Alpha Reversed")) {
                    moduleCache.sort(SortingMode.DEFALPHA.getSorter().reversed());
                }
            }
        }

        if(e instanceof EventRender2D) {
            EventRender2D event = (EventRender2D) e;
            ScaledResolution sr = new ScaledResolution(mc);

            int rainbowTick = 0;
            Color rainbow2 = new Color(Color.HSBtoRGB((float) ((double) mc.thePlayer.ticksExisted / 50.0 + Math.sin((double) rainbowTick / 50.0 * 1.6)) % 1.0f, 0.5f, 1.0f));

            float moduleWidth;

            if(arrayPos.is("Top")) {
                topArrayList = true;
            } else {
                topArrayList = false;
            }

            fontRenderer = Resolute.getFontRenderer();

            int screenX = sr.getScaledWidth();
            int screenY = sr.getScaledHeight();

            if(armorStatus.isEnabled()) {
                renderArmor(event);
            }

            float speed = (float) fadeSpeed.getValue();
            long ms = (long) (speed * 1000L);
            float darkFactor = 0.7f * 0.7f;
            long currentMillis = -1;

            String inf = "Private Build: " + Resolute.build + " : " + "UUID: " + Resolute.instance.uuid + " : " + Authentication.username;

            if(displayHealth.isEnabled()) {
                float i;
                if (mc.thePlayer.getHealth() >= 0.0f && mc.thePlayer.getHealth() < 10.0f) {
                    this.width = 3;
                }
                if (mc.thePlayer.getHealth() >= 10.0f && mc.thePlayer.getHealth() < 100.0f) {
                    this.width = 3;
                }
                float health = mc.thePlayer.getHealth();
                float absorptionHealth = mc.thePlayer.getAbsorptionAmount();
                String absorp = absorptionHealth <= 0.0f ? "" : "§e" + this.decimalFormat.format(absorptionHealth / 2.0f) + "§6❤";
                String string = this.decimalFormat.format(health / 2.0f) + "§c❤ " + absorp;
                int x = new ScaledResolution(mc).getScaledWidth() / 2 - this.width;
                int y = new ScaledResolution(mc).getScaledHeight() / 2 - 25;
                mc.fontRendererObj.drawString(string, absorptionHealth > 0.0f ? (float)x - 15.5f : (float)x - 3.5f, y, this.getHealthColor(), true);
                GL11.glPushAttrib(1048575);
                GL11.glPushMatrix();
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                mc.getTextureManager().bindTexture(Gui.icons);
                for (i = 0.0f; i < mc.thePlayer.getMaxHealth() / 2.0f; i += 1.0f) {
                    Gui.drawTexturedModalRect2((float)sr.getScaledWidth() / 2.0f - mc.thePlayer.getMaxHealth() / 2.5f * 10.0f / 2.0f + i * 8.0f, (float)sr.getScaledHeight() / 2.0f - 35.0f, 16, 0, 9, 9);
                }
                for (i = 0.0f; i < mc.thePlayer.getHealth() / 2.0f; i += 1.0f) {
                    Gui.drawTexturedModalRect2((float)sr.getScaledWidth() / 2.0f - mc.thePlayer.getMaxHealth() / 2.5f * 10.0f / 2.0f + i * 8.0f, (float)sr.getScaledHeight() / 2.0f - 35.0f, 52, 0, 9, 9);
                }
                GL11.glPopAttrib();
                GL11.glPopMatrix();
            }

            if(displayCompass.isEnabled()) {
                CompassUtil cpass = new CompassUtil(325, 325, 1, 2, true);
                ScaledResolution sc = new ScaledResolution(mc);
                cpass.draw(sc);
            }

            if(displayStatistics.isEnabled()) {
                Gui.drawRect(sr.getScaledWidth() / 20 - 40, sr.getScaledHeight() / 5 + this.yadd.getValue(), sr.getScaledWidth() / 8 + 30, sr.getScaledHeight() / 3.5 + this.yadd.getValue() - 5, 0x90000000);
                Gui.drawRect(sr.getScaledWidth() / 20 - 40, sr.getScaledHeight() / 2 + this.yadd.getValue() - 140, sr.getScaledWidth() / 8 + 30, sr.getScaledHeight() / 2 + this.yadd.getValue() - 138.5f, 0x906C6C6C);
                Gui.drawRect(sr.getScaledWidth() / 20 - 40, sr.getScaledHeight() / 5 + this.yadd.getValue() - 0, sr.getScaledWidth() / 8 + 30, sr.getScaledHeight() / 5 + this.yadd.getValue() - 1, 0xFF00EEFF);

                FontUtil.robo.drawString("Session Information", (float)sr.getScaledWidth() / 16 - 50, (float) ((float) sr.getScaledHeight() / 5 + 1 + this.yadd.getValue()), -1);
                FontUtil.robo.drawString("Session Time: " + formatTime(Resolute.sessionTime.elapsed()), (float)sr.getScaledWidth() / 16 - 50, (float) ((float) sr.getScaledHeight() / 5 + 17 + this.yadd.getValue()), -1);
                FontUtil.robo.drawString("Kills: " + kills, (float)sr.getScaledWidth() / 16 - 50, (float) ((float) sr.getScaledHeight() / 5 + 28 + this.yadd.getValue()), -1);
            }

            if(info.isEnabled()) {
                FontUtil.roboSmall.drawStringWithShadow(inf, (float) (screenX - 1 - FontUtil.roboSmall.getStringWidth(inf)),  (float) (screenY - ((mc.currentScreen instanceof GuiChat) ? 24 : 11)), 0xFFC3C1C1);
            }

            if(bps.isEnabled()) {
                FontUtil.roboSmall.drawStringWithShadow(String.format("%.2f blocks/s", this.lastDist * 20.0 * mc.timer.timerSpeed), 2.0f, (float) (screenY - ((mc.currentScreen instanceof GuiChat) ? 24 : 11)), -1);
            }

            if(potion.isEnabled()) {
                int potionY = 11;
                for (PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
                    Potion potion = Potion.potionTypes[effect.getPotionID()];

                    String effectName = I18n.format(potion.getName()) + " " + (effect.getAmplifier() + 1) + " \2477" + Potion.getDurationString(effect);
                    FontUtil.roboSmall.drawStringWithShadow(effectName, (float) (screenX - 2 - FontUtil.roboSmall.getStringWidth(effectName)), screenY - potionY - (info.isEnabled() ? 11 : 0) - (float) ((mc.currentScreen instanceof GuiChat) ? 13 : 0), potion.getLiquidColor());

                    potionY += FontUtil.roboSmall.getHeight();
                }
            }
            if(FakeFPS.enabled) {
                int fakefps = (int) FakeFPS.fakefpsnumber.getValue();
                FontUtil.roboSmall.drawStringWithShadow("[" + fakefps + Minecraft.getDebugFPS() + " FPS]", 4.0f, (float) (screenY - ((mc.currentScreen instanceof GuiChat) ? 38 : 24)), -1);
            }

            if (this.timer.hasElapsed(500L)) {
                if (this.timer.hasElapsed(150L)) {
                    RenderUtils.drawImage(new ResourceLocation("resolute/lag2.png"), sr.getScaledWidth() / 2 - 20, sr.getScaledHeight() / 2 - 65, 40, 40);
                    hasUnLaggedShit.reset();
                }
                RenderUtils.drawOutlinedString("§lLag Detected", (float)sr.getScaledWidth() / 2.0f - (float)mc.fontRendererObj.getStringWidth("§lLag Detected") / 2.0f - 3.0f, (float)sr.getScaledHeight() / 2.0f - 20, new Color(255, 127, 0).getRGB(), new Color(0, 0, 0).getRGB());
            } else if(!hasUnLaggedShit.hasElapsed(200L)) {
                RenderUtils.drawImage(new ResourceLocation("resolute/lag.png"), sr.getScaledWidth() / 2 - 20, sr.getScaledHeight() / 2 - 65, 40, 40);
            }

            if(watermark.isEnabled()) {
                currentMillis = System.currentTimeMillis();
                String text;

                text = Clientname.nameofwatermark;

                float posX = 5;;

                if(theme.is("Normal")) {
                    if (displayMode.is("Minesense")) {
                        char [] words2 = Clientname.nameofwatermark.toCharArray();
                        mc.fontRendererObj.drawStringWithShadow(words2[0] + "§f" + Clientname.nameofwatermark.substring(1, Clientname.nameofwatermark.length()) + " §7[" + "§f" +  "1.8.x" + "§7]" + " §7[" + "§f" + mc.getDebugFPS() + "§f FPS" + "§7]", 1, 2,  waterColor.getColor());
                    } else if (displayMode.is("ONETAP")) {
                        Date date = Calendar.getInstance().getTime();
                        DateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
                        String strDate = dateFormat.format(date);
                        String server = mc.isSingleplayer() ? "local server" : mc.getCurrentServerData().serverIP.toLowerCase();
                        String text1 = "resolute.pub | " + Minecraft.getDebugFPS() + " fps" + " | " + "movement flags : 0" + " | " + Authentication.username + " | " + server + " | " + strDate;
                        float width = (float) (FontUtil.tahomaSmall.getStringWidth(text1) + 6);
                        RenderUtils.drawRect(2, 2, width, 3, waterColor.getColor());
                        RenderUtils.drawRect(2, 3, width, 13, new Color(60, 60, 60).getRGB());
                        FontUtil.tahomaSmall.drawString(text1, 4, 5.5f, -1);
                    } else if (displayMode.is("Sixsense")) {
                        String server = mc.isSingleplayer() ? "Singleplayer" : (mc.getCurrentServerData().serverIP.toLowerCase());
                        String user = Authentication.username;
                        String print = text + " | "+ mc.getDebugFPS() + " FPS | " + user + " | "+ server;
                        float width = (float) (FontUtil.tahomaSmall.getStringWidth(print) + 8);
                        int height = 20;
                        int posX1 = 2;
                        int posY1 = 2;

                        Gui.drawRect(posX1, posY1, posX1 + width + 2, posY1 + height, new Color(5, 5, 5, 255).getRGB());
                        RenderUtils.drawBorderedRect(posX1 + .5, posY1 + .5, posX1 + width + 1.5, posY1 + height - .5, 0.5, new Color(40, 40, 40, 255).getRGB(), new Color(60, 60, 60, 255).getRGB(), true);
                        RenderUtils.drawBorderedRect(posX1 + 2, posY1 + 2, posX1 + width, posY1 + height - 2, 0.5, new Color(22, 22, 22, 255).getRGB(), new Color(60, 60, 60, 255).getRGB(), true);
                        Gui.drawRect(posX1 + 2.5, posY1 + 2.5, posX1 + width - .5, posY1 + 4.5, new Color(9, 9, 9, 255).getRGB());

                        RenderUtils.drawGradientSideways(4, posY1 + 3, 4 + (width / 3), posY1 + 4, new Color(81, 149, 219, 255).getRGB(), new Color(180, 49, 218, 255).getRGB());
                        RenderUtils.drawGradientSideways(4 + (width / 3), posY1 + 3, 4 + ((width / 3) * 2), posY1 + 4, new Color(180, 49, 218, 255).getRGB(), new Color(236, 93, 128, 255).getRGB());
                        RenderUtils.drawGradientSideways(4 + ((width / 3) * 2), posY1 + 3, ((width / 3) * 3) + 1, posY1 + 4, new Color(236, 93, 128, 255).getRGB(), new Color(167, 171, 90, 255).getRGB());
                        FontUtil.tahomaSmall.drawString(print , 7.5F, 10, Color.white.getRGB());
                    } else if(displayMode.is("Neverlose")) {
                        String watermark = " | " + Authentication.username + " | " + (mc.isSingleplayer() ? "Singleplayer" : (mc.getCurrentServerData().serverIP)) + " | FPS " + Minecraft.getDebugFPS();
                        Gui.drawRect(0, 0, FontUtil.neverlose.getStringWidth(text.toUpperCase()) + FontUtil.tahomaSmall.getStringWidth(watermark) + 5, 12.5f, new Color(10, 10, 10, 255).getRGB());
                        FontUtil.neverlose.drawStringWithShadow(text.toUpperCase(), 1, 4, -1);
                        FontUtil.tahomaSmall.drawStringWithShadow(watermark, FontUtil.neverlose.getStringWidth(text.toUpperCase()) + 2, 5, Color.GRAY.brighter().getRGB());
                    } else if(displayMode.is("LABEL")) {
                        char [] words2 = Clientname.nameofwatermark.toCharArray();
                        Date date = new Date();
                        String time = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
                        FontUtil.roboSmall.drawStringWithShadow(words2[0] + "§f" + Clientname.nameofwatermark.substring(1) + "§f (" + time + ")", 1, 2,  waterColor.getColor());
                    }
                } else if(theme.is("Flux")) {
                    FontUtil.c22.drawStringWithShadow(text, 2, 5, rainbow2.getRGB());
                } else if(theme.is("Exhibition")) {
                    char [] words2 = Clientname.nameofwatermark.toCharArray();
                    mc.fontRendererObj.drawStringWithShadow(words2[0] + "§f" + Clientname.nameofwatermark.substring(1) + " §7[" + "§f" +  "1.8.x" + "§7]" + " §7[" + "§f" + mc.getDebugFPS() + "§f FPS" + "§7]", 1, 2, Color.getHSBColor(this.hue / 255.0F, 0.55F, 0.9F).getRGB());
                }
            }

            if(arraylist.isEnabled()) {
                if (theme.is("Normal")) {
                    if (currentMillis == -1) currentMillis = System.currentTimeMillis();

                    int arrayListColor = arrayColor.getColor();
                    int sArrayListColor = secondColor.getColor();

                    if (moduleCache == null) {
                        if (!cfont.is("Default")) {
                            updateModulePositions(RenderUtils.getScaledResolution());
                        } else {
                            defupdateModulePositions(RenderUtils.getScaledResolution());
                        }
                    }

                    final int moduleSpacing = (int) spacing.getValue();
                    int heightOffset = (int) spacing.getValue();

                    if (arrayPos.is("Top")) {
                        y = 2;
                    }
                    if(arrayPos.is("Left")) {
                        y = (displayMode.is("ONETAP") || displayMode.is("Minesense") || displayMode.is("Neverlose")) ? 14 : 24;
                    }
                    if (arrayPos.is("Bottom")) {
                        y = screenY - 9;
                    }

                    int i = 0;
                    boolean left = true;

                    for (Module module : moduleCache) {
                        Translate translate = module.getTranslate();
                        String name = module.getDisplayName();
                        float x = left ? 1.0F : (float) ((float) sr.getScaledWidth() - fontRenderer.getStringWidth(module.getDisplayName()) - 1.0F);

                        if (module.isEnabled() && !module.isHidden()) {
                            if (!cfont.is("Default")) {
                                moduleWidth = (float) fontRenderer.getStringWidth(name);
                            } else {
                                moduleWidth = (float) mc.fontRendererObj.getStringWidth(name);
                            }

                            if(!arrayPos.is("Left")) {
                                translate.animate2(screenX - moduleWidth - (line.isEnabled() ? 2 : 1), y);
                            } else {
                                translate.animate2(x, (float)y);
                            }

                            if (arrayPos.is("Top") || arrayPos.is("Left")) {
                                y += moduleSpacing;
                            }

                            if (arrayPos.is("Bottom")) {
                                y -= moduleSpacing;
                            }

                        } else {
                            if(!arrayPos.is("Left")) {
                                translate.interpolate(sr.getScaledWidth(), arrayPos.is("Bottom") ? 700.0F : -20.0F, 1.0F);
                                //translate.animate2(screenX, y);
                            } else {
                                translate.interpolate(-50.0F, -20.0F, 1.0F);
                                //translate.animate2(-100.0f, y);
                            }
                        }

                        boolean shown = arrayPos.is("Left") ? translate.getX() != -50.0f : translate.getX() < screenX;

                        if (shown) {
                            int wColor = -1;
                            final float offset = (currentMillis + (i * 100)) % ms / (ms / 2.0F);

                            if (colormode.is("Fade")) {
                                wColor = fadeBetween(arrayListColor, darker(arrayListColor, darkFactor), offset);
                            }

                            if (colormode.is("Blend")) {
                                wColor = fadeBetween(arrayListColor, sArrayListColor, offset);
                            }

                            if (colormode.is("Astolfo")) {
                                wColor = ColorUtils.astolfoColors(10, y);
                            }

                            if (colormode.is("Static")) {
                                wColor = arrayListColor;
                            }

                            if (colormode.is("Rainbow")) {
                                wColor = RenderUtils.getRainbow(3000, i);
                            }

                            if (background.isEnabled()) {
                                if(arrayPos.is("Left")) {
                                    if (!cfont.is("Default")) {
                                        moduleWidth = (float) fontRenderer.getStringWidth(name);
                                    } else {
                                        moduleWidth = (float) mc.fontRendererObj.getStringWidth(name);
                                    }
                                    if (transparency.getValue() == 1)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), translate.getX() + moduleWidth, translate.getY() + heightOffset, 0x1F000000);
                                    if (transparency.getValue() == 2)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), translate.getX() + moduleWidth, translate.getY() + heightOffset, 0x2F000000);
                                    if (transparency.getValue() == 3)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), translate.getX() + moduleWidth, translate.getY() + heightOffset, 0x3F000000);
                                    if (transparency.getValue() == 4)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), translate.getX() +  moduleWidth, translate.getY() + heightOffset, 0x4F000000);
                                    if (transparency.getValue() == 5)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), translate.getX() +  moduleWidth, translate.getY() + heightOffset, 0x5F000000);
                                    if (transparency.getValue() == 6)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), translate.getX() + moduleWidth, translate.getY() + heightOffset, 0x6F000000);
                                    if (transparency.getValue() == 7)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), translate.getX() + moduleWidth, translate.getY() + heightOffset, 0x7F000000);
                                    if (transparency.getValue() == 8)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), translate.getX() + moduleWidth, translate.getY() + heightOffset, 0x8F000000);
                                    if (transparency.getValue() == 9)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), translate.getX() + moduleWidth, translate.getY() + heightOffset, 0x9F000000);
                                    if (transparency.getValue() == 10)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), translate.getX() + moduleWidth, translate.getY() + heightOffset, 0xFF000000);
                                } else {
                                    if (transparency.getValue() == 1)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), screenX, translate.getY() + heightOffset, 0x1F000000);
                                    if (transparency.getValue() == 2)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), screenX, translate.getY() + heightOffset, 0x2F000000);
                                    if (transparency.getValue() == 3)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), screenX, translate.getY() + heightOffset, 0x3F000000);
                                    if (transparency.getValue() == 4)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), screenX, translate.getY() + heightOffset, 0x4F000000);
                                    if (transparency.getValue() == 5)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), screenX, translate.getY() + heightOffset, 0x5F000000);
                                    if (transparency.getValue() == 6)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), screenX, translate.getY() + heightOffset, 0x6F000000);
                                    if (transparency.getValue() == 7)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), screenX, translate.getY() + heightOffset, 0x7F000000);
                                    if (transparency.getValue() == 8)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), screenX, translate.getY() + heightOffset, 0x8F000000);
                                    if (transparency.getValue() == 9)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), screenX, translate.getY() + heightOffset, 0x9F000000);
                                    if (transparency.getValue() == 10)
                                        Gui.drawRect(translate.getX() - 1, translate.getY() - (moduleSpacing - heightOffset), screenX, translate.getY() + heightOffset, 0xFF000000);
                                }
                            }

                            if (!cfont.is("Default")) {
                                fontRenderer.drawStringWithShadow(name, (float) translate.getX(), (float) translate.getY() + 2, wColor);
                            } else {
                                mc.fontRendererObj.drawStringWithShadow(name, (float) translate.getX(), (float) translate.getY(), wColor);
                            }

                            if (arraybar.isEnabled()) {
                                Gui.drawRect(translate.getX() - 2.0, translate.getY(), translate.getX() - 1, translate.getY() + moduleSpacing, wColor - 1);
                            }

                            if (!cfont.is("Default")) {
                                moduleWidth = (float) fontRenderer.getStringWidth(name);
                            } else {
                                moduleWidth = (float) mc.fontRendererObj.getStringWidth(name);
                            }

                            if (line.isEnabled()) {
                                if(arrayPos.is("Left")) {
                                    Gui.drawRect(translate.getX() - 1, translate.getY() - 1, translate.getX() , translate.getY() + moduleSpacing - 1, wColor - 1);
                                } else {
                                    Gui.drawRect(screenX - 1, translate.getY() - 1, screenX, translate.getY() + moduleSpacing - 1, wColor - 1);
                                }
                            }

                            i++;
                        }
                    }
                } else if(theme.is("Flux")) {
                    if (!mc.gameSettings.showDebugInfo) {
                        MinecraftFontRenderer fontRenderer = FontUtil.c16;

                        String name;
                        ArrayList<Module> sorted = new ArrayList<Module>();
                        for (Module m : Resolute.modules) {
                            sorted.add(m);
                        }
                        sorted.sort((o1, o2) -> (int) (fontRenderer.getStringWidth(o2.getName() + (o2.suffix == null ? "" : " " + o2.suffix)) - fontRenderer.getStringWidth(o1.getName() + (o1.suffix == null ? "" : " " + o1.suffix))));
                        int y = 8;
                        rainbowTick = 0;
                        for (Module m : sorted) {
                            Color rainbow = new Color(Color.HSBtoRGB((float) ((double) mc.thePlayer.ticksExisted / 50.0 + Math.sin((double) rainbowTick / 50.0 * 1.6)) % 1.0f, 0.5f, 1.0f));
                            name = m.getName() + (m.suffix == null ? "" : " §W" + m.suffix);


                            if (m.getXAnim() < fontRenderer.getStringWidth(name) && m.isEnabled()) {
                                if (m.getXAnim() < fontRenderer.getStringWidth(name) / 3) {
                                    m.setXAnim(m.getXAnim() + 1);
                                } else {
                                    m.setXAnim(m.getXAnim() + 1);
                                }
                            }
                            if (m.getXAnim() > -1 && !m.isEnabled()) {
                                m.setXAnim(m.getXAnim() - 1);
                            }

                            if (m.getXAnim() > fontRenderer.getStringWidth(name) && m.isEnabled()) {
                                m.setXAnim((float) fontRenderer.getStringWidth(name));
                            }

                            if (m.getYAnim() < y) {
                                m.setYAnim(m.getYAnim() + 180f / mc.getDebugFPS());
                            }

                            if (m.getYAnim() > y) {
                                m.setYAnim(m.getYAnim() - 180f / mc.getDebugFPS());
                            }

                            if(Math.abs(m.getYAnim() - y)<1){
                                m.setYAnim(y);
                            }

                            if (m.isEnabled()) {
                                float x2 = sr.getScaledWidth() - m.getXAnim();
                                if ((Boolean) background.isEnabled()) {
                                    RenderUtils.drawRect(x2 - 8, m.getYAnim(), sr.getScaledWidth(), m.getYAnim() + 10, new Color(0, 0, 0, 150).getRGB());
                                    RenderUtils.drawRect(new ScaledResolution(mc).getScaledWidth_double() - 2, m.getYAnim() - 2, new ScaledResolution(mc).getScaledWidth_double(), m.getYAnim() + 10, rainbow.getRGB());
                                }

                                fontRenderer.drawStringWithShadow(name, x2 - 5, m.getYAnim() + 2, rainbow.getRGB());

                                y += 10;
                            }
                            if (++rainbowTick > 50) {
                                rainbowTick = 0;
                            } else {
                                rainbowTick += 0.1;
                            }
                        }
                    }
                } else if(theme.is("Exhibition")) {

                    FontRenderer fr = mc.fontRendererObj;
                    boolean left = true;
                    int y = left ? 12 : 1;
                    List<Module> modules = new CopyOnWriteArrayList();

                    for(Module m : Resolute.modules) {
                        if (m.isEnabled() || m.translate.getX() != -50.0F) {
                            modules.add(m);
                        }

                        if (!m.isEnabled() || m.isHidden()) {
                            m.translate.interpolate(left ? -50.0F : (float)sr.getScaledWidth(), -20.0F, 1F);
                        }
                    }

                    modules.sort(Comparator.comparingDouble((o) -> {
                        return -MathUtils.getIncremental((double)fr.getStringWidth(o.suffix != null ? o.getName() + " " + o.suffix : o.getName()), 0.5D);
                    }));
                    Iterator var42 = modules.iterator();

                    this.hue += colorSpeed.getValue() / 5.0F;
                    if (this.hue > 255.0F) {
                        this.hue = 0.0F;
                    }

                    float h = this.hue;

                    while(var42.hasNext()) {
                        Module module = (Module)var42.next();
                        if (h > 255.0F) {
                            h = 0.0F;
                        }

                        String suffix = module.suffix != null ? " \2477" + module.suffix : "";
                        float x = left ? 2.0F : (float)sr.getScaledWidth() - fr.getStringWidth(module.getName() + suffix) - 1.0F;
                        if (module.isEnabled() && !module.isHidden()) {
                            module.translate.interpolate(x, (float)y, 1);
                        }

                        Color color = Color.getHSBColor(h / 255.0F, 0.55F, 0.9F);
                        int c = color.getRGB();
                        fr.drawStringWithShadow(module.getName() + suffix, (float) module.translate.getX(), (float) module.translate.getY(),  c);
                        if (module.isEnabled() && !module.isHidden()) {
                            h += 9.0F;
                            y += 9;
                        }
                    }
                }
            }
        }

        if(e instanceof EventPacket) {
            EventPacket eventPacket = (EventPacket) e;
            Packet<?> packet = eventPacket.getPacket();
            if(((EventPacket) e).getPacket() instanceof S08PacketPlayerPosLook) {
                flags++;
            }

            if(this.mc.thePlayer != null && this.mc.thePlayer.ticksExisted >= 0 && packet instanceof S45PacketTitle) {
                if (((S45PacketTitle) packet).getMessage() == null)
                    return;

                String message = ((S45PacketTitle) packet).getMessage().getUnformattedText();

                if(message.equals("VICTORY!")) {
                    wins++;
                }

                if(message.equals("YOU DIED!") || message.equals("GAME END") || message.equals("You are now a spectator!")) {
                    lost++;
                }
            }

            if(this.mc.thePlayer != null && this.mc.thePlayer.ticksExisted >= 0 && packet instanceof S02PacketChat) {
                final String look = "killed by " + mc.thePlayer.getName();
                final String look2 = "slain by " + mc.thePlayer.getName();
                final String look3 = "void while escaping " + mc.thePlayer.getName();
                final String look4 = "was killed with magic while fighting " + mc.thePlayer.getName();
                final String look5 = "couldn't fly while escaping " + mc.thePlayer.getName();
                final String look6 = "fell to their death while escaping " + mc.thePlayer.getName();
                final String look7 = "foi morto por " + mc.thePlayer.getName();
                final String look8 = "fue asesinado por " + mc.thePlayer.getName();
                final String look9 = "fue destrozado a manos de " + mc.thePlayer.getName();
                final S02PacketChat s02PacketChat = (S02PacketChat) packet;
                final String cp21 = s02PacketChat.getChatComponent().getUnformattedText();

                if (cp21.contains(look) || cp21.contains(look2) || cp21.contains(look3) || cp21.contains(look4) || cp21.contains(look5) || cp21.contains(look6) || cp21.contains(look7) || cp21.contains(look8) || cp21.contains(look9)) {
                    kills++;
                }

                if((cp21.contains(mc.thePlayer.getName() + "killed by ") && cp21.contains("elimination")) || (cp21.contains(mc.thePlayer.getName() + " morreu sozinho")) || (cp21.contains(mc.thePlayer.getName() + " foi morto por"))) {
                    lost++;
                }

                if(cp21.contains(mc.thePlayer.getName() + " venceu a partida!")) {
                    wins++;
                }
            }
        }

        if(e instanceof EventUpdate) {
            mc.thePlayer.setLocationOfCape(new ResourceLocation("resolute/cape.png"));
        }

        if(e instanceof EventPacket) {
            Packet<?> receive = ((EventPacket) e).getPacket();
            if (!(receive instanceof S02PacketChat)) {
                this.timer.reset();
            }
        }
    }

    private String formatTime(long time) {
        time /= 1000;
        return String.format("%d:%02d", time / 60, time % 60);
    }

    private void renderArmor(EventRender2D event) {
        ScaledResolution sr = new ScaledResolution(mc);

        final boolean currentItem = true;
        GL11.glPushMatrix();
        final List<ItemStack> stuff = new ArrayList<ItemStack>();
        final boolean onwater = mc.thePlayer.isEntityAlive() && mc.thePlayer.isInsideOfMaterial(Material.water);
        int split = -3;
        for (int index = 3; index >= 0; --index) {
            final ItemStack armer = mc.thePlayer.inventory.armorInventory[index];
            if (armer != null) {
                stuff.add(armer);
            }
        }
        if (mc.thePlayer.getCurrentEquippedItem() != null && currentItem) {
            stuff.add(mc.thePlayer.getCurrentEquippedItem());
        }
        for (final ItemStack errything : stuff) {
            if (mc.theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting();
                split += 16;
            }
            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            GlStateManager.enableBlend();
            mc.getRenderItem().zLevel = -150.0f;
            mc.getRenderItem().renderItemAndEffectIntoGUI(errything, split + sr.getScaledWidth() / 2 - 4, sr.getScaledHeight() - (onwater ? 65 : 55));
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, errything, split + sr.getScaledWidth() / 2 - 4, sr.getScaledHeight() - (onwater ? 65 : 55));
            mc.getRenderItem().zLevel = 0.0f;
            GlStateManager.disableBlend();
            GlStateManager.scale(0.5, 0.5, 0.5);
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
            errything.getEnchantmentTagList();
        }
        GL11.glPopMatrix();
    }

    public static boolean checkPing(final EntityPlayer entity) {
        final NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(entity.getUniqueID());
        return info != null && info.getResponseTime() == 1;
    }

    private int darker(int color, float factor) {
        int r = (int) ((color >> 16 & 0xFF) * factor);
        int g = (int) ((color >> 8 & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        int a = color >> 24 & 0xFF;

        return ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF) |
                ((a & 0xFF) << 24);
    }

    private int fadeBetween(int color1, int color2, float offset) {
        if (offset > 1)
            offset = 1 - offset % 1;

        double invert = 1 - offset;
        int r = (int) ((color1 >> 16 & 0xFF) * invert +
                (color2 >> 16 & 0xFF) * offset);
        int g = (int) ((color1 >> 8 & 0xFF) * invert +
                (color2 >> 8 & 0xFF) * offset);
        int b = (int) ((color1 & 0xFF) * invert +
                (color2 & 0xFF) * offset);
        int a = (int) ((color1 >> 24 & 0xFF) * invert +
                (color2 >> 24 & 0xFF) * offset);
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF);
    }

    private void updateModulePositions(ScaledResolution scaledResolution) {
        if (moduleCache == null) {
            setupModuleCache();
        }

        int y = 1;
        for (Module module : moduleCache) {
            if (module.isEnabled())
                module.getTranslate().setX((float) (scaledResolution.getScaledWidth() -
                        fontRenderer.getStringWidth(module.getDisplayName()) - 2));
            else
                module.getTranslate().setX(scaledResolution.getScaledWidth());
            module.getTranslate().setY(y);
            if (module.isEnabled())
                y += spacing.getValue();
        }
    }

    private void defupdateModulePositions(ScaledResolution scaledResolution) {
        if (moduleCache == null) {
            setupModuleCache();
        }

        int y = 1;
        for (Module module : moduleCache) {
            if (module.isEnabled())
                module.getTranslate().setX((float) (scaledResolution.getScaledWidth() -
                        mc.fontRendererObj.getStringWidth(module.getDisplayName()) - 2));
            else
                module.getTranslate().setX(scaledResolution.getScaledWidth());
            module.getTranslate().setY(y);
            if (module.isEnabled())
                y += spacing.getValue();
        }
    }

    private void setupModuleCache() {
        moduleCache = new ArrayList<>(Resolute.modules);
    }

    private enum SortingMode {
        LENGTH(new LengthComparator()),
        ALPHABETICAL(new AlphabeticalComparator()),
        DEFLENGTH(new DefLengthComparator()),
        DEFALPHA(new DefAlphabeticalComparator());

        private final ModuleComparator sorter;

        SortingMode(ModuleComparator sorter) {
            this.sorter = sorter;
        }

        public ModuleComparator getSorter() {
            return sorter;
        }
    }

    private int getHealthColor() {
        if (mc.thePlayer.getHealth() <= 2.0f) {
            return new Color(255, 0, 0).getRGB();
        }
        if (mc.thePlayer.getHealth() <= 6.0f) {
            return new Color(255, 110, 0).getRGB();
        }
        if (mc.thePlayer.getHealth() <= 8.0f) {
            return new Color(255, 182, 0).getRGB();
        }
        if (mc.thePlayer.getHealth() <= 10.0f) {
            return new Color(255, 255, 0).getRGB();
        }
        if (mc.thePlayer.getHealth() <= 13.0f) {
            return new Color(255, 255, 0).getRGB();
        }
        if (mc.thePlayer.getHealth() <= 15.5f) {
            return new Color(182, 255, 0).getRGB();
        }
        if (mc.thePlayer.getHealth() <= 18.0f) {
            return new Color(108, 255, 0).getRGB();
        }
        if (mc.thePlayer.getHealth() <= 20.0f) {
            return new Color(0, 255, 0).getRGB();
        }
        return 0;
    }

    private abstract static class ModuleComparator implements Comparator<Module> {
        protected MinecraftFontRenderer fontRenderer = Resolute.getFontRenderer();

        public MinecraftFontRenderer getFontRenderer() {
            return Resolute.getFontRenderer();
        }

        @Override
        public abstract int compare(Module o1, Module o2);
    }

    private static class LengthComparator extends ModuleComparator {
        @Override
        public int compare(Module o1, Module o2) {
            return ((int) getFontRenderer().getStringWidth(o2.getDisplayName()) -
                    (int) getFontRenderer().getStringWidth(o1.getDisplayName()));
        }
    }

    private static class AlphabeticalComparator extends ModuleComparator {
        @Override
        public int compare(Module o1, Module o2) {
            String n = o1.getDisplayName();
            String n1 = o2.getDisplayName();
            if (n.equals(n1)) return 0;
            if (n.length() == 0 || n1.length() == 0) return 0;
            return n.charAt(0) - n1.charAt(0);
        }
    }

    private static class DefLengthComparator extends ModuleComparator {
        @Override
        public int compare(Module o1, Module o2) {
            return ((int) mc.fontRendererObj.getStringWidth(o2.getDisplayName()) -
                    (int) mc.fontRendererObj.getStringWidth(o1.getDisplayName()));
        }
    }

    private static class DefAlphabeticalComparator extends ModuleComparator {
        @Override
        public int compare(Module o1, Module o2) {
            String n = o1.getDisplayName();
            String n1 = o2.getDisplayName();
            if (n.equals(n1)) return 0;
            if (n.length() == 0 || n1.length() == 0) return 0;
            return n.charAt(0) - n1.charAt(0);
        }
    }
}