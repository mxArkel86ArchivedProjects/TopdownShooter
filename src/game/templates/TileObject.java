package game.templates;

import java.awt.Graphics2D;

public abstract class TileObject {
    public double lx;
    public double ly;
    public double lwidth;
    public double lheight;
    public int asset;

    public abstract void paint(Graphics2D g, double dx, double dy, double gridsize);
}
