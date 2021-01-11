package initialbot.Robots;

import java.util.HashMap;
import java.util.Map;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import initialbot.Helpers;
import initialbot.Communication.Communicator;
import initialbot.Communication.Messages.DefenseLocationMessage;
import initialbot.Communication.Messages.ChangeRadiusMessage;

public class EnlightenmentCenter implements RobotInterface {
    private final RobotController rc;
    private final Communicator communicator;

    private GamePhase phase;
    private double passability;
    
    private static int bidValue = 1;
    private static int lastVoteCount = 0;
    private final int adder = 1;
    private final double multiplier = 1.5;

    private final int politicianInfluence = 20;

    private Direction defenseDirection = Direction.WEST;
    private int slandererDefenseRadius = 3;
    private int politicianDefenseRadius = 6;

    // Early game state
    private int earlyGameInfluenceIncAchieved = 0;
    private int earlyGamePoliticiansCreated = 0;

    // Mid game state
    private RobotType midGameUnitToCreate = RobotType.SLANDERER;
    private int lastPoliticianRadiusIncrement = 0;
    private int lastSlandererRadiusIncrement = 0;
    private int lastUnitCreated = 0;

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
        
        boolean finished;
        switch (phase) {
            case EARLY:
                finished = runEarlyGameTurn(turn);
                if (finished) {
                    phase = GamePhase.MID;
                }
                break;
            case MID:
                finished = runMidGameTurn(turn);
                if (finished) {
                    phase = GamePhase.LATE;
                }
                break;
            case LATE:
                break;
        }
    }

    // Return value indicates whether the early-game strategy is finished
    // Goal of the early game is to get 8 extra influence per turn by turn 20
    private boolean runEarlyGameTurn(int turn) throws GameActionException {
        final int influenceIncTarget = 8;
        final int politicianTarget = 8;

        double cooldown = 2 / passability;
        int buildsBeforeTurn20 = (int) (20 / cooldown);
        int influencePerSlanderer = Math.min((int) Math.ceil(influenceIncTarget / buildsBeforeTurn20), 6);
        
        Direction buildDirection = getBuildDirection();
        if (earlyGameInfluenceIncAchieved < influenceIncTarget && 
                buildDirection != null &&
                rc.canBuildRobot(RobotType.SLANDERER, buildDirection, influenceIncToCost.get(influencePerSlanderer))) {
    
            buildSlandererAndDefend(turn, buildDirection, influencePerSlanderer);
        } else if (earlyGamePoliticiansCreated < politicianTarget &&
                buildDirection != null &&
                rc.canBuildRobot(RobotType.POLITICIAN, buildDirection, politicianInfluence)) {

            buildPoliticianAndDefend(turn, buildDirection);
        }
 
        bid();

        return earlyGameInfluenceIncAchieved >= influenceIncTarget && earlyGamePoliticiansCreated >= politicianTarget;
    }

    // Return value indicates whether the mid-game strategy is finished
    // In the mid-game right now we just alternate creating slanderers and politicians, and bidding on every turn.
    private boolean runMidGameTurn(int turn) throws GameActionException {
        Direction buildDirection = getBuildDirection();
        if (midGameUnitToCreate.equals(RobotType.POLITICIAN) && 
                buildDirection != null &&
                rc.canBuildRobot(RobotType.POLITICIAN, buildDirection, politicianInfluence)) {

            buildPoliticianAndDefend(turn, buildDirection);
            midGameUnitToCreate = RobotType.SLANDERER;
            lastUnitCreated = turn;
        } else if (midGameUnitToCreate.equals(RobotType.SLANDERER) &&
                buildDirection != null &&
                rc.canBuildRobot(RobotType.SLANDERER, buildDirection, influenceIncToCost.get(1))) {
            buildSlandererAndDefend(turn, buildDirection, 1);
            midGameUnitToCreate = RobotType.POLITICIAN;
            lastUnitCreated = turn;
        }

        bid();

        if (turn - lastUnitCreated >= 2) {
            if (turn - lastPoliticianRadiusIncrement >= 200) {
                politicianDefenseRadius += 1;
                communicator.sendMessage(new ChangeRadiusMessage(RobotType.POLITICIAN, politicianDefenseRadius));
                lastPoliticianRadiusIncrement = turn;
            } else if (buildDirection == null && turn - lastSlandererRadiusIncrement >= 100 && slandererDefenseRadius < 5) {
                slandererDefenseRadius += 1;
                communicator.sendMessage(new ChangeRadiusMessage(RobotType.SLANDERER, slandererDefenseRadius));
                lastSlandererRadiusIncrement = turn;
            }
        }

        return false;
    }

    private void updateBidAmount() {
        // No point in updating the bid amount if we have a guaranteed majority of votes
        if (rc.getTeamVotes() > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS / 2) {
            return;
        } 

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
        // If we have more than 1500 votes we can't lose the vote, so stop bidding
        if (rc.getTeamVotes() > GameConstants.GAME_MAX_NUMBER_OF_ROUNDS / 2) {
            return;
        }

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

    private void buildSlandererAndDefend(int turn, Direction buildDirection, int influenceInc) throws GameActionException {
        rc.buildRobot(RobotType.SLANDERER, buildDirection, influenceIncToCost.get(influenceInc));
        communicator.sendMessage(new DefenseLocationMessage(RobotType.SLANDERER, turn, slandererDefenseRadius, defenseDirection));
        earlyGameInfluenceIncAchieved += influenceInc;
        defenseDirection = defenseDirection.rotateLeft();
    }

    private void buildPoliticianAndDefend(int turn, Direction buildDirection) throws GameActionException {
        rc.buildRobot(RobotType.POLITICIAN, buildDirection, politicianInfluence);
        communicator.sendMessage(new DefenseLocationMessage(RobotType.POLITICIAN, turn, politicianDefenseRadius, defenseDirection));
        earlyGamePoliticiansCreated++;
        defenseDirection = defenseDirection.rotateLeft();
    }
}
