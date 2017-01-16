package model;

public class WeightedAverageFilter {
    public static final double FILTER_WEIGHT = 0.3;
    private boolean isFirstData;
    private double prevData;

    public WeightedAverageFilter() {
        reset();
    }

    public double update(double measurement) {
        if (isFirstData) {
            isFirstData = false;
            prevData = measurement * FILTER_WEIGHT + measurement * (1.0 - FILTER_WEIGHT);
        }
        else {
            prevData = measurement * FILTER_WEIGHT + prevData * (1.0 - FILTER_WEIGHT);
        }
        return prevData;
    }

    public void reset() {
        isFirstData = true;
        prevData = 0.0;
    }
}
