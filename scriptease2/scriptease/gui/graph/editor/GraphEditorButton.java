package scriptease.gui.graph.editor;

import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import scriptease.gui.SEIconButton;
import scriptease.util.FileOp;

@SuppressWarnings("serial")
public class GraphEditorButton extends SEIconButton {
	public enum GraphEditorButtonType {
		INSERT, SELECT, CONNECT, DISCONNECT, DELETE
	}

	private final static String CONNECT_TAG = "con";
	private final static String DISCONNECT_TAG = "dc";
	private final static String DELETE_TAG = "del";
	private final static String INSERT_TAG = "insertBet";
	private final static String SELECT_TAG = "selection";

	private GraphEditorButtonType buttonType;

	private String getStringTag() {
		switch (buttonType) {

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

	public boolean getState() {
		return isUp;
	}

	public GraphEditorButton(GraphEditorButtonType type) {
		setContentAreaFilled(false);
		buttonType = type;
		isUp = true;

		if (this.loadImages()) {
			setStatic();

			setRolloverIcon(new ImageIcon(onHoverButtonImage));
			setPressedIcon(new ImageIcon(depressedButtonImage));
		}
		// Add a better error checking later...or a backup
		else {
			System.out.println("Fatal image loading error");
		}
	}

	public GraphEditorButtonType getQuestButtonType() {
		return buttonType;
	}

	@Override
	protected boolean loadImages() {
		String typeString = getStringTag();

		try {
			staticButtonImage = ImageIO.read(FileOp
					.getFileResource("scriptease/resources/icons/questIcons/"
							+ typeString + "_static.png"));
		} catch (IOException e) {
			return false;
		}

		try {
			onHoverButtonImage = ImageIO.read(FileOp
					.getFileResource("scriptease/resources/icons/questIcons/"
							+ typeString + "_hover.png"));
		} catch (IOException e) {
			return false;
		}

		try {
			depressedButtonImage = ImageIO.read(FileOp
					.getFileResource("scriptease/resources/icons/questIcons/"
							+ typeString + "_dep.png"));
		} catch (IOException e) {
			return false;
		}

		return true;

	}

	@Override
	protected boolean setButtonImage(JLabel img) {
		return false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		isUp = !isUp;
		if (isUp) {
			this.setOnHover();
		}
		if (!isUp) {
			this.setDepressed();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (isUp) {
			this.setOnHover();
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mousePressed(MouseEvent e) {
		this.setDepressed();

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (isUp) {
			this.setOnHover();
		} else
			this.setDepressed();
	}

	@Override
	protected void setDepressed() {
		this.setIcon(new ImageIcon(depressedButtonImage));
		this.reDraw();
	}

	@Override
	protected void setOnHover() {
		this.setIcon(new ImageIcon(onHoverButtonImage));
		this.reDraw();
	}

	@Override
	protected void setStatic() {
		this.setIcon(new ImageIcon(staticButtonImage));
		this.reDraw();
	}

	public void toggleState() {
		isUp = !isUp;
		if (isUp) {
			this.setIcon(new ImageIcon(staticButtonImage));
			this.reDraw();
		} else {
			this.setIcon(new ImageIcon(depressedButtonImage));
			this.reDraw();
		}
	}

	public void setBoolState(boolean state) {
		isUp = state;
		if (isUp) {
			this.setIcon(new ImageIcon(staticButtonImage));
			this.reDraw();
		} else {
			this.setIcon(new ImageIcon(depressedButtonImage));
			this.reDraw();
		}
	}
}
