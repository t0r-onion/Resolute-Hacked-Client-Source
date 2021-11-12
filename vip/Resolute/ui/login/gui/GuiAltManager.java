package vip.Resolute.ui.login.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


import vip.Resolute.Resolute;
import vip.Resolute.ui.MainMenu;
import vip.Resolute.ui.login.gui.thread.AltLoginThread;
import vip.Resolute.ui.login.gui.components.GuiAccountList;
import vip.Resolute.ui.login.gui.impl.AccountImport;
import vip.Resolute.ui.login.gui.impl.GuiAddAlt;
import vip.Resolute.ui.login.gui.impl.GuiAltLogin;
import vip.Resolute.ui.login.system.Account;
import vip.Resolute.ui.shader.GLSLSandboxShader;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.font.MinecraftFontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import javax.swing.*;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class GuiAltManager extends GuiScreen {
    public static GuiAltManager INSTANCE;
    private GuiAccountList accountList;
    private Account selectAccount = null;
    public static Account currentAccount;
    private GLSLSandboxShader backgroundShader;
    public static AltLoginThread loginThread;
    private long initTime = System.currentTimeMillis();
    private final Random random = new Random();

    public GuiAltManager() {
        INSTANCE = this;

        try {
            this.backgroundShader = new GLSLSandboxShader("/mainMenuShader.frag");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load backgound shader", e);
        }
    }

    public void initGui() {
        accountList = new GuiAccountList(this);
        accountList.registerScrollButtons(7, 8);
        accountList.elementClicked(-1, false, 0, 0);

        this.buttonList.add(new GuiButton(0, this.width / 2 + 158, this.height - 24, 100, 20, "Cancel"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 154, this.height - 48, 100, 20, "Login"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 50, this.height - 24, 100, 20, "Remove"));
        this.buttonList.add(new GuiButton(5, this.width / 2 + 4 + 50, this.height - 48, 100, 20, "Import Alts"));
        this.buttonList.add(new GuiButton(4, this.width / 2 - 50, this.height - 48, 100, 20, "Direct Login"));
        this.buttonList.add(new GuiButton(3, this.width / 2 - 154, this.height - 24, 100, 20, "Add Alt"));
        this.buttonList.add(new GuiButton(7, this.width / 2 + 54, this.height - 24, 100, 20, "Random Alt"));
        this.buttonList.add(new GuiButton(8, this.width / 2 - 258, this.height - 48, 100, 20, "Last Alt"));
        this.buttonList.add(new GuiButton(9, this.width / 2 + 158, this.height - 48, 100, 20, "Clear Alts"));
    }

    @Override
    public void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_){
        GlStateManager.enableAlpha();
        GlStateManager.disableCull();
        int i = 274;
        int j = this.width / 2 - i / 2;
        int k = 30;
        this.backgroundShader.useShader(this.width, this.height , p_drawScreen_1_, p_drawScreen_2_, (System.currentTimeMillis() - initTime) / 1000f);

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glVertex2f(-1f, -1f);
        GL11.glVertex2f(-1f, 1f);
        GL11.glVertex2f(1f, 1f);
        GL11.glVertex2f(1f, -1f);

        GL11.glEnd();

        // Unbind shader
        GL20.glUseProgram(0);

        accountList.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_);
        super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_);

        String status = "Idle...";

        if(loginThread != null) status = loginThread.getStatus();
        final MinecraftFontRenderer fr = FontUtil.moon;

        fr.drawCenteredStringWithShadow(ChatFormatting.GREEN + mc.session.getUsername(), width / 23, 13, -1);
        fr.drawCenteredStringWithShadow("Account Manager - " + Resolute.getAccountManager().getAccounts().size() + " alts", width / 2, 4, -1);
        fr.drawCenteredStringWithShadow(loginThread == null ? ChatFormatting.GRAY + "Idle..." : loginThread.getStatus(), width / 2, 16, -1);
    }

    @Override
    public void handleMouseInput() throws IOException{
        super.handleMouseInput();
        accountList.handleMouseInput();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException{
        switch (button.id) {
            case 0:
                if (loginThread == null || !loginThread.getStatus().contains("Logging in")) {
                    mc.displayGuiScreen(new MainMenu());
                }
                break;
            case 1:
                if(accountList.selected == -1)
                    return;

                loginThread = new AltLoginThread(accountList.getSelectedAccount().getEmail(),accountList.getSelectedAccount().getPassword());
                loginThread.start();
                break;
            case 2:
                accountList.removeSelected();
                break;
            case 3:
                if (loginThread != null)
                    loginThread = null;

                mc.displayGuiScreen(new GuiAddAlt(this));
                break;
            case 4:
                if (loginThread != null)
                    loginThread = null;

                mc.displayGuiScreen(new GuiAltLogin(this));
                break;
            //case 6:
            //    break;
            case 7:
                ArrayList<Account> registry = Resolute.getAccountManager().getAccounts();
                if (registry.isEmpty()) return;
                Random random = new Random();
                Account randomAlt = registry.get(random.nextInt(Resolute.getAccountManager().getAccounts().size()));
                if(randomAlt.isBanned())
                    return;

                currentAccount = randomAlt;
                login(randomAlt);
                break;
            case 5:
                JFrame frame = new JFrame("Import");
                frame.setAlwaysOnTop(true);
                AccountImport accountImport = new AccountImport();
                frame.setContentPane(accountImport);
                new Thread(() -> accountImport.openButton.doClick()).start();
                break;
            case 8:
                if(Resolute.getAccountManager().getLastAlt() == null)
                    return;
                loginThread = new AltLoginThread(Resolute.getAccountManager().getLastAlt().getEmail(),Resolute.getAccountManager().getLastAlt().getPassword());
                loginThread.start();
                break;
            case 9:
                if (Resolute.getAccountManager().getAccounts().isEmpty()) return;
                Resolute.getAccountManager().getAccounts().clear();
                break;
        }
    }

    public void login(Account account){
        loginThread = new AltLoginThread(account.getEmail(),account.getPassword());
        loginThread.start();
    }
}
