package initialbot.Communication.Messages;

import battlecode.common.RobotType;
import initialbot.Helpers;
import initialbot.Communication.DecodingContext;
import initialbot.Communication.Message;
import initialbot.Communication.MessagesList;

// Flag encoding is:
// MessagesList.getNBitsForTypeCode bits for the type code
// 2 bits for the unit type that should consider this message
// 6 bits for the new radius
public class ChangeRadiusMessage implements Message {
    private RobotType robotType;
    private int radius;

    private final int bitsForRobotType = 2;
    private final int bitsForRadius = 6;

    public ChangeRadiusMessage() {}

    public ChangeRadiusMessage(RobotType robotType, int radius) {
        this.robotType = robotType;
        this.radius = radius;
    }

    @Override
    public int toFlag() {
        return (MessagesList.getTypeCode(this) << (Helpers.flagBits - MessagesList.getNBitsForTypeCode())) +
                    (Helpers.robotTypetoInt(robotType) << bitsForRadius) +
                    (radius);
    }

    @Override
    public void fromFlag(DecodingContext context, int flag) {
        radius = Helpers.getMaskForNLSBs(bitsForRadius) & flag;
        flag >>= bitsForRadius;

        robotType = Helpers.robotTypeFromInt(Helpers.getMaskForNLSBs(bitsForRobotType) & flag);
    }

    @Override
    public boolean shouldIgnore(RobotType robotType, int roundCreated) {
        return !robotType.equals(this.robotType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(this.getClass())) {
            return false;
        }

        ChangeRadiusMessage irm = (ChangeRadiusMessage) obj;
        return irm.robotType.equals(this.robotType) && irm.radius == this.radius;
    }
    
    public int getRadius() {
        return radius;
    }
}
