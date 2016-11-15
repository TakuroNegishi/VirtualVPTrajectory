package util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import main.MainController;
import model.Point2d;

import java.io.*;
import java.util.ArrayList;

public class Util {
    private Util(){};

    public static String dToS(double d) {
        return String.format("%.2f", d);
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
                reader.close();
            } catch (IOException e) {
                showMessage("ファイルクローズ処理に失敗しました.\n" +
                        e.getLocalizedMessage());
                return null;
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
