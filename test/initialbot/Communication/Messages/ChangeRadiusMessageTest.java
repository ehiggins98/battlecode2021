package initialbot.Communication.Messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import battlecode.common.GameConstants;
import battlecode.common.RobotType;

public class ChangeRadiusMessageTest {
    @Test
    public void testInvertibility() {
        ChangeRadiusMessage initial = new ChangeRadiusMessage(RobotType.POLITICIAN, 1);
        int flag = initial.toFlag();

        assertTrue("flag was greater than the limit", flag <= GameConstants.MAX_FLAG_VALUE);

        ChangeRadiusMessage received = new ChangeRadiusMessage();
        received.fromFlag(null, flag);

        assertEquals(initial, received);
    }
}
