package uitl;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ZoomImage {
	public final int WIDTH;
	public final int HEIGHT;
	BufferedImage buffImg ;
	Graphics g;
	
	public ZoomImage(int targetWidth, int targetHeight){
		WIDTH = targetWidth;
		HEIGHT = targetHeight;
		buffImg = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
		g = buffImg.getGraphics();
	}
	
	
	public String zoomImage(String path){
		Image srcImg=null;
		String newName = path.substring(0,path.lastIndexOf('.'))+"(1).png";
//		System.out.println(newName);
		try {
			srcImg = ImageIO.read(new File(path));
//			srcImg = ImageIO.read(ZoomImage.class.getResource(path));
			
			if(srcImg!=null && (srcImg.getWidth(null)>WIDTH || srcImg.getHeight(null)>HEIGHT) ){
				//将原图绘制到BufferedImage上
				g.drawImage(srcImg, 0, 0, WIDTH, HEIGHT, null);
				//将压缩后的头像写入文件
				ImageIO.write(buffImg, "png", new File(newName));
				return newName;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return path;
	}
}
