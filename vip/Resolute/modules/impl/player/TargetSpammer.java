package vip.Resolute.modules.impl.player;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventTick;
import vip.Resolute.modules.Module;
import vip.Resolute.modules.impl.combat.KillAura;
import vip.Resolute.util.misc.TimerUtils;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Random;

public class TargetSpammer extends Module {
    public String targetName;
    public boolean sentToTarget;
    public String[] focusKillerMessages;

    public TimerUtils timerUtils = new TimerUtils();

    public TargetSpammer() {
        super("TargetSpam", 0, "Spams whoever you are targeting", Category.PLAYER);
        this.targetName = "";
        this.sentToTarget = false;
        this.focusKillerMessages = new String[] { "SOBADSOBADSOBADSOBADSOBADSOBADSOBADSOBADSOBADSOBADSOBAD", "LOLOLOLOLOLOLOLOLOLOLOLOLOLOLOLOLOLOLOLOLOLOLOLOL", "LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL", "OUCHOUCHOUCHOUCHOUCHOUCHOUCHOUCHOUCHOUCHOUCHOUCH", "HAHAHAHAHAHAHAHAHAHAHAHAHAHAHA", "TRASHTRASHTRASHTRASHTRASHTRASHTRASHTRASH", "YOU ARE SO BAD LLLLLLLLLLLLLLLLLLLLLLLLLLL", "GAMING CHAIRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR", "OOOOOOOOOOOOOOO YAAAAAAAAAAAAAAAAAAAAAAAAA", "Just go get Impulse client you fucking noob", "fragfawehiuwhqiurwheiurqhiuher", "F4GF4GF4GF4GF4GF4GF4GF4GF4GF4GF4GF4GF4G", "FUCKEDFUCKEDFUCKEDFUCKEDFUCKEDFUCKEDFUCKED", "SURPRISE BITCH SURPRISE BITCH SURPRISE BITCH SURPRISE BITCH" };

    }

    public void onEnable() {
        timerUtils.reset();
    }

    public void onEvent(Event e) {
        if(e instanceof EventTick) {
            if(KillAura.target != null && KillAura.target instanceof EntityPlayer) {
                if(!sentToTarget) {
                    this.mc.thePlayer.sendChatMessage("/tell " + KillAura.target.getName() + " " + this.focusKillerMessages[new Random().nextInt(this.focusKillerMessages.length)]);
                    sentToTarget = true;
                } else {
                    this.mc.thePlayer.sendChatMessage("/r " + this.focusKillerMessages[new Random().nextInt(this.focusKillerMessages.length)]);
                }
            } else {
                sentToTarget = false;
            }
        }
    }
}
