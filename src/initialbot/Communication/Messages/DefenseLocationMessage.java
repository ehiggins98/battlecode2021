package initialbot.Communication.Messages;

import battlecode.common.Direction;
import initialbot.Helpers;
import initialbot.Communication.DecodingContext;
import initialbot.Communication.Message;
import initialbot.Communication.MessagesList;

// Flag encoding is:
// MessagesList.getNBitsForTypeCode bits for the type code (aligned left)
// 7 bits for the radius (0 to 64 in practice)
// 3 bits for the direction (0 to 7)
public class DefenseLocationMessage implements Message {
    private int radius;
    private Direction direction;

    private final int bitsforDirection = 3;
    private final int bitsForRadius = 7;

    public DefenseLocationMessage() {}

    public DefenseLocationMessage(int radius, Direction direction) {
        this.radius = radius;
        this.direction = direction;
    }

    @Override
    public int toFlag() {
        return (MessagesList.getTypeCode(this) << (Helpers.flagBits - MessagesList.getNBitsForTypeCode())) + (radius << bitsforDirection) + (directionToInt(direction));
    }

    @Override
    public void fromFlag(DecodingContext context, int flag) {
        // ~(~0 << 3) is the value containing three 1s in the LSBs, and 0s otherwise
        direction = directionFromInt(Helpers.getMaskForNLSBs(bitsforDirection) & flag);
        flag >>= bitsforDirection;
        radius = Helpers.getMaskForNLSBs(bitsForRadius) & flag;
    }

    public int getRadius() {
        return radius;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        DefenseLocationMessage m = (DefenseLocationMessage) obj;

        return this.radius == m.radius && this.direction.equals(m.direction);
    }
    
    private int directionToInt(Direction d) {
        for (int i = 0; i < Helpers.directions.length; i++) {
            if (Helpers.directions[i].equals(d)) {
                return i;
            }
        }

        return -1;
    }

    private Direction directionFromInt(int d) {
        return Helpers.directions[d];
    }
}
