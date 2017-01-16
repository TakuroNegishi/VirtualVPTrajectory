package main;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import model.MovingAverageFilter;
import model.Point2d;
import model.WeightedAverageFilter;
import util.Util;

import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings({"unchecked", "unused", "SpellCheckingInspection"})
public class MainController {
    @FXML private TextField distField;
    @FXML private TextField angleField;
    @FXML private Canvas canvas;
    @FXML private Canvas projectionCanvas;
    @FXML Slider scaleSlider;
    @FXML private Label heightMeterLabel;
    @FXML private CheckBox pointCoordinateCB;
    @FXML private CheckBox leftWallCB;
    @FXML private CheckBox floorCB;
    @FXML private CheckBox rightWallCB;
    @FXML private CheckBox drawPrevProjCB;
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
        distField.setText("0.6");     // 1m
        angleField.setText("0");    // 0°

        // スライダーイベントリスナー
        scaleSlider.valueProperty().addListener((ObservableValue<? extends Number> observe, Number oldVal, Number newVal)->{
//            double oldnum = oldVal.doubleValue();
            double newNum = newVal.doubleValue();
            canvasController.setScale(newNum);
            canvasController.draw(objectController);
            heightMeterLabel.setText("Height: " + String.format("%.2f", newNum) + "(m)");
        });

        projectionCanvasController = new ProjectionCanvasController(projectionCanvas);
        projectionCanvasController.calc(objectController);
        projectionCanvasController.draw(objectController);
        vpLineChart.setCreateSymbols(false);
        vpLineChart.setAnimated(false);
    }

//    public void addHorizontalValueMarker(XYChart.Data<String, Double> marker, ObservableList<XYChart.Series<String, Double>> seriesList) {
//        Objects.requireNonNull(marker, "the marker must not be null");
//        if (seriesList.contains(marker)) {
//            return;
//        }
//        Line line = new Line();
////        line.setEndX();
////        line.setStyle("-fx-stroke:green;-fx-stroke-width:1px;");
//        marker.setNode(line);
////        getPlotChildren().add(line);
//        seriesList.add(marker);
//    }

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
        projectionCanvasController.calc(objectController);
        projectionCanvasController.draw(objectController);
    }

    @FXML
    public void OnSelectedDrawPointCoordinate(ActionEvent event) {
        canvasController.setDrawPointCoordinate(pointCoordinateCB.isSelected());
        canvasController.draw(objectController);
    }

    @FXML
    public void OnSelectedLeftWall(ActionEvent event) {
        projectionCanvasController.setDrawLeftWall(leftWallCB.isSelected());
        projectionCanvasController.draw(objectController);
    }

    @FXML
    public void OnSelectedFloor(ActionEvent event) {
        projectionCanvasController.setDrawFloor(floorCB.isSelected());
        projectionCanvasController.draw(objectController);
    }

    @FXML
    public void OnSelectedRightWall(ActionEvent event) {
        projectionCanvasController.setDrawRightWall(rightWallCB.isSelected());
        projectionCanvasController.draw(objectController);
    }

    @FXML
    public void OnSelectedPrevProj(ActionEvent event) {
        projectionCanvasController.setDrawPrevPoint(drawPrevProjCB.isSelected());
        projectionCanvasController.draw(objectController);
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
        projectionCanvasController.reset(leftWallCB.isSelected(), floorCB.isSelected(), rightWallCB.isSelected(), drawPrevProjCB.isSelected());
        projectionCanvasController.calc(objectController);
        projectionCanvasController.draw(objectController);
        vpLineChart.setData(getChartData());
    }

    @FXML
    public void OnRouteNextBtn(ActionEvent event) {
//        for (int i = 1; i <= 52; i++) {
            objectController.moveRoute();
            canvasController.draw(objectController);
            logTextArea.setText(objectController + logTextArea.getText());
            projectionCanvasController.calc(objectController);
            projectionCanvasController.draw(objectController);
            vpLineChart.setData(getChartData());
//        }
    }

    private ObservableList<Series<String, Double>> getChartData() {
        ArrayList<Point2d> vpHistory = projectionCanvasController.getVpHistory();
        ArrayList<Double> vpMAHistory = projectionCanvasController.getVpMAHistory();
        ArrayList<Double> vpWAHistory = projectionCanvasController.getVpWAHistory();

        Series<String, Double> series1 = new Series<>();
        Series<String, Double> series2 = new Series<>();
        Series<String, Double> series3 = new Series<>();
        Series<String, Double> series4 = new Series<>();
        Series<String, Double> series5 = new Series<>();
        series1.setName("320");
        series2.setName("640");
        series3.setName("origin");
        series4.setName("移動平均:size=" + MovingAverageFilter.WIN_SIZE);
        series5.setName("加重平均:α=" + WeightedAverageFilter.FILTER_WEIGHT);

        for (int i = 0; i < vpHistory.size(); i++) {
            series1.getData().add(new XYChart.Data<>(Integer.toString(i), 320.0));
        }
        for (int i = 0; i < vpHistory.size(); i++) {
            series2.getData().add(new XYChart.Data<>(Integer.toString(i), 640.0));
        }
        for (int i = 0; i < vpHistory.size(); i++) {
            double vpx = vpHistory.get(i).x;
            series3.getData().add(new XYChart.Data<>(Integer.toString(i), vpx));
        }
        for (int i = 0; i < vpMAHistory.size(); i++) {
            double vpx = vpMAHistory.get(i);
            series4.getData().add(new XYChart.Data<>(Integer.toString(i), vpx));
        }
        for (int i = 0; i < vpWAHistory.size(); i++) {
            double vpx = vpWAHistory.get(i);
            series5.getData().add(new XYChart.Data<>(Integer.toString(i), vpx));
        }

        ObservableList<XYChart.Series<String, Double>> seriesList = FXCollections.observableArrayList();
        seriesList.addAll(series1, series2, series3, series4, series5);

        return seriesList;
    }
}
