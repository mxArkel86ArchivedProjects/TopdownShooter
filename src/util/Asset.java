package util;

import java.awt.image.BufferedImage;

public class Asset{
	public String name;
    public BufferedImage src;
    public double scaledHeight(double displaywidth){
        double displayheight = (src.getHeight()*1.0/src.getWidth())*displaywidth;
        return displayheight;
    }
}
