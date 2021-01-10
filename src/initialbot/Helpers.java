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

    public static final RobotType[] robotTypes = {
        RobotType.ENLIGHTENMENT_CENTER,
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER
    };

    public static final int flagBits = 24;

    public static double getBaseCooldown(RobotType robotType) {
        switch (robotType) {
            case ENLIGHTENMENT_CENTER:
                return 2.0;
            case POLITICIAN:
                return 1.0;
            case SLANDERER:
                return 2.0;
            case MUCKRAKER:
                return 1.5;
            default:
                throw new IllegalArgumentException("argument to getSensorRadiusSquared must be a valid unit type");
        }
    }

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

    public static int radiusSquared(int dx, int dy) {
        return (int) (Math.pow(dx, 2) + Math.pow(dy, 2));
    }

    // Get a mask for the N least-significant bits
    public static int getMaskForNLSBs(int n) {
        return ~(~0 << n);
    }
}