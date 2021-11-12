package vip.Resolute.ui.click.skeet.component.impl.sub.button;

import java.util.function.Consumer;

import vip.Resolute.ui.click.skeet.SkeetUI;
import vip.Resolute.ui.click.skeet.framework.ButtonComponent;
import vip.Resolute.ui.click.skeet.framework.Component;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.render.RenderUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

public final class ButtonComponentImpl extends ButtonComponent {
    private final String text;
    private final Consumer<Integer> onPress;

    public ButtonComponentImpl(Component parent, String text, Consumer<Integer> onPress, float width, float height) {
        super(parent, 0.0F, 0.0F, width, height);
        this.text = text;
        this.onPress = onPress;
    }

    public void drawComponent(ScaledResolution lockedResolution, int mouseX, int mouseY) {
        float x = this.getX();
        float y = this.getY();
        float width = this.getWidth();
        float height = this.getHeight();
        boolean hovered = this.isHovered(mouseX, mouseY);
        Gui.drawRect((double)x, (double)y, (double)(x + width), (double)(y + height), SkeetUI.getColor(1118481));
        Gui.drawRect((double)(x + 0.5F), (double)(y + 0.5F), (double)(x + width - 0.5F), (double)(y + height - 0.5F), SkeetUI.getColor(2500134));
        RenderUtils.drawGradientRect((double)(x + 1.0F), (double)(y + 1.0F), (double)(x + width - 1.0F), (double)(y + height - 1.0F), false, SkeetUI.getColor(hovered ? RenderUtils.darker(2236962, 1.2F) : 2236962), SkeetUI.getColor(hovered ? RenderUtils.darker(1973790, 1.2F) : 1973790));
        if (SkeetUI.shouldRenderText()) {
            FontUtil.tahomaSmall.drawStringWithShadow(this.text, x + width / 2.0F - FontUtil.tahomaSmall.getStringWidth(this.text) / 2.0F, y + height / 2.0F - 1.0F, -1);
        }

    }

    public void onPress(int mouseButton) {
        this.onPress.accept(mouseButton);
    }
}
