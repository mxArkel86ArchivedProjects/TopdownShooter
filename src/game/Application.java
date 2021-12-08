package game;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.ColorUIResource;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import java.awt.Rectangle;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Font;

public class Application extends JFrame {
	// App Constants
	// static private long STARTTIME = System.currentTimeMillis();
	final float FPS = 60;
	final int TICK_RATE = 10; // TICK RATE
	GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice device = env.getDefaultScreenDevice();
	GraphicsConfiguration config = device.getDefaultConfiguration();

	// Application Semi-Constants
	int CANW;
	int CANH;
	Peripherals PERI;
	AssetManager AMGR;

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

			float scaling = 1.0f;
			int scaled_w = (int) (CANW * scaling);
			int scaled_h = (int) (CANH * scaling);
			BufferedImage canvas = new BufferedImage(scaled_w, scaled_h, BufferedImage.TYPE_INT_RGB);

			paint_(canvas.getGraphics(), scaled_w, scaled_h);
			g2d.drawImage(canvas, 0, 0, CANW, CANH, Color.WHITE, null);

		}
	};

	float movement_base_speed = 0.8f;
	float sprint_mult = 2f;

	final int TILEBASESIZE = 60;
	double zoom_mult = 1;
	final int TILE_BUFFER = 3;
	final int PLAYER_SIZE = 40;
	double player_x = 200;
	double player_y = 200;
	double player_angle_facing = 0;
	double camera_x = 0;
	double camera_y = 0;
	double mouse_x = 0;
	double mouse_y = 0;

	// double viewing_angle = Math.PI / 6;

	void paint_(Graphics g_base, int WIDTH, int HEIGHT) {
		Graphics2D g = (Graphics2D) g_base;

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int) WIDTH, (int) HEIGHT);

		int COLUMNS = (int) ((int) Math.ceil(WIDTH / TILEBASESIZE) / zoom_mult) + 2;
		int ROWS = (int) ((int) Math.ceil(HEIGHT / TILEBASESIZE) / zoom_mult) + 2;
		double tilesize = TILEBASESIZE * zoom_mult;

		new TileAction() {
			@Override
			public void action(float x, float y, double dx, double dy, int ix, int iy,
					int px, int py) {

				if (px >= 0 && px < LEVEL_W && py >= 0 && py < LEVEL_H) {
					if(level[px][py]==null){
						g.drawImage(AMGR.getAsset("grass").src, (int) dx, (int) dy, (int) tilesize + TILE_BUFFER, (int) tilesize + TILE_BUFFER,
							new Color(0, 0, 0, 0), null);
					}else{
						g.drawImage(AMGR.getAsset(level[px][py].floor).src, (int) dx, (int) dy, (int) tilesize + TILE_BUFFER, (int) tilesize + TILE_BUFFER,
							new Color(0, 0, 0, 0), null);
					}
					
						
					g.drawString(String.format("(%d %d)", px, py), (int) dx, (int) dy + g.getFontMetrics().getAscent());

				}

				g.drawRect((int) dx, (int) dy, (int) tilesize, (int) tilesize);

			}
		}.run(ROWS, COLUMNS, camera_x, camera_y, tilesize);

		g.drawImage(rotateImage(AMGR.getAsset("stone").src, player_angle_facing), (int)player_x-PLAYER_SIZE/2, (int)player_y-PLAYER_SIZE/2, PLAYER_SIZE, PLAYER_SIZE, null);
		
		g.setColor(Color.GREEN);
		g.drawLine((int)player_x, (int)player_y, (int)mouse_x, (int)mouse_y);
		g.fillRect((int)mouse_x-4, (int)mouse_y-4, 8, 8);
	}

	final int LEVEL_W = 50;
	final int LEVEL_H = 50;
	Tile level[][] = new Tile[LEVEL_W][LEVEL_H];

	void tick(int tr) {

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
			float sprint = PERI.keyPressed(KeyEvent.VK_SHIFT) ? sprint_mult : 1;
			double intent_direction = Math.atan2(intent_y, intent_x);
			double displacement_x = Math.cos(intent_direction) * movement_base_speed * sprint;
			double displacement_y = Math.sin(intent_direction) * movement_base_speed * sprint;

			player_x += displacement_x;
			player_y -= displacement_y;
		}
		player_angle_facing = Math.atan2((mouse_y-player_y),(mouse_x-player_x));
	}

	BufferedImage rotateImage(BufferedImage img, double angle) {
		int w = img.getWidth();
		int h = img.getHeight();

		//System.out.println(String.format("(%d,%d)", w, h));

		AffineTransform tf = AffineTransform.getRotateInstance(angle, w / 2.0, h / 2.0);
		AffineTransform tf_t = AffineTransform.getTranslateInstance((w*Math.sqrt(2)-w)/2, (h*Math.sqrt(2)-h)/2);

		BufferedImage imgTgt = new BufferedImage((int)(w*Math.sqrt(2)), (int)(h*Math.sqrt(2)), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = imgTgt.createGraphics();

		g2d.transform(tf_t);
		g2d.transform(tf);
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();
		return imgTgt;
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

	BufferedImage ImageFile(String name) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(name));
		} catch (IOException e) {
		}
		return img;
	}

	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}
	
	void ImportAssets() {
		AMGR.addAsset("debug_grad_floor", ImageFile("assets/debug_grad.jpeg"));
		AMGR.addAsset("grass", ImageFile("assets/grass.jpeg"));
		AMGR.addAsset("sand", ImageFile("assets/sand.jpeg"));
		AMGR.addAsset("stone", ImageFile("assets/stone.jpeg"));
		AMGR.addAsset("wood", ImageFile("assets/wood.jpeg"));
	}

	float zoom = 0;
	float zoom_speed = 0.02f;

	public void addEventHooks() {
		PERI.addMouseMoveHook(new MouseMoveEvent(){

			@Override
			public void action(double x, double y) {
				mouse_x = x;
				mouse_y = y;
			}

		});
		/*
		PERI.addScrollHook(new ScrollEvent() {

			@Override
			public void action(double val, double x, double y) {
				double change = zoom_speed * val;
				zoom += change;
				zoom_mult = Math.exp(zoom);

			}

		});*/
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
		this.addMouseListener(PERI);
		panel.addMouseMotionListener(PERI);
		this.addMouseWheelListener(PERI);

		ImportAssets();

		addEventHooks();

		level[3][3] = new Tile(AMGR, "sand");

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
	Tile(AssetManager amgr, String floorname) {
		floor = amgr.assetID(floorname);
	}

	Tile() {

	}

	int floor = -1;
}