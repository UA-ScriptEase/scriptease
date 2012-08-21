package scriptease.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;

import javax.swing.JComponent;

//General imageop class, anything that deals with graphics and has no where else where to go
//goes here, all methods should be static

public class ImageOp {

	// Take a Jcomponent and converts it into an image
	public static BufferedImage componentToImage(JComponent component) {
		BufferedImage image = new BufferedImage(component.getWidth(),
				component.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = image.createGraphics();
		component.paint(g);

		return image;
	}

	// Merges a collection of images into a single image
	public static BufferedImage mergeImages(
			Collection<BufferedImage> imagesToMerge) {
		BufferedImage image;
		int pixelBuffer = 3;
		int maxWidth = 0;
		int sigmaHeight = 0;
		int tempNW = 0;

		for (BufferedImage img : imagesToMerge) {
			tempNW = img.getWidth();
			if (tempNW > maxWidth)
				maxWidth = tempNW;

			sigmaHeight += img.getHeight();
		}

		image = new BufferedImage(maxWidth, sigmaHeight + imagesToMerge.size()
				* pixelBuffer, BufferedImage.TYPE_INT_ARGB);

		int deltaY = 0;
		for (BufferedImage img : imagesToMerge) {
			image.createGraphics().drawImage(img, 0, deltaY, null);
			deltaY += img.getHeight() + pixelBuffer;
		}

		return image;
	}

}
