package game.templates;

import java.awt.geom.Point2D;

public class Point {
    public double x;
    public double y;
    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }
    public Point() {
    }
    public static Point2D toPoint2D(Point p1){
        Point2D p = new Point2D.Double(p1.x, p1.y);
        return p;
    }
    public static Point toPoint2D(Point2D p1){
        Point p = new Point(p1.getX(), p1.getY());
        return p;
    }
    public Point2D toPoint2D(){
        Point2D p = new Point2D.Double(x, y);
        return p;
    }
    public static Point2D[] toPoints(Point in[]){
        Point2D list[] = new Point2D[in.length];
        for(int i =0;i<in.length;i++){
            list[i] = toPoint2D(in[i]);
        }
        return list;
    }
    public static Point[] toPoints(Point2D in[]){
        Point list[] = new Point[in.length];
        for(int i =0;i<in.length;i++){
            list[i] = toPoint2D(in[i]);
        }
        return list;
    }
}
