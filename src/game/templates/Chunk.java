package game.templates;

public class Chunk {
    public Tile [][] tiles;
    public int left;
    public int top;
    public int right;
    public int bottom;
    public Chunk(int left, int top, int width, int height){
        this.left = left;
        this.top = top;
        this.right = left+width;
        this.bottom = top+height;
        tiles = new Tile[width][height];
    }
}
