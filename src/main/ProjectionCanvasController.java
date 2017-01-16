package main;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Pedestrian;
import model.Point2d;
import model.Wall;
import org.opencv.core.Mat;
import org.opencv.core.Point3;
import util.Util;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class ProjectionCanvasController {
    private GraphicsContext gc;
    private double canvasW;
    private double canvasH;

    private Point2d[] prevFeaturePoints;
    private Point2d[] featurePoints;
    private Point3[] points3DArray;
    private Point3[] prevPoints3DArray;
    private double[] zIndex;
    private boolean[] pointVisible;

    private double[][] wallXArray;
    private double[][] wallYArray;
    private Wall[] wall3DArray;

    Point2d vp;
    private VanishingPointEstimator vpEstimator;
    private boolean isFirst;

    private boolean isDrawLeftWall;     // 左壁描画フラグ
    private boolean isDrawFloor;        // 床描画フラグ
    private boolean isDrawRightWall;    // 右壁描画フラグ
    private boolean isDrawPrevPoint;    // 前フレームの点描画フラグ

    private static final int WIDTH = 640; // 解像度:幅
    private static final int HEIGHT = 480;

    private static final double globalAlpha = 0.1;

    ProjectionCanvasController(Canvas canvas) {
        gc = canvas.getGraphicsContext2D();
        canvasW = canvas.getWidth();
        canvasH = canvas.getHeight();
        prevFeaturePoints = new Point2d[ObjectController.POINT_NUM];
        featurePoints = new Point2d[ObjectController.POINT_NUM];
        points3DArray = new Point3[ObjectController.POINT_NUM];
        prevPoints3DArray = new Point3[ObjectController.POINT_NUM];
        zIndex = new double[ObjectController.POINT_NUM];
        pointVisible = new boolean[ObjectController.POINT_NUM];
        wallXArray = new double[ObjectController.WALL_NUM][4]; // wall1つにつき4点
        wallYArray = new double[ObjectController.WALL_NUM][4];
        wall3DArray = new Wall[ObjectController.WALL_NUM];
        vpEstimator = new VanishingPointEstimator();
        reset();
    }

    private void reset() {
        reset(false, false, false, false);
    }

    void reset(boolean isDrawLeftWall, boolean isDrawFloor, boolean isDrawRightWall, boolean isDrawPrevPoint) {
        vp = new Point2d(WIDTH / 2, HEIGHT / 2);
        vpEstimator.reset();
        isFirst = true;
        this.isDrawLeftWall = isDrawLeftWall;
        this.isDrawFloor = isDrawFloor;
        this.isDrawRightWall = isDrawRightWall;
        this.isDrawPrevPoint = isDrawPrevPoint;
    }

    void calc(ObjectController objectController) {
        Point3[] originPoints = objectController.getPointArray();
        Point2d[] points = objectController.getDrawingPointArray();
        Point2d[] prevPoints = objectController.getPrevDrawingPointArray();
        for (int i = 0; i < ObjectController.POINT_NUM; i++) {
            points3DArray[i] = new Point3(points[i].x, -originPoints[i].z, points[i].y);
            prevPoints3DArray[i] = new Point3(prevPoints[i].x, -originPoints[i].z, prevPoints[i].y);
        }

        Wall[] drawingWall = objectController.getDrawingWallArray();
        for (int i = 0; i < ObjectController.WALL_NUM; i++) {
            wall3DArray[i] = new Wall(drawingWall[i].getP1().x, -drawingWall[i].getP1().z, drawingWall[i].getP1().y,
                    drawingWall[i].getP2().x, -drawingWall[i].getP2().z, drawingWall[i].getP2().y,
                    drawingWall[i].getP3().x, -drawingWall[i].getP3().z, drawingWall[i].getP3().y,
                    drawingWall[i].getP4().x, -drawingWall[i].getP4().z, drawingWall[i].getP4().y);
        }

        // (歩行者-点)と壁の交差処理
        Pedestrian pedestrian = objectController.getPedestrian();
        Wall[] originWall = objectController.getWallArray();
        for (int i = 0; i < ObjectController.POINT_NUM; i++) {
            pointVisible[i] = true;
            for (int k = 0; k < ObjectController.WALL_NUM; k++) {
                if (isCrossLine(pedestrian, originPoints[i], originWall[k])) {
                    pointVisible[i] = false;
                }
            }
        }

//        MatOfPoint3f points3D = new MatOfPoint3f();
//        points3D.fromArray(points3DArray);
//        // 視点の設定
//        float distance = 0; // 奥行き
//        float thetaX = 0, thetaZ = 0; // 回転
//        float thetaY = (float)(Pedestrian.DIR_NORTH + pedestrian.getDirection());
//        double radX = Math.toRadians(thetaX);
//        double radY = Math.toRadians(thetaY);
//        double radZ = Math.toRadians(thetaZ);
//        float sinX = (float)Math.sin(radX);
//        float cosX = (float)Math.cos(radX);
//        float sinY = (float)Math.sin(radY);
//        float cosY = (float)Math.cos(radY);
//        float sinZ = (float)Math.sin(radZ);
//        float cosZ = (float)Math.cos(radZ);
//        Mat rMatX = Mat.zeros(3, 3, CvType.CV_32FC1);
//        rMatX.put(0, 0, 1);
//        rMatX.put(1, 1, cosX);
//        rMatX.put(1, 2, -sinX);
//        rMatX.put(2, 1, sinX);
//        rMatX.put(2, 2, cosX);
//        Mat rMatY = Mat.zeros(3, 3, CvType.CV_32FC1);
//        rMatY.put(0, 0, cosY);
//        rMatY.put(0, 2, sinY);
//        rMatY.put(1, 1, 1);
//        rMatY.put(2, 0, -sinY);
//        rMatY.put(2, 2, cosY);
//        Mat rMatZ = Mat.zeros(3, 3, CvType.CV_32FC1);
//        rMatZ.put(0, 0, cosZ);
//        rMatZ.put(0, 1, -sinZ);
//        rMatZ.put(1, 0, sinZ);
//        rMatZ.put(1, 1, cosZ);
//        rMatZ.put(2, 2, 1);
//        Mat rMat = getMultiMat(getMultiMat(rMatX, rMatY), rMatZ);
//        Mat rvec = Mat.zeros(1, 3, CvType.CV_32FC1);
//        Calib3d.Rodrigues(rMat, rvec);
//
//        Mat tvec = Mat.zeros(1, 3, CvType.CV_32FC1); // 並進
//        tvec.put(0, 2, distance);
//        // カメラ内部パラメータ
//        Mat cameraMatrix = Mat.eye(3, 3, CvType.CV_32FC1); // 浮動小数1チャンネル
//        cameraMatrix.put(0, 0, 640); // 焦点距離
//        cameraMatrix.put(1, 1, 640);
//        cameraMatrix.put(0, 2, 320);
//        cameraMatrix.put(1, 2, 240);
//        MatOfDouble distCoeffs = new MatOfDouble();
//        distCoeffs.fromArray(0, 0, 0, 0, 0); // 歪み=>0
//
//        MatOfPoint2f imagePointsL = new MatOfPoint2f();
//        Calib3d.projectPoints(points3D, rvec, tvec, cameraMatrix, distCoeffs, imagePointsL);
//        System.out.println(imagePointsL.dump());
//        Point[] pp = imagePointsL.toArray();
//        gc.setFill(Color.BLACK);
//        for (int i = 0; i < pp.length; i++) {
//            if (pp[i].x >= 0 && pp[i].x <= 640 && pp[i].y >= 0 && pp[i].y <= 480) {
//                gc.fillOval(pp[i].x - POINT_SIZE / 2, pp[i].y - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE);
//                gc.fillText("P" + (i + 1), pp[i].x - POINT_SIZE, pp[i].y + 20);
//                System.out.println(":::" + pp[i].x + ", " + pp[i].y);
//            }
//        }

//        660.179810; // fx
//        660.198608; // fy
//        326.028229; // cx
//        231.445267; // cy

        // 透視投影変換
        double f = WIDTH, cx = WIDTH / 2.0, cy = HEIGHT / 2.0, scale = 1;
        for (int i = 0; i < ObjectController.POINT_NUM; i++) {
            Point3 p3 = points3DArray[i];
            Point3 prevP3 = prevPoints3DArray[i];
            featurePoints[i] = new Point2d(p3.x / p3.z * f * scale + cx, p3.y / p3.z * f * scale + cy);
            prevFeaturePoints[i] = new Point2d(prevP3.x / prevP3.z * f * scale + cx, prevP3.y / prevP3.z * f * scale + cy);
            zIndex[i] = p3.z * f;
        }

        for (int i = 0; i < ObjectController.WALL_NUM; i++) {
            Wall wall3D = wall3DArray[i];
            for (int k = 0; k < wallXArray[i].length; k++) {
                Point3 p3 = wall3D.getP1();
                if (k == 1) p3 = wall3D.getP2();
                else if (k == 2) p3 = wall3D.getP3();
                else if (k == 3) p3 = wall3D.getP4();
                wallXArray[i][k] = p3.x / p3.z * f * scale + cx;
                wallYArray[i][k] = p3.y / p3.z * f * scale + cy;
            }
        }

        // 透視投影変換
//        int angle = 45; // 視野角
//        double size = ((WIDTH > HEIGHT) ? WIDTH : HEIGHT) * 0.5; // カメラ焦点距離(pixel)
//        double fov = 1 / Math.tan(angle * 0.5 * Math.PI / 180);
//
//        for (int i = 0; i < ObjectController.POINT_NUM; i++) {
//            Point3 p = points3DArray[i];
//            Point3 pp = prevPoints3DArray[i];
//            featurePoints[i] = new Point2d(p.x / p.z * fov * size, p.y / p.z * fov * size);
//            featurePoints[i].x += WIDTH / 2;
//            featurePoints[i].y += HEIGHT / 2;
//
//            prevFeaturePoints[i] = new Point2d(pp.x / pp.z * fov * size, pp.y / pp.z * fov * size);
//            prevFeaturePoints[i].x += WIDTH / 2;
//            prevFeaturePoints[i].y += HEIGHT / 2;
//            System.out.println("P[" + (i+1) + "]: " + featurePoints[i].x + ", " + featurePoints[i].y);
//            zIndex[i] = 10 / points3DArray[i].z * fov * size;
//        }

        // 壁
//        gc.setStroke(Color.BLACK);
//        gc.setFill(Color.GRAY);
//        for (int i = ObjectController.WALL_NUM - 1; i >= 0; i--) {
//            gc.fillPolygon(wallXArray[i], wallYArray[i], 4);
//            gc.strokePolygon(wallXArray[i], wallYArray[i], 4);
//        }

        // 消失点計算
        vp = vpEstimator.getCrossPoint(featurePoints, prevFeaturePoints, zIndex, pointVisible);
        if (isFirst) {
            isFirst = false;
        } else {
            vpEstimator.addVP(vp);
        }
    }

    void draw(ObjectController objectController) {
        gc.setGlobalAlpha(1.0);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasW, canvasH);

        final double wallFar = 27.2 - 0.035;
        // 点
        for (int i = ObjectController.POINT_NUM - 1; i >= 0; i--) {
            Point2d fp = featurePoints[i];
            Point2d prevFP = prevFeaturePoints[i];
            // 画面外の点は描画しない
            if (!vpEstimator.isValidPoint(fp, zIndex[i]) || !pointVisible[i]) continue;

            Color color = CanvasController.COLOR_ARRAY[i % CanvasController.COLOR_ARRAY.length];
            gc.setStroke(color);
            gc.setFill(color);
            boolean isClear = !setAlpha(objectController, i);
            if (!isClear) {
                if (!isDrawPrevPoint) gc.setGlobalAlpha(globalAlpha);
                else gc.setGlobalAlpha(1.0);
            }
            gc.strokeLine(prevFP.x, prevFP.y, fp.x, fp.y); // 特徴点軌跡
            gc.strokeRect(prevFP.x - Util.PROJ_POINT_SIZE / 2, prevFP.y - Util.PROJ_POINT_SIZE / 2,
                    Util.PROJ_POINT_SIZE, Util.PROJ_POINT_SIZE);
            setAlpha(objectController, i);
            if (!vpEstimator.isValidPoint(prevFP, zIndex[i])) {
                double[] xPoints = {fp.x - Util.PROJ_POINT_SIZE / 2, fp.x, fp.x + Util.PROJ_POINT_SIZE / 2};
                double[] yPoints1 = {fp.y, fp.y - Util.PROJ_POINT_SIZE / 2, fp.y};
                double[] yPoints2 = {fp.y, fp.y + Util.PROJ_POINT_SIZE / 2, fp.y};
                gc.fillPolygon(xPoints, yPoints1, 3);
                gc.fillPolygon(xPoints, yPoints2, 3);
            } else {
                Util.drawProjPoint(gc, fp);
            }
//            gc.setFill(Color.BLACK);
//            gc.fillText("P" + (i + 1), fp.x - 20, fp.y + 20);
        }

        gc.setGlobalAlpha(1.0);
        gc.setFill(Color.RED);
        gc.fillRect(vp.x - Util.PROJ_VP_SIZE / 2, vp.y - Util.PROJ_VP_SIZE / 2,
                Util.PROJ_VP_SIZE, Util.PROJ_VP_SIZE);
        // 中心座標描画
        gc.setStroke(Color.BLACK);
        gc.strokeLine(WIDTH / 2, 0, WIDTH / 2, 640);
        gc.strokeLine(0, HEIGHT / 2, WIDTH, HEIGHT / 2);
    }

    private boolean setAlpha(ObjectController oc, int i) {
        gc.setGlobalAlpha(1.0);
        if (oc.isLeftWallPoint(i)) {
            if (!isDrawLeftWall) {
                gc.setGlobalAlpha(globalAlpha);
                return false;
            }
        } else if (oc.isRightWallPoint(i)) {
            if (!isDrawRightWall) {
                gc.setGlobalAlpha(globalAlpha);
                return false;
            }
        } else if (oc.isFloorPoint(i)) {
            if (!isDrawFloor) {
                gc.setGlobalAlpha(globalAlpha);
                return false;
            }
        }
        return true;
    }

    private boolean isCrossLine(Pedestrian pedestrian, Point3 point, Wall wall) {
        return isCrossLine(pedestrian.getX(), pedestrian.getY(), wall.getP1().x, wall.getP1().y,
                point.x, point.y, wall.getP3().x, wall.getP3().y);
    }

    private boolean isCrossLine(double p1StartX, double p1StartY, double p2StartX, double p2StartY,
                                double p1EndX, double p1EndY, double p2EndX, double p2EndY) {
        // 端点がちょうど重なる場合は「交差してない」判定
        double ta = (p2StartX - p2EndX) * (p1StartY - p2StartY) + (p2StartY - p2EndY) * (p2StartX - p1StartX);
        double tb = (p2StartX - p2EndX) * (p1EndY - p2StartY) + (p2StartY - p2EndY) * (p2StartX - p1EndX);
        double tc = (p1StartX - p1EndX) * (p2StartY - p1StartY) + (p1StartY - p1EndY) * (p1StartX - p2StartX);
        double td = (p1StartX - p1EndX) * (p2EndY - p1StartY) + (p1StartY - p1EndY) * (p1StartX - p2EndX);
        return tc * td < 0 && ta * tb < 0;
    }

    /**
     *
     * @param p1StartX 線1始点X
     * @param p1StartY 線1始点Y
     * @param p2StartX 線2始点X
     * @param p2StartY 線2始点Y
     * @param p1EndX 線1終点X
     * @param p1EndY 線1終点Y
     * @param p2EndX 線2終点X
     * @param p2EndY 線2終点Y
     * @return 交点座標
     */
    private Point2d getCrossPoint(double p1StartX, double p1StartY, double p2StartX, double p2StartY,
                                  double p1EndX, double p1EndY, double p2EndX, double p2EndY) {
        double s1 = ((p2EndX - p2StartX) * (p1StartY - p2StartY) - (p2EndY - p2StartY) * (p1StartX - p2StartX)) / 2;
        double s2 = ((p2EndX - p2StartX) * (p2StartY - p1EndY) - (p2EndY - p2StartY) * (p2StartX - p1EndX)) / 2;
        double cx = p1StartX + (p1EndX - p1StartX) * s1 / (s1 + s2);
        double cy = p1StartY + (p1EndY - p1StartY) * s1 / (s1 + s2);
        return new Point2d(cx, cy);
    }

    public Mat getMultiMat(Mat m1, Mat m2) {
        if (m1.cols() != m2.rows()) return null;
        else if (m1.rows() != m2.cols()) return null;

        Mat mm = Mat.eye(m1.rows(), m2.cols(), m1.type());
        for (int i = 0; i < mm.rows(); i++) {
            for (int j = 0; j < mm.cols(); j++) {
                float sum = 0;
                for (int k = 0; k < mm.cols(); k++) {
                    sum += (float) (m1.get(i, k)[0] * m2.get(k, j)[0]);
                }
                mm.put(i, j, sum);
            }
        }
        return mm;
    }

    ArrayList<Point2d> getVpHistory() {
        return vpEstimator.getVpHistory();
    }

    ArrayList<Double> getVpMAHistory() {
        return vpEstimator.getVpMAHistory();
    }

    ArrayList<Double> getVpWAHistory() {
        return vpEstimator.getVpWAHistory();
    }

    void setDrawLeftWall(boolean drawLeftWall) {
        isDrawLeftWall = drawLeftWall;
    }

    void setDrawFloor(boolean drawFloor) {
        isDrawFloor = drawFloor;
    }

    void setDrawRightWall(boolean drawRightWall) {
        isDrawRightWall = drawRightWall;
    }

    void setDrawPrevPoint(boolean drawPrevPoint) {
        isDrawPrevPoint = drawPrevPoint;
    }
}
