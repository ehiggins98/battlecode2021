package initialbot.Communication;

import initialbot.Communication.Messages.DefenseLocationMessage;
import initialbot.Communication.Messages.RobotLocationMessage;

public class MessagesList {
    public static Message[] allMessages = {
        new DefenseLocationMessage(),
        new RobotLocationMessage()
    };

    public static int getNBitsForTypeCode() {
        int bits = 0;
        int l = allMessages.length - 1;

        while (l > 0) {
            bits++;
            l >>= 1;
        }

        return bits;
    }

    public static int getTypeCode(Message m) {
        for (int i = 0; i < allMessages.length; i++) {
            if (m.getClass() == allMessages[i].getClass()) {
                return i;
            }
        }

        return -1;
    }

    public static Message getMessage(int typeCode) {
        return allMessages[typeCode];
    }
}
