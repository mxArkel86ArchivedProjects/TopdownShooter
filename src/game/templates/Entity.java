package game.templates;

public abstract class Entity extends DynamicGameObject {
    public double health;
    public double stamina;
    public abstract double MAX_HEALTH();
    public abstract double MAX_STAMINA();
    public abstract double BASE_SPEED();
    public abstract double SPRINT_SPEED_MULT();
}
