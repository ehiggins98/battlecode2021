package initialbot.Robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class EnlightenmentCenter implements RobotInterface {
    private final RobotController rc;

    // private static int lastInfluence = GameConstants.INITIAL_ENLIGHTENMENT_CENTER_INFLUENCE;
    private static int bidValue = 1;
    private static int lastVoteCount = 0;
    private final int adder = 1;
    private final double multiplier = 1.5;

    public EnlightenmentCenter(RobotController rc) {
        this.rc = rc;
    }

    @Override
    public void runTurn(int turn) throws GameActionException {
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

        int influence = this.rc.getInfluence();
        System.out.println(influence);

        if (turn % 100 == 0 && influence >= 20) {
            rc.buildRobot(RobotType.POLITICIAN, Direction.EAST, 20);
        }

        bidValue = Math.min(influence / 4, bidValue);

        lastVoteCount = this.rc.getTeamVotes();

        if (this.rc.canBid(bidValue)) {
            this.rc.bid(bidValue);
            System.out.println("Bid " + bidValue);
        }
    }
}
