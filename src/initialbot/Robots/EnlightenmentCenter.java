package initialbot.Robots;

import java.util.HashMap;
import java.util.Map;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import initialbot.Helpers;
import initialbot.Communication.Communicator;
import initialbot.Communication.Messages.DefenseLocationMessage;

public class EnlightenmentCenter implements RobotInterface {
    private final RobotController rc;
    private final Communicator communicator;

    private GamePhase phase;
    private double passability;
    
    private static int bidValue = 1;
    private static int lastVoteCount = 0;
    private final int adder = 1;
    private final double multiplier = 1.5;

    private final int slandererDefenseRadius = 3;

    // Early game state
    private int earlyGameInfluenceIncAchieved = 0;
    private Direction earlyGameDirection = Direction.WEST;

    private final Map<Integer, Integer> influenceIncToCost;

    public EnlightenmentCenter(RobotController rc) throws GameActionException {
        this.rc = rc;
        this.communicator = new Communicator(rc);
        this.phase = GamePhase.EARLY;
        this.passability = rc.sensePassability(rc.getLocation());

        influenceIncToCost = new HashMap<Integer, Integer>();
        influenceIncToCost.put(1, 21);
        influenceIncToCost.put(2, 41);
        influenceIncToCost.put(3, 63);
        influenceIncToCost.put(4, 85);
        influenceIncToCost.put(5, 107);
        influenceIncToCost.put(6, 130);
    }

    @Override
    public void runTurn(int turn) throws GameActionException {
        updateBidAmount();
        
        switch (phase) {
            case EARLY:
                boolean finished = runEarlyGameTurn(turn);
                if (finished) {
                    phase = GamePhase.MID;
                }
                break;
            case MID:
                break;
            case LATE:
                break;
        }
    }

    // Return value indicates whether the early-game strategy is finished
    // Goal of the early game is to get 8 extra influence per turn by turn 20
    private boolean runEarlyGameTurn(int turn) throws GameActionException {
        final int influenceTarget = 8;
        double cooldown = 2 / passability;
        int buildsBeforeTurn20 = (int) (20 / cooldown);
        int influencePerSlanderer = Math.min((int) Math.ceil(influenceTarget / buildsBeforeTurn20), 6);
        
        Direction buildDirection = getBuildDirection();
        if (buildDirection != null && rc.canBuildRobot(RobotType.SLANDERER, buildDirection, influenceIncToCost.get(influencePerSlanderer))) {
            rc.buildRobot(RobotType.SLANDERER, buildDirection, influenceIncToCost.get(influencePerSlanderer));
            earlyGameInfluenceIncAchieved += influencePerSlanderer;
            communicator.sendMessage(new DefenseLocationMessage(RobotType.SLANDERER, turn, slandererDefenseRadius, earlyGameDirection));
            earlyGameDirection = earlyGameDirection.rotateLeft();
        }

        bid();

        return earlyGameInfluenceIncAchieved >= influenceTarget;
    }

    private void updateBidAmount() {
        if (this.rc.getTeamVotes() > lastVoteCount) {
            // We won the vote
            System.out.println("We won the vote");
            if (bidValue > adder) {
                bidValue -= adder;
            }
        } else if (this.rc.getTeamVotes() == lastVoteCount) {
            // We lost the vote
            bidValue = (int) Math.ceil(bidValue * multiplier);
        }

        lastVoteCount = this.rc.getTeamVotes();
    }

    private void bid() throws GameActionException {
        int influence = this.rc.getInfluence();
        System.out.println(influence);

        bidValue = Math.min(influence / 4, bidValue);


        if (this.rc.canBid(bidValue)) {
            this.rc.bid(bidValue);
            System.out.println("Bid " + bidValue);
        }
    }

    private Direction getBuildDirection() throws GameActionException {
        MapLocation loc = rc.getLocation();
        for (Direction d : Helpers.directions) {
            if (!rc.isLocationOccupied(loc.add(d))) {
                return d;
            }
        }

        return null;
    }
}
