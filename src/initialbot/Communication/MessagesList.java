package initialbot.Communication;

import initialbot.Communication.Messages.DefenseLocationMessage;
import initialbot.Communication.Messages.RobotLocationMessage;

// Right now we can only handle 4 message types, as the DefenseLocationMessage has only 2 free bits
public class MessagesList {
    private static final int numberOfMessages = 2;

    public static int getNBitsForTypeCode() {
        int bits = 0;
        int l = numberOfMessages - 1;

        while (l > 0) {
            bits++;
            l >>= 1;
        }

        return bits;
    }

    public static int getTypeCode(Message m) {
        if (m.getClass().equals(DefenseLocationMessage.class)) {
            return 0;
        } else if (m.getClass().equals(RobotLocationMessage.class)) {
            return 1;
        } else {
            return -1;
        }
    }

    public static Message getMessage(int typeCode) {
        switch (typeCode) {
            case 0:
                return new DefenseLocationMessage();
            case 1:
                return new RobotLocationMessage();
            default:
                return null;
        }
    }
}
