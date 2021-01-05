package initialbot.Robots;

import battlecode.common.GameActionException;

public interface RobotInterface {
    public void runTurn(int turn) throws GameActionException;
}
