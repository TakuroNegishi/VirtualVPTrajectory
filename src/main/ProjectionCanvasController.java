package main;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Pedestrian;
import model.Point2d;

import org.opencv.core.*;
import org.opencv.calib3d.Calib3d;

import java.util.ArrayList;

public class ProjectionCanvasController {
    private static final int POINT_SIZE = 7;
    private GraphicsContext gc;
    private double canvasW;
    private double canvasH;

    private Point2d[] prevFeaturePoints;
    private Point2d[] featurePoints;
    private Point3[] points3DArray;
    private Point3[] prevPoints3DArray;
    private double[] zIndex;
    private VanishingPointEstimator vpEstimator;
    private boolean isFirst;

    private static final int WIDTH = 640; // 解像度:幅
    private static final int HEIGHT = 480;

    public ProjectionCanvasController(Canvas canvas) {
        gc = canvas.getGraphicsContext2D();
        canvasW = canvas.getWidth();
        canvasH = canvas.getHeight();
        prevFeaturePoints = new Point2d[ObjectController.POINT_NUM];
        featurePoints = new Point2d[ObjectController.POINT_NUM];
        points3DArray = new Point3[ObjectController.POINT_NUM];
        prevPoints3DArray = new Point3[ObjectController.POINT_NUM];
        zIndex = new double[ObjectController.POINT_NUM];
        vpEstimator = new VanishingPointEstimator();
        reset();
    }

    public void reset() {
        vpEstimator.reset();
        isFirst = true;
    }

    public void draw(ObjectController objectController) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasW, canvasH);

//        Pedestrian pedestrian = objectController.getPedestrian();
        Point3[] originPoints = objectController.getPointArray();
        Point2d[] points = objectController.getDrawingPointArray();
        Point2d[] prevPoints = objectController.getPrevDrawingPointArray();
        for (int i = 0; i < ObjectController.POINT_NUM; i++) {
            points3DArray[i] = new Point3(points[i].x, -originPoints[i].z, points[i].y);
            prevPoints3DArray[i] = new Point3(prevPoints[i].x, -originPoints[i].z, prevPoints[i].y);
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

        int angle = 45; // 視野角
        double size = ((WIDTH > HEIGHT) ? WIDTH : HEIGHT) * 0.5; // カメラ焦点距離(pixel)
        double fov = 1 / Math.tan(angle * 0.5 * Math.PI / 180);
//        double fov = 5; // near平面までの距離?
//        System.out.println("size: " + size);
//        System.out.println("fov: " + fov);

//        Point2d[] point2DArray = new Point2d[ObjectController.POINT_NUM];
        for (int i = 0; i < ObjectController.POINT_NUM; i++) {
            Point3 p = points3DArray[i];
            Point3 pp = prevPoints3DArray[i];
            featurePoints[i] = new Point2d(p.x / p.z * fov * size, p.y / p.z * fov * size);
            featurePoints[i].x += WIDTH / 2;
            featurePoints[i].y += HEIGHT / 2;

            prevFeaturePoints[i] = new Point2d(pp.x / pp.z * fov * size, pp.y / pp.z * fov * size);
            prevFeaturePoints[i].x += WIDTH / 2;
            prevFeaturePoints[i].y += HEIGHT / 2;
            System.out.println("P[" + (i+1) + "]: " + featurePoints[i].x + ", " + featurePoints[i].y);
        }

        for (int i = 0; i < ObjectController.POINT_NUM; i++) {
            zIndex[i] = 10 / points3DArray[i].z * fov * size;
            Point2d fp = featurePoints[i];
            Point2d prevFP = prevFeaturePoints[i];
            if (!vpEstimator.isValidPoint(fp, zIndex[i])) continue;

            Color color = CanvasController.COLOR_ARRAY[i % CanvasController.COLOR_ARRAY.length];
            gc.setStroke(color);
            gc.setFill(color);
            gc.strokeRect(prevFP.x - POINT_SIZE / 2, prevFP.y - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE);
            gc.fillOval(fp.x - POINT_SIZE / 2, fp.y - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE);
            gc.strokeLine(prevFP.x, prevFP.y, fp.x, fp.y); // 特徴点軌跡
            gc.setFill(Color.BLACK);
            gc.fillText("P" + (i + 1), fp.x - POINT_SIZE, fp.y + 20);
        }

        Point2d vp = vpEstimator.getCrossPoint(featurePoints, prevFeaturePoints, zIndex);
        gc.setFill(Color.RED);
//        gc.fillOval(vp.x - POINT_SIZE, vp.y - POINT_SIZE, POINT_SIZE * 2, POINT_SIZE * 2);
        double[] xPoints = {vp.x, vp.x - 10, vp.x + 10};
        double[] yPoints = {vp.y - 10, vp.y + 10, vp.y + 10};
        gc.fillPolygon(xPoints, yPoints, xPoints.length);
        System.out.println("VP: " + vp.x + ", " + vp.y);
        if (isFirst) {
            isFirst = false;
        } else {
            vpEstimator.addVP(vp);
        }
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

    public ArrayList<Point2d> getVpHistory() {
        return vpEstimator.getVpHistory();
    }
}
