package disabler.mc.protocol.data.status.handler;

import disabler.mc.protocol.data.status.ServerStatusInfo;
import disabler.packetlib.Session;

public interface ServerInfoBuilder {
    public ServerStatusInfo buildInfo(Session session);
}
