package model;

import org.opencv.core.Point3;

public class Wall {
    private Point3 p1;
    private Point3 p2;
    private Point3 p3;
    private Point3 p4;

    public Wall(double p1x, double p1y, double p1z, double p2x, double p2y, double p2z,
                double p3x, double p3y, double p3z, double p4x, double p4y, double p4z) {
        p1 = new Point3(p1x, p1y, p1z);
        p2 = new Point3(p2x, p2y, p2z);
        p3 = new Point3(p3x, p3y, p3z);
        p4 = new Point3(p4x, p4y, p4z);
    }

    public Wall(Wall wall) {
        p1 = new Point3(wall.getP1().x, wall.getP1().y, wall.getP1().z);
        p2 = new Point3(wall.getP2().x, wall.getP2().y, wall.getP2().z);
        p3 = new Point3(wall.getP3().x, wall.getP3().y, wall.getP3().z);
        p4 = new Point3(wall.getP4().x, wall.getP4().y, wall.getP4().z);
    }

    public void forward(double distance) {
        p1.y -= distance;
        p2.y -= distance;
        p3.y -= distance;
        p4.y -= distance;
    }

    public void forward(double distanceX, double distanceY) {
        p1.x -= distanceX;
        p2.x -= distanceX;
        p3.x -= distanceX;
        p4.x -= distanceX;
        p1.y -= distanceY;
        p2.y -= distanceY;
        p3.y -= distanceY;
        p4.y -= distanceY;
    }

    public void makeSameValue(Wall wall) {
        p1.x = wall.getP1().x;
        p1.y = wall.getP1().y;
        p1.z = wall.getP1().z;
        p2.x = wall.getP2().x;
        p2.y = wall.getP2().y;
        p2.z = wall.getP2().z;
        p3.x = wall.getP3().x;
        p3.y = wall.getP3().y;
        p3.z = wall.getP3().z;
        p4.x = wall.getP4().x;
        p4.y = wall.getP4().y;
        p4.z = wall.getP4().z;
    }

    public Point3 getP1() {
        return p1;
    }

    public Point3 getP2() {
        return p2;
    }

    public Point3 getP3() {
        return p3;
    }

    public Point3 getP4() {
        return p4;
    }

}
