package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import game.templates.Bullet;
import game.templates.DynamicGameObject;
import game.templates.GameObject;
import game.templates.Player;
import game.templates.Point;
import game.templates.TileObject;
import util.ImageUtil;
import util.MathUtil;
import util.MouseEvent;
import util.TileAction;

public class Application extends JFrame {
	// App Constants
	// static private long STARTTIME = System.currentTimeMillis();
	final float FPS = 60;
	final int TICK_RATE = 10; // TICK RATE
	GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice device = env.getDefaultScreenDevice();
	GraphicsConfiguration config = device.getDefaultConfiguration();

	// Application Semi-Constants
	int CANW = 650;
	int CANH = 500;
	static Peripherals PERI;
	static AssetManager AMGR;
	Player p;

	public JPanel panel = new JPanel() {
		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			// super.paintComponent(g);

			// Custom Screen Options
			RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);

			rh.put(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_SPEED);

			g2d.setRenderingHints(rh);

			// BufferedImage canvas = new BufferedImage(CANW, CANH,
			// BufferedImage.TYPE_INT_RGB);

			paint_(g2d);
		}
	};

	final int TILEBASESIZE = 40;
	double zoom_mult = 1;
	final int TILE_BUFFER = 3;
	double camera_x = 0;
	double camera_y = 0;
	double mouse_x = 0;
	double mouse_y = 0;
	boolean mouse_clicked = false;
	double mouse_click_x;
	double mouse_click_y;

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

		int COLUMNS = (int) ((int) Math.ceil(CANW / TILEBASESIZE) / zoom_mult) + 2;
		int ROWS = (int) ((int) Math.ceil(CANH / TILEBASESIZE) / zoom_mult) + 2;
		double tilesize = TILEBASESIZE * zoom_mult;

		new TileAction() {
			@Override
			public int action(float x, float y, double dx, double dy, int ix, int iy,
					int px, int py) {

				if (px >= 0 && px < LEVEL_W && py >= 0 && py < LEVEL_H) {
					//System.out.println(String.format("px=%d py=%d", px, py));
					if (level[px][py] == null) {
						g.drawImage(AMGR.getAsset("debug_grad_floor").src, (int) dx, (int) dy, (int) tilesize + TILE_BUFFER,
								(int) tilesize + TILE_BUFFER,
								new Color(0, 0, 0, 0), null);
					} else {
						g.drawImage(AMGR.getAsset(level[px][py].floor).src, (int) dx, (int) dy,
								(int) tilesize + TILE_BUFFER, (int) tilesize + TILE_BUFFER,
								new Color(0, 0, 0, 0), null);
						TileObject tree = level[px][py].tree;
						if (level[px][py].showtree) {
							tree.paint(g, dx, dy, tilesize);
						}
					}
				}

				g.drawRect((int) dx, (int) dy, (int) tilesize, (int) tilesize);
				return 0;
			}
		}.run(ROWS, COLUMNS, camera_x, camera_y, tilesize);

		g.setColor(Color.GREEN);
		g.drawLine((int) p.centerx(), (int) p.centery(), (int) mouse_x, (int) mouse_y);
		g.fillRect((int) mouse_x - 4, (int) mouse_y - 4, 8, 8);

		g.setColor(Color.RED);
		g.drawLine((int) p.centerx(), (int) p.centery(), (int) (Math.cos(p.angle) * 10 + p.centerx()),
				(int) (Math.sin(p.angle) * 10 + p.centery()));
		g.fillRect((int) (Math.cos(p.angle) * 50 + p.centerx() - 4), (int) (Math.sin(p.angle) * 50 + p.centery() - 4),
				8, 8);

		for (Bullet b : bullets) {
			b.paint(g);
			g.setColor(Color.RED);
			g.drawRect((int) b.x, (int) b.y, (int) b.width, (int) b.height);
		}

		g.setColor(new Color(255,0,0,40));
		g.fillRect(0, (int)(CANH*(1-transition_edge)), CANW, (int)(CANH*transition_edge));
		g.fillRect(0, 0, CANW, (int)(CANH*transition_edge));

		g.setColor(new Color(0,255,0,40));
		g.fillRect(0, 0, (int)(CANW*transition_edge), CANH);
		g.fillRect((int)(CANW*(1-transition_edge)), 0, (int)(CANW*transition_edge), CANH);

		g.setColor(new Color(0,0,255,40));
		g.fillRect(0, 0, (int)(CANW*transition_corner), (int)(CANH*transition_corner));
		g.fillRect(0, (int)(CANH*(1-transition_corner)), (int)(CANW*transition_corner), (int)(CANH*transition_corner));
		g.fillRect((int)(CANW*(1-transition_corner)), (int)(CANH*(1-transition_corner)), (int)(CANW*transition_corner), (int)(CANH*transition_corner));
		g.fillRect((int)(CANW*(1-transition_corner)), 0, (int)(CANW*transition_corner), (int)(CANH*transition_corner));

		p.paint(g);
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
		final double COLLISION_BUFFER = 4;
		CollisionReturn ret = new CollisionReturn();

		double cx = Math.cos(a.angle) * a.magnitude;
		double cy = Math.sin(a.angle) * a.magnitude;

		Point[] points = RotatePoints(a.angle, b.getObjectPoints());
		for (Point p : points) {
			//System.out.println(String.format("%.1f %.1f", p.x, p.y));
		}

		// boolean inline_x = (a.bottom()+cy+COLLISION_BUFFER > b.top()
		// && a.top()+cy+COLLISION_BUFFER < b.top())
		// || (a.top()+cy-COLLISION_BUFFER < b.bottom()
		// && a.bottom()+cy+COLLISION_BUFFER > b.bottom());
		// boolean inline_y = (a.right()+cx+COLLISION_BUFFER > b.left()
		// && a.left()+cx-COLLISION_BUFFER < b.left())
		// || (a.left()+cx-COLLISION_BUFFER < b.right()
		// && a.right()+cx+COLLISION_BUFFER > b.right());

		// if (inline_x && Math.abs(Math.sin(a.angle))>0.1) {// may be colliding
		// horizontally
		// if (a.left()+cx-COLLISION_BUFFER < b.right() && a.right()+cx+COLLISION_BUFFER
		// > b.right()) {// left into right side
		// ret.left = a.left() - b.right();
		// a.x = b.right();
		// }
		// if (a.right()+cx +COLLISION_BUFFER> b.left() && a.left()+cx-COLLISION_BUFFER
		// < b.left()) {// right into left side
		// ret.right = b.right() - a.left();
		// a.x = b.left() - a.width;
		// }

		// }

		// if (inline_y) {// may be colliding vertically
		// if (a.bottom()+cx+COLLISION_BUFFER > b.top() && a.top()+cx-COLLISION_BUFFER <
		// b.top()) {// bottom into top side
		// ret.top = b.top() - a.bottom();
		// a.y = b.top() - a.height;
		// }
		// if (a.top()+cx-COLLISION_BUFFER< b.bottom() && a.bottom()+cx+COLLISION_BUFFER
		// > b.bottom()) {// top into bottom side
		// ret.bottom = a.top() - b.bottom();
		// a.y = b.bottom();
		// }
		// }
		ret.colliding = ret.top != -1 || ret.left != -1 || ret.bottom != -1 || ret.right != -1;
		return ret;
	}

	boolean DynamicDynamicCollision(DynamicGameObject a, DynamicGameObject b) {
		return false;
	}

	double animation_pos = 0;
	boolean animating_screen = false;
	double anim_speed_x = 0;
	double anim_base_speed = 0.045;
	double anim_speed_y = 0;
	double anim_timer = 150;
	void tick(int tr) {
		int COLUMNS = (int) ((int) Math.ceil(CANW / TILEBASESIZE) / zoom_mult) + 2;
		int ROWS = (int) ((int) Math.ceil(CANH / TILEBASESIZE) / zoom_mult) + 2;
		double tilesize = TILEBASESIZE * zoom_mult;

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

		double displacement_x = 0;
		double displacement_y = 0;
		if (intent_x != 0 || intent_y != 0) {
			float sprint = PERI.keyPressed(KeyEvent.VK_SHIFT) ? p.SPRINT_MULT : 1;
			double intent_direction = Math.atan2(intent_y, intent_x);
			displacement_x = Math.cos(intent_direction) * p.speed() * sprint;
			displacement_y = Math.sin(intent_direction) * p.speed() * sprint;

		}

		if (mouse_clicked) {
			mouse_clicked = false;
			System.out.println("clicked");
			Bullet b = new Bullet();
			b.x = p.centerx() + Math.cos(p.angle) * p.width / 2;
			b.y = p.centery() + Math.sin(p.angle) * p.width / 2;
			b.angle = p.angle;
			bullets.add(b);

		}

		if(p.centerx()>CANW*(1-transition_edge)){
			animating_screen = true;
			animation_pos = 0;
			anim_speed_x=anim_base_speed;
			anim_speed_y=0;
		}
		if(p.centerx()<CANW*transition_edge){
			animating_screen = true;
			animation_pos = 0;
			anim_speed_x = -anim_base_speed;
			anim_speed_y=0;
		}
		if(p.centery()<CANH*transition_edge){
			animating_screen = true;
			animation_pos = 0;
			anim_speed_y = -anim_base_speed;
			anim_speed_x=0;
		}
		if(p.centery()>CANH*(1-transition_edge)){
			animating_screen = true;
			animation_pos = 0;
			anim_speed_y = anim_base_speed;
			anim_speed_x=0;
		}
		if(p.centery()>CANH*(1-transition_corner) && p.centerx()>CANW*(1-transition_corner)){//bottom right
			animating_screen = true;
			animation_pos = 0;
			anim_speed_y = anim_base_speed/Math.sqrt(2);
			anim_speed_x= anim_base_speed/Math.sqrt(2);
		}
		if(p.centery()<CANH*transition_corner && p.centerx()>CANW*(1-transition_corner)){//top right
			animating_screen = true;
			animation_pos = 0;
			anim_speed_y = -anim_base_speed/Math.sqrt(2);
			anim_speed_x= anim_base_speed/Math.sqrt(2);
		}
		if(p.centery()<CANH*transition_corner && p.centerx()<CANW*transition_corner){//top left
			animating_screen = true;
			animation_pos = 0;
			anim_speed_y = -anim_base_speed/Math.sqrt(2);
			anim_speed_x= -anim_base_speed/Math.sqrt(2);
		}
		if(p.centery()>CANH*(1-transition_corner) && p.centerx()<CANW*transition_corner){//bottom left
			animating_screen = true;
			animation_pos = 0;
			anim_speed_y = anim_base_speed/Math.sqrt(2);
			anim_speed_x= -anim_base_speed/Math.sqrt(2);
		}

		if(animating_screen){
			camera_x+=anim_speed_x;
			p.x-=anim_speed_x*tilesize;
			for(int i = 0;i<bullets.size();i++){
				Bullet b = bullets.get(i);
				b.x-=anim_speed_x*tilesize;
				bullets.set(i, b);
			}
			camera_y+=anim_speed_y;
			p.y-=anim_speed_y*tilesize;
			for(int i = 0;i<bullets.size();i++){
				Bullet b = bullets.get(i);
				b.x-=anim_speed_y*tilesize;
				bullets.set(i, b);
			}
			animation_pos+=1;
			if(animation_pos>=anim_timer)
				animating_screen = false;
		}

		for (int i = 0; i < bullets.size(); i++) {
			Bullet b = bullets.get(i);
			b.x += Math.cos(b.angle) * b.magnitude;
			b.y += Math.sin(b.angle) * b.magnitude;
			bullets.set(i, b);
			if (b.x < 0 || b.x > CANW || b.y < 0 || b.y > CANH) {
				bullets.remove(i);
				System.out.println(String.format("Removed bullet (%d)", i));
				i--;

			}
		}

		final double dis_x = displacement_x;
		final double dis_y = displacement_y;
		int returns_[] = new TileAction() {
			@Override
			public int action(float x, float y, double dx, double dy, int ix, int iy,
					int px, int py) {

				int retx = 0;
				int rety = 0;

				if (px >= 0 && px < LEVEL_W && py >= 0 && py < LEVEL_H) {
					if (level[px][py] != null) {
						Tile tile = level[px][py];
						TileObject obj = tile.tree;

						GameObject obj2 = new GameObject() {
							@Override
							public void paint(Graphics2D g) {
							}
						};
						obj2.x = dx + obj.lx * tilesize;
						obj2.y = dy + obj.ly * tilesize;
						obj2.width = obj.lwidth * tilesize;
						obj2.height = obj.lheight * tilesize;

						for (int i = 0; i < bullets.size(); i++) {
							Bullet b_ = bullets.get(i);
							CollisionReturn ret = staticDynamicCollision(b_, obj2);
							if (ret.colliding) {
								b_.magnitude = 0;
								bullets.set(i, b_);
							}
						}
						// player collision
						{/*
							 * int intent_x = (int) (Math.round(dis_x / movement_base_speed)) == 0 ? 0
							 * : dis_x > 0 ? 1 : -1;
							 * int intent_y = (int) (Math.round(dis_y / movement_base_speed)) == 0 ? 0
							 * : dis_y > 0 ? 1 : -1;
							 * 
							 * boolean inline_x1 = (player_y + PLAYER_SIZE / 2 > ot - OUTER_COLLISION_BUFFER
							 * && player_y - PLAYER_SIZE / 2 < ot + OUTER_COLLISION_BUFFER)
							 * || (player_y - PLAYER_SIZE / 2 < ob + OUTER_COLLISION_BUFFER
							 * && player_y + PLAYER_SIZE > ob - OUTER_COLLISION_BUFFER);
							 * boolean inline_y1 = (player_x + PLAYER_SIZE / 2 > ol - OUTER_COLLISION_BUFFER
							 * && player_x - PLAYER_SIZE / 2 < ol + OUTER_COLLISION_BUFFER)
							 * || (player_x - PLAYER_SIZE / 2 < or + OUTER_COLLISION_BUFFER
							 * && player_x + PLAYER_SIZE > or - OUTER_COLLISION_BUFFER);
							 * 
							 * boolean inline_x = (player_y + PLAYER_SIZE / 2 > ot && player_y - PLAYER_SIZE
							 * / 2 < ot)
							 * || (player_y - PLAYER_SIZE / 2 < ob && player_y + PLAYER_SIZE > ob);
							 * boolean inline_y = (player_x + PLAYER_SIZE / 2 > ol && player_x - PLAYER_SIZE
							 * / 2 < ol)
							 * || (player_x - PLAYER_SIZE / 2 < or && player_x + PLAYER_SIZE > or);
							 * 
							 * if (Math.sqrt(Math.pow(player_x - ol + obj.width / 2 * tilesize, 2)
							 * + Math.pow(player_y - ot + obj.height / 2 * tilesize, 2)) < (obj.height +
							 * obj.width)
							 * / 4 * tilesize + OUTER_COLLISION_BUFFER) {
							 * // System.out.println("something");
							 * }
							 * 
							 * if (intent_x == 1 && inline_x1 && player_x + PLAYER_SIZE / 2 > ol -
							 * OUTER_COLLISION_BUFFER
							 * && player_x - PLAYER_SIZE / 2 < ol - OUTER_COLLISION_BUFFER) {
							 * retx = 1;
							 * }
							 * if (intent_x == -1 && inline_x1 && player_x - PLAYER_SIZE / 2 < or +
							 * OUTER_COLLISION_BUFFER
							 * && player_x + PLAYER_SIZE / 2 > or - OUTER_COLLISION_BUFFER) {
							 * retx = 1;
							 * }
							 * if (intent_y == -1 && inline_y1 && player_y + PLAYER_SIZE / 2 > ot -
							 * OUTER_COLLISION_BUFFER
							 * && player_y - PLAYER_SIZE / 2 < ot + OUTER_COLLISION_BUFFER) {
							 * rety = 1;
							 * }
							 * if (intent_y == 1 && inline_y1 && player_y - PLAYER_SIZE / 2 < ob +
							 * OUTER_COLLISION_BUFFER
							 * && player_y + PLAYER_SIZE / 2 > ob - OUTER_COLLISION_BUFFER) {
							 * rety = 1;
							 * }
							 * System.out.println(
							 * String.format("x=%b y=%b x=%d y=%d rx=%.1f ry=%.1f retx=%d rety=%d",
							 * inline_x,
							 * inline_y,
							 * intent_x, intent_y, dis_x, dis_y, retx, rety));
							 * 
							 * if (intent_x == 1 && inline_x && player_x + dis_x + PLAYER_SIZE / 2 > ol
							 * && player_x + dis_x - PLAYER_SIZE / 2 < ol) {
							 * player_x = ol - PLAYER_SIZE / 2 - COLLISION_BUFFER;
							 * retx = 1;
							 * }
							 * if (intent_x == -1 && inline_x && player_x + dis_x - PLAYER_SIZE / 2 < or
							 * && player_x + dis_x + PLAYER_SIZE / 2 > or) {
							 * player_x = or + PLAYER_SIZE / 2 + COLLISION_BUFFER;
							 * retx = 1;
							 * }
							 * if (intent_y == -1 && inline_y && player_y + dis_y + PLAYER_SIZE / 2 > ot
							 * && player_y + dis_y - PLAYER_SIZE / 2 < ot) {
							 * player_y = ot - PLAYER_SIZE / 2 + COLLISION_BUFFER;
							 * rety = 1;
							 * }
							 * if (intent_y == 1 && inline_y && player_y + dis_y - PLAYER_SIZE / 2 < ob
							 * && player_y + dis_y + PLAYER_SIZE / 2 > ob) {
							 * player_y = ob - PLAYER_SIZE / 2 + COLLISION_BUFFER;
							 * rety = 1;
							 * }
							 * }
							 */

							// System.out.println(String.format("p=(%.1f,%.1f) ol=%.1f %or=%.1f", player_x,
							// player_y, ol, or));
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
		switch (val_) {
			case 0:
				p.x += displacement_x;
				p.y -= displacement_y;
				break;
			case 1:
				p.y += displacement_y;
				break;
			case 2:
				p.x -= displacement_x;
				break;
			case 3:
				break;
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

	void ImportAssets() {
		AMGR.addAsset("debug_grad_floor", ImageUtil.ImageFile("assets/debug_grad.jpeg"));
		AMGR.addAsset("grass", ImageUtil.ImageFile("assets/grass.jpeg"));
		AMGR.addAsset("sand", ImageUtil.ImageFile("assets/sand.jpeg"));
		AMGR.addAsset("stone", ImageUtil.ImageFile("assets/stone.jpeg"));
		AMGR.addAsset("wood", ImageUtil.ImageFile("assets/wood.jpeg"));
		AMGR.addAsset("tree", ImageUtil.ImageFile("assets/tree.png"));
	}

	public void addEventHooks() {
		PERI.addMouseMoveHook(new MouseEvent() {

			@Override
			public void action(double x, double y) {
				mouse_x = x;
				mouse_y = y;
				p.angle = Math.atan2((y - p.centery()), (x - p.centerx()));
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
		/*
		 * PERI.addScrollHook(new ScrollEvent() {
		 * 
		 * @Override
		 * public void action(double val, double x, double y) {
		 * double change = zoom_speed * val;
		 * zoom += change;
		 * zoom_mult = Math.exp(zoom);
		 * 
		 * }
		 * 
		 * });
		 */
	}

	void InitializeLevel(){
		for(int y=0;y<LEVEL_H;y++){
			for(int x=0;x<LEVEL_W;x++){
				Tile t = new Tile();
				t.floor = AMGR.assetID("grass");
				if(Math.random()>0.9f)
					t.showtree = true;
				else
					t.showtree = false;
				level[x][y] = t;
			}
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
		defineTree();
	}

	Tile(AssetManager amgr, String floorname) {
		floor = amgr.assetID(floorname);
		defineTree();
	}

	void defineTree() {
		tree = new TileObject() {

			@Override
			public void paint(Graphics2D g, double dx, double dy, double gridsize) {
				g.drawImage(Application.AMGR.getAsset(asset).src, (int) (lx * gridsize + dx),
						(int) (ly * gridsize + dy), (int) (lwidth * gridsize), (int) (lheight * gridsize), null);
			}

		};
		tree.lwidth = 0.6;
		tree.lheight = 0.6;
		tree.lx = 0.2;
		tree.ly = 0.2;
		tree.asset = Application.AMGR.assetID("tree");
	}

	int floor = -1;
	public boolean showtree;
	TileObject tree;
}

class CollisionReturn {
	public double left = -1;
	public double right = -1;
	public double bottom = -1;
	public double top = -1;
	public boolean colliding;
}