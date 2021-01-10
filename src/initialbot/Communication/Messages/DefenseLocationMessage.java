package initialbot.Communication.Messages;

import battlecode.common.Direction;
import battlecode.common.RobotType;
import initialbot.Helpers;
import initialbot.Communication.DecodingContext;
import initialbot.Communication.Message;
import initialbot.Communication.MessagesList;

// Flag encoding is:
// MessagesList.getNBitsForTypeCode bits for the type code
// 2 bits for the unit type that should consider this message
// 11 bits expressing a round number - units created after this round should consider this message
// 6 bits for the radius (0 to 63)
// 3 bits for the direction (0 to 7)
public class DefenseLocationMessage implements Message {
    private RobotType robotType;
    private int roundNumber;
    private int radius;
    private Direction direction;

    private final int bitsForRobotType = 2;
    private final int bitsForRoundNumber = 11;
    private final int bitsforDirection = 3;
    private final int bitsForRadius = 7;

    public DefenseLocationMessage() {}

    public DefenseLocationMessage(RobotType robotType, int roundNumber, int radius, Direction direction) {
        this.robotType = robotType;
        this.roundNumber = roundNumber;
        this.radius = radius;
        this.direction = direction;
    }

    @Override
    public int toFlag() {
        return (MessagesList.getTypeCode(this) << (Helpers.flagBits - MessagesList.getNBitsForTypeCode())) + 
                    (Helpers.robotTypetoInt(robotType) << (bitsForRoundNumber + bitsforDirection + bitsForRadius)) +
                    (roundNumber << (bitsforDirection + bitsForRadius)) +
                    (radius << bitsforDirection) + 
                    (directionToInt(direction));
    }

    @Override
    public void fromFlag(DecodingContext context, int flag) {
        direction = directionFromInt(Helpers.getMaskForNLSBs(bitsforDirection) & flag);
        flag >>= bitsforDirection;

        radius = Helpers.getMaskForNLSBs(bitsForRadius) & flag;
        flag >>= bitsForRadius;

        roundNumber = Helpers.getMaskForNLSBs(bitsForRoundNumber) & flag;
        flag >>= bitsForRoundNumber;

        robotType = Helpers.robotTypeFromInt(Helpers.getMaskForNLSBs(bitsForRobotType) & flag);
    }

    @Override
    public boolean shouldIgnore(RobotType robotType, int roundCreated) {
        return !this.robotType.equals(robotType) || roundCreated < this.roundNumber;
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

        return this.robotType.equals(m.robotType) &&
                this.roundNumber == m.roundNumber &&
                this.radius == m.radius &&
                this.direction.equals(m.direction);
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
