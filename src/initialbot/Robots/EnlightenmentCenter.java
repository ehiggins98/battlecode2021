package initialbot.Robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class EnlightenmentCenter implements RobotInterface {
    private final RobotController rc;

    private static int lastInfluence = 150;
    private static int bidValue = 1;
    private final int adder = 1;
    private final double multiplier = 1.5;

    public EnlightenmentCenter(RobotController rc) {
        this.rc = rc;
    }

    @Override
    public void runTurn(int turn) throws GameActionException {
        int influence = this.rc.getInfluence();

        System.out.println(influence);

        if (influence == lastInfluence - bidValue) {
            // We won the vote
            System.out.println("We won the vote");
            if (bidValue > adder) {
                bidValue -= adder;
            }
        } else if (influence == lastInfluence - Math.ceil(bidValue / 2.0) && turn > 1) {
            // We lost the vote
            bidValue = (int) Math.ceil(bidValue * multiplier);
        }

        if (turn % 100 == 0) {
            rc.buildRobot(RobotType.POLITICIAN, Direction.EAST, 20);
        }

        lastInfluence = this.rc.getInfluence();

        bidValue = Math.min(lastInfluence / 4, bidValue);

        if (this.rc.canBid(bidValue)) {
            this.rc.bid(bidValue);
            System.out.println("Bid " + bidValue);
        }
    }
}
