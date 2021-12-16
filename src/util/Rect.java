package util;

public class Rect {
    public double x;
    public double y;
    public double width;
    public double height;
    public Rect(double x, double y, double width, double height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public Point[] getPoints(){
        return new Point[]{new Point(x, y), new Point(x+width, y), new Point(x+width, y+height), new Point(x, y+height)};
    }
    public Point[] getObjectPoints(){
        return new Point[]{new Point(0,0), new Point(width, 0), new Point(width, height), new Point(0, height)};
    }
    public Rect(){

    }
    public double left(){
        return x;
    }
    public double top(){
        return y;
    }
    public double right(){
        return x+width;
    }

    public double bottom(){
        return y+height;
    }

}
