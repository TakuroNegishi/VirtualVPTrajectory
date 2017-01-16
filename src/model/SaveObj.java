package model;

import org.opencv.core.Point3;

import java.util.ArrayList;

public class SaveObj {
    ArrayList<Point2d> pedestrianPositions;         // 歩行者の位置履歴
    ArrayList<Double> pedestrianAngle;              // 歩行者の向き履歴
    ArrayList<Point3> points;                       // 点群の三次元座標
    ArrayList<ArrayList<Point2d>> drawingPoints;    // 描画用点群の二次元座標
    ArrayList<ArrayList<Point2d>> drawingPrevPoints;// 1F前の     〃
    ArrayList<ArrayList<Point2d>> projPoints;       // 透視投影後の点群の二次元座標
    ArrayList<ArrayList<Point2d>> projPrevPoints;   // 透視投影後の1F前の   〃
    ArrayList<Point2d> vpHistory;   // 消失点の履歴
    ArrayList<Double> vpWAHistory;  // 加重平均フィルター適応後の消失点の履歴
    ArrayList<Double> vpMAHistory;  // 移動           〃
}
