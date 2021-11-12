package vip.Resolute.auth;

import java.io.IOException;

import vip.Resolute.Resolute;
import vip.Resolute.ui.MainMenu;
import vip.Resolute.ui.notification.Notification;
import vip.Resolute.ui.notification.NotificationType;
import vip.Resolute.ui.shader.GLSLSandboxShader;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.font.MinecraftFontRenderer;
import vip.Resolute.util.render.TranslationUtils;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;


public class LoginScreen extends GuiScreen {
    private GuiScreen previousScreen;
    private Authentication thread;
    private GuiTextField username;
    private PasswordField password;

    private GLSLSandboxShader backgroundShader;
    private long initTime = System.currentTimeMillis();

    MinecraftFontRenderer fontRenderer = FontUtil.clientfont;

    public static LoginScreen instance = new LoginScreen();

    public TranslationUtils translate;
    public static String progression = "Idle...";

    public LoginScreen() {

        this.previousScreen = previousScreen;

        try {
            this.backgroundShader = new GLSLSandboxShader("/mainMenuShader.frag");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load backgound shader", e);
        }
    }

    public static LoginScreen getInstance() {
        return instance;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if(button.id == 0) {
            progression = "Initializing...";
            this.thread = new Authentication(this.username.getText(), this.password.getText());
            this.thread.start();
        }

        if(button.id == 69) {
            GuiScreen.setClipboardString(Authentication.getEncryptedAuthString(this.username.getText(), this.password.getText(), Authentication.key));
            Resolute.getNotificationManager().add(new Notification("Success", "Copied key to clipboard", 5000L, NotificationType.SUCCESS));
        }
    }

    @Override
    public void drawScreen(int x2, int y2, float z2) {
        GlStateManager.enableAlpha();
        GlStateManager.disableCull();
        int i = 274;
        int j = this.width / 2 - i / 2;
        int k = 30;
        this.backgroundShader.useShader(this.width * 2, this.height * 2, x2, y2, (System.currentTimeMillis() - initTime) / 1000f);

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glVertex2f(-1f, -1f);
        GL11.glVertex2f(-1f, 1f);
        GL11.glVertex2f(1f, 1f);
        GL11.glVertex2f(1f, -1f);

        GL11.glEnd();

        // Unbind shader
        GL20.glUseProgram(0);

        translate.interpolate(this.width, this.height, 2);

        double xmod = this.height - (translate.getY());
        GL11.glPushMatrix();
        GlStateManager.translate(0, 0.5 * xmod, 0);

        Gui.drawRect(width / 2 - 100, this.height / 2 + 55, width / 2 + 100, this.height / 2 - 55, 0x8F000000);

        FontUtil.tahoma.drawCenteredString(progression, width / 2, this.height / 2 - 45, -1);

        this.username.drawTextBox();
        this.password.drawTextBox();

        super.drawScreen(x2, y2, z2);
        GL11.glPopMatrix();

        if(Resolute.authorized) {
            try {
                mc.displayGuiScreen(new MainMenu());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initGui() {
        int var3 = height / 4 + 24;
        this.translate = new TranslationUtils(0, 0);
        this.buttonList.add(new GuiButton(0, width / 2 - 73, this.height / 2 + 25, 150, 20, "Login"));
        this.username = new GuiTextField(var3, this.mc.fontRendererObj, width / 2 - 73, this.height / 2 - 25, 150, 20);
        this.password = new PasswordField(this.mc.fontRendererObj, width / 2 - 73, this.height / 2 , 150, 20);
        this.buttonList.add(new GuiButton(69, 5, 5, 90, 20, "Copy Key"));
        this.username.setFocused(true);
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    protected void keyTyped(char character, int key) {
        try {
            super.keyTyped(character, key);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (character == '\t') {
            if (!this.username.isFocused() && !this.password.isFocused()) {
                this.username.setFocused(true);
            } else {
                this.username.setFocused(this.password.isFocused());
                this.password.setFocused(!this.username.isFocused());
            }
        }
        if (character == '\r') {
            this.actionPerformed((GuiButton)this.buttonList.get(0));
        }
        this.username.textboxKeyTyped(character, key);
        this.password.textboxKeyTyped(character, key);
    }

    @Override
    protected void mouseClicked(int x2, int y2, int button) {
        try {
            super.mouseClicked(x2, y2, button);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        this.username.mouseClicked(x2, y2, button);
        this.password.mouseClicked(x2, y2, button);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        this.username.updateCursorCounter();
        this.password.updateCursorCounter();
    }
}
