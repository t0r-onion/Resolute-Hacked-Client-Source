package vip.Resolute.ui;

import vip.Resolute.Resolute;
import vip.Resolute.auth.LoginScreen;
import vip.Resolute.com.viamcp.gui.GuiProtocolSelector;
import vip.Resolute.ui.login.gui.GuiAltManager;
import vip.Resolute.ui.shader.GLSLSandboxShader;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.render.TranslationUtils;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.resources.I18n;

import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.io.IOException;
import net.minecraft.client.renderer.texture.DynamicTexture;

public class MainMenu extends GuiScreen {
    private GuiButton buttonResetDemo;
    private DynamicTexture viewportTexture;

    private GLSLSandboxShader backgroundShader;
    private long initTime = System.currentTimeMillis();

    public TranslationUtils translate;

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
    }

    public MainMenu() {
        try {
            this.backgroundShader = new GLSLSandboxShader("/mainMenuShader.frag");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load backgound shader", e);
        }
    }

    @Override
    public void initGui() {
        Resolute.getInstance().getDiscordRP().update("Idle", "");
        this.translate = new TranslationUtils(0, 0);
        this.viewportTexture = new DynamicTexture(256, 256);
        final int i = 24;
        final int j = this.height / 4 + 48;
        this.addSingleplayerMultiplayerButtons(j, 24);
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, j + 72 + 12, 98, 20, I18n.format("menu.options", new Object[0])));
        this.buttonList.add(new GuiButton(4, this.width / 2 + 2, j + 72 + 12, 98, 20, I18n.format("menu.quit", new Object[0])));
        this.buttonList.add(new GuiButtonLanguage(5, this.width / 2 - 124, j + 72 + 12));
        this.buttonList.add(new GuiButton(69, 5, 5, 90, 20, "Protocol"));
        this.mc.func_181537_a(false);

        if(!Resolute.authorized)
            mc.displayGuiScreen(new LoginScreen());
    }

    private void addSingleplayerMultiplayerButtons(final int p_73969_1_, final int p_73969_2_) {
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, p_73969_1_, I18n.format("menu.singleplayer", new Object[0])));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, p_73969_1_ + p_73969_2_, I18n.format("menu.multiplayer", new Object[0])));
        this.buttonList.add(new GuiButton(100, this.width / 2 - 100, p_73969_1_ + p_73969_2_ * 2, "Alt Manager"));
    }

    @Override
    protected void actionPerformed(final GuiButton button) throws IOException {
        if (button.id == 0) {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        }
        else if (button.id == 5) {
            this.mc.displayGuiScreen(new GuiLanguage(this, this.mc.gameSettings, this.mc.getLanguageManager()));
        }
        else if (button.id == 1) {
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
        }
        else if (button.id == 2) {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }
        else if (button.id == 14) {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }
        else if (button.id == 100) {
            this.mc.displayGuiScreen(new GuiAltManager());
        }
        else if (button.id == 4) {
            this.mc.shutdown();
        }

        if (button.id == 69)
        {
            this.mc.displayGuiScreen(new GuiProtocolSelector(this));
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        GlStateManager.enableAlpha();
        GlStateManager.disableCull();
        int i = 274;
        int j = this.width / 2 - i / 2;
        int k = 30;
        this.backgroundShader.useShader(this.width * 2, this.height * 2, mouseX, mouseY, (System.currentTimeMillis() - initTime) / 1000f);

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glVertex2f(-1f, -1f);
        GL11.glVertex2f(-1f, 1f);
        GL11.glVertex2f(1f, 1f);
        GL11.glVertex2f(1f, -1f);

        GL11.glEnd();

        // Unbind shader
        GL20.glUseProgram(0);

        float textX = this.width / 2 - 60;
        int textHeight = this.height / 4;

        String title = "Resolute";

        long currentMillis = System.currentTimeMillis();

        float posX = textX;
        long ms = (long) (4 * 1000L);

        translate.interpolate(this.width, this.height, 5);

        double xmod = this.height - (translate.getY());
        GL11.glPushMatrix();
        GlStateManager.translate(0, 0.5 * xmod, 0);

        for(int index = 0; index < title.length(); index++) {
            String ch = String.valueOf(title.charAt(index));

            final float offset = (currentMillis + (index * 100)) % ms / (ms / 2.0F);
            FontUtil.oxide.drawStringWithShadow(ch, posX, textHeight, fadeBetween(new Color(190, 0, 0).getRGB(), darker(new Color(237, 0, 255).getRGB(), 0.49f), offset));
            posX += FontUtil.oxide.getStringWidth(ch);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        GL11.glPopMatrix();
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
}
