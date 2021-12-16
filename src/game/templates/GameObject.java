package game.templates;

import java.awt.Graphics2D;

import util.Rect;

public abstract class GameObject extends Rect {
    public int asset;

    public double left(){
        return x;
    }
    public double right(){
        return x+width;
    }
    public double bottom(){
        return y+height;
    }
    public double top(){
        return y;
    }
    public double centerx(){
        return x+width/2;
    }
    public double centery(){
        return y+height/2;
    }
    public abstract void paint(Graphics2D g);
}
