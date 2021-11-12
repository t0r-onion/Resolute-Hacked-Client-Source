package vip.Resolute.command.impl;

import vip.Resolute.Resolute;
import vip.Resolute.command.Command;
import vip.Resolute.ui.notification.Notification;
import vip.Resolute.ui.notification.NotificationType;
import net.minecraft.client.Minecraft;

public class SpectatorAlt extends Command {
    public Minecraft mc = Minecraft.getMinecraft();

    public SpectatorAlt() {
        super("Alt", "Sets mineplex alt", ".alt <email:pass>", "alt");
    }

    @Override
    public void onCommand(String[] args, String command) {
        if(args.length > 0) {
            Resolute.instance.setAlt(args[0]);
            Resolute.getNotificationManager().add(new Notification("Success", "Set Account to " + args[0], 5000L, NotificationType.SUCCESS));
        }
    }
}
