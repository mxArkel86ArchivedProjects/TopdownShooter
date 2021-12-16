package game.templates;

import java.util.ArrayList;
import java.util.List;
import java.awt.Graphics2D;

import game.managers.AssetManager;

public class Tile {
	public Tile() {

	}

	public Tile(AssetManager amgr, String floorname) {
		floor = amgr.assetID(floorname);
	}

	public int floor = -1;
	public List<TileObject> tileObjects = new ArrayList<TileObject>();

	public void addTileObject(TileObject tile) {
		tileObjects.add(tile);
	}

	public void DrawTileObjects(Graphics2D g, double dx, double dy, double tilesize) {
		for (TileObject o : tileObjects) {
			o.paint(g, dx, dy, tilesize);
		}
	}
}
