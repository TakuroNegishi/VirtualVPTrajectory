package main;

import model.Pedestrian;
import model.Point2d;
import model.Wall;
import org.opencv.core.Point3;
import util.Util;

import java.util.ArrayList;

public class ObjectController {
//    public static final int POINT_NUM = 32;
    static int POINT_NUM;
    // eye
//    public static final int WALL_NUM = 4;
//    private static final double SPAN = 0.1; // Y方向間隔
//    private static final int STRAIGHT_Y_NUM = 250; // 0.1m * 200 = 20m
//    private static final int LEFT_X_NUM = 0; // 0.5m * 100 = 50m
    // rotate
    static final int WALL_NUM = 4;
    private static final double SPAN = 0.5; // Y方向間隔
    private static final int STRAIGHT_Y_NUM = 120; // 0.5m * 120 = 60m
    private static final int LEFT_X_NUM = 100; // 0.5m * 100 = 50m

    private Pedestrian pedestrian;
    private Point3[] pointArray;
    private int[] pointTypeArray;
    private Point2d[] drawingPointArray;
    private Point2d[] prevDrawingPointArray;

    public static final int POINT_TYPE_LEFT = 0;
    public static final int POINT_TYPE_FLOOR = 1;
    public static final int POINT_TYPE_RIGHT = 2;

    private Wall[] wallArray;
    private Wall[] drawingWallArray;
//    private Random rand;

    ObjectController() {
//        rand = new Random(22);
        pedestrian = new Pedestrian();  // 歩行者初期化
        // 点群初期化
        ArrayList<String> lines = Util.read("./points.csv");
        int lineNum = lines.size();
        final int straightNum = lines.size() * STRAIGHT_Y_NUM;
        final int leftNum = lines.size() * LEFT_X_NUM;
        POINT_NUM = straightNum + leftNum;
        pointArray = new Point3[POINT_NUM];
        pointTypeArray = new int[POINT_NUM];
        // 正面通路
        for (int i = 0; i < straightNum; i++) {
            String[] ss = lines.get(i % lineNum).split(",");
            pointArray[i] = new Point3(Double.parseDouble(ss[0]),
                    Double.parseDouble(ss[1]) + SPAN * (i / lineNum), Double.parseDouble(ss[2]));
            if (pointArray[i].x <= Util.WALL_POINT_LEFT_X)
                pointTypeArray[i] = POINT_TYPE_LEFT;
            else if (pointArray[i].x >= Util.WALL_POINT_RIGHT_X)
                pointTypeArray[i] = POINT_TYPE_RIGHT;
            else
                pointTypeArray[i] = POINT_TYPE_FLOOR;
        }

        // 左通路
        final double xOffset = -1.015 - SPAN;
        final double yOffset = 28.2; // 左通路真ん中
        for (int i = straightNum; i < POINT_NUM; i++) {
            int ii = i - straightNum;
            String[] ss = lines.get(ii % lineNum).split(",");
            pointArray[i] = new Point3(xOffset + Double.parseDouble(ss[1]) - SPAN * (ii / lineNum),
                    yOffset + Double.parseDouble(ss[0]), Double.parseDouble(ss[2]));
            if (pointArray[i].y <= yOffset + Util.WALL_POINT_LEFT_X)
                pointTypeArray[i] = POINT_TYPE_LEFT;
            else if (pointArray[i].y >= yOffset + Util.WALL_POINT_RIGHT_X)
                pointTypeArray[i] = POINT_TYPE_RIGHT;
            else
                pointTypeArray[i] = POINT_TYPE_FLOOR;
        }


        // 描画用点群座標情報を初期化
        drawingPointArray = new Point2d[POINT_NUM];
        prevDrawingPointArray = new Point2d[POINT_NUM];
        for (int i = 0; i < POINT_NUM; i++) {
            drawingPointArray[i] = new Point2d(pointArray[i].x, pointArray[i].y);
            prevDrawingPointArray[i] = new Point2d(pointArray[i].x, pointArray[i].y);
        }

        // 壁の座標初期化
        wallArray = new Wall[WALL_NUM];
        final double wallLeft = -1.05; // -1.015 + 3.5cm
        final double wallRight = 1.05;
        final double wallTop = 0.8;
        final double wallBottom = -1.5;
        final double wallNear = 0.1;
        final double wallFar = 27.2 - 0.035;
//        final double wallFar = SPAN * STRAIGHT_Y_NUM;
        // 左壁
        wallArray[0] = new Wall(
                wallLeft, wallNear, wallTop,    // 手前上
                wallLeft, wallNear, wallBottom, // 手前下
                wallLeft, wallFar, wallBottom,  // 奥下
                wallLeft, wallFar, wallTop);    // 奥上
        // 右壁
        wallArray[1] = new Wall(
                wallRight, wallNear, wallTop,   // 手前上
                wallRight, wallNear, wallBottom,// 手前下
                wallRight, wallFar, wallBottom, // 奥下
                wallRight, wallFar, wallTop);   // 奥上
        // 左通路-左(手前)壁
        wallArray[2] = new Wall(
                wallLeft, wallFar, wallTop,             // 手前上
                wallLeft, wallFar, wallBottom,          // 手前下
                wallLeft - 20, wallFar, wallBottom,// 奥下
                wallLeft - 20, wallFar, wallTop);  // 奥上
        // 左通路-右(奥)壁
        wallArray[3] = new Wall(
                wallLeft, wallFar + wallRight * 2, wallTop,             // 手前上
                wallLeft, wallFar + wallRight * 2, wallBottom,          // 手前下
                wallLeft - 20, wallFar + wallRight * 2, wallBottom,// 奥下
                wallLeft - 20, wallFar + wallRight * 2, wallTop);  // 奥上

        drawingWallArray = new Wall[WALL_NUM];
        for (int i = 0; i < WALL_NUM; i++) {
            drawingWallArray[i] = new Wall(wallArray[i]);
        }
    }

