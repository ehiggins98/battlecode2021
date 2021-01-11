package initialbot.Communication;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import initialbot.Helpers;

public class Communicator {
    private final RobotController rc;
    private final List<Integer> robotIds;
    private RobotType robotType;
    private int roundCreated;
    private int ecId;

    public Communicator(RobotController rc) throws RuntimeException {
        this.rc = rc;
        this.robotIds = new ArrayList<Integer>();
        this.robotType = rc.getType();
        this.roundCreated = rc.getRoundNum();

        boolean foundEC = false;
        for (RobotInfo info : rc.senseNearbyRobots(2)) {
            if (info.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                this.ecId = info.getID();
                foundEC = true;
                break;
            }
        }

        if (!foundEC) {
            ecId = -1;
        }
    }

    public void logRobotCreated(int id) {
        this.robotIds.add(id);
    }

    public Message getMessageFromEC() throws GameActionException {
        if (ecId < 0 || !rc.canGetFlag(ecId)) {
            return null;
        }
        
        int flag = rc.getFlag(ecId);
        Message message = decodeFlag(flag);

        if (message.shouldIgnore(robotType, roundCreated)) {
            return null;
        } else {
            return message;
        }
    }

    public void sendMessage(Message message) throws GameActionException {
        rc.setFlag(message.toFlag());
    }

    public List<Message> getAllMessages() throws GameActionException {
        List<Message> messages = new ArrayList<Message>();
        for (int id : robotIds) {
            if (rc.canGetFlag(id)) {
                Message m = decodeFlag(rc.getFlag(id));
                if (m != null && !m.shouldIgnore(robotType, roundCreated)) {
                    messages.add(m);
                }
            }
        }

        return messages;
    }

    private Message decodeFlag(int flag) {
        if (flag == 0) {
            return null;
        }

        MapLocation loc = rc.getLocation();
        Message message = MessagesList.getMessage(getTypeCode(flag));
        if (message == null) {
            return null;
        }

        message.fromFlag(new DecodingContext(loc.x, loc.y), flag);
        return message;
    }

    private int getTypeCode(int flag) {
        return flag >> (Helpers.flagBits - MessagesList.getNBitsForTypeCode());
    }
}
