package vip.Resolute.ui.notification;

import vip.Resolute.util.render.LockedResolution;
import vip.Resolute.util.player.Manager;
import net.minecraft.client.gui.ScaledResolution;

import java.util.ArrayList;
import java.util.List;

public final class NotificationManager extends Manager<Notification> {

    public NotificationManager() {
        super(new ArrayList());
    }

    public void render(final ScaledResolution scaledResolution, final LockedResolution lockedResolution, final boolean inGame, final int yOffset) {
        final List<Notification> notifications = this.getElements();
        Notification remove = null;
        for (int i = 0; i < notifications.size(); ++i) {
            final Notification notification = notifications.get(i);
            if (notification.isDead()) {
                remove = notification;
            }
            else {
                notification.render(lockedResolution, scaledResolution, i + 1, yOffset);
            }
        }
        if (remove != null) {
            this.getElements().remove(remove);
        }
    }

    public void add(final Notification notification) {
        this.getElements().add(notification);
    }

}
