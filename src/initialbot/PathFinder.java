package initialbot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class PathFinder {
    private final RobotController rc;
    private final double baseCooldown;
    private final int sensorRadius;
    private final double[][] window;
    private final int windowDim;

    public PathFinder(RobotController rc) {
        this.rc = rc;
        this.baseCooldown = Helpers.getBaseCooldown(rc.getType());
        this.sensorRadius = (int) Math.floor(Math.sqrt(Helpers.getSensorRadiusSquared(rc.getType())));

        // We pick the largest square such that we can sense every tile in the square
        this.windowDim = (int) Math.floor(2 * sensorRadius / Math.sqrt(2));

        // Here we think of the first index as being x and the second as being y,
        // so (0, 0) in this array should be thought of as the lower-left corner.
        // This is so that the array matches up nicely with the map coordinates.
        this.window = new double[windowDim][windowDim];
    }

    // This algorithm first finds the row or column that's closest to the goal (which is usually)
    // on a boundary of window), and fills each square with its Manhattan distance from the goal.
    // These are used as an approximation of the total cooldown accumulated while traveling from
    // each of these squares to the goal.
    //
    // We then backtrack towards the current location, using the actual cooldown values to find
    // an approximation of the optimal path to the goal. This is somewhat similar to Dijkstra's algorithm,
    // but there are some optimizations done so that performance is good without needing a priority queue.
    public Direction getStepTowardGoal(MapLocation goal) throws GameActionException {
        MapLocation current = rc.getLocation();
        if (current.equals(goal) || (current.isAdjacentTo(goal) && rc.isLocationOccupied(goal))) {
            return Direction.CENTER;
        }

        Direction toGoal = current.directionTo(goal);
        boolean goalInFrame = Math.abs(current.x - goal.x) <= windowDim / 2 && Math.abs(current.y - goal.y) <= windowDim / 2;

        if (toGoal.dy != 0) {
            int seedRowIndex;
            if (goalInFrame) {
                seedRowIndex = goal.y - getYOffset();
            } else if (toGoal.dy == -1) {
                seedRowIndex = 0;
            } else {
                seedRowIndex = windowDim - 1;
            }

            fillSeedValues(window, goal, false, seedRowIndex);
            fillWindowFromSeed(window, seedRowIndex - toGoal.dy, -1 * toGoal.dy, goal.x < current.x, false);
        } else {
            int seedColIndex;
            if (goalInFrame) {
                seedColIndex = goal.x - getXOffset();
            } else if (toGoal.dx == -1) {
                seedColIndex = 0;
            } else {
                seedColIndex = windowDim - 1;
            }

            fillSeedValues(window, goal, false, seedColIndex);
            fillWindowFromSeed(window, seedColIndex - toGoal.dx, -1 * toGoal.dx, goal.y < current.y, true);
        }

        Direction argmin = null;
        double min = Double.MAX_VALUE;
        for (Direction d : Helpers.directions) {
            if (window[windowDim / 2 + d.dx][windowDim / 2 + d.dy] < min && validMovementDirection(d, toGoal) && rc.canMove(d)) {
                argmin = d;
                min = window[windowDim / 2 + d.dx][windowDim / 2 + d.dy];
            }
        }
        System.out.println(argmin);

        return argmin;
    }

    // Leaving this in because it's a way to do pathfinding without using much bytecode
    private Direction getStraightLineDirection(MapLocation current, MapLocation goal) throws GameActionException {
        if (current.equals(goal) || (current.isAdjacentTo(goal) && !rc.isLocationOccupied(goal))) {
            return Direction.CENTER;
        }

        Direction ideal = current.directionTo(goal);
        if (!rc.isLocationOccupied(current.add(ideal))) {
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

            if (!rc.isLocationOccupied(current.add(Helpers.directions[newIndexLeft]))) {
                return Helpers.directions[newIndexLeft];
            } else if (!rc.isLocationOccupied(current.add(Helpers.directions[newIndexRight]))) {
                return Helpers.directions[newIndexRight];
            }
            
            offset += 1;
        }

        return null;
    }

    private void fillSeedValues(double[][] window, MapLocation goal, boolean vertical, int index) {
        for (int i = 0; i < windowDim; i++) {
            if (!vertical) {
                window[i][index] = Math.abs(getXOffset() + i - goal.x) + Math.abs(getYOffset() + index - goal.y);
            } else {
                window[index][i] = Math.abs(getXOffset() + index - goal.x) + Math.abs(getYOffset() + i - goal.y);
            }
        }
    }

    // Note that this doesn't actually fill the full window - it only goes from the edge
    // (either a row or a column) to the center
    private void fillWindowFromSeed(double[][] window, int initialI, int di, boolean jIncreasing, boolean outerLoopIsX) throws GameActionException {
        int xOffset = getXOffset();
        int yOffset = getYOffset();

        int dj = jIncreasing ? 1 : -1;
        for (int i = initialI; i != windowDim / 2 + di; i += di) {
            for (int j = jIncreasing ? 0 : windowDim - 1; j >= 0 && j < windowDim; j += dj) {
                if (outerLoopIsX) {
                    MapLocation loc = new MapLocation(xOffset + i, yOffset + j);
                    window[i][j] = modifiedCooldown(loc) + Math.min(Math.min(valueIfInBounds(i - di, j), valueIfInBounds(i - di, j + dj)), Math.min(valueIfInBounds(i, j - dj), valueIfInBounds(i - di, j - dj)));
                } else {
                    MapLocation loc = new MapLocation(xOffset + j, yOffset + i);
                    window[j][i] = modifiedCooldown(loc) + Math.min(Math.min(valueIfInBounds(j - dj, i), valueIfInBounds(j + dj, i - di)), Math.min(valueIfInBounds(j, i - di), valueIfInBounds(j - dj, i - di)));
                }
            }
        }
    }

    private int getXOffset() {
        return rc.getLocation().x - windowDim / 2;
    }

    private int getYOffset() {
        return rc.getLocation().y - windowDim / 2;
    }

    // Helper function for getStepTowardGoal
    private double valueIfInBounds(int i, int j) {
        return 0 <= i && i < windowDim && 0 <= j && j < windowDim ? window[i][j] : 1000; // 1000 is arbitrary
    }

    // Helper function for getStepTowardGoal
    private double modifiedCooldown(MapLocation loc) throws GameActionException {
        return rc.canSenseLocation(loc) ? baseCooldown / rc.sensePassability(loc) : 1000; // 1000 is arbitrary
    }

    // Helper function for getStepTowardGoal
    private boolean validMovementDirection(Direction d, Direction toGoal) {
        if (toGoal.dy != 0) {
            return d.dy == toGoal.dy || d.dy == 0;
        } else {
            return d.dx == toGoal.dx || d.dx == 0;
        }
    }
}
