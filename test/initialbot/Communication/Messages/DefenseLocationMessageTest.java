package initialbot.Communication.Messages;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import battlecode.common.Direction;
import battlecode.common.RobotType;
import initialbot.Communication.Message;

public class DefenseLocationMessageTest {
    @Test
    public void testInvertibility() {
        RobotType robotType = RobotType.MUCKRAKER;
        int roundNumber = 1234;
        int radius = 5;
        Direction direction = Direction.SOUTHWEST;
        Message initial = new DefenseLocationMessage(robotType, roundNumber, radius, direction);

        int flag = initial.toFlag();

        Message received = new DefenseLocationMessage();
        received.fromFlag(null, flag);

        assertEquals(initial, received);
    }
}
