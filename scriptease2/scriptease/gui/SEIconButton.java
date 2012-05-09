package scriptease.gui;

import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JToggleButton;

//generic Icon/button

@SuppressWarnings("serial")
public abstract class SEIconButton extends JToggleButton {

	protected BufferedImage staticButtonImage;
	//protected BufferedImage onHoverButtonImage;
	//protected BufferedImage depressedButtonImage;
	

	protected boolean isUp;

	protected abstract boolean loadImages();

	protected abstract boolean setButtonImage(JLabel img);

	protected void reDraw() {
		this.validate();
		this.repaint();
	}

}
