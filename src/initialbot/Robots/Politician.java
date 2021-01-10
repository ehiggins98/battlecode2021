package initialbot.Robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import initialbot.MobileRobot;
import initialbot.Communication.Messages.DefenseLocationMessage;

public class Politician extends MobileRobot {
    public Politician(RobotController rc) {
        super(rc);
    }

    @Override
    public void runTurn(int turn) throws GameActionException {
        super.runTurn(turn);

        if (lastMessage != null && lastMessage.getClass().equals(DefenseLocationMessage.class)) {
            DefenseLocationMessage dlm = (DefenseLocationMessage) lastMessage;
            MapLocation goal = pathFinder.getLocationAtRadius(ecLocation, dlm.getRadius(), dlm.getDirection());
            Direction direction = pathFinder.getStepTowardGoal(goal);
            if (direction != Direction.CENTER && rc.canMove(direction)) {
                rc.move(direction);
            }
        }
    }
}
