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

import game.templates.Bullet;
import game.templates.DynamicGameObject;
import game.templates.GameObject;
import game.templates.Player;
import game.templates.Point;
import game.templates.TileObject;
import game.templates.Zombie;
import util.ImageUtil;
import util.KeyPressEvent;
import util.MathUtil;
import util.MouseEvent;
import util.TileAction;
import util.TypeEvent;

public class Application extends JFrame {
	// App Constants
	// static private long STARTTIME = System.currentTimeMillis();
	final float FPS = 60;
	final int TICK_RATE = 10; // TICK RATE
	GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice device = env.getDefaultScreenDevice();
	GraphicsConfiguration config = device.getDefaultConfiguration();
	Font def_font = new Font("Arial", Font.PLAIN, 24);
	Font console_font = new Font("Arial", Font.PLAIN, 16);

	// Application Semi-Constants
	int CANW = 1280;
	int CANH = 720;
	static Peripherals PERI;
	static AssetManager AMGR;
	Player p;
	List<Zombie> zombies = new ArrayList<Zombie>();

	public JPanel panel = new JPanel() {
		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			// super.paintComponent(g);

			// Custom Screen Options
			RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			rh.put(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);

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
	Tile level[][] = new Tile[LEVEL_W][LEVEL_H];

	List<Bullet> bullets = new ArrayList<Bullet>();

	// double viewing_angle = Math.PI / 6;

	void paint_(Graphics g_base) {
		Graphics2D g = (Graphics2D) g_base;

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int) CANW, (int) CANH);

		g.setFont(def_font);

		int COLUMNS = (int) ((int) Math.ceil(CANW / TILEBASESIZE) / zoom_mult) + 2;
		int ROWS = (int) ((int) Math.ceil(CANH / TILEBASESIZE) / zoom_mult) + 2;
		double tilesize = TILEBASESIZE * zoom_mult;

