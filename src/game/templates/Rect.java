package game.templates;

public class Rect {
    public double x;
    public double y;
    public double width;
    public double height;
    Rect(double x, double y, double width, double height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public Point[] getPoints(){
        return new Point[]{new Point(x, y), new Point(x+width, y), new Point(x+width, y+height), new Point(x, y+height)};
    }
    Rect(){

    }

}
