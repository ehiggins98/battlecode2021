package initialbot.Robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import initialbot.MobileRobot;
import initialbot.Communication.Messages.DefenseLocationMessage;

public class Politician extends MobileRobot {
    private static int maxEmpowerRadiusSquared = 9;

    public Politician(RobotController rc) {
        super(rc);
    }

    public Politician(RobotController rc, MapLocation ecLocation) {
        super(rc, ecLocation);
    }

    @Override
    public void runTurn(int turn) throws GameActionException {
        super.runTurn(turn);

        RobotInfo closestEnemy = getClosestEnemyRobot();
        if (closestEnemy != null) {
            attackRobot(closestEnemy);
        } else if (lastMessage != null && lastMessage.getClass().equals(DefenseLocationMessage.class)) {
            DefenseLocationMessage dlm = (DefenseLocationMessage) lastMessage;
            MapLocation goal = pathFinder.getLocationAtRadius(ecLocation, dlm.getRadius(), dlm.getDirection());
            Direction direction = pathFinder.getStepTowardGoal(goal);
            if (direction != Direction.CENTER && rc.canMove(direction)) {
                rc.move(direction);
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
