package initialbot.Communication.Messages;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import battlecode.common.GameConstants;
import battlecode.common.RobotType;
import initialbot.Communication.DecodingContext;

public class RobotLocationMessageTest {
    @Test
    public void testInvertibility() {
        int x = 10022;
        int y = 23947;
        int currentX = 10005;
        int currentY = 23926;

        RobotType robotType = RobotType.MUCKRAKER;
        RobotLocationMessage initial = new RobotLocationMessage(robotType, x, y);
        
        int flag = initial.toFlag();
        RobotLocationMessage received = new RobotLocationMessage();
        received.fromFlag(new DecodingContext(currentX, currentY), flag);

        assertEquals(initial, received);
    }

    // This is definitely overkill, but it doesn't take that long to run so why not
    // Also sending coordinates is pretty central to the bot so I wanted to make sure it works.
    @Test
    public void testDecodingWithManyLocations() {
        RobotType robotType = RobotType.ENLIGHTENMENT_CENTER;
        int offsetX = 10000;
        int offsetY = 15000;

        for (int currentX = offsetX; currentX < offsetX + GameConstants.MAP_MAX_WIDTH; currentX++) {
            for (int currentY = offsetY; currentY < offsetY + GameConstants.MAP_MAX_HEIGHT; currentY++) {
                for (int targetX = offsetX; targetX < offsetX + GameConstants.MAP_MAX_WIDTH; targetX++) {
                    for (int targetY = offsetY; targetY < offsetY + GameConstants.MAP_MAX_WIDTH; targetY++) {
                        RobotLocationMessage initial = new RobotLocationMessage(robotType, targetX, targetY);
                        RobotLocationMessage received = new RobotLocationMessage();
                        received.fromFlag(new DecodingContext(currentX, currentY), initial.toFlag());

                        if (!initial.equals(received)) {
                            System.out.println("here");
                        }

                        assertEquals(initial, received);
                    }   
                }
            }
        }
    }
}
