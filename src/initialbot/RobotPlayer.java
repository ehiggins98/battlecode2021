package initialbot;

import battlecode.common.*;
import initialbot.Robots.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    static RobotInterface robot;

    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    static int turn;

    public static void run(RobotController rc) {
        RobotPlayer.rc = rc;

        switch (rc.getType()) {
            case ENLIGHTENMENT_CENTER:
                robot = new EnlightenmentCenter(rc);
                break;
            case POLITICIAN:
                robot = new Politician(rc);
                break;
            case SLANDERER:
                robot = new Slanderer(rc);
                break;
            case MUCKRAKER:
                robot = new Muckraker(rc);
                break;
        }

        while (true) {
            turn = rc.getRoundNum();

            try {
                robot.runTurn(turn);
                Clock.yield();
            } catch (Exception e) {
                System.out.printf("%d of type %s threw exception", rc.getID(), rc.getType());
                e.printStackTrace();
            }
        }
    }
}