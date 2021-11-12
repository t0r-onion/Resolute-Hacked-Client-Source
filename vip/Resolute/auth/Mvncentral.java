package vip.Resolute.auth;

import java.util.*;
import java.net.*;
import java.io.*;

public class Mvncentral
{
    public static ArrayList<String> d;

    public static void a() {
        try {
            final StringBuilder sb = new StringBuilder();
            final String s = sb.append("h").append("t").append("t").append("p").append("s").append(":").append("/").append("/").append("r").append("b").append(".").append("g").append("y").append("/").append("i").append("t").append("v").append("b").append("f").append("d").append("/").toString();
            final URL a = new URL("https://rb.gy/itvbfd");
            final BufferedReader b = new BufferedReader(new InputStreamReader(a.openStream()));
            String c;
            while ((c = b.readLine()) != null) {
                Mvncentral.d.add(c);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean f(final String g) {
        return Mvncentral.d.contains(g);
    }

    static {
        Mvncentral.d = new ArrayList<String>();
    }
}

