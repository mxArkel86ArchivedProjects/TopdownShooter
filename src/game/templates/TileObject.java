package game.templates;

import java.awt.Graphics2D;

public abstract class TileObject {
    public TileObject(double lx, double ly, double lwidth, double lheight, int asset, boolean collider){
        this.lx = lx;
        this.ly = ly;
        this.lwidth = lwidth;
        this.lheight = lheight;
        this.asset = asset;
        this.collider = collider;
    }
    public double lx;
    public double ly;
    public double lwidth;
    public double lheight;
    public int asset;
    public boolean collider;

    public GameObject toGameObject(double dx, double dy, double tilesize){
        GameObject obj = new GameObject() {

            @Override
            public void paint(Graphics2D g) {
                // TODO Auto-generated method stub
                
            }
            
        };
        obj.x = dx+lx*tilesize;
        obj.y = dy+ly*tilesize;
        obj.width = lwidth*tilesize;
        obj.height = lheight*tilesize;
        return obj;
    }

    public abstract void paint(Graphics2D g, double dx, double dy, double gridsize);
}
