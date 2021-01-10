package initialbot.Robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import initialbot.MobileRobot;
import initialbot.Communication.Messages.DefenseLocationMessage;

public class Politician extends MobileRobot {
    private static int maxEmpowerRadiusSquared = 9;

    public Politician(RobotController rc) {
        super(rc);
    }

    @Override
    public void runTurn(int turn) throws GameActionException {
        super.runTurn(turn);

        MapLocation closestEnemy = getClosestEnemyRobotLocation();
        if (closestEnemy != null) {
            int distSquared = rc.getLocation().distanceSquaredTo(closestEnemy);
            if (distSquared <= maxEmpowerRadiusSquared && rc.canEmpower(distSquared)) {
                rc.empower(distSquared);
            } else {
                Direction step = pathFinder.getStepTowardGoal(closestEnemy);
                if (step != Direction.CENTER && rc.canMove(step)) {
                    rc.move(step);
                }
            }
        } else if (lastMessage != null && lastMessage.getClass().equals(DefenseLocationMessage.class)) {
            DefenseLocationMessage dlm = (DefenseLocationMessage) lastMessage;
            MapLocation goal = pathFinder.getLocationAtRadius(ecLocation, dlm.getRadius(), dlm.getDirection());
            Direction direction = pathFinder.getStepTowardGoal(goal);
            if (direction != Direction.CENTER && rc.canMove(direction)) {
                rc.move(direction);
            }
        }
    }

    private MapLocation getClosestEnemyRobotLocation() {
        MapLocation closest = null;
        int closestDistSquared = Integer.MAX_VALUE;
        MapLocation current = rc.getLocation();

        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (!info.team.equals(rc.getTeam())) {
                int dist = current.distanceSquaredTo(info.location);
                if (dist < closestDistSquared) {
                    closest = info.location;
                    closestDistSquared = dist;
                }
            }
        }

        return closest;
    }
}
