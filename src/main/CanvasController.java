package main;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Pedestrian;
import model.Point2d;
import model.WallLine;
import org.opencv.core.Point3;

public class CanvasController {
    private static final double NORMAL_STROKE = 1.0f;   // 通常の線の太さ
    private static final double WALL_STROKE = 5.0f;     // 壁の線太さ
    private static final double EYE_STROKE = 10.0f;     // 注視線太さ
    private static final int POINT_SIZE = 20;            // 点の大きさ
    public static final Color[] COLOR_ARRAY =
            { Color.RED, Color.BLUE, Color.GREEN, Color.PINK,
                    Color.CYAN, Color.YELLOW, Color.ORANGE };

    private double heightMeter; //(m) 縦表示メートル
    private double widthMeter;             //(m) 横表示メートル
    private double baseYMeter; //(m) 歩行者表示位置(縦)
    private double canvasW; // キャンバス幅
    private double canvasH; // キャンバス高さ
    private double baseX;   // (0,0) x描画位置
    private double baseY;   // (0,0) y描画位置
    private GraphicsContext gc;

    private boolean isDrawPointCoordinate;

    public CanvasController(Canvas canvas) {
        gc = canvas.getGraphicsContext2D();
        canvasW = canvas.getWidth();
        canvasH = canvas.getHeight();
        baseX = canvasW / 2;
    }

    public void setScale(double scale) {
        heightMeter = scale;
        baseYMeter = heightMeter * 0.8;
        baseY = canvasH * baseYMeter / heightMeter;
        widthMeter = heightMeter * canvasW / canvasH;
    }

    public void draw(ObjectController objectController) {
        this.draw(objectController, -1, -1);
    }

    public void draw(ObjectController objectController, double mouseX, double mouseY) {
        // 背景
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasW, canvasH);

        // 基準線
//        gc.setLineWidth(NORMAL_STROKE);
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.strokeLine(baseX, 0, baseX, canvasH);
        gc.strokeLine(0, baseY, canvasW, baseY);

        // 壁
        gc.setLineWidth(WALL_STROKE);
        WallLine[] walls = objectController.getDrawingWallArray();
        for (int i = 0; i < ObjectController.WALL_LINE_NUM; i++) {
            WallLine wallLine = walls[i];
            gc.strokeLine(getDrawX(wallLine.start.x), getDrawY(wallLine.start.y),
                    getDrawX(wallLine.end.x), getDrawY(wallLine.end.y));
        }

        // 点群
        gc.setLineWidth(NORMAL_STROKE);
        Point2d[] points = objectController.getDrawingPointArray();
        Point2d[] prevPoints = objectController.getPrevDrawingPointArray();
        for (int i = 0; i < ObjectController.POINT_NUM; i++) {
            gc.setStroke(COLOR_ARRAY[i % COLOR_ARRAY.length]);
            gc.setFill(COLOR_ARRAY[i % COLOR_ARRAY.length]);
            Point2d prevP = prevPoints[i];
            Point2d p = points[i];
            double prevPX = getDrawX(prevP.x);
            double prevPY = getDrawY(prevP.y);
            double px = getDrawX(p.x);
            double py = getDrawY(p.y);
            // 移動軌跡
            gc.strokeLine(prevPX, prevPY, px, py);
            // 過去位置
            gc.strokeRect(prevPX - POINT_SIZE / 2, prevPY - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE);
            // 現在位置
            drawPoint(px, py);
        }
        // 点群文字
        Point3[] originPoints = objectController.getPointArray();
        for (int i = 0; i < ObjectController.POINT_NUM; i++) {
            Point3 p = originPoints[i];
            Point2d drawP = points[i];
            double drawX = getDrawX(drawP.x);
            double drawY = getDrawY(drawP.y);
            String pointStr = "P" + (i + 1);
            if (isDrawPointCoordinate) {
                pointStr += " (" + String.format("%.2f", p.x) + ", " + String.format("%.2f", p.y) + ")";
            }
            drawPointInfo(pointStr, drawX - 30, drawY + 15);
            // マウスオーバー位置にある点情報を描画
            if (mouseX == -1 && mouseY == -1) continue;;
            if (drawX - 10 <= mouseX && mouseX <= drawX + 10 &&
                    drawY - 10 <= mouseY && mouseY <= drawY + 10) {
                String detailStr = "P" + (i + 1) + " (" + String.format("%.2f", p.x) + ", " + String.format("%.2f", p.y) + ")";
                drawDetail(detailStr, drawX + POINT_SIZE, drawY - POINT_SIZE);
            }
        }

        Pedestrian pedestrian = objectController.getPedestrian();
        // 注視線
//        gc.setLineWidth(EYE_STROKE);
//        gc.setStroke(Color.RED);
//        double eyeDrawX = getDrawX(pedestrian.getINIT_EYE_X());
//        double eyeDrawY = getDrawY(pedestrian.getINIT_EYE_Y());
//        gc.strokeLine(baseX, baseY, eyeDrawX, eyeDrawY);
//        gc.setLineWidth(NORMAL_STROKE);
        // 歩行者
        double xPoints[] = {baseX, baseX - 10, baseX + 10};
        double yPoints[] = {baseY - 20, baseY + 10, baseY + 10};
        gc.fillPolygon(xPoints, yPoints, 3);
        // 注視点
//        gc.setFill(Color.RED);
//        drawPoint(eyeDrawX, eyeDrawY); // 注視点
//        drawPedestrianInfo("(" + pedestrian + ")", baseX - 105, baseY + 30);
        // 視野範囲
//        gc.strokeLine(baseX, baseY, baseX + 500, 0);
//        gc.strokeLine(baseX, baseY, baseX - 500, 0);
    }

    private double getDrawX(double xm) {
//        x(m):xPX = WIDTH_METER:canvasW
        return baseX + xm * canvasW / widthMeter;
    }

    private double getDrawY(double ym) {
        return baseY - ym * canvasH / heightMeter;
    }

    private void drawPoint(double x, double y) {
        gc.fillOval(x - POINT_SIZE / 2, y - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE);
    }

    private void drawPedestrianInfo(String infoStr, double x, double y) {
//        String[] strArray = infoStr.split("\n");
//        int maxLength = 0;
//        for (int i = 0; i < strArray.length; i++) {
//            if (maxLength < strArray[i].length())
//                maxLength = strArray[i].length();
//        }
        gc.setFill(Color.WHITE);
        gc.setGlobalAlpha(0.95);
        gc.fillRect(x, y, infoStr.length() * 10, 30);
        gc.setGlobalAlpha(1.0);
        gc.setFill(Color.BLACK);
        gc.fillText(infoStr, x + 10, y + 23);
    }

    private void drawPointInfo(String infoStr, double x, double y) {
        gc.setFill(Color.WHITE);
        gc.setGlobalAlpha(0.95);
        gc.fillRect(x, y, infoStr.length() * 20, 30);
        gc.setGlobalAlpha(1.0);
        gc.setFill(Color.BLACK);
        gc.fillText(infoStr, x + 10, y + 23);
    }

    private void drawDetail(String infoStr, double x, double y) {
        drawPedestrianInfo(infoStr, x, y);
        gc.setStroke(Color.RED);
        gc.strokeRect(x, y, infoStr.length() * 10, 30);
//        gc.setStroke(Color.BLACK);
    }

//    public boolean isDrawPointCoordinate() {
//        return isDrawPointCoordinate;
//    }

    public void setDrawPointCoordinate(boolean drawPointCoordinate) {
        isDrawPointCoordinate = drawPointCoordinate;
    }

}
