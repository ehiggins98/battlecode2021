package initialbot.Robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import initialbot.MobileRobot;
import initialbot.Communication.Messages.DefenseLocationMessage;
import initialbot.Communication.Messages.ChangeRadiusMessage;

public class Politician extends MobileRobot {
    private static int maxEmpowerRadiusSquared = 9;
    private int defenseRadius = 0;
    private Direction defenseDirection = null;

    public Politician(RobotController rc) {
        super(rc);
    }

    public Politician(RobotController rc, MapLocation ecLocation, int defenseRadius, Direction defenseDirection) {
        super(rc, ecLocation);
        this.defenseRadius = defenseRadius;
        this.defenseDirection = defenseDirection;
    }

    @Override
    public void runTurn(int turn) throws GameActionException {
        super.runTurn(turn);
        
        if (lastMessage != null && defenseRadius == 0 && defenseDirection == null && lastMessage.getClass().equals(DefenseLocationMessage.class)) {
            DefenseLocationMessage dlm = (DefenseLocationMessage) lastMessage;
            defenseRadius = dlm.getRadius();
            defenseDirection = dlm.getDirection();
        } else if (lastMessage != null && lastMessage.getClass().equals(ChangeRadiusMessage.class)) {
            ChangeRadiusMessage crm = (ChangeRadiusMessage) lastMessage;
            defenseRadius = crm.getRadius();
        }

        RobotInfo closestEnemy = getClosestEnemyRobot();
        if (closestEnemy != null) {
            attackRobot(closestEnemy);
        } else if (defenseDirection != null && defenseRadius > 0 && ecLocation != null && rc.getCooldownTurns() < 1) {
            MapLocation goal = pathFinder.getLocationAtRadius(ecLocation, defenseRadius, defenseDirection);
            Direction direction = pathFinder.getStepTowardGoal(goal);
            if (direction != Direction.CENTER && rc.canMove(direction)) {
                rc.move(direction);
            } else if (direction == Direction.CENTER) {
                defenseDirection = defenseDirection.rotateLeft();
            }
        }
    }

    private void attackRobot(RobotInfo robot) throws GameActionException {
        MapLocation current = rc.getLocation();
        int distSquared = current.distanceSquaredTo(robot.location);
        if (rc.canEmpower(distSquared) && 
                (current.isAdjacentTo(robot.location) ||
                (robot.type.equals(RobotType.POLITICIAN) && distSquared <= maxEmpowerRadiusSquared))) {
            rc.empower(distSquared);
        } else {
            Direction step = pathFinder.getStepTowardGoal(robot.location);
            if (step != Direction.CENTER && rc.canMove(step)) {
                rc.move(step);
            }
        }
    }

    private RobotInfo getClosestEnemyRobot() {
        RobotInfo closest = null;
        int closestDistSquared = Integer.MAX_VALUE;
        MapLocation current = rc.getLocation();

        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (!info.team.equals(rc.getTeam())) {
                int dist = current.distanceSquaredTo(info.location);
                if (dist < closestDistSquared) {
                    closest = info;
                    closestDistSquared = dist;
                }
            }
        }

        return closest;
    }
}
