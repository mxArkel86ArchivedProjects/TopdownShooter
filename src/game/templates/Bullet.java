package game.templates;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

public class Bullet extends DynamicGameObject {
    public Bullet(){
        width = 12;
        height = 12;
        magnitude = 10.0;
        damage = 20;
    }
    
    public double damage;
    public int player;
    public int texture;
    private double taillength = 30;
    
    @Override
    public
    void paint(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillOval((int) x, (int) y, (int)width, (int)height);
        g.setColor(new Color(90,90,90,210));
        g.setStroke(new BasicStroke(4));
        g.drawLine((int)(x+width/2), (int)(y+height/2), (int)(x+width/2-Math.cos(angle)*taillength), (int)(y+height/2+Math.sin(angle)*taillength));
    }
}
