package model;

import util.Util;

import java.util.ArrayList;

public class Pedestrian {
    private double x; // m
    private double y; // m
    private double eyeX; // 注視点
    private double eyeY;
    private double direction; // °

    private final double INIT_EYE_X = 0;
    private final double INIT_EYE_Y = 10; // 10m

    public static final double DIR_NORTH = 90;
    public static final double DIR_EAST = 0;
    public static final double DIR_WEST = 180;
    public static final double DIR_SOUTH = 270;

    private double[] xList;
    private double[] yList;
    private double[] dList;
    private int routeIndex;

    public Pedestrian() {
        ArrayList<String> lines = Util.read("./points.csv");
        int lineNum = lines.size();
        xList = new double[lineNum];
        yList = new double[lineNum];
        dList = new double[lineNum];
        for (int i = 0; i < lineNum; i++) {
            String[] ss = lines.get(i).split(",");
            xList[i] = Double.parseDouble(ss[0]);
            yList[i] = Double.parseDouble(ss[1]);
            dList[i] = Double.parseDouble(ss[2]);
        }
        reset();
    }

    public void reset() {
        x = 0;
        y = 0;
        eyeX = INIT_EYE_X;
        eyeY = INIT_EYE_Y;
        direction = DIR_NORTH;
        routeIndex = 0;
    }

    public void goForward(double distance) {
        double r = Math.toRadians(direction);
        double diffX = Math.cos(r) * distance;
        double diffY = Math.sin(r) * distance;
        x += diffX;
        y += diffY;
        eyeX += diffX;
        eyeY += diffY;
    }

    public void moveRoute(double ax, double ay) {
        double diffX = ax - x;
        double diffY = ay - y;
        this.x = ax;
        this.y = ay;
        eyeX += diffX;
        eyeY += diffY;
    }

    public void rotate(double angle) {
        direction += angle;
        if (direction < 0)
            direction += 360;
        else if (direction >= 360)
            direction -= 360;
        // 注視点回転
        double r = Math.toRadians(angle); // 歩行者とは逆向きに回転する
        // 現在位置(x,y)を原点(0,0)として回転
        double ex = eyeX - x;
        double ey = eyeY - y;
        eyeX = ex * Math.cos(r) - ey * Math.sin(r) + x;
        eyeY = ex * Math.sin(r) + ey * Math.cos(r) + y;
    }

    public void nextRouteIndex() {
        routeIndex++;
        if (routeIndex >= xList.length) {
            routeIndex = 0;
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getEyeX() {
        return eyeX;
    }

    public double getEyeY() {
        return eyeY;
    }

    public double getDirection() {
        return direction;
    }

    public double getINIT_EYE_X() {
        return INIT_EYE_X;
    }

    public double getINIT_EYE_Y() {
        return INIT_EYE_Y;
    }

    public double getXListElem() {
        return  xList[routeIndex];
    }

    public double getYListElem() {
        return  yList[routeIndex];
    }

    public double getDListElem() {
        return  dList[routeIndex];
    }

    @Override
    public String toString() {
        String footer = "";
        if (direction == DIR_NORTH)
            footer = "(NORTH)";
        else if (direction == DIR_SOUTH)
            footer = "(SOUTH)";
        else if (direction == DIR_EAST)
            footer = "(EAST)";
        else if (direction == DIR_WEST)
            footer = "(WEST)";
        return "PD x:" + Util.dToS(x) + ", y:" + Util.dToS(y) + "\n"
                + "d:" + Util.dToS(direction) + " " + footer + " routeIndex:"  + routeIndex;
//                + "eye(" + Util.dToS(eyeX) + ", " + Util.dToS(eyeY) + ")";
    }
}
