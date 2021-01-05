package initialbot.Robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class EnlightenmentCenter implements RobotInterface {
    private final RobotController rc;

    public EnlightenmentCenter(RobotController rc) {
        this.rc = rc;
    }

    @Override
    public void runTurn(int turn) throws GameActionException {
        if (turn == 1) {
            rc.buildRobot(RobotType.POLITICIAN, Direction.EAST, 20);
        }
    }
}
