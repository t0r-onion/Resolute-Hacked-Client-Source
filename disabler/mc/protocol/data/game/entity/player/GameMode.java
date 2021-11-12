package disabler.mc.protocol.data.game.entity.player;

import disabler.mc.protocol.data.game.world.notify.ClientNotificationValue;

public enum GameMode implements ClientNotificationValue {
    SURVIVAL,
    CREATIVE,
    ADVENTURE,
    SPECTATOR;
}
