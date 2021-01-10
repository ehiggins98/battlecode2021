package initialbot.Robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import initialbot.PathFinder;
import initialbot.Communication.Communicator;
import initialbot.Communication.Message;
import initialbot.Communication.Messages.DefenseLocationMessage;

public class Slanderer implements RobotInterface {
    private final RobotController rc;
    private final PathFinder pathFinder;
    private final Communicator communicator;
    private Message lastMessage;
    private MapLocation ecLocation;

    public Slanderer(RobotController rc) throws RuntimeException {
        this.rc = rc;
        this.pathFinder = new PathFinder(rc);
        this.communicator = new Communicator(rc);
        this.lastMessage = null;

        boolean ecFound = false;
        for (RobotInfo info : rc.senseNearbyRobots(2)) {
            if (info.type == RobotType.ENLIGHTENMENT_CENTER) {
                ecLocation = info.location;
                ecFound = true;
                break;
            }
        }

        if (!ecFound) {
            throw new RuntimeException("could not find ec during Slanderer initialization");
        }
    }

    @Override
    public void runTurn(int turn) throws GameActionException {
        readMessageIfPresent();
        if (lastMessage == null) {
            return;
        }

        if (lastMessage.getClass() == DefenseLocationMessage.class) {
            DefenseLocationMessage dlm = (DefenseLocationMessage) lastMessage;
            MapLocation goal = pathFinder.getLocationAtRadius(ecLocation, dlm.getRadius(), dlm.getDirection());
            Direction direction = pathFinder.getStepTowardGoal(goal);
            if (direction != Direction.CENTER && rc.canMove(direction)) {
                rc.move(direction);
            }
        }
    }

    private void readMessageIfPresent() throws GameActionException {
        Message message = communicator.getMessageFromEC();
        if (message != null) {
            lastMessage = message;
        }
    }
}
