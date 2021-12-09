package util;


import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtil {
    public static BufferedImage rotateImage(BufferedImage img, double angle) {
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
    public static BufferedImage ImageFile(String name) {
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
	
}
