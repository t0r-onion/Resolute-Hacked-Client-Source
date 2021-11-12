package vip.Resolute.ui.login.gui.components;

import vip.Resolute.Resolute;
import vip.Resolute.ui.login.gui.GuiAltManager;
import vip.Resolute.ui.login.system.Account;
import vip.Resolute.util.render.RenderUtils;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.font.MinecraftFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.FontRenderer;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;

import com.mojang.realmsclient.gui.ChatFormatting;

public class GuiAccountList extends GuiSlot {

    public int selected = -1;
    private GuiAltManager parent;

    public GuiAccountList(GuiAltManager parent){
        super(Minecraft.getMinecraft(), parent.width, parent.height, 36, parent.height - 56, 40);
        this.parent = parent;

    }

    @Override
    public int getSize(){
        return Resolute.getAccountManager().getAccounts().size();
    }

    @Override
    public void elementClicked(int i, boolean b, int i1, int i2){
        selected = i;

        if(b){
            parent.login(getAccount(i));
        }
    }

    @Override
    protected boolean isSelected(int i){
        return i == selected;
    }

    @Override
    protected void drawBackground(){

    }

    @Override
    protected void drawSlot(int i, int i1, int i2, int i3, int i4, int i5) {

        Account account = getAccount(i);
        Minecraft minecraft = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(minecraft);
        FontRenderer fontRenderer = minecraft.fontRendererObj;

        int x = i1 + 2;
        int y = i2;

        if (y >= scaledResolution.getScaledHeight() || y < 0)
            return;

        glTranslated(x, y, 0);

        //RenderUtil.drawImage(account.getHead(), 0, 6, 24, 24);
        RenderUtils.drawRect(0, 6, 24, 24, Color.TRANSLUCENT);
        final MinecraftFontRenderer fr = FontUtil.moon;
        fr.drawStringWithShadow(account.getName(), 30, 6, 0xFFFFFFFF);
        fr.drawStringWithShadow(ChatFormatting.GRAY + account.getEmail(), 30, 6 + fontRenderer.FONT_HEIGHT + 2, 0xFFFFFFFF);

        glTranslated(-x, -y, 0);

    }

    public Account getAccount(int i){
        return  Resolute.getAccountManager().getAccounts().get(i);
    }

    public void removeSelected(){
        if(selected == -1)
            return;

        Resolute.getAccountManager().getAccounts().remove(getAccount(selected));
        Resolute.getAccountManager().save();
    }

    public Account getSelectedAccount(){
        return getAccount(selected);
    }
}
