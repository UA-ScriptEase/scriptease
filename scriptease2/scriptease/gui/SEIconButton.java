package scriptease.gui;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.w3c.dom.events.MouseEvent;

//generic Icon/button

public abstract class SEIconButton extends JToggleButton implements MouseListener{

protected BufferedImage statImg;
protected BufferedImage onHoverImg;
protected BufferedImage ddepImg;

protected boolean isUp;
	
protected abstract boolean loadImages();
protected abstract boolean setButImg(JLabel img);
protected abstract void setStatic();
protected abstract void setDepressed();
protected abstract void setOnHover();

protected void reDraw(){
	this.validate();
	this.repaint();
}


	
}
