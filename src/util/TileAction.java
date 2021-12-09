package util;

public abstract class TileAction {
    public abstract int action(float x, float y, double dx, double dy, int ix, int iy, int px, int py);
    public int[] run(int ROWS, int COLUMNS, double playerx, double playery, double tilesize){
        int returns[] = new int[ROWS*COLUMNS];
        double offsetx = playerx % 1;
		double offsety = playery % 1;

        for (float y = 0; y < ROWS; y+=1.0f) {
			for (float x = 0; x < COLUMNS; x+=1.0f) {

				double dx = x * tilesize-offsetx*tilesize;
				double dy = y * tilesize-offsety*tilesize;

				int ix = (int) (x+offsetx);
				int iy = (int) (y+offsety);
                int px = (int) (x+playerx);
				int py = (int) (y+playery);
                int ret = action(x, y, dx, dy, ix, iy, px, py);
                returns[(int)(y*COLUMNS+x)] = ret;
            }
        }
        return returns;
    }
}

