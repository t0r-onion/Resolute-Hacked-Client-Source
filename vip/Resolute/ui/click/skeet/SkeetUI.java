package vip.Resolute.ui.click.skeet;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.gui.inventory.GuiInventory;
import org.lwjgl.input.Mouse;
import vip.Resolute.Resolute;
import vip.Resolute.modules.Module;
import vip.Resolute.modules.impl.render.ClickGUI;
import vip.Resolute.settings.Setting;
import vip.Resolute.settings.impl.*;
import vip.Resolute.ui.click.skeet.component.TabComponent;
import vip.Resolute.ui.click.skeet.component.impl.GroupBoxComponent;
import vip.Resolute.ui.click.skeet.component.impl.sub.button.ButtonComponentImpl;
import vip.Resolute.ui.click.skeet.component.impl.sub.checkbox.CheckBoxTextComponent;
import vip.Resolute.ui.click.skeet.component.impl.sub.color.ColorPickerTextComponent;
import vip.Resolute.ui.click.skeet.component.impl.sub.comboBox.ComboBoxTextComponent;
import vip.Resolute.ui.click.skeet.component.impl.sub.key.KeyBindComponent;
import vip.Resolute.ui.click.skeet.component.impl.sub.player.PlayerComponent;
import vip.Resolute.ui.click.skeet.component.impl.sub.slider.SliderTextComponent;
import vip.Resolute.ui.click.skeet.framework.Component;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.font.MinecraftFontRenderer;
import vip.Resolute.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;

public final class SkeetUI extends GuiScreen {
    public static final int GROUP_BOX_MARGIN = 8;
    public static final MinecraftFontRenderer ICONS_RENDERER;
    public static final MinecraftFontRenderer GROUP_BOX_HEADER_RENDERER = FontUtil.tahomaSmall;
    public static final MinecraftFontRenderer FONT_RENDERER;
    public static final MinecraftFontRenderer KEYBIND_FONT_RENDERER;
    private static final SkeetUI INSTANCE;
    private static final ResourceLocation BACKGROUND_IMAGE = new ResourceLocation("resolute/skeetchainmail.png");
    private static final char[] ICONS = new char[]{'E', 'G', 'F', 'D', 'A', 'H', 'J', 'I'};
    private static final float USABLE_AREA_HEIGHT = 341.5F;
    private static final int TAB_SELECTOR_HEIGHT;

    private static final ColorSetting GUI_COLOR;

