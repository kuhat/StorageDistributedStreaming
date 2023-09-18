package utils;

public enum ScaleDirection {
    UP,
    DOWN;

    public static ScaleDirection toScaleDirection(int directionInt) {
        if(directionInt < 0)
        {
            return DOWN;
        }
        else
        {
            return UP;
        }
    }
}
