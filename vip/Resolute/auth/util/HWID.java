package vip.Resolute.auth.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class HWID {

    public static String hwid;

    static {
        hwid = System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getProperty("os.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
    }

    public static String getIP() throws IOException {
        //if you think im logging your ip you are actually retarded
        URL ip = new URL("http://checkip.amazonaws.com");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ip.openStream()));
        return bufferedReader.readLine();
    }
}