    void move(double dist, double angle) {
        pedestrian.goForward(dist);
        pedestrian.rotate(angle);
        // 計算
        for (int i = 0; i < POINT_NUM; i++) {
            // 現在点群 -> 過去点群
            prevDrawingPointArray[i].makeSameValue(drawingPointArray[i]);

            drawingPointArray[i].y -= dist; // 直進
            // 回転
            rotatePoint(drawingPointArray[i], angle);
        }
        for (int i = 0; i < WALL_NUM; i++) {
            // 直進
            drawingWallArray[i].forward(dist);

            // 回転
            rotatePoint(drawingWallArray[i].getP1(), angle);
            rotatePoint(drawingWallArray[i].getP2(), angle);
            rotatePoint(drawingWallArray[i].getP3(), angle);
            rotatePoint(drawingWallArray[i].getP4(), angle);
        }
    }

    private void rotatePoint(Point2d point, double angle) {
        double x = point.x;
        double y = point.y;
        double r = Math.toRadians(-angle); // 歩行者とは逆向きに回転する
        point.x = x * Math.cos(r) - y * Math.sin(r);
        point.y = x * Math.sin(r) + y * Math.cos(r);
    }

    private void rotatePoint(Point3 point, double angle) {
        double x = point.x;
        double y = point.y;
        double r = Math.toRadians(-angle); // 歩行者とは逆向きに回転する
        point.x = x * Math.cos(r) - y * Math.sin(r);
        point.y = x * Math.sin(r) + y * Math.cos(r);
    }

    void moveRoute() {
        double x = pedestrian.getX();
        double y = pedestrian.getY();
//        double ex = pedestrian.getEyeX();
//        double ey = pedestrian.getEyeY();
        double gx = pedestrian.getXListElem();
        double gy = pedestrian.getYListElem();
        double direction = pedestrian.getDListElem();
        double diffX = gx - x;
        double diffY = gy - y;
        double angle = direction - pedestrian.getDirection();

        double r = Math.toRadians(pedestrian.getDirection() - 90);
        double pointDiffX = diffX * Math.cos(r) + diffY * Math.sin(r);
        double pointDiffY = (-diffX) * Math.sin(r) + diffY * Math.cos(r);

        for (int i = 0; i < POINT_NUM; i++) {
            // 現在点群 -> 過去点群
            prevDrawingPointArray[i].makeSameValue(drawingPointArray[i]);
            // 進行方向に回転
//            rotatePoint(drawingPointArray[i], -theta);
            // 並進
            drawingPointArray[i].x -= pointDiffX;
            drawingPointArray[i].y -= pointDiffY;
            // (進行方向分戻す+)通常の回転
            rotatePoint(drawingPointArray[i], angle);
        }

        for (int i = 0; i < WALL_NUM; i++) {
            // 進行方向に回転
//            rotatePoint(drawingWallArray[i].getP1(), -theta);
//            rotatePoint(drawingWallArray[i].getP2(), -theta);
//            rotatePoint(drawingWallArray[i].getP3(), -theta);
//            rotatePoint(drawingWallArray[i].getP4(), -theta);
            // 直進
            drawingWallArray[i].forward(pointDiffX, pointDiffY);

            // (進行方向分戻す+)通常の回転
            rotatePoint(drawingWallArray[i].getP1(), angle);
            rotatePoint(drawingWallArray[i].getP2(), angle);
            rotatePoint(drawingWallArray[i].getP3(), angle);
            rotatePoint(drawingWallArray[i].getP4(), angle);
        }
        // 歩行者移動
        pedestrian.moveRoute(gx, gy);    // 指定座標に移動
        pedestrian.rotate(angle);   // 指定角度に回転

        pedestrian.nextRouteIndex();
    }

    void reset() {
        pedestrian.reset();
        for (int i = 0; i < POINT_NUM; i++) {
            drawingPointArray[i].makeSameValue(pointArray[i]);
            prevDrawingPointArray[i].makeSameValue(pointArray[i]);
        }
        for (int i = 0; i < WALL_NUM; i++) {
            drawingWallArray[i].makeSameValue(wallArray[i]);
        }
    }

    Pedestrian getPedestrian() {
        return pedestrian;
    }

    Point3[] getPointArray() { return pointArray; }

    Point2d[] getDrawingPointArray() {
        return drawingPointArray;
    }

    Point2d[] getPrevDrawingPointArray() {
        return prevDrawingPointArray;
    }

    Wall[] getWallArray() { return wallArray; }

    Wall[] getDrawingWallArray() {
        return drawingWallArray;
    }

    boolean isLeftWallPoint(int i) { return pointTypeArray[i] == POINT_TYPE_LEFT; }

    boolean isRightWallPoint(int i) {
        return pointTypeArray[i] == POINT_TYPE_RIGHT;
    }

    boolean isFloorPoint(int i) {
        return pointTypeArray[i] == POINT_TYPE_FLOOR;
    }

    int[] getPointTypeArray() { return pointTypeArray; }

    @Override
    public String toString() {
        return pedestrian + "\n----------\n";
    }
}
