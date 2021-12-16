package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.BasicStroke;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Font;

import game.managers.AssetManager;
import game.managers.ItemManager;
import game.managers.Peripherals;
import game.templates.Bullet;
import game.templates.Chunk;
import game.templates.DynamicGameObject;
import game.templates.GameObject;
import game.templates.Item;
import game.templates.Player;
import game.templates.Stack;
import game.templates.Tile;
import game.templates.TileObject;
import game.templates.Zombie;
import util.Asset;
import util.ImageUtil;
import util.KeyPressEvent;
import util.MathUtil;
import util.MouseEvent;
import util.Point;
import util.Rect;
import util.TypeEvent;

public class Application extends JFrame {
	// App Constants
	// static private long STARTTIME = System.currentTimeMillis();
	final float FPS = 60;
	final int TICK_RATE = 10; // TICK RATE
	GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice device = env.getDefaultScreenDevice();
	GraphicsConfiguration config = device.getDefaultConfiguration();
	Font def_font = new Font("Arial", Font.PLAIN, 18);
	Font small_font = new Font("Arial", Font.PLAIN, 12);
	Font console_font = new Font("Arial", Font.PLAIN, 16);

	// Application Semi-Constants
	int CANW = 1280;
	int CANH = 720;
	Player p;
	List<Zombie> zombies = new ArrayList<Zombie>();

