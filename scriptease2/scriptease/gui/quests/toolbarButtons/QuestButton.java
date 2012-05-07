package scriptease.gui.quests.toolbarButtons;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.w3c.dom.events.EventTarget;
import org.w3c.dom.views.AbstractView;

import scriptease.gui.SEIconButton;
import scriptease.util.FileOp;
import sun.swing.SwingUtilities2.Section;

public class QuestButton extends SEIconButton{
	public enum QuestButtonType{INSERT, SELECT, CONNECT, DISCONNECT, DELETE}
	
	private final static String CONNECT_TAG = "con";
	private final static String DISCONNECT_TAG = "dc";
	private final static String DELETE_TAG = "del";
	private final static String INSERT_TAG = "insertBet";
	private final static String SELECT_TAG = "selection";
	
	private QuestButtonType buttonType;
	
	
	private String getStringTag(){
		switch (buttonType){
		
		case INSERT: 
			return INSERT_TAG;
		case SELECT:
			return SELECT_TAG;
		case CONNECT:
			return CONNECT_TAG;
			
		case DISCONNECT:
			return DISCONNECT_TAG;
		
		case DELETE:
			return DELETE_TAG;
		default:
			return null;
		}
	}
	
	public boolean getState(){
		return isUp;
	}
	
	public QuestButton(QuestButtonType type){
		setContentAreaFilled(false);
		buttonType = type;
		//isUp = true;
		//buttonType = type;
		//if(type == QuestButtonType.SELECT)
			//isUp = false;
		//else
			isUp = true;
		
		if(this.loadImages()){
		/*	if(type == QuestButtonType.SELECT)
				setDepressed();
				
			else*/
			setStatic();
			
			setRolloverIcon(new ImageIcon(onHoverImg));
			setPressedIcon(new ImageIcon(ddepImg));
			
		}
		//Add a better error checking later...or a backup
		else{
			System.out.println("Fatal image loading error");
		}
		
		
	}
	
	public QuestButtonType getQuestButtonType(){
		return buttonType;
	}
	
	@Override
	protected boolean loadImages() {
		// TODO Auto-generated method stub
		String typeString = getStringTag();
		
		
		try{
			statImg = ImageIO.read(FileOp.getFileResource("scriptease/resources/icons/questIcons/" + typeString + "_static.png"));
		}
		catch(IOException e){return false;}
		
		try{
			onHoverImg = ImageIO.read(FileOp.getFileResource("scriptease/resources/icons/questIcons/" + typeString + "_hover.png"));
		}
		catch(IOException e){return false;}
		
		try{
			ddepImg = ImageIO.read(FileOp.getFileResource("scriptease/resources/icons/questIcons/" + typeString + "_dep.png"));
		}
		catch(IOException e){return false;}
		
		
		return true;
		
	}
	
	
	
	@Override
	protected boolean setButImg(JLabel img) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		isUp = !isUp;
		if(isUp){
			this.setOnHover();
		}
		if(!isUp){
			this.setDepressed();
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		if(isUp){
			this.setOnHover();
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		this.setDepressed();
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		if(isUp){
			this.setOnHover();
		}
		else
			this.setDepressed();
		
	}

	@Override
	protected void setDepressed() {
		// TODO Auto-generated method stub
		this.setIcon(new ImageIcon(ddepImg)); 
		this.reDraw();
	}

	@Override
	protected void setOnHover() {
		// TODO Auto-generated method stub
		this.setIcon(new ImageIcon(onHoverImg));
		this.reDraw();
	}

	@Override
	protected void setStatic() {
		// TODO Auto-generated method stub
		this.setIcon(new ImageIcon(statImg));
		this.reDraw();
	}

	public void toggleState(){
		isUp = !isUp;
		if(isUp){
			this.setIcon(new ImageIcon(statImg));
			this.reDraw();
		}
		else{
			this.setIcon(new ImageIcon(ddepImg));
			this.reDraw();
		}
	}
	
	public void setBoolState(boolean state){
		isUp = state;
		if(isUp){
			this.setIcon(new ImageIcon(statImg));
			this.reDraw();
		}
		else{
			this.setIcon(new ImageIcon(ddepImg));
			this.reDraw();
		}
	}
	
	



}
