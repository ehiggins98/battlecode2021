package initialbot.PathFinder;

import java.util.HashSet;
import java.util.Set;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import initialbot.Helpers;

public class PathFinder {
    private final RobotController rc;

    public PathFinder(RobotController rc) {
        this.rc = rc;
    }

    public Direction getStepTowardGoal(MapLocation goal) {
        Set<MapLocation> robots = new HashSet<MapLocation>();
        for (MapLocation loc : rc.detectNearbyRobots()) {
            robots.add(loc);
        }

        return getGreedyDirection(rc.getLocation(), goal, robots);
    }

    private Direction getGreedyDirection(MapLocation current, MapLocation goal, Set<MapLocation> robots) {
        Direction ideal = current.directionTo(goal);
        if (!robots.contains(current.add(ideal))) {
            return ideal;
        }

        int index = 0;
        for (int i = 0; i < Helpers.directions.length; i++) {
            if (Helpers.directions[i].equals(ideal)) {
                index = i;
                break;
            }
        }

        int offset = 1;
        while (offset <= Helpers.directions.length / 2) {
            int newIndexLeft = (index - offset) % Helpers.directions.length;
            int newIndexRight = (index + offset) % Helpers.directions.length;

            if (!robots.contains(current.add(Helpers.directions[newIndexLeft]))) {
                return Helpers.directions[newIndexLeft];
            } else if (!robots.contains(current.add(Helpers.directions[newIndexRight]))) {
                return Helpers.directions[newIndexRight];
            }
            
            offset += 1;
        }

        return null;
    }
}