    private static final BooleanSetting PLAYERS;
    private static final BooleanSetting ANIMALS;
    private static final BooleanSetting MOBS;
    private static final BooleanSetting INVISIBLES;
    private static final BooleanSetting VILLAGERS;
    private static final BooleanSetting TEAMS;
    float hue;
    private static double alpha;
    private static boolean open;
    private final Component rootComponent = new Component((Component)null, 230.0F, 30.0F, 430.0F, 400.0F) {
        public boolean isHovered(int mouseX, int mouseY) {
            float x;
            float y;
            return (float)mouseX >= (x = this.getX() + 3.0F) && (float)mouseY >= (y = this.getY() + 3.0F) && (float)mouseX <= x + this.getWidth() - 6.0F && (float)mouseY <= y + this.getHeight() - 6.0F;
        }

        public void drawComponent(ScaledResolution lockedResolution, int mouseX, int mouseY) {
            if (SkeetUI.this.dragging) {
                this.setX(Math.max(0.0F, Math.min((float)lockedResolution.getScaledWidth() - this.getWidth(), (float)mouseX - SkeetUI.this.prevX)));
                this.setY(Math.max(0.0F, Math.min((float)lockedResolution.getScaledHeight() - this.getHeight(), (float)mouseY - SkeetUI.this.prevY)));
            }

            float borderX = this.getX();
            float borderY = this.getY();
            float width = this.getWidth();
            float height = this.getHeight();

            Gui.drawRect((double)borderX, (double)borderY, (double)(borderX + width), (double)(borderY + height), SkeetUI.getColor(1052942));
            Gui.drawRect((double)(borderX + 0.5F), (double)(borderY + 0.5F), (double)(borderX + width - 0.5F), (double)(borderY + height - 0.5F), SkeetUI.getColor(3619386));
            Gui.drawRect((double)(borderX + 1.0F), (double)(borderY + 1.0F), (double)(borderX + width - 1.0F), (double)(borderY + height - 1.0F), SkeetUI.getColor(2302755));
            Gui.drawRect((double)(borderX + 3.0F), (double)(borderY + 3.0F), (double)(borderX + width - 3.0F), (double)(borderY + height - 3.0F), SkeetUI.getColor(3092271));
            float left = borderX + 3.5F;
            float top = borderY + 3.5F;
            float right = borderX + width - 3.5F;
            float bottom = borderY + height - 3.5F;

            float h = SkeetUI.this.hue;
            float h2 = SkeetUI.this.hue + 85.0f;
            float h3 = SkeetUI.this.hue + 170.0f;
            if (h > 255.0f) {
                h = 0.0f;
            }
            if (h2 > 255.0f) {
                h2 -= 255.0f;
            }
            if (h3 > 255.0f) {
                h3 -= 255.0f;
            }
            Color no = Color.getHSBColor(h / 255.0f, 0.55f, 1.0f);
            Color yes = Color.getHSBColor(h2 / 255.0f, 0.55f, 1.0f);
            Color bruh = Color.getHSBColor(h3 / 255.0f, 0.55f, 1.0f);

            SkeetUI.this.hue += 0.5f;



            //Gui.drawRect((double)left, (double)top, (double)right, (double)bottom, SkeetUI.getColor(1381653));

            if (SkeetUI.alpha > 20.0D) {
                GL11.glEnable(3089);
                RenderUtils.startScissorBox(lockedResolution, (int)left, (int)top, (int)(right - left), (int)(bottom - top));

                Minecraft.getMinecraft().getTextureManager().bindTexture(SkeetUI.BACKGROUND_IMAGE);
                RenderUtils.drawImage(left, top, 325.0F, 275.0F, -1);
                RenderUtils.drawImage(left + 325.0F, top + 1.0F, 325.0F, 275.0F, -1);
                RenderUtils.drawImage(left + 1.0F, top + 275.0F, 325.0F, 275.0F, -1);
                RenderUtils.drawImage(left + 326.0F, top + 276.0F, 325.0F, 275.0F, -1);
                GL11.glDisable(3089);
            }



            float xDif = (right - left) / 2.0F;
            top += 0.5F;
            left += 0.5F;
            right -= 0.5F;
            RenderUtils.drawGradientRect((double)left, (double)top, (double)(left + xDif), (double)(top + 1.5F - 0.5F), true, SkeetUI.getColor(RenderUtils.darker(3957866, 1.5F)), SkeetUI.getColor(RenderUtils.darker(7352943, 1.5F)));
            RenderUtils.drawGradientRect((double)(left + xDif), (double)top, (double)right, (double)(top + 1.5F - 0.5F), true, SkeetUI.getColor(RenderUtils.darker(7352943, 1.5F)), SkeetUI.getColor(RenderUtils.darker(8094516, 1.5F)));

            if (SkeetUI.alpha >= 112.0D) {
                Gui.drawRect((double)left, (double)(top + 1.5F - 1.0F), (double)right, (double)(top + 1.5F - 0.5F), 1879048192);
            }

            if(ClickGUI.rainbow.isEnabled()) {
                RenderUtils.drawGradientSideways(left, top, right / 2.0f, bottom, no.getRGB(), yes.getRGB());
                RenderUtils.drawGradientSideways(left, top, right, bottom, yes.getRGB(), bruh.getRGB());
                Gui.drawRect(left, top, right, bottom, new Color(21, 21, 21, 205).getRGB());
            }

            Iterator var13 = this.children.iterator();

            while(true) {
                Component child;
                do {
                    if (!var13.hasNext()) {
                        return;
                    }

                    child = (Component)var13.next();
                } while(child instanceof TabComponent && SkeetUI.this.selectedTab != child);

                child.drawComponent(lockedResolution, mouseX, mouseY);
            }
        }

        public void onKeyPress(int keyCode) {
            Iterator var2 = this.children.iterator();

            while(true) {
                Component child;
                do {
                    if (!var2.hasNext()) {
                        return;
                    }

                    child = (Component)var2.next();
                } while(child instanceof TabComponent && SkeetUI.this.selectedTab != child);

                child.onKeyPress(keyCode);
            }
        }

        public void onMouseClick(int mouseX, int mouseY, int button) {
            Iterator var4 = this.children.iterator();

            Component tabOrSideBar;
            while(var4.hasNext()) {
                tabOrSideBar = (Component)var4.next();
                if (tabOrSideBar instanceof TabComponent) {
                    if (SkeetUI.this.selectedTab != tabOrSideBar) {
                        continue;
                    }

                    if (tabOrSideBar.isHovered(mouseX, mouseY)) {
                        tabOrSideBar.onMouseClick(mouseX, mouseY, button);
                        break;
                    }
                }

                tabOrSideBar.onMouseClick(mouseX, mouseY, button);
            }

            if (button == 0 && this.isHovered(mouseX, mouseY)) {
                var4 = this.getChildren().iterator();

                while(true) {
                    label44:
                    do {
                        do {
                            if (!var4.hasNext()) {
                                //SkeetUI.this.dragging = true;
                                SkeetUI.this.prevX = (float)mouseX - this.getX();
                                SkeetUI.this.prevY = (float)mouseY - this.getY();
                                return;
                            }

                            tabOrSideBar = (Component)var4.next();
                            if (tabOrSideBar instanceof TabComponent) {
                                continue label44;
                            }
                        } while(!tabOrSideBar.isHovered(mouseX, mouseY));

                        return;
                    } while(SkeetUI.this.selectedTab != tabOrSideBar);

                    Iterator var6 = tabOrSideBar.getChildren().iterator();

                    while(var6.hasNext()) {
                        Component groupBox = (Component)var6.next();
                        if (groupBox instanceof GroupBoxComponent) {
                            GroupBoxComponent groupBoxComponent = (GroupBoxComponent)groupBox;
                            if (groupBoxComponent.isHoveredEntire(mouseX, mouseY)) {
                                return;
                            }
                        }
                    }
                }
            }
        }

        public void onMouseRelease(int button) {
            super.onMouseRelease(button);
            SkeetUI.this.dragging = false;
        }
    };

