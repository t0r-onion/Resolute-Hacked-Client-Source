package vip.Resolute.ui.click.skeet.component.impl.sub.text;

import vip.Resolute.ui.click.skeet.SkeetUI;
import vip.Resolute.ui.click.skeet.framework.Component;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.font.MinecraftFontRenderer;
import net.minecraft.client.gui.ScaledResolution;

public final class TextComponent extends Component {
    private static final MinecraftFontRenderer FONT_RENDERER = FontUtil.tahomaVerySmall;
    private final String text;

    public TextComponent(Component parent, String text, float x, float y) {
        super(parent, x, y, (float) FONT_RENDERER.getStringWidth(text), FONT_RENDERER.getHeight());
        this.text = text;
    }

    public void drawComponent(ScaledResolution resolution, int mouseX, int mouseY) {
        if (SkeetUI.shouldRenderText()) {
            FONT_RENDERER.drawString(this.text, this.getX(), this.getY(), SkeetUI.getColor(15132390));
        }

    }
}
