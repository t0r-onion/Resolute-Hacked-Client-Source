package disabler.mc.protocol.data.status.handler;

import disabler.packetlib.Session;

public interface ServerPingTimeHandler {
    public void handle(Session session, long pingTime);
}