    private final Component tabSelectorComponent;
    private double targetAlpha;
    private boolean closed;
    private boolean dragging;
    private float prevX;
    private float prevY;
    private int selectorIndex;
    private TabComponent selectedTab;

    private SkeetUI() {
        Module.Category[] var1 = Module.Category.values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            final Module.Category category = var1[var3];
            TabComponent categoryTab = new TabComponent(this.rootComponent, StringUtils.upperSnakeCaseToPascal(category.name()), 51.5F, 5.0F, 415.0F, 397.5F) {
                public void setupChildren() {
                    List<Module> modulesInCategory = Resolute.getModulesByCategory(category);
                    Iterator var2 = modulesInCategory.iterator();

                    while(var2.hasNext()) {
                        Module module = (Module)var2.next();
                        GroupBoxComponent groupBoxComponent = new GroupBoxComponent(this, module.getName(), 0.0F, 0.0F, 94.333336F, 6.0F);
                        final CheckBoxTextComponent enabledButton = new CheckBoxTextComponent(groupBoxComponent, "Enabled", module::isEnabled, module::setState);
                        enabledButton.addChild(new KeyBindComponent(enabledButton, module::getKey, module::setKey, 42.0f, 1.0f));
                        groupBoxComponent.addChild(enabledButton);
                        this.addChild(groupBoxComponent);
                        Iterator var6 = module.getSettings().iterator();

                        while(var6.hasNext()) {
                            Setting property = (Setting) var6.next();
                            Component component = null;

                            if(property instanceof BooleanSetting) {
                                BooleanSetting bool = (BooleanSetting) property;

                                component = new CheckBoxTextComponent(groupBoxComponent, bool.name, bool::isEnabled, bool::setEnabled, bool::isAvailable);
                            }

                            if(property instanceof ModeSetting) {
                                ModeSetting mode = (ModeSetting) property;

                                component = new ComboBoxTextComponent(groupBoxComponent, mode.name, mode::getModes, mode::setSelected, Collections.singletonList(mode.getMode()), mode::isAvailable);
                            }

                            if(property instanceof NumberSetting) {
                                NumberSetting number = (NumberSetting) property;

                                component = new SliderTextComponent(groupBoxComponent, number.name, number::getValue, number::setValue, number::getMinimum, number::getMaximum, number::getIncrement, number::isAvailable);
                            }
                            if(property instanceof ColorSetting) {
                                ColorSetting color = (ColorSetting) property;

                                component = new ColorPickerTextComponent(groupBoxComponent, color.name, color::getColor, color::setColor, color::isAvailable);
                            }

                            if(component != null)
                                groupBoxComponent.addChild(component);
                        }
                    }

                    this.getChildren().sort(Comparator.comparingDouble(Component::getHeight).reversed());
                }
            };
            this.rootComponent.addChild(categoryTab);
        }

