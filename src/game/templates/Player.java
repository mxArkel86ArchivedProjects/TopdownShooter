package game.templates;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Graphics2D;

public class Player extends Entity {

    public Player(){
        x = 300;
        y = 400;
        width=30;
        height=30;
        health=MAX_HEALTH();
        stamina=MAX_STAMINA();
    }

    final Color PLAYER_COLOR = Color.ORANGE;
    public final double WALL_FRICTION_CONST = 0.6;
    final int ENTITY_BORDER_WEIGHT = 4;
    final double HAND_DISTANCE = 25;
    public final int SPRINT_REGEN_DELAY = 120;
    public int regen_delay_time; 
    final double HAND_SIZE = 10;
    public double look_angle =0;
    
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
        
        double angle1 = look_angle+Math.PI/8;
        double x1 = centerx()+Math.cos(angle1)*HAND_DISTANCE;
        double y1 = centery()+Math.sin(angle1)*HAND_DISTANCE;
        drawCircle(g, x1-HAND_SIZE/2, y1-HAND_SIZE/2, HAND_SIZE, HAND_SIZE, 
        PLAYER_COLOR, ENTITY_BORDER_WEIGHT/4);

        double angle2 = look_angle-Math.PI/8;
        double x2 = centerx()+Math.cos(angle2)*HAND_DISTANCE;
        double y2 = centery()+Math.sin(angle2)*HAND_DISTANCE;
        drawCircle(g, x2-HAND_SIZE/2, y2-HAND_SIZE/2, HAND_SIZE, HAND_SIZE, 
        PLAYER_COLOR, ENTITY_BORDER_WEIGHT/4);
    }

    public double speed(){
        return magnitude;
    }
    @Override
    public double MAX_HEALTH() {
        return 100;
    }
    @Override
    public double MAX_STAMINA() {
        return 100;
    }
    @Override
    public double BASE_SPEED() {
        return 1.0;
    }
    @Override
    public double SPRINT_SPEED_MULT() {
        return 1.4;
    }
}
