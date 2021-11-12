package vip.Resolute.auth;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import vip.Resolute.Resolute;
import vip.Resolute.auth.util.Encryption;
import vip.Resolute.auth.util.HWID;
import vip.Resolute.auth.util.Security;
import vip.Resolute.ui.notification.Notification;
import vip.Resolute.ui.notification.NotificationType;

public class Authentication extends Thread {
    public Minecraft mc = Minecraft.getMinecraft();

    public static String key = "1behk23kbb2kf8o22";
    public String status;
    public static String username;
    public static String password;

    public Authentication(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    @Override
    public void run()
    {
        try
        {
            Mvncentral.a();
            //tenie i swear to god dont fucking change this shit let me whitelist ppl (Evident <3 still luv u tho)
            if (Mvncentral.f(getEncryptedAuthString(this.username, this.password, this.key)) && !Security.wiresharkRunning()) {
                Resolute.instance.setAuthorized(true);
                Resolute.instance.setResoluteName(this.username);
                Resolute.instance.setUUID(this.password);
                LoginScreen.progression = "Authorized!";
                Resolute.getNotificationManager().add(new Notification("Success", "Logged in! Welcome to vip.Resolute, " + this.username + "!", 5000L, NotificationType.SUCCESS));
                this.status = EnumChatFormatting.GREEN + "Logged in! Welcome to vip.Resolute, " + this.username + "!";
            } else {
                LoginScreen.progression = "Wrong login credentials!";
                Resolute.getNotificationManager().add(new Notification("Warning", "Invalid Username or Password", 5000L, NotificationType.WARNING));
                Resolute.instance.setAuthorized(false);
            }

        }
        catch (Exception e)
        {
            LoginScreen.progression = "Error";
            Resolute.getNotificationManager().add(new Notification("Error", "Authentication has failed", 5000L, NotificationType.ERROR));
            System.out.println("Error with authentication");
            e.printStackTrace();

        }
    }

    public static String getAuthString(String inputUsername, String inputPassword)
    {
        return inputUsername + "::" + inputPassword + "::" + HWID.hwid;
    }

    public static String getEncryptedAuthString(String inputUsername, String inputPassword, String key)
    {
        String authString = getAuthString(inputUsername, inputPassword);

        return Encryption.hashMD5(Encryption.encryptAES(authString, key));
    }

    public String getStatus()
    {
        return this.status;
    }
}
