package model;

public class MovingAverageFilter {
    public static final int WIN_SIZE = 7; // 平均窓サイズ
    private double[] data; //
    private double total;
    private int pointer;
    private boolean isFilledDataArray;

    public MovingAverageFilter() {
        data = new double[WIN_SIZE];
        reset();
//        Object aa;
//        Integer num = new Integer(111);
//        aa = num;
//        aa = new Object();
//        System.out.println(num);
    }

    public double update(double measurement) {
        total -= data[pointer];
        total += measurement;
        data[pointer] = measurement;
        pointer++;
        if (pointer == WIN_SIZE) {
            pointer = 0;
            isFilledDataArray = true;
        }
        // dataが埋まらない最初の方は、data数で平均
        if (isFilledDataArray)
            return total / WIN_SIZE;
        else
            return total / pointer;
    }

    public void reset() {
        for (int i = 0; i < WIN_SIZE; i++) {
            data[i] = 0.0;
        }
        total = 0.0;
        pointer = 0;
        isFilledDataArray = false;
    }

}
