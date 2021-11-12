package vip.Resolute.ui.click.skeet.component.impl.sub.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import vip.Resolute.ui.click.skeet.SkeetUI;
import vip.Resolute.ui.click.skeet.framework.ButtonComponent;
import vip.Resolute.ui.click.skeet.framework.Component;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.font.MinecraftFontRenderer;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlayerComponent extends ButtonComponent {
    private static final MinecraftFontRenderer FONT_RENDERER = FontUtil.tahomaSmall;
    private Minecraft mc = Minecraft.getMinecraft();

    public PlayerComponent(Component parent, float x, float y) {
        super(parent, x, y, (float) (FONT_RENDERER.getStringWidth("[") * 2.0F), FONT_RENDERER.getHeight());
    }

    public float getWidth() {
        return (float) (super.getWidth());
    }

    public void drawComponent(ScaledResolution lockedResolution, int mouseX, int mouseY) {
        float x = this.getX();
        float y = this.getY();
        float width = this.getWidth();
        try {
            GuiInventory.drawEntityOnScreen((int) (x + 52f), (int) (y + 124.0f), 50, 0, 0, Minecraft.getMinecraft().thePlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isHovered(int mouseX, int mouseY) {
        float x = this.getX();
        float y = this.getY();
        return (float)mouseX >= x + 50.166668F - this.getWidth() && (float)mouseY >= y - 10 && (float)mouseX <= x + 150 && (float)mouseY <= y + this.getHeight() - 10;
    }

    public void onKeyPress(int keyCode) {}

    public void onPress(int mouseButton) {}
}
