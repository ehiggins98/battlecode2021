package initialbot.Robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Direction;
import battlecode.common.Team;

public class Slanderer implements RobotInterface {
	private final RobotController rc;
    private boolean foundPosition;
		private boolean startPosition;
		private Direction direction;
		private int remainingSteps;
		public Team ourTeam;
		public Team enemyTeam;

    public Slanderer(RobotController rc) {
        this.rc = rc;
        this.startPosition = true;
        this.foundPosition = false;

    }

    @Override
    public void runTurn(int turn) throws GameActionException {
        if( !foundPosition ) {
            if( this.startPosition ) {
                MapLocation enlightenmentDirection = findEnlightenment();
                moveAway(enlightenmentDirection);	
				this.startPosition = false;
            }
            if ( rc.canMove(this.direction)) {
                rc.move(this.direction);
                this.remainingSteps--;
            }
            RobotInfo[] foundRobots = this.rc.senseNearbyRobots(1, this.ourTeam);
            boolean foundPolitician = false;
            for (RobotInfo robot: foundRobots) {
                if( robot.type == RobotType.POLITICIAN)
                    foundPolitician = true;
            }
            if( this.remainingSteps <=0 || foundPolitician) {
                this.foundPosition = true;
            }
        }
    }
    
    public void moveAway(MapLocation moveAway) {
		MapLocation robotLocation = this.rc.getLocation();
		if ( robotLocation.x - moveAway.x == 1) {
			if( robotLocation.y - moveAway.y == 1) {
				this.direction = Direction.SOUTHEAST;
			} else if (robotLocation.y - moveAway.y == -1 ) {
				this.direction = Direction.NORTHEAST;
			}else {
				this.direction = Direction.EAST;
			}
		} else if (robotLocation.x - moveAway.x == -1 ) {
			if( robotLocation.y - moveAway.y == 1) {
				this.direction = Direction.SOUTHWEST;
			} else if (robotLocation.y - moveAway.y == -1 ) {
				this.direction = Direction.NORTHWEST;
			}else {
				this.direction = Direction.WEST;
			}
		} else {
			if (robotLocation.y - moveAway.y == 1) {
				this.direction = Direction.SOUTH;
			} else {
				this.direction = Direction.NORTH;
			}
		}
    }
    
    public  MapLocation findEnlightenment() {
		RobotInfo[] allClosestRobots = this.rc.senseNearbyRobots(2);
		for ( RobotInfo robot: allClosestRobots ) {
			if( robot.type == RobotType.ENLIGHTENMENT_CENTER ) {
				return robot.location;
			}
		}
		return new MapLocation(-1, -1);
	}
    
}
