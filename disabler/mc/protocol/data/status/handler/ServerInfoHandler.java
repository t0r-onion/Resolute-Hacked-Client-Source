package disabler.mc.protocol.data.status.handler;

import disabler.mc.protocol.data.status.ServerStatusInfo;
import disabler.packetlib.Session;

public interface ServerInfoHandler {
    public void handle(Session session, ServerStatusInfo info);
}
