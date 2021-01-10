package initialbot.Communication;

public interface Message {
    public int toFlag();

    public void fromFlag(DecodingContext context, int flag);

    @Override
    public boolean equals(Object obj);
}
