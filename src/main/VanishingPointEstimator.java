package main;

import model.MovingAverageFilter;
import model.Point2d;
import model.WeightedAverageFilter;

import java.util.ArrayList;

@SuppressWarnings("SpellCheckingInspection")
class VanishingPointEstimator {
    private ArrayList<Point2d> vpHistory;
    private ArrayList<Double> vpMAHistory;
    private ArrayList<Double> vpWAHistory;
    private MovingAverageFilter maFilter;
    private WeightedAverageFilter waFilter;

    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;
    private static final int ERROR_VP = -999;

    VanishingPointEstimator() {
        vpHistory = new ArrayList<>();
        vpMAHistory = new ArrayList<>();
        vpWAHistory = new ArrayList<>();
        maFilter = new MovingAverageFilter();
        waFilter = new WeightedAverageFilter();
    }

    void reset() {
        vpHistory.clear();
        vpMAHistory.clear();
        vpWAHistory.clear();
        maFilter.reset();
        waFilter.reset();
    }

    void addVP(Point2d vp) {
        vpHistory.add(vp);
        double ma = maFilter.update(vp.x);
        vpMAHistory.add(ma);
        double wa = waFilter.update(vp.x);
        vpWAHistory.add(wa);
        System.out.println(vp.x + "," + ma + "," + wa);
    }

    /**
     * 消失点推定
     * @param featurePoints 特徴点
     * @param prevFeaturePoints 1フレーム前の特徴点
     * @param zIndex 深さインデックス
     * @param pointVisible 壁に隠れている点かフラグ
     * @return 推定された消失点の座標 */
    Point2d getCrossPoint(Point2d[] featurePoints, Point2d[] prevFeaturePoints, double[] zIndex, boolean[] pointVisible) {
        float a = 0;
        float b = 0;
        float p = 0;
        float c = 0;
        float d = 0;
        float q = 0;
        float bunbo;
        for (int i = 0; i < ObjectController.POINT_NUM; ++i) {
            Point2d p1 = featurePoints[i];
            Point2d p2 = prevFeaturePoints[i];
            if (!isValidPoint(p1, zIndex[i])) continue;
            if (!isValidPoint(p2, zIndex[i])) continue;
            if (!pointVisible[i]) continue;
//            System.out.print("P" + (i + 1) + ": " + p1.x + ", " + p1.y);
            // 連立方程式公式 - https://t-sv.sakura.ne.jp/text/num_ana/ren_eq22/ren_eq22.html
            //		sumX += 2*X * (p1.y - p2.y) * (p1.y - p2.y) + 2*Y * (p2.x - p1.x) * (p1.y - p2.y)
            //				+ 2 * (p1.x * p2.y - p2.x * p1.y) * (p1.y - p2.y); // = 0 偏微分X
            //		sumY += 2*Y * (p2.x - p1.x) * (p2.x - p1.x) + 2*X * (p2.x - p1.x) * (p1.y - p2.y)
            //				+ 2 * (p1.x * p2.y - p2.x * p1.y) * (p2.x - p1.x); // = 0 偏微分Y

            a += (p1.y - p2.y) * (p1.y - p2.y);
            b += (p2.x - p1.x) * (p1.y - p2.y);
            p += (p1.x * p2.y - p2.x * p1.y) * (p1.y - p2.y);
            c += (p2.x - p1.x) * (p1.y - p2.y);
            d += (p2.x - p1.x) * (p2.x - p1.x);
            q += (p1.x * p2.y - p2.x * p1.y) * (p2.x - p1.x);
        }
        p *= -1;
        q *= -1;
        bunbo = ((a * d) - (b * c));
        if (bunbo == 0) return new Point2d(ERROR_VP, ERROR_VP);
        float X = (d * p - b * q) / bunbo;
        float Y = (a * q - c * p) / bunbo;
        return new Point2d(X, Y);
    }

    boolean isValidPoint(Point2d p, double zIndex) {
        if (zIndex <= 0 || zIndex == Double.NEGATIVE_INFINITY || zIndex == Double.POSITIVE_INFINITY || zIndex == Double.NaN)
            return false;
        else if (p.x < 0 || p.x > WIDTH || p.y < 0 || p.y > HEIGHT) return false;
        else if (p.x == Double.NEGATIVE_INFINITY || p.x == Double.POSITIVE_INFINITY
                || p.y == Double.NEGATIVE_INFINITY || p.y == Double.POSITIVE_INFINITY) {
            return false;
        }
        return true;
    }

    ArrayList<Point2d> getVpHistory() {
        return vpHistory;
    }

    ArrayList<Double> getVpMAHistory() {
        return vpMAHistory;
    }

    ArrayList<Double> getVpWAHistory() { return vpWAHistory; }
}
