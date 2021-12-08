package game;

import javax.swing.SwingUtilities;

public class Entry {
	static Application app;
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	app = new Application();
        		app.InitializeApplication();
        		app.setVisible(true);
            }
        });
	}

}
