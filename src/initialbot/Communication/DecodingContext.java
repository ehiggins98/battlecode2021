package initialbot.Communication;

public class DecodingContext {
    private int currentX;
    private int currentY;

    public DecodingContext(int currentX, int currentY) {
        this.currentX = currentX;
        this.currentY = currentY;
    }

    public int getCurrentX() {
        return this.currentX;
    }

    public int getCurrentY() {
        return this.currentY;
    }
}
