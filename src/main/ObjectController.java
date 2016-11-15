package main;

import model.WallLine;
import model.Pedestrian;
import model.Point2d;
import org.opencv.core.Point3;

import java.util.Random;

public class ObjectController {
//    public static final int POINT_NUM = 32;
    public static final int POINT_NUM = 19 * 3 * 3;
    public static final int WALL_LINE_NUM = 4;

    private Pedestrian pedestrian;
    private Point3[] pointArray;
    private Point2d[] drawingPointArray;
    private Point2d[] prevDrawingPointArray;
    private WallLine[] wallArray;
    private WallLine[] drawingWallArray;
    private Random rand;

    public ObjectController() {
        rand = new Random(22);
        pedestrian = new Pedestrian();  // 歩行者初期化
        // 点群初期化
        pointArray = new Point3[POINT_NUM];
        for (int i = 0; i < 19; i++) {      // y
            for (int j = 0; j < 3; j++) {   // x
                pointArray[(i * 9) + (j * 3) + 0] = new Point3(-5 + (j * 5), i * 5, -2);
                pointArray[(i * 9) + (j * 3) + 1] = new Point3(-5 + (j * 5), i * 5, 0);
                pointArray[(i * 9) + (j * 3) + 2] = new Point3(-5 + (j * 5), i * 5, 2);
            }
        }

        // 描画用点群座標情報を初期化
        drawingPointArray = new Point2d[POINT_NUM];
        prevDrawingPointArray = new Point2d[POINT_NUM];
        for (int i = 0; i < POINT_NUM; i++) {
            drawingPointArray[i] = new Point2d(pointArray[i].x, pointArray[i].y);
            prevDrawingPointArray[i] = new Point2d(pointArray[i].x, pointArray[i].y);
        }
        // 壁の座標初期化
        wallArray = new WallLine[WALL_LINE_NUM];
        wallArray[0] = new WallLine(5, 0, 5, 40);
        wallArray[1] = new WallLine(5, 40, -40, 40);
        wallArray[2] = new WallLine(-5, 0, -5, 30);
        wallArray[3] = new WallLine(-5, 30, -40, 30);
        drawingWallArray = new WallLine[WALL_LINE_NUM];
        for (int i = 0; i < WALL_LINE_NUM; i++) {
            // 点情報だけコピー(wallArray[i].startのディープコピー)
            drawingWallArray[i] = new WallLine(wallArray[i].start, wallArray[i].end);
        }
    }

    public void move(double dist, double angle) {
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
        for (int i = 0; i < WALL_LINE_NUM; i++) {
            // 直進
            drawingWallArray[i].start.y -= dist;
            drawingWallArray[i].end.y -= dist;

            // 回転
            rotatePoint(drawingWallArray[i].start, angle);
            rotatePoint(drawingWallArray[i].end, angle);
        }
    }

    public void rotatePoint(Point2d point, double angle) {
        double x = point.x;
        double y = point.y;
        double r = Math.toRadians(-angle); // 歩行者とは逆向きに回転する
        point.x = x * Math.cos(r) - y * Math.sin(r);
        point.y = x * Math.sin(r) + y * Math.cos(r);
    }

    public void moveRoute() {
        double x = pedestrian.getX();
        double y = pedestrian.getY();
        double ex = pedestrian.getEyeX();
        double ey = pedestrian.getEyeY();
        double gx = pedestrian.getXListElem();
        double gy = pedestrian.getYListElem();
        double direction = pedestrian.getDListElem();
        double diffX = gx - x;
        double diffY = gy - y;
        double angle = direction - pedestrian.getDirection();

        Point2d a = new Point2d(ex - x, ey - y);
        Point2d b = new Point2d(gx - x, gy - y);
        double cos = (a.x * b.x + a.y * b.y) /
                Math.sqrt(a.x * a.x + a.y * a.y) * Math.sqrt(b.x * b.x + b.y * b.y);
        double theta = Math.acos(cos);
        theta = Math.toDegrees(theta);

        for (int i = 0; i < POINT_NUM; i++) {
            // 現在点群 -> 過去点群
            prevDrawingPointArray[i].makeSameValue(drawingPointArray[i]);

            // 進行方向に回転
            rotatePoint(drawingPointArray[i], -theta);
            // 並進
            drawingPointArray[i].x += diffX;
            drawingPointArray[i].y -= diffY;
            // (進行方向分戻す+)通常の回転
            rotatePoint(drawingPointArray[i], angle + theta);
        }

        for (int i = 0; i < WALL_LINE_NUM; i++) {
            // 進行方向に回転
            rotatePoint(drawingWallArray[i].start, -theta);
            rotatePoint(drawingWallArray[i].end, -theta);
            // 直進
            drawingWallArray[i].start.x += diffX;
            drawingWallArray[i].start.y -= diffY;
            drawingWallArray[i].end.x += diffX;
            drawingWallArray[i].end.y -= diffY;

            // (進行方向分戻す+)通常の回転
            rotatePoint(drawingWallArray[i].start, angle + theta);
            rotatePoint(drawingWallArray[i].end, angle + theta);
        }
        // 歩行者移動
        pedestrian.moveRoute(gx, gy);    // 指定座標に移動
        pedestrian.rotate(angle);   // 指定角度に回転

        pedestrian.nextRouteIndex();
    }

    public void reset() {
        pedestrian.reset();
        for (int i = 0; i < POINT_NUM; i++) {
            drawingPointArray[i].makeSameValue(pointArray[i]);
            prevDrawingPointArray[i].makeSameValue(pointArray[i]);
        }
        for (int i = 0; i < WALL_LINE_NUM; i++) {
            drawingWallArray[i].makeSameValue(wallArray[i]);
        }
    }

    public Pedestrian getPedestrian() {
        return pedestrian;
    }

    public Point3[] getPointArray() { return pointArray; }

    public Point2d[] getDrawingPointArray() {
        return drawingPointArray;
    }

    public Point2d[] getPrevDrawingPointArray() {
        return prevDrawingPointArray;
    }

    public WallLine[] getDrawingWallArray() {
        return drawingWallArray;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(pedestrian + "\n----------\n");
        return sb.toString();
    }
}
