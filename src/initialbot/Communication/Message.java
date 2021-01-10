package initialbot.Communication;

import battlecode.common.RobotType;

public interface Message {
    public int toFlag();

    public void fromFlag(DecodingContext context, int flag);

    public boolean shouldIgnore(RobotType robotType, int roundCreated);

    @Override
    public boolean equals(Object obj);
}
