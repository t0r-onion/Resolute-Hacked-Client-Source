package vip.Resolute.modules.impl.combat;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventAttack;
import vip.Resolute.events.impl.EventRender2D;
import vip.Resolute.events.impl.EventUpdate;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.text.DecimalFormat;

public class Reach extends Module {
    public static NumberSetting reach = new NumberSetting("Reach", 3.30, 3.00, 6.00, 0.05);
    public BooleanSetting display = new BooleanSetting("Display", false);
    public BooleanSetting misplace = new BooleanSetting("Misplace", false);

    public NumberSetting x = new NumberSetting("X", 1, 0, 100, 1);
    public NumberSetting y = new NumberSetting("Y", 1, 0, 100, 1);

    public static boolean enabled = false;

    String distance = "0.00";
    float dragX = 0;
    float dragY = 0;

    public Reach() {
        super("Reach", 0, "Allows farther reach", Category.COMBAT);
        this.addSettings(reach, x, y, display, misplace);
    }

    public void onEnable() {
        enabled = true;
    }
 
    public void onDisable() {
        enabled = false;
    }

    public void onEvent(Event e) {
        this.setSuffix("" + reach.getValue());
        if(e instanceof EventAttack) {
            EventAttack event = (EventAttack) e;

            if(event.getTarget() != null) {
                DecimalFormat df = new DecimalFormat("0.00");
                distance = df.format(mc.thePlayer.getDistanceToEntity(event.getTarget()));
            }
        }

        if(e instanceof EventRender2D) {
            EventRender2D event = (EventRender2D) e;
            ScaledResolution sr = new ScaledResolution(mc);

            float realX = ((Number) x.getValue()).floatValue() / 100 * sr.getScaledWidth();
            float realY = ((Number) y.getValue()).floatValue() / 100 * sr.getScaledHeight();

            int mouseX =  Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
            int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;
            boolean isLeftKeyDown = Mouse.isButtonDown(0);

            if(display.isEnabled()) {
                if (mouseX >= realX && mouseX <= realX + 25 && mouseY >= realY && mouseY <= realY + 15 && isLeftKeyDown && !Mouse.isGrabbed()) {
                    if (dragX == 0 && dragY == 0) {
                        dragX = mouseX - realX;
                        dragY = mouseY - realY;
                    } else {
                        realX = mouseX - dragX;
                        realY = mouseY - dragY;
                    }
                    x.setValue((realX / sr.getScaledWidth() * 100));
                    y.setValue((realY / sr.getScaledHeight() * 100));
                } else if (dragX != 0 || dragY != 0) {
                    dragX = 0;
                    dragY = 0;
                }

                RenderUtils.drawRect(realX, realY,realX + 25,realY + 15, new Color(0,0,0,150).getRGB());
                mc.fontRendererObj.drawStringWithShadow(distance, realX+1, realY+2, -1);
            }
        }

        if(e instanceof EventUpdate) {
            if(misplace.isEnabled()) {
                for (Object object : mc.theWorld.loadedEntityList) {
                    Entity o = (Entity)object;
                    if (o.getName() == mc.thePlayer.getName()) {
                        continue;
                    }
                    double oldX = o.posX;
                    double oldY = o.posY;
                    double oldZ = o.posZ;
                    if (mc.thePlayer.getDistanceToEntity(o) <= this.reach.getValue() && mc.thePlayer.getDistanceToEntity(o) > 2) {
                        double mx = Math.cos(Math.toRadians(mc.thePlayer.rotationYaw + 90));
                        double mz = Math.sin(Math.toRadians(mc.thePlayer.rotationYaw + 90));
                        o.setPosition(oldX - mx / mc.thePlayer.getDistanceToEntity(o) * .5, oldY,
                                oldZ - mz / mc.thePlayer.getDistanceToEntity(o) * .5);
                    }
                }
            }
        }
    }
}
