package vip.Resolute.command.impl;

import vip.Resolute.command.Command;

public class Clientname extends Command {
    public static String nameofwatermark = "Resolute";

    public Clientname() {
        super("Clientname", "Clientname watermark", ".clientname <name> | .clientname reset", ".clientname");
    }

    @Override
    public void onCommand(String[] args, String command) {
        if (args[0].equalsIgnoreCase("reset")) {
            nameofwatermark = null;
        } else {
            nameofwatermark = String.join(" ", args);
            nameofwatermark = nameofwatermark.replace("&", "\247");
            nameofwatermark = nameofwatermark.replace("\\247", "\247");
        }
    }
}
