package initialbot.Robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Politician implements RobotInterface {
    private final RobotController rc;

    public Politician(RobotController rc) {
        this.rc = rc;
    }

    @Override
    public void runTurn(int turn) throws GameActionException {
        System.out.println(rc.getCooldownTurns());
        if (rc.canMove(Direction.EAST)) {
            rc.move(Direction.EAST);
        }
    }
}
