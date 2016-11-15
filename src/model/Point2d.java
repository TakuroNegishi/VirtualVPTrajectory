package model;

import org.opencv.core.Point3;

import java.awt.*;

public class Point2d {
    public double x;
    public double y;

    public Point2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void makeSameValue(Point2d p) {
        x = p.x;
        y = p.y;
    }

    public void makeSameValue(Point3 p) {
        x = p.x;
        y = p.y;
    }

    public void makeSameValue(Point p) {
        x = p.x;
        y = p.y;
    }
}
