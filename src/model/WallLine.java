package model;

public class WallLine {
    public Point2d start;
    public Point2d end;

    /**
     * コンストラクタ(Pint2dがディープコピーされる)</br>
     * 値は同じだけど、別のインスタンス
     * @param start 線の端点
     * @param end 線のもう一方の端点
     */
    public WallLine(Point2d start, Point2d end) {
        this(start.x, start.y, end.x, end.y);
    }

    public WallLine(double startX, double startY, double endX, double endY) {
        this.start = new Point2d(startX, startY);
        this.end = new Point2d(endX, endY);
    }

    public void makeSameValue(WallLine wall) {
        start.makeSameValue(wall.start);
        end.makeSameValue(wall.end);
    }
}
