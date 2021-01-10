package initialbot;

import battlecode.common.*;
import initialbot.Robots.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    static RobotInterface robot;

    static int turn;

    public static void run(RobotController rc) {
        RobotPlayer.rc = rc;

        try {
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
        } catch (Exception e) {
            System.out.printf("%d of type %s threw exception during initialization", rc.getID(), rc.getType());
                e.printStackTrace();
        }

        while (true) {
            turn = rc.getRoundNum();

            try {
                if (robot.getClass().equals(Slanderer.class) && rc.getType().equals(RobotType.POLITICIAN)) {
                    robot = new Politician(rc, ((Slanderer) robot).ecLocation);
                }

                robot.runTurn(turn);
                Clock.yield();
            } catch (Exception e) {
                System.out.printf("%d of type %s threw exception", rc.getID(), rc.getType());
                e.printStackTrace();
            }
        }
    }
}