		new TileAction() {
			@Override
			public int action(float x, float y, double dx, double dy, int ix, int iy,
					int px, int py) {

				if (px >= 0 && px < LEVEL_W && py >= 0 && py < LEVEL_H) {
					Tile tile = level[px][py];
					if (tile == null) {

						// g.drawImage(AMGR.getAsset("debug_grad_floor").src, (int) dx, (int) dy,
						// (int) tilesize + TILE_BUFFER,
						// (int) tilesize + TILE_BUFFER,
						// new Color(0, 0, 0, 0), null);

						g.setColor(Color.lightGray);
						g.fillRect((int) dx, (int) dy, (int) (tilesize + TILE_BUFFER), (int) (tilesize + TILE_BUFFER));
					} else {

						g.drawImage(AMGR.getAsset(level[px][py].floor).src, (int) dx, (int) dy,
								(int) tilesize + TILE_BUFFER, (int) tilesize + TILE_BUFFER,
								new Color(0, 0, 0, 0), null);

						// g.setColor(new Color(48, 173, 23));
						// g.fillRect((int) dx, (int) dy, (int) tilesize + TILE_BUFFER, (int) tilesize +
						// TILE_BUFFER);
						tile.DrawTileObjects(g, dx, dy, tilesize);
					}
				}

				g.setStroke(new BasicStroke(1));
				g.setColor(Color.BLACK);
				g.drawRect((int) dx, (int) dy, (int) tilesize, (int) tilesize);
				return 0;
			}
		}.run(ROWS, COLUMNS, camera_x, camera_y, tilesize);

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
			g.setColor(Color.RED);
			g.drawRect((int) b.x, (int) b.y, (int) b.width, (int) b.height);
		}

		g.setColor(new Color(255, 0, 0, 40));
		g.fillRect(0, (int) (CANH * (1 - transition_edge)), CANW, (int) (CANH * transition_edge));
		g.fillRect(0, 0, CANW, (int) (CANH * transition_edge));

		g.setColor(new Color(0, 255, 0, 40));
		g.fillRect(0, 0, (int) (CANW * transition_edge), CANH);
		g.fillRect((int) (CANW * (1 - transition_edge)), 0, (int) (CANW * transition_edge), CANH);

		g.setColor(new Color(0, 0, 255, 40));
		g.fillRect(0, 0, (int) (CANW * transition_corner), (int) (CANH * transition_corner));
		g.fillRect(0, (int) (CANH * (1 - transition_corner)), (int) (CANW * transition_corner),
				(int) (CANH * transition_corner));
		g.fillRect((int) (CANW * (1 - transition_corner)), (int) (CANH * (1 - transition_corner)),
				(int) (CANW * transition_corner), (int) (CANH * transition_corner));
		g.fillRect((int) (CANW * (1 - transition_corner)), 0, (int) (CANW * transition_corner),
				(int) (CANH * transition_corner));

		p.paint(g);

		for (Zombie z : zombies) {
			z.paint(g);
		}
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

			boolean top_intersect = bottom_right.y >= object_top_left.y && top_left.y < object_top_left.y;
			boolean bottom_intersect = top_left.y <= object_bottom_right.y && bottom_right.y > object_bottom_right.y;
			boolean center_intersect_y = bottom_right.y <= object_bottom_right.y && top_left.y >= object_top_left.y;

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			boolean inline_x = top_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if (inline_y && a.top() <= b.bottom() && a.top() + dy > b.bottom()) {
				ret.valid_change_y = true;
				ret.change_y = Math.ceil(a.top() - b.bottom());
				return ret;
			} else if (inline_x && a.right() <= b.left() && a.right() + dx > b.left()) {
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

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			// check if object is valid before move
			if (inline_y && a.top() <= b.bottom() && a.top() + dy > b.bottom()) {
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

			boolean top_intersect = bottom_left.y >= object_top_right.y && top_left.y < object_top_right.y;
			boolean bottom_intersect = top_left.y <= object_bottom_right.y && bottom_left.y > object_bottom_right.y;
			boolean center_intersect_y = bottom_left.y <= object_bottom_right.y && top_left.y >= object_top_right.y;

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			boolean inline_x = top_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if (inline_y && a.top() <= b.bottom() && a.top() + dy > b.bottom()) {
				ret.valid_change_y = true;
				ret.change_y = Math.ceil(a.top() - b.bottom());
			} else if (inline_x && a.left() >= b.right() && a.left() + dx < b.right()) {
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

			boolean inline_x = top_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if (inline_x && a.left() >= b.right() && a.left() + dx < b.right()) {
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

			boolean top_intersect = bottom_left.y >= object_top_right.y && top_left.y < object_top_right.y;
			boolean bottom_intersect = top_left.y <= object_bottom_right.y && bottom_left.y > object_bottom_right.y;
			boolean center_intersect_y = bottom_left.y <= object_bottom_right.y && top_left.y >= object_top_right.y;

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			boolean inline_x = top_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if (inline_y && a.bottom() <= b.top() && a.bottom() - dy > b.top()) {
				ret.valid_change_y = true;
				ret.change_y = Math.ceil(a.bottom() - b.top());
			} else if (inline_x && a.left() >= b.right() && a.left() + dx < b.right()) {
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

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			// check if object is valid before move
			if (inline_y && a.bottom() <= b.top() && a.bottom() - dy > b.top()) {
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

			boolean top_intersect = bottom_left.y >= object_top_right.y && top_right.y < object_top_right.y;
			boolean bottom_intersect = top_right.y <= object_bottom_left.y && bottom_left.y > object_bottom_left.y;
			boolean center_intersect_y = bottom_left.y <= object_bottom_left.y && top_right.y >= object_top_right.y;

			boolean inline_y = left_intersect || right_intersect || center_intersect_x;
			boolean inline_x = top_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if (inline_y && a.bottom() <= b.top() && a.bottom() - dy > b.top()) {
				ret.valid_change_y = true;
				ret.change_y = Math.ceil(a.bottom() - b.top());
			} else if (inline_x && a.right() <= b.left() && a.right() + dx > b.left()) {
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

			boolean inline_x = top_intersect || bottom_intersect || center_intersect_y;
			// check if object is valid before move
			if (inline_x && a.right() <= b.left() && a.right() + dx > b.left()) {
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
		int COLUMNS = (int) ((int) Math.ceil(CANW / TILEBASESIZE) / zoom_mult) + 2;
		int ROWS = (int) ((int) Math.ceil(CANH / TILEBASESIZE) / zoom_mult) + 2;
		double tilesize = TILEBASESIZE * zoom_mult;

		if (!console_toggle && PERI.keyPressed(KeyEvent.VK_MINUS)) {
			console_toggle = true;
			console_up = !console_up;
			System.out.println("toggled");
			current_command = "";
		}
		if (!PERI.keyPressed(KeyEvent.VK_MINUS)) {
			console_toggle = false;
		}
		if (console_up)
			return;

		int intent_x = 0;
		int intent_y = 0;
		if (PERI.keyPressed('a'))
			intent_x--;
		if (PERI.keyPressed('d'))
			intent_x++;
		if (PERI.keyPressed('w'))
			intent_y++;
		if (PERI.keyPressed('s'))
			intent_y--;

		if (intent_x != 0 || intent_y != 0) {
			double sprint = PERI.keyPressed(KeyEvent.VK_SHIFT) && p.stamina > 20 ? p.SPRINT_SPEED_MULT() : 1;
			double intent_direction = Math.atan2(intent_y, intent_x);
			p.angle = intent_direction;

			displacement_x = Math.cos(intent_direction) * p.BASE_SPEED() * sprint;
			displacement_y = Math.sin(intent_direction) * p.BASE_SPEED() * sprint;
			p.magnitude = p.BASE_SPEED() * sprint;

		} else {
			displacement_x = 0;
			displacement_y = 0;
		}
		System.out.println(String.format("x=%d y=%d", (int)((camera_x+p.x+p.width/2)/tilesize), (int)((camera_y+p.y+p.height/2)/tilesize)));
		if (Math.sqrt(Math.pow(mouse_y - p.centery(), 2) + Math.pow(mouse_x - p.centerx(), 2)) > p.width / 2) {
			p.look_angle = Math.atan2((mouse_y - p.centery()), (mouse_x - p.centerx()));
		}

		if (PERI.keyPressed(KeyEvent.VK_SHIFT)) {
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
			b.angle = p.look_angle;
			bullets.add(b);

		}
		if (animating_screen == false) {
			if (p.centerx() > CANW * (1 - transition_edge)) {
				animating_screen = true;
				animation_pos = 0;
				anim_speed_x = anim_base_speed;
				anim_speed_y = 0;
			}
			if (p.centerx() < CANW * transition_edge) {
				animating_screen = true;
				animation_pos = 0;
				anim_speed_x = -anim_base_speed;
				anim_speed_y = 0;
			}
			if (p.centery() < CANH * transition_edge) {
				animating_screen = true;
				animation_pos = 0;
				anim_speed_y = -anim_base_speed;
				anim_speed_x = 0;
			}
			if (p.centery() > CANH * (1 - transition_edge)) {
				animating_screen = true;
				animation_pos = 0;
				anim_speed_y = anim_base_speed;
				anim_speed_x = 0;
			}
			if (p.centery() > CANH * (1 - transition_corner) && p.centerx() > CANW * (1 - transition_corner)) {// bottom
																												// right
				animating_screen = true;
				animation_pos = 0;
				anim_speed_y = anim_base_speed / Math.sqrt(2);
				anim_speed_x = anim_base_speed / Math.sqrt(2);
			}
			if (p.centery() < CANH * transition_corner && p.centerx() > CANW * (1 - transition_corner)) {// top
																											// right
				animating_screen = true;
				animation_pos = 0;
				anim_speed_y = -anim_base_speed / Math.sqrt(2);
				anim_speed_x = anim_base_speed / Math.sqrt(2);
			}
			if (p.centery() < CANH * transition_corner && p.centerx() < CANW * transition_corner) {// top left
				animating_screen = true;
				animation_pos = 0;
				anim_speed_y = -anim_base_speed / Math.sqrt(2);
				anim_speed_x = -anim_base_speed / Math.sqrt(2);
			}
			if (p.centery() > CANH * (1 - transition_corner) && p.centerx() < CANW * transition_corner) {// bottom
																											// left
				animating_screen = true;
				animation_pos = 0;
				anim_speed_y = anim_base_speed / Math.sqrt(2);
				anim_speed_x = -anim_base_speed / Math.sqrt(2);
			}
		} else {
			camera_x += anim_speed_x;
			p.x -= anim_speed_x;
			camera_y += anim_speed_y;
			p.y -= anim_speed_y;
			for (int i = 0; i < bullets.size(); i++) {
				Bullet b = bullets.get(i);
				b.x -= anim_speed_x;
				b.y -= anim_speed_y;
				bullets.set(i, b);
			}
			for (int i = 0; i < zombies.size(); i++) {
				Zombie z = zombies.get(i);
				z.x -= anim_speed_x;
				z.y -= anim_speed_y;
				zombies.set(i, z);
			}

			animation_pos += 1;
			if (animation_pos >= anim_timer)
				animating_screen = false;
		}

		int returns_[] = new TileAction() {
			@Override
			public int action(float x, float y, double dx, double dy, int ix, int iy,
					int px, int py) {

				int retx = 0;
				int rety = 0;

				if (px >= 0 && px < LEVEL_W && py >= 0 && py < LEVEL_H) {
					if (level[px][py] != null) {
						Tile tile = level[px][py];

						for (TileObject o : tile.tileObjects) {
							if (!o.collider)
								continue;
							GameObject o1 = o.toGameObject(dx, dy, tilesize);
							CollisionReturn ret = staticDynamicCollision(p, o1);
							if (ret.valid_change_x) {
								displacement_x = ret.change_x;
								retx = 1;
							}
							if (ret.valid_change_y) {
								displacement_y = ret.change_y;
								rety = 1;
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
				return (int) (retx + 2 * rety);
				// 0 = stop none
				// 1 = stop retx
				// 2 = stop rety
				// 3 = stop bothretx and rety

			}
		}.run(ROWS, COLUMNS, camera_x, camera_y, tilesize);

		int val_ = MathUtil.maxInList(returns_);
		// System.out.println(String.format("%.2f %.2f", displacement_x,
		// displacement_y));
		switch (val_) {
			case 0:
				p.x += displacement_x;
				p.y -= displacement_y;
				break;
			case 1:
				p.y -= displacement_y;
				p.x += displacement_x * p.WALL_FRICTION_CONST;
				break;
			case 2:
				p.x += displacement_x;
				p.y -= displacement_y * p.WALL_FRICTION_CONST;
				break;
			case 3:
				break;
		}
		for (int i = 0; i < bullets.size(); i++) {
			Bullet b = bullets.get(i);
			b.x += Math.cos(b.angle) * b.magnitude;
			b.y += Math.sin(b.angle) * b.magnitude;
			bullets.set(i, b);
			if (b.x + b.width < 0 || b.x > CANW || b.y + b.height < 0 || b.y > CANH) {
				bullets.remove(i);
				i--;
			}
			for (int z1 = 0; z1 < zombies.size(); z1++) {
				Zombie z = zombies.get(z1);
				CollisionReturn ret = staticDynamicCollision(b, z);
				if (ret.colliding()) {
					System.out.println("hit zombie");
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
			if (level[ix][iy] == null)
				level[ix][iy] = new Tile();

			level[ix][iy].floor = AMGR.assetID(assetname);
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
			if (level[ix][iy] == null)
				level[ix][iy] = new Tile();

			level[ix][iy].addTileObject(wall);
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
			if (level[ix][iy] == null)
				return "tile is null";

			int count = level[ix][iy].tileObjects.size();
			level[ix][iy].tileObjects.clear();
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
		}
		return "null";
	}

	void ImportAssets() {
		AMGR.addAsset("debug_grad_floor", ImageUtil.ImageFile("assets/debug_grad.jpeg"));
		AMGR.addAsset("grass", ImageUtil.ImageFile("assets/grass.jpeg"));
		AMGR.addAsset("sand", ImageUtil.ImageFile("assets/sand.jpeg"));
		AMGR.addAsset("stone", ImageUtil.ImageFile("assets/stone.jpeg"));
		AMGR.addAsset("wood", ImageUtil.ImageFile("assets/wood.jpeg"));
		AMGR.addAsset("tree", ImageUtil.ImageFile("assets/tree.png"));
		AMGR.addAsset("fern", ImageUtil.ImageFile("assets/fern.png"));
		AMGR.addAsset("weeds", ImageUtil.ImageFile("assets/weeds.png"));
	}

	public void addEventHooks() {
		PERI.addMouseMoveHook(new MouseEvent() {

			@Override
			public void action(double x, double y) {
				mouse_x = x;
				mouse_y = y;
			}

		});

		PERI.addMouseClickHook(new MouseEvent() {

			@Override
			public void action(double x, double y) {
				mouse_clicked = true;
				mouse_click_x = x;
				mouse_click_y = y;
			}

		});
		PERI.addTypeHook(new TypeEvent() {

			@Override
			public void action(char c, int keycode) {
				if (console_up && (Character.isLetterOrDigit(c) || Character.isSpaceChar(c))) {
					current_command += c;
				}
			}

		});
		PERI.addKeyPressHook(new KeyPressEvent() {

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
					if (!PERI.keyPressed(KeyEvent.VK_BACK_SPACE)) {
						backspace_toggle = false;
					}
				}
			}

		});
	}

	TileObject wall;

	void InitializeLevel() {
		wall = new TileObject(0, 0, 1, 1, AMGR.assetID("stone"), true) {

			@Override
			public void paint(Graphics2D g, double dx, double dy, double gridsize) {
				g.drawImage(AMGR.getAsset(asset).src, (int) (dx),
						(int) (dy), (int) (gridsize), (int) (gridsize),
						null);
			}

		};
		for (int y = 0; y < LEVEL_H; y++) {
			for (int x = 0; x < LEVEL_W; x++) {
				Tile t = new Tile();
				t.floor = AMGR.assetID("grass");
				if (Math.random() > 0.95f) {// TREE
					double minsize = 0.4;
					double size = minsize + Math.random() * 0.4;
					double x_ = Math.random() * (1 - size);
					double y_ = Math.random() * (1 - size);
					TileObject tree = new TileObject(x_, y_, size, size, AMGR.assetID("tree"), true) {

						@Override
						public void paint(Graphics2D g, double dx, double dy, double gridsize) {
							g.drawImage(AMGR.getAsset(asset).src, (int) (dx + lx * gridsize),
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
					TileObject fern = new TileObject(x_, y_, size, size, AMGR.assetID("fern"), false) {

						@Override
						public void paint(Graphics2D g, double dx, double dy, double gridsize) {
							Asset a = AMGR.getAsset(asset);
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
					TileObject weeds = new TileObject(x_, y_, size, size, AMGR.assetID("weeds"), false) {

						@Override
						public void paint(Graphics2D g, double dx, double dy, double gridsize) {
							Asset a = AMGR.getAsset(asset);
							g.drawImage(a.src, (int) (dx + lx * gridsize), (int) (dy + ly * gridsize),
									(int) (lwidth * gridsize), (int) (a.scaledHeight(lwidth * gridsize)), null);
						}

					};
					t.addTileObject(weeds);
				}
				level[x][y] = t;
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

		PERI = new Peripherals();
		AMGR = new AssetManager();
		this.addKeyListener(PERI);
		panel.addMouseListener(PERI);
		panel.addMouseMotionListener(PERI);
		panel.addMouseWheelListener(PERI);

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

class Tile {
	Tile() {

	}

	Tile(AssetManager amgr, String floorname) {
		floor = amgr.assetID(floorname);
	}

	int floor = -1;
	List<TileObject> tileObjects = new ArrayList<TileObject>();

	public void addTileObject(TileObject tile) {
		tileObjects.add(tile);
	}

	public void DrawTileObjects(Graphics2D g, double dx, double dy, double tilesize) {
		for (TileObject o : tileObjects) {
			o.paint(g, dx, dy, tilesize);
		}
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