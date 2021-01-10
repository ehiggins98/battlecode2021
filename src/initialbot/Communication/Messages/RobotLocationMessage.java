package initialbot.Communication.Messages;

import battlecode.common.GameConstants;
import battlecode.common.RobotType;
import initialbot.Helpers;
import initialbot.Communication.DecodingContext;
import initialbot.Communication.Message;
import initialbot.Communication.MessagesList;

// Flag encoding is:
// MessagesList.getNBitsForTypeCode bits for the type code (aligned left)
// 2 bits for robot type (0 to 3)
// 7 bits for x coordinate (expressed mod 128)
// 7 bits for y coordinate (expressed mod 128)
// Expressing location mod 128 allows us to uniquely determine position using only 14 bits (explained in pathfinding lecture)
public class RobotLocationMessage implements Message {
    private RobotType robotType;
    private int x;
    private int y;

    private final int bitsForRobotType = 2;
    private final int bitsForX = 7;
    private final int bitsForY = 7;
    // Max height and width are equal, but I split then out in case
    // of a spec update where they became not equal.
    private final int xMod = 2 * GameConstants.MAP_MAX_WIDTH;
    private final int yMod = 2 * GameConstants.MAP_MAX_HEIGHT;
    
    public RobotLocationMessage() {}

    public RobotLocationMessage(RobotType robotType, int x, int y) {
        this.robotType = robotType;
        this.x = x;
        this.y = y;
    }

    @Override
    public int toFlag() {
        return (MessagesList.getTypeCode(this) << (Helpers.flagBits - MessagesList.getNBitsForTypeCode())) + 
                    (Helpers.robotTypetoInt(robotType) << (bitsForX + bitsForY)) + 
                    ((x % xMod) << (bitsForY)) +
                    (y % yMod);
    }

    @Override
    public void fromFlag(DecodingContext context, int flag) {
        int yInMod = Helpers.getMaskForNLSBs(bitsForY) & flag;
        this.y = modToAbsolute(yInMod, context.getCurrentY(), yMod);
        flag >>= bitsForY;
        
        int xInMod = Helpers.getMaskForNLSBs(bitsForX) & flag;
        this.x = modToAbsolute(xInMod, context.getCurrentX(), xMod);
        flag >>= bitsForX;

        int robotType = Helpers.getMaskForNLSBs(bitsForRobotType) & flag;
        this.robotType = Helpers.robotTypeFromInt(robotType);
    }

    public RobotType getRobotType() {
        return robotType;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        RobotLocationMessage m = (RobotLocationMessage) obj;
        return this.x == m.x && this.y == m.y && this.robotType.equals(m.robotType);
    }

    private int modToAbsolute(int coordInMod, int currAbsolute, int mod) {
        int distanceIncr = (coordInMod - currAbsolute % mod + mod) % mod;
        int distanceDecr = mod - distanceIncr;

        if (distanceIncr < mod / 2) {
            return currAbsolute + distanceIncr;
        } else {
            return currAbsolute - distanceDecr;
        }
    }
}
