package vip.Resolute.command.impl;

import vip.Resolute.Resolute;
import vip.Resolute.command.Command;
import vip.Resolute.ui.notification.Notification;
import vip.Resolute.ui.notification.NotificationType;
import net.minecraft.client.Minecraft;

public class API extends Command {
    public Minecraft mc = Minecraft.getMinecraft();

    public API() {
        super("API", "Sets watchdog api", ".api <key>", "api");
    }

    @Override
    public void onCommand(String[] args, String command) {
        if(args.length > 0) {
            Resolute.getInstance().setAPI(args[0]);
            Resolute.getNotificationManager().add(new Notification("Success", "Set API Key to " + args[0], 5000L, NotificationType.SUCCESS));
        }
    }
}
