package initialbot.Communication.Messages;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import battlecode.common.Direction;
import initialbot.Communication.Message;

public class DefenseLocationMessageTest {
    @Test
    public void testInvertibility() {
        int radius = 5;
        Direction direction = Direction.SOUTHWEST;
        Message initial = new DefenseLocationMessage(radius, direction);

        int flag = initial.toFlag();

        Message received = new DefenseLocationMessage();
        received.fromFlag(null, flag);

        assertEquals(initial, received);
    }
}
