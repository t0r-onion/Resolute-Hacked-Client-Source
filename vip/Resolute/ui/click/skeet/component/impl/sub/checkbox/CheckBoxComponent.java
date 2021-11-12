package vip.Resolute.ui.click.skeet.component.impl.sub.checkbox;

import vip.Resolute.ui.click.skeet.SkeetUI;
import vip.Resolute.ui.click.skeet.framework.ButtonComponent;
import vip.Resolute.ui.click.skeet.framework.Component;
import vip.Resolute.ui.click.skeet.framework.PredicateComponent;
import vip.Resolute.util.render.RenderUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

public abstract class CheckBoxComponent extends ButtonComponent implements PredicateComponent {
    public CheckBoxComponent(Component parent, float x, float y, float width, float height) {
        super(parent, x, y, width, height);
    }

    public void drawComponent(ScaledResolution resolution, int mouseX, int mouseY) {
        float x = this.getX();
        float y = this.getY();
        float width = this.getWidth();
        float height = this.getHeight();
        Gui.drawRect((double)x, (double)y, (double)(x + width), (double)(y + height), SkeetUI.getColor(855309));
        boolean checked = this.isChecked();
        boolean hovered = this.isHovered(mouseX, mouseY);
        RenderUtils.drawGradientRect((double)(x + 0.5F), (double)(y + 0.5F), (double)(x + width - 0.5F), (double)(y + height - 0.5F), false, checked ? SkeetUI.getColor() : SkeetUI.getColor(hovered ? RenderUtils.darker(4802889, 1.4F) : 4802889), checked ? RenderUtils.darker(SkeetUI.getColor(), 0.8F) : SkeetUI.getColor(hovered ? RenderUtils.darker(3158064, 1.4F) : 3158064));
    }

    public void onPress(int mouseButton) {
        if (mouseButton == 0) {
            this.setChecked(!this.isChecked());
        }

    }

    public abstract boolean isChecked();

    public abstract void setChecked(boolean var1);
}
