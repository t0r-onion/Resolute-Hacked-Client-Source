package vip.Resolute.ui.notification;

import java.awt.*;

public enum NotificationType {
    SUCCESS("SUCCESS", 0, new Color(3522898).getRGB()),
    INFO("INFO", 1, 0xFF7D00EB),
    WARNING("WARNING", 2, new Color(13807392).getRGB()),
    ERROR("ERROR", 3, new Color(13120307).getRGB());

    private final int color;

    private NotificationType(final String name, final int ordinal, final int color) {
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }
}
