package disabler.mc.protocol;

import disabler.packetlib.Session;

public interface ServerLoginHandler {
    public void loggedIn(Session session);
}
