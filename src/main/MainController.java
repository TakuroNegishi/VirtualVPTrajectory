package main;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import model.Point2d;
import org.opencv.core.Core;
import util.Util;

import java.util.ArrayList;

public class MainController {
    @FXML private TextField distField;
    @FXML private TextField angleField;
    @FXML private Canvas canvas;
    @FXML private Canvas projectionCanvas;
    @FXML Slider scaleSlider;
    @FXML private Label heightMeterLabel;
    @FXML private CheckBox pointCoordinateCB;
    @FXML private TextArea logTextArea;
    @FXML private LineChart vpLineChart;
    private CanvasController canvasController;
    private ProjectionCanvasController projectionCanvasController;
    private ObjectController objectController;

    private double defaultScale;

    @FXML
    public void initialize() {
        // 初期化メソッド
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // OpenCV Load
        canvasController = new CanvasController(canvas);
        objectController = new ObjectController();
        defaultScale = scaleSlider.getValue();
        canvasController.setScale(defaultScale);
        heightMeterLabel.setText("Height: " + defaultScale + "(m)");
        canvasController.draw(objectController);
        logTextArea.setText(objectController + logTextArea.getText());
        distField.setText("1");     // 1m
        angleField.setText("0");    // 0°

        // スライダーイベントリスナー
        scaleSlider.valueProperty().addListener((ObservableValue<? extends Number> observ, Number oldVal, Number newVal)->{
//            double oldnum = oldVal.doubleValue();
            double newNum = newVal.doubleValue();
            canvasController.setScale(newNum);
            canvasController.draw(objectController);
            heightMeterLabel.setText("Height: " + String.format("%.2f", newNum) + "(m)");
        });

        projectionCanvasController = new ProjectionCanvasController(projectionCanvas);
        projectionCanvasController.draw(objectController);
    }

    @FXML
    public void OnMouseMoved(MouseEvent event) {
//        System.out.println(event.getX() + ", " + event.getY());
//        canvasController.drawPointDetail(objectController, event.getX(), event.getY());
        canvasController.draw(objectController, event.getX(), event.getY());
    }

    @FXML
    public void OnNextBtn(ActionEvent event) {
//        System.out.println(distField.getText() + ", " + angleField.getText());
        double dist;
        double angle;
        try {
            dist = Double.parseDouble(distField.getText());
            angle = Double.parseDouble(angleField.getText());
        } catch (NumberFormatException nfe) {
            Util.showMessage("数値を入力してください。");
            return;
        }
        objectController.move(dist, angle);
        canvasController.draw(objectController);
        logTextArea.setText(objectController + logTextArea.getText());
        projectionCanvasController.draw(objectController);
    }

    @FXML
    public void OnSelectedDrawPointCoordinate(ActionEvent event) {
        canvasController.setDrawPointCoordinate(pointCoordinateCB.isSelected());
        canvasController.draw(objectController);
    }

    @FXML
    public void OnHeightReset(ActionEvent event) {
        scaleSlider.setValue(defaultScale);
        canvasController.draw(objectController);
    }

    @FXML
    public void OnResetBtn(ActionEvent event) {
//        System.out.println(distField.getText() + ", " + angleField.getText());
        objectController.reset();
        canvasController.draw(objectController);
        logTextArea.setText(objectController + logTextArea.getText());
        projectionCanvasController.reset();
        projectionCanvasController.draw(objectController);
        vpLineChart.setData(getChartData(projectionCanvasController.getVpHistory()));
    }

    @FXML
    public void OnRouteBtn(ActionEvent event) {

    }

    @FXML
    public void OnRouteNextBtn(ActionEvent event) {
        objectController.moveRoute();
        canvasController.draw(objectController);
        logTextArea.setText(objectController + logTextArea.getText());
        projectionCanvasController.draw(objectController);
        vpLineChart.setData(getChartData(projectionCanvasController.getVpHistory()));
    }

    private ObservableList<Series<String, Double>> getChartData(ArrayList<Point2d> vpHistory) {
        Series<String, Double> series1 = new Series<>();
        series1.setName("消失点 x座標");

        for (int i = 0; i < vpHistory.size(); i++) {
            double vpx = vpHistory.get(i).x;
            series1.getData().add(new XYChart.Data(Integer.toString(i), vpx));
        }

        ObservableList<XYChart.Series<String, Double>> seriesList = FXCollections.observableArrayList();
        seriesList.add(series1);

        return seriesList;
    }
}