        TabComponent colorTab = new TabComponent(this.rootComponent, "Settings", 51.5F, 5.0F, 415.0F, 341.5F) {
            public void setupChildren() {
                GroupBoxComponent guiSettingsGroupBox = new GroupBoxComponent(this, "GUI Settings", 8.0F, 8.0F, 94.333336F, 100.0F);
                Supplier var10005 = SkeetUI::getColor;
                Consumer<Color> var10006 = SkeetUI::setColorValue;
                Setting var10007 = SkeetUI.GUI_COLOR;
                var10007.getClass();
                guiSettingsGroupBox.addChild(new ColorPickerTextComponent(guiSettingsGroupBox, "Gui Color", var10005, var10006));
                this.addChild(guiSettingsGroupBox);
            }
        };

        this.rootComponent.addChild(colorTab);

        TabComponent targetTab = new TabComponent(this.rootComponent, "Targets", 51.5F, 5.0F, 415.0F, 341.5F) {
            public void setupChildren() {
                GroupBoxComponent guiSettingsGroupBox = new GroupBoxComponent(this, "Targets", 8.0F, 8.0F, 94.333336F, 100.0F);
                BooleanSetting var10007 = SkeetUI.PLAYERS;
                BooleanSetting var10008 = SkeetUI.ANIMALS;
                BooleanSetting var10009 = SkeetUI.MOBS;
                BooleanSetting var10010 = SkeetUI.INVISIBLES;
                BooleanSetting var10011 = SkeetUI.VILLAGERS;
                BooleanSetting var10012 = SkeetUI.TEAMS;

                var10007.getClass();
                guiSettingsGroupBox.addChild(new CheckBoxTextComponent(guiSettingsGroupBox, var10007.name, var10007::isEnabled, var10007::setEnabled, var10007::isAvailable));
                guiSettingsGroupBox.addChild(new CheckBoxTextComponent(guiSettingsGroupBox, var10008.name, var10008::isEnabled, var10008::setEnabled, var10008::isAvailable));
                guiSettingsGroupBox.addChild(new CheckBoxTextComponent(guiSettingsGroupBox, var10009.name, var10009::isEnabled, var10009::setEnabled, var10009::isAvailable));
                guiSettingsGroupBox.addChild(new CheckBoxTextComponent(guiSettingsGroupBox, var10010.name, var10010::isEnabled, var10010::setEnabled, var10010::isAvailable));
                guiSettingsGroupBox.addChild(new CheckBoxTextComponent(guiSettingsGroupBox, var10011.name, var10011::isEnabled, var10011::setEnabled, var10011::isAvailable));
                guiSettingsGroupBox.addChild(new CheckBoxTextComponent(guiSettingsGroupBox, var10012.name, var10012::isEnabled, var10012::setEnabled, var10012::isAvailable));
                this.addChild(guiSettingsGroupBox);
            }
        };

        this.rootComponent.addChild(targetTab);

