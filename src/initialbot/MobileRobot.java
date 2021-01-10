package initialbot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import initialbot.Communication.Communicator;
import initialbot.Communication.Message;
import initialbot.Robots.RobotInterface;

public abstract class MobileRobot implements RobotInterface {
    protected final RobotController rc;
    protected final PathFinder pathFinder;
    protected final Communicator communicator;
    protected final MapLocation ecLocation;
    protected Message lastMessage;

    protected MobileRobot(RobotController rc) throws RuntimeException {
        this.rc = rc;
        this.pathFinder = new PathFinder(rc);
        this.communicator = new Communicator(rc);
        this.ecLocation = getECLocation();
    }

    @Override
    public void runTurn(int turn) throws GameActionException {
        readMessageIfPresent();
    }

    protected void readMessageIfPresent() throws GameActionException {
        Message message = communicator.getMessageFromEC();
        if (message != null) {
            lastMessage = message;
        }
    }

    private MapLocation getECLocation() throws RuntimeException {
        for (RobotInfo info : rc.senseNearbyRobots(2)) {
            if (info.type == RobotType.ENLIGHTENMENT_CENTER) {
                return info.location;
            }
        }

        throw new RuntimeException("couldn't find EC on robot initialization");
    }
}