	public JPanel panel = new JPanel() {
		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			// super.paintComponent(g);

			// Custom Screen Options
			RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			g2d.setRenderingHints(rh);

			// BufferedImage canvas = new BufferedImage(CANW, CANH,
			// BufferedImage.TYPE_INT_RGB);

			paint_(g2d);
		}
	};

	final int TILEBASESIZE = 40;
	double zoom_mult = 1;
	final int TILE_BUFFER = 3;
	final double SPRINT_DRAIN = 0.2;
	final double SPRINT_REGEN = 0.1;
	double camera_x = 0;
	double camera_y = 0;
	double mouse_x = 0;
	double mouse_y = 0;
	boolean mouse_clicked = false;
	double mouse_click_x;
	double mouse_click_y;
	int player_points = 0;
	boolean console_up = false;
	boolean console_toggle = false;
	boolean backspace_toggle = false;
	List<ConsoleLine> console_history = new ArrayList<ConsoleLine>();
	String current_command = "";

	final int LEVEL_W = 50;
	final int LEVEL_H = 50;
	final double transition_edge = 0.2;
	final double transition_corner = 0.25;
	Chunk level[][] = new Chunk[LEVEL_W][LEVEL_H];
	int chunktilesx = 20;
	int chunktilesy = 16;

	List<Bullet> bullets = new ArrayList<Bullet>();

	// double viewing_angle = Math.PI / 6;

	void paint_(Graphics g_base) {
		Graphics2D g = (Graphics2D) g_base;

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int) CANW, (int) CANH);

		g.setFont(def_font);

		double tilesize = TILEBASESIZE * zoom_mult;
		Rect view = new Rect(camera_x, camera_y, CANW, CANH);
		double offsetx = -(view.left() / chunktilesx) % 1;
		double offsety = -(view.top() / chunktilesy) % 1;

		for (int chunkx = 0; chunkx < LEVEL_W; chunkx++) {
			for (int chunky = 0; chunky < LEVEL_H; chunky++) {
				Rect chunkdrawspace = new Rect(((chunkx + offsetx) * chunktilesx) * tilesize,
						((chunky + offsety) * chunktilesy) * tilesize, tilesize * chunktilesx, tilesize * chunktilesy);
				if (!((chunkdrawspace.left() >= view.left() && chunkdrawspace.left() <= view.right())
						|| (chunkdrawspace.right() >= view.left() && chunkdrawspace.right() <= view.right())
						|| (chunkdrawspace.left() <= view.left() && chunkdrawspace.right() >= view.right())))
					continue;
				if (!((chunkdrawspace.top() >= view.top() && chunkdrawspace.top() <= view.bottom())
						|| (chunkdrawspace.bottom() >= view.top() && chunkdrawspace.bottom() <= view.bottom())
						|| (chunkdrawspace.top() <= view.top() && chunkdrawspace.bottom() >= view.bottom())))
					continue;
				if (chunkx >= 0 && chunkx < LEVEL_W && chunky >= 0 && chunky < LEVEL_H) {
					Chunk chunk = level[chunkx][chunky];
					int cix = (int) (camera_x / chunktilesx) + chunkx;
					int ciy = (int) (camera_y / chunktilesy) + chunky;

					for (int tx = 0; tx < chunktilesx; tx++) {
						for (int ty = 0; ty < chunktilesy; ty++) {
							int tix = (int) (tx + cix * chunktilesx);
							int tiy = (int) (ty + ciy * chunktilesy);
							Rect tiledrawspace = new Rect(((chunkx + offsetx) * chunktilesx + tx) * tilesize,
									((chunky + offsety) * chunktilesy + ty) * tilesize, tilesize, tilesize);
							if (!(tx >= 0 && tx < chunktilesx && ty >= 0 && ty < chunktilesy))
								continue;

							if (chunk == null || chunk.tiles[tx][ty] == null) {
								/*
								 * g.drawImage(AssetManager.AMGR.getAsset("grass").src,
								 * (int) tiledrawspace.left(), (int) tiledrawspace.top(),
								 * (int) tiledrawspace.width, (int) tiledrawspace.height,
								 * new Color(0, 0, 0, 0), null);
								 */
								g.setColor(Color.GRAY);
								g.fillRect((int) tiledrawspace.left(), (int) tiledrawspace.top(),
										(int) tiledrawspace.width, (int) tiledrawspace.height);
							} else {
								// Tile t = chunk.tiles[tx][ty];
								Tile t = getTile(tix, tiy);
								g.drawImage(AssetManager.AMGR.getAsset(t.floor).src,
										(int) tiledrawspace.left(), (int) tiledrawspace.top(),
										(int) tiledrawspace.width, (int) tiledrawspace.height,
										new Color(0, 0, 0, 0), null);
								t.DrawTileObjects(g, tiledrawspace.left(), tiledrawspace.top(), tilesize);
							}
							g.setStroke(new BasicStroke(1));
							g.setColor(Color.RED);
							g.drawRect((int) tiledrawspace.left(), (int) tiledrawspace.top(),
									(int) tiledrawspace.width, (int) tiledrawspace.height);
							g.setColor(Color.WHITE);
							g.setFont(small_font);
							g.drawString(String.format("%d %d", tix, tiy), (int) tiledrawspace.left(),
									(int) (tiledrawspace.top() + g.getFontMetrics().getAscent()));

						}
					}
					g.setStroke(new BasicStroke(4));
					g.setColor(Color.BLUE);
					g.drawRect((int) chunkdrawspace.left(), (int) chunkdrawspace.top(),
							(int) chunkdrawspace.width, (int) chunkdrawspace.height);
					g.setColor(Color.YELLOW);
					g.setFont(def_font);
					g.drawString(String.format("%d %d", cix, ciy), (int) chunkdrawspace.left(),
							(int) (chunkdrawspace.top() + g.getFontMetrics().getAscent()));
				}
			}
		}

		g.setColor(Color.GREEN);
		g.drawLine((int) p.centerx(), (int) p.centery(), (int) mouse_x, (int) mouse_y);
		g.fillRect((int) mouse_x - 4, (int) mouse_y - 4, 8, 8);

		g.setColor(Color.RED);
		g.drawLine((int) p.centerx(), (int) p.centery(), (int) (Math.cos(p.look_angle) * 10 + p.centerx()),
				(int) (Math.sin(p.look_angle) * 10 + p.centery()));
		g.fillRect((int) (Math.cos(p.look_angle) * 50 + p.centerx() - 4),
				(int) (Math.sin(p.look_angle) * 50 + p.centery() - 4),
				8, 8);

		for (Bullet b : bullets) {
			b.paint(g);
			// g.setColor(Color.RED);
			// g.drawRect((int) b.x, (int) b.y, (int) b.width, (int) b.height);
		}
		p.paint(g);

		for (Zombie z : zombies) {
			z.paint(g);
		}
		if (inventory_up)
			drawInventory(g);

		drawGUI(g);
	}

	void drawGUI(Graphics2D g) {
		g.setColor(Color.GRAY);
		g.fillRect(20, CANH - 74, 175, 20);
		g.setColor(Color.YELLOW);
		g.fillRect(24, CANH - 70, (int) (167.0f * p.stamina / 100), 12);

		g.setColor(Color.GRAY);
		g.fillRect(20, CANH - 104, 175, 20);
		g.setColor(Color.RED);
		g.fillRect(24, CANH - 100, (int) (167.0f * p.health / 100), 12);

		g.setColor(Color.WHITE);
		g.fillRect(20, 30, 40, 24);
		g.setColor(Color.BLACK);
		g.drawString(String.format("%d", player_points), 24, 34 + g.getFontMetrics().getAscent());

		if (console_up) {
			g.setFont(console_font);
			int texth = g.getFontMetrics(console_font).getAscent();
			g.setColor(new Color(120, 120, 120, 180));
			g.fillRect(0, 0, CANW, CANH);
			for (int i = 0; i < console_history.size() && i < (int) (CANW / texth); i++) {
				ConsoleLine line = console_history.get(i);
				if (line.owner == 0)
					g.setColor(Color.ORANGE);
				else if (line.owner == 1)
					g.setColor(Color.LIGHT_GRAY);
				g.drawString(line.text, 20, i * texth + 20);
			}
			g.setColor(Color.WHITE);
			g.drawString(current_command, 20, CANH - 80);
		}
	}

	void drawInventory(Graphics2D g) {
		g.setColor(new Color(200, 200, 200, 180));
		g.fillRect(50, 50, CANW - 100, CANH - 100);

		g.setColor(Color.BLACK);
		for (int i = 0; i < p.inventory.stacks_max; i++) {
			Stack stack = p.inventory.stacks[i];
			String msg = "";
			if (stack == null)
				msg = "null";
			else
				msg = String.format("%s (%d)", stack.getItem().name, stack.count);
			g.drawString(msg, 60, 60 + g.getFontMetrics().getAscent() * (i + 1));
		}
	}

	CollisionReturn staticStaticCollision(GameObject a, GameObject b) {
		final double COLLISION_BUFFER = 0.1;
		final double OUTER_COLLISION_BUFFER = COLLISION_BUFFER * 1.4;
		CollisionReturn ret = new CollisionReturn();

		return ret;
	}

	Point[] RotatePoints(double angle, Point in[]) {
		Point targets[] = new Point[in.length];
		Point2D points_in[] = new Point2D[in.length];
		Point2D points_out[] = new Point2D[in.length];
		double x = 0;
		double x_ = 0;
		double y = 0;
		double y_ = 0;

		for (Point p : in) {
			if (p.x > x_)
				x_ = p.x;
			if (p.x < x)
				x = p.x;
			if (p.y > y_)
				y_ = p.y;
			if (p.y < y)
				y = p.y;
		}
		int w = (int) (x_ - x);
		int h = (int) (y_ - y);

		points_in = Point.toPoints(in);

		AffineTransform tf = AffineTransform.getRotateInstance(angle, w / 2.0, h / 2.0);
		tf.transform(points_in, 0, points_out, 0, in.length);
		targets = Point.toPoints(points_out);
		return targets;
	}

	CollisionReturn staticDynamicCollision(DynamicGameObject a, GameObject b) {
		final double BUFFER = 4;
		CollisionReturn ret = new CollisionReturn();

		double dx = Math.cos(a.angle) * a.magnitude;
		double dy = Math.sin(a.angle) * a.magnitude;

		int intent_x = dx > 0.01 ? 1 : dx < -0.01 ? -1 : 0;
		int intent_y = dy > 0.01 ? 1 : dy < -0.01 ? -1 : 0;

		if (intent_x == 1 && intent_y == 1) {// quadrant I
			Point top_left = new Point(a.left(), a.top());
			Point top_right = new Point(a.right(), a.top());
			Point bottom_right = new Point(a.right(), a.bottom());

			Point object_bottom_left = new Point(b.left(), b.bottom());
			Point object_bottom_right = new Point(b.right(), b.bottom());
			Point object_top_left = new Point(b.left(), b.top());

			boolean left_intersect = top_right.x >= object_bottom_left.x && top_left.x < object_bottom_left.x;
			boolean right_intersect = top_left.x <= object_bottom_right.x && top_right.x > object_bottom_right.x;
			boolean center_intersect_x = top_left.x >= object_bottom_left.x && top_right.x <= object_bottom_right.x;
			boolean pass_by_x = top_right.x <= object_bottom_left.x && top_left.x + dx >= object_bottom_right.x;

			boolean top_intersect = bottom_right.y >= object_top_left.y && top_left.y < object_top_left.y;
			boolean bottom_intersect = top_left.y <= object_bottom_right.y && bottom_right.y > object_bottom_right.y;
			boolean center_intersect_y = bottom_right.y <= object_bottom_right.y && top_left.y >= object_top_left.y;
			boolean pass_by_y = bottom_right.y <= object_top_left.y && top_left.y - dy >= object_bottom_right.y;

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			boolean inline_x = top_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if ((inline_y || pass_by_x) && a.top() <= b.bottom() && a.top() + dy > b.bottom()) {
				ret.valid_change_y = true;
				ret.change_y = Math.ceil(a.top() - b.bottom());
				return ret;
			} else if ((inline_x || pass_by_y) && a.right() <= b.left() && a.right() + dx > b.left()) {
				ret.valid_change_x = true;
				ret.change_x = Math.floor(b.left() - a.right());
			}
		} else if (intent_x == 0 && intent_y == 1) {// y axis up
			Point top_left = new Point(a.left(), a.top());
			Point top_right = new Point(a.right(), a.top());

			Point object_bottom_left = new Point(b.left(), b.bottom());
			Point object_bottom_right = new Point(b.right(), b.bottom());

			boolean left_intersect = top_right.x >= object_bottom_left.x && top_left.x < object_bottom_left.x;
			boolean right_intersect = top_left.x <= object_bottom_right.x && top_right.x > object_bottom_right.x;
			boolean center_intersect_x = top_left.x >= object_bottom_left.x && top_right.x <= object_bottom_right.x;
			boolean pass_by_x = top_right.x <= object_bottom_left.x && top_left.x + dx >= object_bottom_right.x;

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			// check if object is valid before move
			if ((inline_y || pass_by_x) && a.top() <= b.bottom() && a.top() + dy > b.bottom()) {
				ret.valid_change_y = true;
				ret.change_y = Math.ceil(a.top() - b.bottom());
			}
		} else if (intent_x == -1 && intent_y == 1) {// quadrant II
			Point top_left = new Point(a.left(), a.top());
			Point top_right = new Point(a.right(), a.top());
			Point bottom_left = new Point(a.right(), a.bottom());

			Point object_bottom_left = new Point(b.left(), b.bottom());
			Point object_bottom_right = new Point(b.right(), b.bottom());
			Point object_top_right = new Point(b.right(), b.top());

			boolean left_intersect = top_right.x >= object_bottom_left.x && top_left.x < object_bottom_left.x;
			boolean right_intersect = top_left.x <= object_bottom_right.x && top_right.x > object_bottom_right.x;
			boolean center_intersect_x = top_left.x >= object_bottom_left.x && top_right.x <= object_bottom_right.x;
			boolean pass_by_x = top_right.x <= object_bottom_left.x && top_left.x + dx >= object_bottom_right.x;

			boolean top_intersect = bottom_left.y >= object_top_right.y && top_left.y < object_top_right.y;
			boolean bottom_intersect = top_left.y <= object_bottom_right.y && bottom_left.y > object_bottom_right.y;
			boolean center_intersect_y = bottom_left.y <= object_bottom_right.y && top_left.y >= object_top_right.y;
			boolean pass_by_y = bottom_left.y <= object_top_right.y && top_left.y - dy >= object_bottom_right.y;

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			boolean inline_x = top_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if ((inline_y || pass_by_x) && a.top() <= b.bottom() && a.top() + dy > b.bottom()) {
				ret.valid_change_y = true;
				ret.change_y = Math.ceil(a.top() - b.bottom());
			} else if ((inline_x || pass_by_y) && a.left() >= b.right() && a.left() + dx < b.right()) {
				ret.valid_change_x = true;
				ret.change_x = Math.floor(a.left() - b.right());
			}
		} else if (intent_x == -1 && intent_y == 0) {// x axis left
			Point top_left = new Point(a.right(), a.top());
			Point bottom_left = new Point(a.right(), a.bottom());

			Point object_top_right = new Point(b.left(), b.top());
			Point object_bottom_right = new Point(b.left(), b.bottom());

			boolean top_intersect = bottom_left.y >= object_top_right.y && top_left.y < object_top_right.y;
			boolean bottom_intersect = top_left.y <= object_bottom_right.y && bottom_left.y > object_bottom_right.y;
			boolean center_intersect_y = bottom_left.y <= object_bottom_right.y && top_left.y >= object_top_right.y;
			boolean pass_by_y = bottom_left.y <= object_top_right.y && top_left.y - dy >= object_bottom_right.y;

			boolean inline_x = top_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if ((inline_x || pass_by_y) && a.left() >= b.right() && a.left() + dx < b.right()) {
				ret.valid_change_x = true;
				ret.change_x = Math.ceil(b.right() - a.left());
			}
		} else if (intent_x == -1 && intent_y == -1) {// quadrant III
			Point top_left = new Point(a.left(), a.top());
			Point bottom_right = new Point(a.right(), a.bottom());
			Point bottom_left = new Point(a.left(), a.bottom());

			Point object_top_left = new Point(b.left(), b.top());
			Point object_top_right = new Point(b.right(), b.top());
			Point object_bottom_right = new Point(b.right(), b.bottom());

			boolean left_intersect = bottom_right.x >= object_top_left.x && top_left.x < object_top_left.x;
			boolean right_intersect = top_left.x <= object_bottom_right.x && bottom_right.x > object_bottom_right.x;
			boolean center_intersect_x = top_left.x >= object_top_left.x && bottom_right.x < object_bottom_right.x;
			boolean pass_by_x = bottom_right.x <= object_top_left.x && top_left.x + dx >= object_bottom_right.x;

			boolean top_intersect = bottom_left.y >= object_top_right.y && top_left.y < object_top_right.y;
			boolean bottom_intersect = top_left.y <= object_bottom_right.y && bottom_left.y > object_bottom_right.y;
			boolean center_intersect_y = bottom_left.y <= object_bottom_right.y && top_left.y >= object_top_right.y;
			boolean pass_by_y = bottom_left.y <= object_top_right.y && top_left.y - dy >= object_bottom_right.y;

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			boolean inline_x = top_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if ((inline_y || pass_by_x) && a.bottom() <= b.top() && a.bottom() - dy > b.top()) {
				ret.valid_change_y = true;
				ret.change_y = Math.ceil(a.bottom() - b.top());
			} else if ((inline_x || pass_by_y) && a.left() >= b.right() && a.left() + dx < b.right()) {
				ret.valid_change_x = true;
				ret.change_x = Math.floor(a.left() - b.right());
			}
		} else if (intent_x == 0 && intent_y == -1) {// y axis down
			Point bottom_left = new Point(a.left(), a.bottom());
			Point bottom_right = new Point(a.right(), a.bottom());

			Point object_top_left = new Point(b.left(), b.top());
			Point object_top_right = new Point(b.right(), b.top());

			boolean left_intersect = bottom_right.x >= object_top_left.x && bottom_left.x < object_top_left.x;
			boolean right_intersect = bottom_left.x <= object_top_right.x && bottom_right.x > object_top_right.x;
			boolean center_intersect_x = bottom_left.x >= object_top_left.x && bottom_right.x <= object_top_right.x;
			boolean pass_by_x = bottom_right.x <= object_top_left.x && bottom_left.x + dx >= object_top_right.x;

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			// check if object is valid before move
			if ((inline_y || pass_by_x) && a.bottom() <= b.top() && a.bottom() - dy > b.top()) {
				ret.valid_change_y = true;
				ret.change_y = Math.floor(b.top() - a.bottom());
			}
		} else if (intent_x == 1 && intent_y == -1) {// quadrant IV
			Point top_right = new Point(a.right(), a.top());
			Point bottom_right = new Point(a.right(), a.bottom());
			Point bottom_left = new Point(a.left(), a.bottom());

			Point object_top_left = new Point(b.left(), b.top());
			Point object_top_right = new Point(b.right(), b.top());
			Point object_bottom_left = new Point(b.left(), b.bottom());

			boolean left_intersect = bottom_right.x >= object_top_left.x && bottom_left.x < object_top_left.x;
			boolean right_intersect = bottom_left.x <= object_top_right.x && bottom_right.x > object_top_right.x;
			boolean center_intersect_x = bottom_left.x >= object_top_left.x && bottom_right.x <= object_top_right.x;
			boolean pass_by_x = bottom_right.x <= object_top_left.x && bottom_left.x + dx >= object_top_right.x;

			boolean top_intersect = bottom_left.y >= object_top_right.y && top_right.y < object_top_right.y;
			boolean bottom_intersect = top_right.y <= object_bottom_left.y && bottom_left.y > object_bottom_left.y;
			boolean center_intersect_y = bottom_left.y <= object_bottom_left.y && top_right.y >= object_top_right.y;
			boolean pass_by_y = bottom_left.y <= object_top_right.y && top_right.y - dy >= object_bottom_left.y;

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			boolean inline_x = top_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if ((inline_y || pass_by_x) && a.bottom() <= b.top() && a.bottom() - dy > b.top()) {
				ret.valid_change_y = true;
				ret.change_y = Math.ceil(a.bottom() - b.top());
			} else if ((inline_x || pass_by_y) && a.right() <= b.left() && a.right() + dx > b.left()) {
				ret.valid_change_x = true;
				ret.change_x = Math.floor(b.left() - a.right());
			}

		} else if (intent_x == 1 && intent_y == 0) {// x axis right
			Point top_right = new Point(a.right(), a.top());
			Point bottom_right = new Point(a.right(), a.bottom());

			Point object_top_left = new Point(b.left(), b.top());
			Point object_bottom_left = new Point(b.left(), b.bottom());

			boolean top_intersect = bottom_right.y >= object_top_left.y && top_right.y < object_top_left.y;
			boolean bottom_intersect = top_right.y <= object_bottom_left.y && bottom_right.y > object_bottom_left.y;
			boolean center_intersect_y = bottom_right.y <= object_bottom_left.y && top_right.y >= object_top_left.y;
			boolean pass_by_y = bottom_right.y <= object_top_left.y && top_right.y - dy >= object_bottom_left.y;

			boolean inline_x = top_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if ((inline_x || pass_by_y) && a.right() <= b.left() && a.right() + dx > b.left()) {
				ret.valid_change_x = true;
				ret.change_x = Math.floor(b.left() - a.right());
			}
		}
		return ret;
	}

	public double displacement_x = 0;
	public double displacement_y = 0;

	boolean DynamicDynamicCollision(DynamicGameObject a, DynamicGameObject b) {
		return false;
	}

	double animation_pos = 0;
	boolean animating_screen = false;
	double anim_speed_x = 0;
	double anim_base_speed = 2;
	double anim_speed_y = 0;
	double anim_timer = 150;

	void tick(int tr) {
		double tilesize = TILEBASESIZE * zoom_mult;

		if (!console_toggle && Peripherals.PERI.keyPressed(KeyEvent.VK_MINUS)) {
			console_toggle = true;
			console_up = !console_up;
			current_command = "";
		}

		if (Peripherals.PERI.keyPressed(KeyEvent.VK_G)) {
			if (!inventory_toggle) {
				inventory_toggle = true;
				inventory_up = !inventory_up;
			}
		} else {
			inventory_toggle = false;
		}

		if (!Peripherals.PERI.keyPressed(KeyEvent.VK_MINUS)) {
			console_toggle = false;
		}
		if (console_up)
			return;

		{
			int intent_x = 0;
			int intent_y = 0;
			if (Peripherals.PERI.keyPressed(KeyEvent.VK_LEFT))
				intent_x--;
			if (Peripherals.PERI.keyPressed(KeyEvent.VK_RIGHT))
				intent_x++;
			if (Peripherals.PERI.keyPressed(KeyEvent.VK_UP))
				intent_y++;
			if (Peripherals.PERI.keyPressed(KeyEvent.VK_DOWN))
				intent_y--;

			if (intent_x != 0 || intent_y != 0) {
				double intent_direction = Math.atan2(intent_y, intent_x);
				final double speed = 0.05;
				double dx = Math.cos(intent_direction) * speed;
				double dy = Math.sin(intent_direction) * speed;

				camera_x += dx;
				camera_y -= dy;
				p.x -= dx * tilesize;
				p.y += dy * tilesize;
			}
		}
		{
			int intent_x = 0;
			int intent_y = 0;
			if (Peripherals.PERI.keyPressed(KeyEvent.VK_A))
				intent_x--;
			if (Peripherals.PERI.keyPressed(KeyEvent.VK_D))
				intent_x++;
			if (Peripherals.PERI.keyPressed(KeyEvent.VK_W))
				intent_y++;
			if (Peripherals.PERI.keyPressed(KeyEvent.VK_S))
				intent_y--;

			if (intent_x != 0 || intent_y != 0) {
				double sprint = Peripherals.PERI.keyPressed(KeyEvent.VK_SHIFT) && p.stamina > 20 ? p.SPRINT_SPEED_MULT()
						: 1;
				double intent_direction = Math.atan2(intent_y, intent_x);
				p.angle = intent_direction;

				displacement_x = Math.cos(intent_direction) * p.BASE_SPEED() * sprint;
				displacement_y = Math.sin(intent_direction) * p.BASE_SPEED() * sprint;
				p.magnitude = p.BASE_SPEED() * sprint;

			} else {
				displacement_x = 0;
				displacement_y = 0;
			}
		}

		if (Math.sqrt(Math.pow(mouse_y - p.centery(), 2) + Math.pow(mouse_x - p.centerx(), 2)) > p.width / 2) {
			p.look_angle = Math.atan2((mouse_y - p.centery()), (mouse_x - p.centerx()));
		}

		if (Peripherals.PERI.keyPressed(KeyEvent.VK_SHIFT)) {
			if (p.stamina > 0)
				p.stamina -= SPRINT_DRAIN;
			p.regen_delay_time = 0;
		} else {
			if (p.stamina < p.MAX_STAMINA()) {
				if (p.regen_delay_time != p.SPRINT_REGEN_DELAY) {
					p.regen_delay_time++;
				} else {
					p.stamina += SPRINT_REGEN;
				}
			}
		}

		if (mouse_clicked) {
			mouse_clicked = false;
			Bullet b = new Bullet();
			b.x = p.centerx() + Math.cos(p.look_angle) * p.width / 2 - b.width / 2;
			b.y = p.centery() + Math.sin(p.look_angle) * p.width / 2 - b.height / 2;
			b.angle = -p.look_angle;
			bullets.add(b);

		}
		Rect view = new Rect(camera_x, camera_y, CANW, CANH);
		double offsetx = -(view.left() / chunktilesx) % 1;
		double offsety = -(view.top() / chunktilesy) % 1;

		for (int chunkx = 0; chunkx < LEVEL_W; chunkx++) {
			for (int chunky = 0; chunky < LEVEL_H; chunky++) {
				Rect chunkdrawspace = new Rect(((chunkx + offsetx) * chunktilesx) * tilesize,
						((chunky + offsety) * chunktilesy) * tilesize, tilesize * chunktilesx, tilesize * chunktilesy);
				if (!((chunkdrawspace.left() >= view.left() && chunkdrawspace.left() <= view.right())
						|| (chunkdrawspace.right() >= view.left() && chunkdrawspace.right() <= view.right())
						|| (chunkdrawspace.left() <= view.left() && chunkdrawspace.right() >= view.right())))
					continue;
				if (!((chunkdrawspace.top() >= view.top() && chunkdrawspace.top() <= view.bottom())
						|| (chunkdrawspace.bottom() >= view.top() && chunkdrawspace.bottom() <= view.bottom())
						|| (chunkdrawspace.top() <= view.top() && chunkdrawspace.bottom() >= view.bottom())))
					continue;

				if (chunkx >= 0 && chunkx < LEVEL_W && chunky >= 0 && chunky < LEVEL_H) {
					Chunk chunk = level[chunkx][chunky];
					int cix = (int) (camera_x / chunktilesx) + chunkx;
					int ciy = (int) (camera_y / chunktilesy) + chunky;

					for (int tx = 0; tx < chunktilesx; tx++) {
						for (int ty = 0; ty < chunktilesy; ty++) {
							int tix = (int) (tx + cix * chunktilesx);
							int tiy = (int) (ty + ciy * chunktilesy);
							Rect tiledrawspace = new Rect(((chunkx + offsetx) * chunktilesx + tx) * tilesize,
									((chunky + offsety) * chunktilesy + ty) * tilesize, tilesize, tilesize);
							if (!(tx >= 0 && tx < chunktilesx && ty >= 0 && ty < chunktilesy))
								continue;
							Tile tile = getTile(tix, tiy);
							for (TileObject o : tile.tileObjects) {
								if (!o.collider)
									continue;
								GameObject o1 = o.toGameObject(tiledrawspace.left(), tiledrawspace.top(), tilesize);
								CollisionReturn ret = staticDynamicCollision(p, o1);
								if (ret.valid_change_x) {
									displacement_x = ret.change_x;
								}
								if (ret.valid_change_y) {
									displacement_y = ret.change_y;
								}
								for (int i = 0; i < bullets.size(); i++) {
									Bullet b = bullets.get(i);
									CollisionReturn ret2 = staticDynamicCollision(b, o1);
									if (ret2.colliding()) {
										bullets.remove(b);
										i--;
									}
								}
							}
						}
					}

				}
			}
		}

		/*
		 * if (animating_screen == false) {
		 * if (p.centerx() > CANW * (1 - transition_edge)) {
		 * animating_screen = true;
		 * animation_pos = 0;
		 * anim_speed_x = anim_base_speed;
		 * anim_speed_y = 0;
		 * }
		 * if (p.centerx() < CANW * transition_edge) {
		 * animating_screen = true;
		 * animation_pos = 0;
		 * anim_speed_x = -anim_base_speed;
		 * anim_speed_y = 0;
		 * }
		 * if (p.centery() < CANH * transition_edge) {
		 * animating_screen = true;
		 * animation_pos = 0;
		 * anim_speed_y = -anim_base_speed;
		 * anim_speed_x = 0;
		 * }
		 * if (p.centery() > CANH * (1 - transition_edge)) {
		 * animating_screen = true;
		 * animation_pos = 0;
		 * anim_speed_y = anim_base_speed;
		 * anim_speed_x = 0;
		 * }
		 * if (p.centery() > CANH * (1 - transition_corner) && p.centerx() > CANW * (1 -
		 * transition_corner)) {// bottom
		 * // right
		 * animating_screen = true;
		 * animation_pos = 0;
		 * anim_speed_y = anim_base_speed / Math.sqrt(2);
		 * anim_speed_x = anim_base_speed / Math.sqrt(2);
		 * }
		 * if (p.centery() < CANH * transition_corner && p.centerx() > CANW * (1 -
		 * transition_corner)) {// top
		 * // right
		 * animating_screen = true;
		 * animation_pos = 0;
		 * anim_speed_y = -anim_base_speed / Math.sqrt(2);
		 * anim_speed_x = anim_base_speed / Math.sqrt(2);
		 * }
		 * if (p.centery() < CANH * transition_corner && p.centerx() < CANW *
		 * transition_corner) {// top left
		 * animating_screen = true;
		 * animation_pos = 0;
		 * anim_speed_y = -anim_base_speed / Math.sqrt(2);
		 * anim_speed_x = -anim_base_speed / Math.sqrt(2);
		 * }
		 * if (p.centery() > CANH * (1 - transition_corner) && p.centerx() < CANW *
		 * transition_corner) {// bottom
		 * // left
		 * animating_screen = true;
		 * animation_pos = 0;
		 * anim_speed_y = anim_base_speed / Math.sqrt(2);
		 * anim_speed_x = -anim_base_speed / Math.sqrt(2);
		 * }
		 * } else {
		 * camera_x += anim_speed_x;
		 * p.x -= anim_speed_x;
		 * camera_y += anim_speed_y;
		 * p.y -= anim_speed_y;
		 * for (int i = 0; i < bullets.size(); i++) {
		 * Bullet b = bullets.get(i);
		 * b.x -= anim_speed_x;
		 * b.y -= anim_speed_y;
		 * bullets.set(i, b);
		 * }
		 * for (int i = 0; i < zombies.size(); i++) {
		 * Zombie z = zombies.get(i);
		 * z.x -= anim_speed_x;
		 * z.y -= anim_speed_y;
		 * zombies.set(i, z);
		 * }
		 * 
		 * animation_pos += 1;
		 * if (animation_pos >= anim_timer)
		 * animating_screen = false;
		 * }
		 */

		p.x += displacement_x;
		p.y -= displacement_y;

		for (int i = 0; i < bullets.size(); i++) {
			Bullet b = bullets.get(i);
			b.x += Math.cos(b.angle) * b.magnitude;
			b.y -= Math.sin(b.angle) * b.magnitude;
			bullets.set(i, b);
			if (b.x + b.width < 0 || b.x > CANW || b.y + b.height < 0 || b.y > CANH) {
				bullets.remove(i);
				i--;
			}
			for (int z1 = 0; z1 < zombies.size(); z1++) {
				Zombie z = zombies.get(z1);
				CollisionReturn ret = staticDynamicCollision(b, z);
				if (ret.colliding()) {
					z.health -= b.damage;
					if (z.health <= 0) {
						zombies.remove(z1);
						player_points += 1;
					}

					bullets.remove(i);
					i--;
					break;
				}
			}
		}
	}

	void initScreenRefresh() {
		new Thread() {
			public void run() {
				long lastlong = System.nanoTime();
				float delta = 0;
				final double physicstick = 1000000000 / 140;

				while (true) {
					long now = System.nanoTime();
					delta += (now - lastlong) / physicstick;
					long lastrender = System.nanoTime();
					lastlong = now;
					while (delta >= 1) {
						delta--;
						// physics code 8% CPU, is that normal?
					}

					panel.repaint();
					lastrender = now;

					while (now - lastrender < (1000000000 / FPS)) {
						try {
							Thread.sleep(1);
							// Without sleeping there is a 100% CPU usage CRAZY!
						} catch (InterruptedException ie) {
						}
						now = System.nanoTime();
					}
				}
			}
		}.start();
	}

	String runCommand(String cmd) {
		String[] args = cmd.split(" ");

		if (current_command.equals("clear")) {
			console_history.clear();
			return "cleared console successfully";
		} else if (current_command.startsWith("tile")) {

			int ix = -1;
			int iy = -1;
			try {
				ix = Integer.parseInt(args[1]);
				iy = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				return "failed to parse arguments";
			}
			String assetname = args[3];
			Tile t = new Tile();
			t.floor = AssetManager.AMGR.assetID(assetname);
			setTile(ix, iy, t);

			return "successfully changed tile asset";
		} else if (current_command.startsWith("wall")) {

			int ix = -1;
			int iy = -1;
			try {
				ix = Integer.parseInt(args[1]);
				iy = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				return "failed to parse arguments";
			}

			int cx = (int) (ix / chunktilesx);
			int cy = (int) (iy / chunktilesy);
			int tx = ix - cx * chunktilesx;
			int ty = iy - cy * chunktilesy;

			if (level[cx][cy] == null)
				return "tile not initialized";

			level[cx][cy].tiles[tx][ty].addTileObject(wall);

			return "successfully added wall tile object";
		} else if (current_command.startsWith("cleartile")) {
			int ix = -1;
			int iy = -1;
			try {
				ix = Integer.parseInt(args[1]);
				iy = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				return "failed to parse arguments";
			}
			int cx = (int) (ix / chunktilesx);
			int cy = (int) (iy / chunktilesy);
			int tx = ix - cx * chunktilesx;
			int ty = iy - cy * chunktilesy;

			if (level[cx][cy] == null)
				return "tile not initialized";

			int count = level[cx][cy].tiles[tx][ty].tileObjects.size();
			level[cx][cy].tiles[tx][ty].tileObjects.clear();
			return String.format("successfully cleared %d tile objects", count);

		} else if (current_command.startsWith("nulltile")) {
			int ix = -1;
			int iy = -1;
			try {
				ix = Integer.parseInt(args[1]);
				iy = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				return "failed to parse arguments";
			}
			if (level[ix][iy] == null)
				return "tile is already null";

			level[ix][iy] = null;
			return "successfully reset tile";
		} else if (current_command.startsWith("giveitem")) {
			String itemname;
			int count;
			try {
				itemname = args[1];
				count = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				return "failed to parse arguments";
			}

			int ret = p.inventory.addItemsToInv(ItemManager.IMGR.itemID(itemname), count);
			return String.format("return=%d", ret);
		}
		return "null";
	}

	void ImportAssets() {
		AssetManager.AMGR.addAsset("debug_grad_floor", ImageUtil.ImageFile("assets/debug_grad.jpeg"));
		AssetManager.AMGR.addAsset("grass", ImageUtil.ImageFile("assets/grass.jpeg"));
		AssetManager.AMGR.addAsset("sand", ImageUtil.ImageFile("assets/sand.jpeg"));
		AssetManager.AMGR.addAsset("stone", ImageUtil.ImageFile("assets/stone.jpeg"));
		AssetManager.AMGR.addAsset("wood", ImageUtil.ImageFile("assets/wood.jpeg"));
		AssetManager.AMGR.addAsset("tree", ImageUtil.ImageFile("assets/tree.png"));
		AssetManager.AMGR.addAsset("fern", ImageUtil.ImageFile("assets/fern.png"));
		AssetManager.AMGR.addAsset("weeds", ImageUtil.ImageFile("assets/weeds.png"));

		ItemManager.IMGR.addAsset("stone", -1, 16);
	}

	boolean inventory_up = false;
	boolean inventory_toggle = false;

	public void addEventHooks() {
		Peripherals.PERI.addMouseMoveHook(new MouseEvent() {

			@Override
			public void action(double x, double y) {
				mouse_x = x;
				mouse_y = y;
			}

		});

		Peripherals.PERI.addMouseClickHook(new MouseEvent() {

			@Override
			public void action(double x, double y) {
				mouse_clicked = true;
				mouse_click_x = x;
				mouse_click_y = y;
			}

		});
		Peripherals.PERI.addTypeHook(new TypeEvent() {

			@Override
			public void action(char c, int keycode) {
				if (console_up && (Character.isLetterOrDigit(c) || Character.isSpaceChar(c))) {
					current_command += c;
				}
			}

		});
		Peripherals.PERI.addKeyPressHook(new KeyPressEvent() {

			@Override
			public void action(char c, int keycode, boolean status) {
				if (console_up && current_command.length() > 0 && status == true) {
					if (!backspace_toggle && keycode == KeyEvent.VK_BACK_SPACE) {
						backspace_toggle = true;
						current_command = current_command.substring(0, current_command.length() - 1);
					} else if (keycode == KeyEvent.VK_ENTER) {
						console_history.add(new ConsoleLine(1, current_command));
						String resp = runCommand(current_command);
						console_history.add(new ConsoleLine(0, resp));
						current_command = "";
					}
					if (!Peripherals.PERI.keyPressed(KeyEvent.VK_BACK_SPACE)) {
						backspace_toggle = false;
					}
				} else {

				}
			}

		});
	}

	TileObject wall;

	void setTile(int x, int y, Tile t) {
		int cx = (int) (x / chunktilesx);
		int cy = (int) (y / chunktilesy);
		int tx = x - cx * chunktilesx;
		int ty = y - cy * chunktilesy;

		if (level[cx][cy] == null)
			level[cx][cy] = new Chunk(cx, cy, chunktilesx, chunktilesy);
		level[cx][cy].tiles[tx][ty] = t;
	}

	Tile getTile(int x, int y) {
		int cx = (int) (x / chunktilesx);
		int cy = (int) (y / chunktilesy);
		int tx = x - cx * chunktilesx;
		int ty = y - cy * chunktilesy;

		if (level[cx][cy] == null)
			return null;
		return level[cx][cy].tiles[tx][ty];
	}

	void InitializeLevel() {
		wall = new TileObject(0, 0, 1, 1, AssetManager.AMGR.assetID("stone"), true) {

			@Override
			public void paint(Graphics2D g, double dx, double dy, double gridsize) {
				g.drawImage(AssetManager.AMGR.getAsset(asset).src, (int) (dx),
						(int) (dy), (int) (gridsize), (int) (gridsize),
						null);
			}

		};
		for (int y = 0; y < LEVEL_H * chunktilesy; y++) {
			for (int x = 0; x < LEVEL_W * chunktilesx; x++) {

				Tile t = new Tile();
				t.floor = AssetManager.AMGR.assetID("grass");
				if (Math.random() > 0.95f) {// TREE
					double minsize = 0.4;
					double size = minsize + Math.random() * 0.4;
					double x_ = Math.random() * (1 - size);
					double y_ = Math.random() * (1 - size);
					TileObject tree = new TileObject(x_, y_, size, size, AssetManager.AMGR.assetID("tree"), true) {

						@Override
						public void paint(Graphics2D g, double dx, double dy, double gridsize) {
							g.drawImage(AssetManager.AMGR.getAsset(asset).src, (int) (dx + lx * gridsize),
									(int) (dy + ly * gridsize), (int) (lwidth * gridsize), (int) (lheight * gridsize),
									null);
						}

					};
					t.addTileObject(tree);
				} // GRASS
				else if (Math.random() > 0.95f) {
					double minsize = 0.3;
					double size = minsize + Math.random() * 0.5;
					double x_ = Math.random() * (1 - size);
					double y_ = Math.random() * (1 - size);
					TileObject fern = new TileObject(x_, y_, size, size, AssetManager.AMGR.assetID("fern"), false) {

						@Override
						public void paint(Graphics2D g, double dx, double dy, double gridsize) {
							Asset a = AssetManager.AMGR.getAsset(asset);
							g.drawImage(a.src, (int) (dx + lx * gridsize), (int) (dy + ly * gridsize),
									(int) (lwidth * gridsize), (int) (a.scaledHeight(lwidth * gridsize)), null);
						}

					};
					t.addTileObject(fern);
				} else if (Math.random() > 0.9f) {
					double minsize = 0.2;
					double size = minsize + Math.random() * 0.35;
					double x_ = Math.random() * (1 - size);
					double y_ = Math.random() * (1 - size);
					TileObject weeds = new TileObject(x_, y_, size, size, AssetManager.AMGR.assetID("weeds"), false) {

						@Override
						public void paint(Graphics2D g, double dx, double dy, double gridsize) {
							Asset a = AssetManager.AMGR.getAsset(asset);
							g.drawImage(a.src, (int) (dx + lx * gridsize), (int) (dy + ly * gridsize),
									(int) (lwidth * gridsize), (int) (a.scaledHeight(lwidth * gridsize)), null);
						}

					};
					t.addTileObject(weeds);
				}
				setTile(x, y, t);

			}
		}
	}

	void InitializeEnemies() {
		for (int i = 0; i < 1; i++) {
			Zombie z = new Zombie();
			zombies.add(z);
		}
	}

	public void InitializeApplication() {
		add(panel);

		int init_w = 960;
		int init_h = 600;
		setSize(init_w, init_h);

		CANW = init_w;
		CANH = init_h;

		this.addKeyListener(Peripherals.PERI);
		panel.addMouseListener(Peripherals.PERI);
		panel.addMouseMotionListener(Peripherals.PERI);
		panel.addMouseWheelListener(Peripherals.PERI);

		ImportAssets();
		InitializeLevel();
		InitializeEnemies();

		// TileObject obj = new TileObject(0, 0, 1, 1, AMGR.assetID("tree")) {

		// @Override
		// public void paint(Graphics2D g, double dx, double dy, double gridsize) {
		// g.drawImage(AMGR.getAsset(asset).src, (int) (dx + lx * gridsize), (int) (dy +
		// ly * gridsize),
		// (int) (lwidth * gridsize), (int) (lheight * gridsize), null);
		// }

		// };
		// Tile t = new Tile();
		// t.floor = AMGR.assetID("grass");
		// t.addTileObject(obj);
		// level[9][9] = t;
		// level[11][9] = t;
		// level[9][10] = t;
		// level[11][10] = t;

		// InitializeLevel();
		// InitializeEnemies();

		addEventHooks();

		p = new Player();

		// Event Timers (screen refresh/game time)
		Timer timer = new Timer();
		initScreenRefresh();
		// game timer
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				tick(TICK_RATE);
			}

		}, 0, TICK_RATE);

		setTitle("Application");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}
}

class CollisionReturn {
	public boolean colliding() {
		return valid_change_x || valid_change_y;
	};

	public double change_x;
	public boolean valid_change_x;
	public double change_y;
	public boolean valid_change_y;
}

class ConsoleLine {
	ConsoleLine(int owner, String text) {
		this.text = text;
		this.owner = owner;
	}

	public String text;
	public int owner;
}