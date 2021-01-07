package initialbot.Robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Direction;
import battlecode.common.Team;

public class Politician implements RobotInterface {
    private final RobotController rc;
		private boolean foundPosition;
		private boolean startPosition;
		private Direction direction;
		private int remainingSteps;
		public Team ourTeam;
		public Team enemyTeam;

    public Politician(RobotController rc) {
        this.rc = rc;
		this.startPosition = true;
		this.foundPosition = false;
		if ( this.rc.getID() % 2 == 1) {
			this.remainingSteps = 3;
		} else {
			this.remainingSteps = 2;
		}
		this.ourTeam = this.rc.getTeam();
		this.enemyTeam = this.rc.getTeam();
    }

    @Override
    public void runTurn(int turn) throws GameActionException {
        //System.out.println(rc.getCooldownTurns());
		if ( !foundPosition ) {
			if ( this.startPosition ) {
				//find where enlightenment center is
				MapLocation enlightenmentDirection = findEnlightenment();
				//move opposite direction
				moveAway(enlightenmentDirection);	
				this.startPosition = false;
			}
			if (rc.canMove(this.direction)) {
					rc.move(this.direction);
					this.remainingSteps--;
			}
			if ( this.remainingSteps <= 0 ) {
				this.foundPosition = true;
			}
		}
		RobotInfo[] enemyRobots = this.rc.senseNearbyRobots(9, this.enemyTeam);
		if ( enemyRobots.length > 0 ) {
			this.rc.empower(9);
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
