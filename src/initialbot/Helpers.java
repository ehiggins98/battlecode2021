package initialbot;

import battlecode.common.Direction;
import battlecode.common.RobotType;

public class Helpers {
    // The ordering of this list is used in pathfinding, so pls don't change it
    public static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    public static int getSensorRadiusSquared(RobotType robotType) {
        switch (robotType) {
            case ENLIGHTENMENT_CENTER:
                return 40;
            case POLITICIAN:
                return 25;
            case SLANDERER:
                return 20;
            case MUCKRAKER:
                return 30;
            default:
                throw new IllegalArgumentException("argument to getSensorRadiusSquared must be a valid unit type");
        }
    }
}