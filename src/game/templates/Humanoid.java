package game.templates;

import java.util.ArrayList;
import java.util.List;

public abstract class Humanoid extends Entity {
    public Inventory inventory;
    Humanoid(int maxsize){
        inventory = new Inventory(maxsize);
    }
    
}
