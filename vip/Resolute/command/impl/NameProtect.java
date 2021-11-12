package vip.Resolute.command.impl;

import vip.Resolute.Resolute;
import vip.Resolute.command.Command;
import vip.Resolute.modules.impl.player.StreamerMode;
import vip.Resolute.ui.notification.Notification;
import vip.Resolute.ui.notification.NotificationType;

public class NameProtect extends Command {

    public NameProtect() {
        super("NameProtect", "Sets NameProtect name", ".name <username>", "name");
    }

    @Override
    public void onCommand(String[] args, String command) {
        if(args.length > 0) {
            StreamerMode.name = args[0];
            Resolute.getNotificationManager().add(new Notification("Success", "Set name to " + args[0], 5000L, NotificationType.SUCCESS));
        }
    }
}