        TabComponent configTab = new TabComponent(this.rootComponent, "Player", 51.5F, 5.0F, 415.0F, 341.5F) {
            public void setupChildren() {
                final GroupBoxComponent configsGroupBox = new GroupBoxComponent(this, "Player", 8.0f, 8.0f, 94.333336f, 140.0f);

                configsGroupBox.addChild(new PlayerComponent(configsGroupBox, 8.0f, 8.0f));

                /*
                final Consumer<Integer> onPress = button -> {};
                configsGroupBox.addChild(new ButtonComponentImpl(configsGroupBox, "Load", onPress, 88.333336f, 15.0f));
                configsGroupBox.addChild(new ButtonComponentImpl(configsGroupBox, "Save", onPress, 88.333336f, 15.0f));
                configsGroupBox.addChild(new ButtonComponentImpl(configsGroupBox, "Refresh", onPress, 88.333336f, 15.0f));
                configsGroupBox.addChild(new ButtonComponentImpl(configsGroupBox, "Delete", onPress, 88.333336f, 15.0f));

                 */


                this.addChild(configsGroupBox);
            }
        };

        this.rootComponent.addChild(configTab);

        this.selectedTab = (TabComponent)this.rootComponent.getChildren().get(this.selectorIndex);
        this.tabSelectorComponent = new Component(this.rootComponent, 3.5F, 5.0F, 48.0F, 391.5F) {
            private double selectorY;

            public void onMouseClick(int mouseX, int mouseY, int button) {
                if (this.isHovered(mouseX, mouseY)) {
                    float mouseYOffset = (float)mouseY - SkeetUI.this.tabSelectorComponent.getY() - 10.0F;
                    if (mouseYOffset > 0.0F && mouseYOffset < SkeetUI.this.tabSelectorComponent.getHeight() - 10.0F) {
                        SkeetUI.this.selectorIndex = Math.min(SkeetUI.ICONS.length - 1, (int)(mouseYOffset / (float)SkeetUI.TAB_SELECTOR_HEIGHT));
                        SkeetUI.this.selectedTab = (TabComponent)SkeetUI.this.rootComponent.getChildren().get(SkeetUI.this.selectorIndex);
                    }
                }
            }

            public void drawComponent(ScaledResolution resolution, int mouseX, int mouseY) {
                this.selectorY = RenderUtils.progressiveAnimation(this.selectorY, (double)(SkeetUI.this.selectorIndex * SkeetUI.TAB_SELECTOR_HEIGHT + 10), 1.0D);
                float x = this.getX();
                float y = this.getY();
                float width = this.getWidth();
                float height = this.getHeight();
                int innerColor = SkeetUI.getColor(394758);
                int outerColor = SkeetUI.getColor(2105376);
                Gui.drawRect((double)x, (double)y, (double)(x + width), (double)y + this.selectorY, SkeetUI.getColor(789516));
                Gui.drawRect((double)(x + width - 1.0F), (double)y, (double)(x + width), (double)y + this.selectorY, innerColor);
                Gui.drawRect((double)(x + width - 0.5F), (double)y, (double)(x + width), (double)y + this.selectorY, outerColor);
                Gui.drawRect((double)x, (double)y + this.selectorY - 1.0D, (double)(x + width - 0.5F), (double)y + this.selectorY, innerColor);
                Gui.drawRect((double)x, (double)y + this.selectorY - 0.5D, (double)(x + width), (double)y + this.selectorY, outerColor);
                Gui.drawRect((double)x, (double)y + this.selectorY + (double)SkeetUI.TAB_SELECTOR_HEIGHT, (double)(x + width), (double)(y + height), SkeetUI.getColor(789516));
                Gui.drawRect((double)(x + width - 1.0F), (double)y + this.selectorY + (double)SkeetUI.TAB_SELECTOR_HEIGHT, (double)(x + width), (double)(y + height), innerColor);
                Gui.drawRect((double)(x + width - 0.5F), (double)y + this.selectorY + (double)SkeetUI.TAB_SELECTOR_HEIGHT, (double)(x + width), (double)(y + height), outerColor);
                Gui.drawRect((double)x, (double)y + this.selectorY + (double)SkeetUI.TAB_SELECTOR_HEIGHT, (double)(x + width - 0.5F), (double)y + this.selectorY + (double)SkeetUI.TAB_SELECTOR_HEIGHT + 1.0D, innerColor);
                Gui.drawRect((double)x, (double)y + this.selectorY + (double)SkeetUI.TAB_SELECTOR_HEIGHT, (double)(x + width), (double)y + this.selectorY + (double)SkeetUI.TAB_SELECTOR_HEIGHT + 0.5D, outerColor);
                if (SkeetUI.shouldRenderText()) {
                    for(int i = 0; i < SkeetUI.ICONS.length; ++i) {
                        String c = String.valueOf(SkeetUI.ICONS[i]);
                        Gui.drawRect(0,0,0,0,-1);
                        SkeetUI.ICONS_RENDERER.drawString(c, x + 24.0F - SkeetUI.ICONS_RENDERER.getStringWidth(c) / 2.0F - 1.0F, y + 10.0F + (float)(i * SkeetUI.TAB_SELECTOR_HEIGHT) + (float)SkeetUI.TAB_SELECTOR_HEIGHT / 2.0F - SkeetUI.ICONS_RENDERER.getHeight() / 2.0F, SkeetUI.getColor(i == SkeetUI.this.selectorIndex ?  16777215 : 8421504));
                    }
                }
            }
        };
        this.rootComponent.addChild(this.tabSelectorComponent);
    }

    public static double getAlpha() {
        return alpha;
    }

    public static boolean shouldRenderText() {
        return alpha > 20.0D;
    }

    private static boolean isVisible() {
        return open || alpha > 0.0D;
    }

    public static int getColor() {
        return getColor((Integer)GUI_COLOR.getColor());
    }

    public static boolean isPlayers() {
        return PLAYERS.isEnabled();
    }

    public static boolean isAnimals() {
        return ANIMALS.isEnabled();
    }

    public static boolean isMobs() {
        return MOBS.isEnabled();
    }

    public static boolean isInvisibles() {
        return INVISIBLES.isEnabled();
    }

    public static boolean isVillagers() {
        return VILLAGERS.isEnabled();
    }

    public static boolean isTeams() {
        return TEAMS.isEnabled();
    }

    public static void setColorValue(Color color) {
        GUI_COLOR.setColor(color);
    }

    public static int getColor(int color) {
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        int a = (int)alpha;
        return (r & 255) << 16 | (g & 255) << 8 | b & 255 | (a & 255) << 24;
    }

    public static void init() {
        INSTANCE.open();
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            this.close();
        } else {
            this.rootComponent.onKeyPress(keyCode);
        }

    }

    private void close() {
        if (open) {
            this.targetAlpha = 0.0D;
            open = false;
            this.dragging = false;
        }
    }

    private void open() {
        Minecraft.getMinecraft().displayGuiScreen(this);
        alpha = 0.0D;
        this.targetAlpha = 255.0D;
        open = true;
        this.closed = false;
    }

    private boolean finishedClosing() {
        return !open && SkeetUI.alpha == 0.0 && !this.closed;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.finishedClosing()) {
            Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
        } else {
            if (isVisible()) {
                alpha = RenderUtils.linearAnimation(alpha, this.targetAlpha, 10.0D);
                this.rootComponent.drawComponent(new ScaledResolution(Minecraft.getMinecraft()), mouseX, mouseY);
            }
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isVisible()) {
            this.rootComponent.onMouseClick(mouseX, mouseY, mouseButton);
        }

    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (isVisible()) {
            this.rootComponent.onMouseRelease(state);
        }

    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    static {
        TAB_SELECTOR_HEIGHT = 381 / (ICONS.length + 0);

        GUI_COLOR = new ColorSetting("Gui Color", new Color(255, 255, 255));

        PLAYERS = new BooleanSetting("Players", true);
        ANIMALS = new BooleanSetting("Animals", true);
        MOBS = new BooleanSetting("Mobs", true);
        INVISIBLES = new BooleanSetting("Invisibles", true);
        VILLAGERS = new BooleanSetting("Villagers", true);
        TEAMS = new BooleanSetting("Teams", true);

        ICONS_RENDERER = FontUtil.icons;
        FONT_RENDERER = FontUtil.tahomaSmall;
        KEYBIND_FONT_RENDERER = FontUtil.tahomaSmall;
        INSTANCE = new SkeetUI();
    }
}
