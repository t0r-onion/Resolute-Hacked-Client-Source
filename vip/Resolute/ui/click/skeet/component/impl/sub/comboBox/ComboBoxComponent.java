package vip.Resolute.ui.click.skeet.component.impl.sub.comboBox;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.ScaledResolution;
import vip.Resolute.ui.click.skeet.SkeetUI;
import vip.Resolute.ui.click.skeet.framework.ButtonComponent;
import vip.Resolute.ui.click.skeet.framework.Component;
import vip.Resolute.ui.click.skeet.framework.PredicateComponent;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.font.MinecraftFontRenderer;
import vip.Resolute.util.render.RenderUtils;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

public abstract class ComboBoxComponent extends ButtonComponent implements PredicateComponent {
    private boolean expanded;
    String currentMode;

    public ComboBoxComponent(Component parent, float x, float y, float width, float height) {
        super(parent, x, y, width, height);
    }

    private String getDisplayString() {
        return getMode().toString();
    }

    public void drawComponent(ScaledResolution lockedResolution, int mouseX, int mouseY) {
        float x = this.getX();
        float y = this.getY();
        float width = this.getWidth();
        float height = this.getHeight();
        Gui.drawRect((double)x, (double)y, (double)(x + width), (double)(y + height), SkeetUI.getColor(855309));
        boolean hovered = this.isHovered(mouseX, mouseY);
        RenderUtils.drawGradientRect((double)(x + 0.5F), (double)(y + 0.5F), (double)(x + width - 0.5F), (double)(y + height - 0.5F), false, SkeetUI.getColor(hovered ? RenderUtils.darker(1973790, 1.4F) : 1973790), SkeetUI.getColor(hovered ? RenderUtils.darker(2302755, 1.4F) : 2302755));
        GL11.glColor4f(0.6F, 0.6F, 0.6F, (float)SkeetUI.getAlpha() / 255.0F);
        RenderUtils.drawAndRotateArrow(x + width - 5.0F, y + height / 2.0F - 0.5F, 3.0F, this.isExpanded());
        if (SkeetUI.shouldRenderText()) {
            GL11.glEnable(3089);
            RenderUtils.startScissorBox(lockedResolution, (int)x + 2, (int)y + 1, (int)width - 8, (int)height - 1);
            FontUtil.tahomaVerySmall.drawString(currentMode == null ? this.getDisplayString() : this.currentMode, x + 2.0F, y + height / 3.0F, SkeetUI.getColor(9868950));
            GL11.glDisable(3089);
        }

        if (this.expanded) {
            GL11.glTranslatef(0.0F, 0.0F, 2.0F);
            List<String> values = this.getModes();
            float dropDownHeight = (float)values.size() * height;
            Gui.drawRect((double)x, (double)(y + height), (double)(x + width), (double)(y + height + dropDownHeight + 0.5F), SkeetUI.getColor(855309));
            float valueBoxHeight = height;
            List<String> enums = this.getModes();
            List<String> var13 = enums;
            int var14 = enums.size();

            for(int var15 = 0; var15 < var14; ++var15) {
                List<String> value = Collections.singletonList(var13.get(var15));
                boolean valueBoxHovered = (float)mouseX >= x && (float)mouseY >= y + valueBoxHeight && (float)mouseX <= x + width && (float)mouseY < y + valueBoxHeight + height;
                Gui.drawRect((double)(x + 0.5F), (double)(y + valueBoxHeight), (double)(x + width - 0.5F), (double)(y + valueBoxHeight + height), SkeetUI.getColor(valueBoxHovered ? RenderUtils.darker(2302755, 0.7F) : 2302755));

                boolean selected = value == this.getMode();

                int color = selected ? SkeetUI.getColor() : SkeetUI.getColor(14474460);
                MinecraftFontRenderer fr = FontUtil.tahomaVerySmall;

                fr.drawString(value.toString(), x + 2.0F, y + valueBoxHeight + 4.0F, color);
                valueBoxHeight += height;
            }

            GL11.glTranslatef(0.0F, 0.0F, -2.0F);
        }

    }

    public void onMouseClick(int mouseX, int mouseY, int button) {
        if (this.isHovered(mouseX, mouseY)) {
            this.onPress(button);
        }

        if (this.isExpanded() && button == 0) {
            float x = this.getX();
            float y = this.getY();
            float height = this.getHeight();
            float width = this.getWidth();
            float valueBoxHeight = height;

            for(Object o : this.getModes()) {
                if ((float)mouseX >= x && (float)mouseY >= y + valueBoxHeight && (float)mouseX <= x + width && (float)mouseY <= y + valueBoxHeight + height) {
                    setSelected((String) o);
                    setCurrentMode((String) o);
                    return;
                }

                valueBoxHeight += height;
            }
        }

    }

    private void expandOrClose() {
        this.setExpanded(!this.isExpanded());
    }

    public void onPress(int mouseButton) {
        if (mouseButton == 1) {
            this.expandOrClose();
        }
    }

    public void setCurrentMode(String mode) {
        this.currentMode = mode;
    }

    public String getSelectedMode() {
        return this.currentMode;
    }

    public float getExpandedX() {
        return this.getX();
    }

    public abstract List<String> getMode();

    public abstract void setSelected(String var1);

    public abstract List<String> getModes();

    public boolean isExpanded() {
        return this.expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public float getExpandedWidth() {
        return this.getWidth();
    }

    public float getExpandedHeight() {
        float height = this.getHeight();
        return height + (float)this.getModes().size() * height + height;
    }
}
