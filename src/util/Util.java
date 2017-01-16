package util;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.Point2d;

import java.io.*;
import java.util.ArrayList;

public class Util {
    public static final int PROJ_POINT_SIZE = 16;
    public static final int PROJ_VP_SIZE = 22;
    public static final int MAP_POINT_SIZE = 20; // 点の大きさ
    public static final double WALL_POINT_LEFT_X = -1.015;
    public static final double WALL_POINT_RIGHT_X = 1.015;
    public static final double EYE_POINT_Y = 4.0;

    private Util(){}

    public static String dToS(double d) {
        return String.format("%.2f", d);
    }

    public static void drawMapPoint(GraphicsContext gc, double x, double y) {
        drawPoint(gc, x, y, MAP_POINT_SIZE);
    }

    public static void drawProjPoint(GraphicsContext gc, Point2d p) {
        drawPoint(gc, p.x, p.y, PROJ_POINT_SIZE);
    }

    private static void drawPoint(GraphicsContext gc, double x, double y, int pointSize) {
        gc.fillOval(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize);
    }

    public static ArrayList<String> read(String filePath) {
        ArrayList<String> dataList = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(filePath)), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(""))
                    continue;
                dataList.add(line);
            }
            reader.close();
        } catch (FileNotFoundException fnfe) {
            showMessage("指定されたファイル\n[" + filePath + "]\nが見つかりません.");
            return null;
        } catch (IOException ioe) {
            showMessage("ファイルの読み込みに失敗しました.");
            return null;
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                showMessage("ファイルクローズ処理に失敗しました.\n" +
                        e.getLocalizedMessage());
            }
        }
        return dataList;
    }

    public static void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Info");
        alert.showAndWait();
    }
}
