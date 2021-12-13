package game.templates;

import java.awt.Color;
import java.awt.Graphics2D;

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
    
    @Override
    public
    void paint(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillOval((int) x, (int) y, (int)width, (int)height);
    }
}
