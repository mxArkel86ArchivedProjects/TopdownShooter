package game.templates;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Graphics2D;

public class Player extends DynamicGameObject {
    public Player(){
        x = 300;
        y = 400;
        width=30;
        height=30;
        magnitude = 0.8;
    }

    final Color PLAYER_COLOR = Color.ORANGE;
    final int ENTITY_BORDER_WEIGHT = 4;
    public final float SPRINT_MULT = 2f;
    final double HAND_DISTANCE = 25;
    final double HAND_SIZE = 10;
    
    void drawCircle(Graphics2D g, double x, double y, double w, double h, Color c, double weight){
        g.setColor(c);
		g.fillOval((int) x, (int) y, (int)w, (int)h);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke((int)weight));
		g.drawOval((int) x , (int) y, (int)w, (int)h);
    }
    @Override
    public void paint(Graphics2D g) {
        drawCircle(g, x, y, width, height, PLAYER_COLOR, ENTITY_BORDER_WEIGHT);
        
        double angle1 = angle+Math.PI/8;
        double x1 = centerx()+Math.cos(angle1)*HAND_DISTANCE;
        double y1 = centery()+Math.sin(angle1)*HAND_DISTANCE;
        drawCircle(g, x1-HAND_SIZE/2, y1-HAND_SIZE/2, HAND_SIZE, HAND_SIZE, 
        PLAYER_COLOR, ENTITY_BORDER_WEIGHT/4);

        double angle2 = angle-Math.PI/8;
        double x2 = centerx()+Math.cos(angle2)*HAND_DISTANCE;
        double y2 = centery()+Math.sin(angle2)*HAND_DISTANCE;
        drawCircle(g, x2-HAND_SIZE/2, y2-HAND_SIZE/2, HAND_SIZE, HAND_SIZE, 
        PLAYER_COLOR, ENTITY_BORDER_WEIGHT/4);
    }

    public double speed(){
        return magnitude;
    }
}
