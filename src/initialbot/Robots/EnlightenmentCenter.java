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
    private int convictionAfterLastBuild = 0;

    private Map<Integer, Integer> slandererCostCache = new HashMap<Integer, Integer>();
    private int maxIncInCache = 0;

    public EnlightenmentCenter(RobotController rc) throws GameActionException {
        this.rc = rc;
        this.communicator = new Communicator(rc);
        this.phase = GamePhase.EARLY;
        this.passability = rc.sensePassability(rc.getLocation());
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
        final int politicianInfluence = 20;

        double cooldown = 2 / passability;
        int buildsBeforeTurn20 = (int) (20 / cooldown);
        int influencePerSlanderer = Math.min((int) Math.ceil(influenceIncTarget / buildsBeforeTurn20), 6);
        
        Direction buildDirection = getBuildDirection();
        if (earlyGameInfluenceIncAchieved < influenceIncTarget && 
                buildDirection != null &&
                rc.canBuildRobot(RobotType.SLANDERER, buildDirection, getSlandererInfluenceForInc(influencePerSlanderer))) {
    
            buildSlandererAndDefend(turn, buildDirection, influencePerSlanderer);
        } else if (earlyGamePoliticiansCreated < politicianTarget &&
                buildDirection != null &&
                rc.canBuildRobot(RobotType.POLITICIAN, buildDirection, politicianInfluence)) {

            buildPoliticianAndDefend(turn, buildDirection, politicianInfluence);
        }
 
        bid();

        return earlyGameInfluenceIncAchieved >= influenceIncTarget && earlyGamePoliticiansCreated >= politicianTarget;
    }

    // Return value indicates whether the mid-game strategy is finished
    // In the mid-game right now we just alternate creating slanderers and politicians, and bidding on every turn.
    private boolean runMidGameTurn(int turn) throws GameActionException {
        Direction buildDirection = getBuildDirection();
        int politicianCost = getPoliticianCost(turn);
        int slandererCost = getSlandererCost(turn);

        if (midGameUnitToCreate.equals(RobotType.POLITICIAN) && 
                buildDirection != null &&
                rc.canBuildRobot(RobotType.POLITICIAN, buildDirection, politicianCost)) {
                    
            buildPoliticianAndDefend(turn, buildDirection, politicianCost);
            midGameUnitToCreate = RobotType.SLANDERER;
            lastUnitCreated = turn;
        } else if (midGameUnitToCreate.equals(RobotType.SLANDERER) &&
                buildDirection != null &&
                rc.canBuildRobot(RobotType.SLANDERER, buildDirection, slandererCost)) {

            buildSlandererAndDefend(turn, buildDirection, getInfluenceInc(slandererCost));
            midGameUnitToCreate = RobotType.POLITICIAN;
            lastUnitCreated = turn;
        }

        bid();
        if (turn - lastUnitCreated >= 2) {
            if (turn - lastPoliticianRadiusIncrement >= 200) {
                politicianDefenseRadius += 1;
                communicator.sendMessage(new ChangeRadiusMessage(RobotType.POLITICIAN, politicianDefenseRadius));
                lastPoliticianRadiusIncrement = turn;
            } else if (buildDirection == null && turn - lastSlandererRadiusIncrement >= 100 && slandererDefenseRadius < 6) {
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
        rc.buildRobot(RobotType.SLANDERER, buildDirection, getSlandererInfluenceForInc(influenceInc));
        communicator.sendMessage(new DefenseLocationMessage(RobotType.SLANDERER, turn, slandererDefenseRadius, defenseDirection));
        earlyGameInfluenceIncAchieved += influenceInc;
        defenseDirection = defenseDirection.rotateLeft();
        convictionAfterLastBuild = rc.getConviction();
    }

    private void buildPoliticianAndDefend(int turn, Direction buildDirection, int influence) throws GameActionException {
        rc.buildRobot(RobotType.POLITICIAN, buildDirection, influence);
        communicator.sendMessage(new DefenseLocationMessage(RobotType.POLITICIAN, turn, politicianDefenseRadius, defenseDirection));
        earlyGamePoliticiansCreated++;
        defenseDirection = defenseDirection.rotateLeft();
        convictionAfterLastBuild = rc.getConviction();
    }

    private int getPoliticianCost(int turn) {
        final int minCost = 11;
        int convictionGained = rc.getConviction() - convictionAfterLastBuild;
        if (rc.getConviction() < getMinECConviction(turn)) {
            convictionGained -= 5;
        }
 
        return Math.max(convictionGained, minCost);
    }

    private int getSlandererCost(int turn) {
        final int minCost = 21;
        int convictionGained = rc.getConviction() - convictionAfterLastBuild;
        if (rc.getConviction() < getMinECConviction(turn)) {
            convictionGained -= 5;
        }

        int cost = Math.max(convictionGained, minCost);
        return getSlandererInfluenceForInc(getInfluenceInc(cost));
    }

    private int getMinECConviction(int turn) {
        return 100 + turn / 6;
    }

    private int getSlandererInfluenceForInc(int inc) {
        if (slandererCostCache.containsKey(inc)) {
            return slandererCostCache.get(inc);
        }

        int val;
        if (slandererCostCache.size() > 0 && inc > maxIncInCache) {
            val = slandererCostCache.get(maxIncInCache);
        } else {
            val = 20 * inc;
        }
        
        for (; getInfluenceInc(val) != inc; val++) {}

        slandererCostCache.put(inc, val);
        if (inc > maxIncInCache) {
            maxIncInCache = inc;
        }
        return val;
    }

    private int getInfluenceInc(int x) {
        return (int) (x * (1.0 / 50.0 + 0.03 * Math.exp(-0.001 * x)));
    }
}
