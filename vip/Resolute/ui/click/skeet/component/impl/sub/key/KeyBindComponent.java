package vip.Resolute.ui.click.skeet.component.impl.sub.key;

import java.util.function.Consumer;
import java.util.function.Supplier;

import vip.Resolute.ui.click.skeet.SkeetUI;
import vip.Resolute.ui.click.skeet.framework.ButtonComponent;
import vip.Resolute.ui.click.skeet.framework.Component;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.font.MinecraftFontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;


public final class KeyBindComponent extends ButtonComponent {
    private static final MinecraftFontRenderer FONT_RENDERER = FontUtil.tahomaSmall;
    private final Supplier<Integer> getBind;
    private final Consumer<Integer> onSetBind;
    private boolean binding;

    public KeyBindComponent(Component parent, Supplier<Integer> getBind, Consumer<Integer> onSetBind, float x, float y) {
        super(parent, x, y, (float) (FONT_RENDERER.getStringWidth("[") * 2.0F), FONT_RENDERER.getHeight());
        this.getBind = getBind;
        this.onSetBind = onSetBind;
    }

    public float getWidth() {
        return (float) (super.getWidth() + FONT_RENDERER.getStringWidth(this.getBind()));
    }

    public void drawComponent(ScaledResolution lockedResolution, int mouseX, int mouseY) {
        float x = this.getX();
        float y = this.getY();
        float width = this.getWidth();
        FONT_RENDERER.drawStringWithShadow("[" + this.getBind() + "]", x + 65.166668F - width, y - 10, SkeetUI.getColor(7895160));
    }

    public boolean isHovered(int mouseX, int mouseY) {
        float x = this.getX();
        float y = this.getY();
        return (float)mouseX >= x + 50.166668F - this.getWidth() && (float)mouseY >= y - 10 && (float)mouseX <= x + 150 && (float)mouseY <= y + this.getHeight() - 10;
    }

    public void onKeyPress(int keyCode) {
        if (this.binding) {
            if (keyCode == 211 || keyCode == 14) {
                keyCode = 0;
            }

            this.onChangeBind(keyCode);
            this.binding = false;
        }

    }

    private String getBind() {
        int bind = (Integer)this.getBind.get();
        return this.binding ? "..." : (bind == 0 ? "-" : Keyboard.getKeyName(bind));
    }

    private void onChangeBind(int bind) {
        this.onSetBind.accept(bind);
    }

    public void onPress(int mouseButton) {
        this.binding = !this.binding;
    }
}
