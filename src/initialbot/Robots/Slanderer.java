package initialbot.Robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import initialbot.MobileRobot;
import initialbot.Communication.Messages.ChangeRadiusMessage;
import initialbot.Communication.Messages.DefenseLocationMessage;

public class Slanderer extends MobileRobot {
    private int defenseRadius = 0;
    private Direction defenseDirection = null;

    public Slanderer(RobotController rc) throws RuntimeException {
        super(rc);
    }

    public int getDefenseRadius() {
        return defenseRadius;
    }

    public Direction getDefenseDirection() {
        return defenseDirection;
    }

    @Override
    public void runTurn(int turn) throws GameActionException {
        super.runTurn(turn);

        if (defenseRadius != 0 && defenseDirection != null) {
            MapLocation goal = pathFinder.getLocationAtRadius(ecLocation, defenseRadius, defenseDirection);
            Direction direction = pathFinder.getStepTowardGoal(goal);
            if (direction != Direction.CENTER && rc.canMove(direction)) {
                rc.move(direction);
            }
        }

        if (lastMessage != null && lastMessage.getClass().equals(DefenseLocationMessage.class)) {
            DefenseLocationMessage dlm = (DefenseLocationMessage) lastMessage;
            defenseRadius = dlm.getRadius();
            defenseDirection = dlm.getDirection();
        } else if (lastMessage != null && lastMessage.getClass().equals(ChangeRadiusMessage.class)) {
            ChangeRadiusMessage crm = (ChangeRadiusMessage) lastMessage;
            defenseRadius = crm.getRadius();
        }
    }
}
