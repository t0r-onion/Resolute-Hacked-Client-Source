package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventChat;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ModeSetting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.RandomUtils;

public class ChatFilter extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Filter", "Filter", "Bypass");

    public ChatFilter() {
        super("ChatFilter", 0, "Filters Minecraft chat", Category.RENDER);
        this.addSettings(mode);
    }

    private static final String[] INVIS_CHARS = new String[]{"â›�", "â›˜", "â›œ", "â› ", "â›Ÿ", "â›�", "â›�", "â›¡", "â›‹", "â›Œ", "â›—", "â›©", "â›‰"};
    private String lastMessage;
    private int amount;
    private int line;

    public void onEvent(Event e) {
        this.lastMessage = "";
        S02PacketChat s02PacketChat;
        IChatComponent message;
        String rawMessage;
        GuiNewChat chatGui;

        if(e instanceof EventPacket) {
            if(((EventPacket) e).getPacket() instanceof S2EPacketCloseWindow) {
                if(this.isTypingInChat()) {
                    e.setCancelled(true);
                }
            } else if(((EventPacket) e).getPacket() instanceof S02PacketChat) {
                if(mode.is("Filter")) {
                    s02PacketChat = ((EventPacket) e).getPacket();
                    if (s02PacketChat.getType() == 0) {
                        message = s02PacketChat.getChatComponent();
                        rawMessage = message.getFormattedText();
                        chatGui = mc.ingameGUI.getChatGUI();
                        if (this.lastMessage.equals(rawMessage)) {
                            chatGui.deleteChatLine(this.line);
                            ++this.amount;
                            s02PacketChat.getChatComponent().appendText(EnumChatFormatting.GRAY + " [x" + this.amount + "]");
                        } else {
                            this.amount = 1;
                        }
                        ++this.line;
                        this.lastMessage = rawMessage;
                        chatGui.printChatMessageWithOptionalDeletion(message, this.line);
                        if (this.line > 256) {
                            this.line = 0;
                        }
                        e.setCancelled(true);
                    }
                }

                if(mode.is("Bypass")) {
                    if(e instanceof EventChat) {
                        EventChat eventChat = (EventChat) e;

                        if(!eventChat.getMessage().startsWith("/")) {
                            StringBuilder stringBuilder = new StringBuilder();
                            char[] var3 = eventChat.getMessage().toCharArray();
                            int var4 = var3.length;

                            for(int var5 = 0; var5 < var4; ++var5) {
                                char character = var3[var5];
                                stringBuilder.append(character).append(INVIS_CHARS[RandomUtils.nextInt(0, INVIS_CHARS.length)]);
                            }

                            eventChat.setMessage(stringBuilder.toString());
                        }
                    }
                }
            }
        }
    }

    private boolean isTypingInChat() {
        return mc.currentScreen instanceof GuiChat;
    }
}
