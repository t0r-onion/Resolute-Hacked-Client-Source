package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventRender2D;
import vip.Resolute.modules.Module;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class HealthIndicator extends Module {

    public HealthIndicator() {
        super("HealthIndicator", 0, "Displays player health", Category.RENDER);
    }

    public void onEvent(Event e) {
        if(e instanceof EventRender2D) {
            ScaledResolution sr = new ScaledResolution(mc);

            final int width = sr.getScaledWidth() / 2;
            final int height = sr.getScaledHeight() / 2;
            final String playerHealth = "" + (int)mc.thePlayer.getHealth();
            final int print = mc.fontRendererObj.getStringWidth(playerHealth);
            float health = mc.thePlayer.getHealth();
            if (health > 20.0f) {
                health = 20.0f;
            }
            final int red = (int)Math.abs(health * 5.0f * 0.01f * 0.0f + (1.0f - health * 5.0f * 0.01f) * 255.0f);
            final int green = (int)Math.abs(health * 5.0f * 0.01f * 255.0f + (1.0f - health * 5.0f * 0.01f) * 0.0f);
            final Color customColor = new Color(red, green, 0).brighter();
            mc.fontRendererObj.drawStringWithShadow(playerHealth, -print / 2 + width, height - 17, customColor.getRGB());
        }
    }
}
