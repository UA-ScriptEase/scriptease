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

	private final static String CONNECT_TAG = "path_draw";
	private final static String DISCONNECT_TAG = "path_erase";
	private final static String DELETE_TAG = "node_delete";
	private final static String INSERT_TAG = "node_add";
	private final static String SELECT_TAG = "selection";

	private GraphEditorButtonType buttonType;

	private String getStringTag() {
		switch (buttonType) {

		case SELECT:
			return SELECT_TAG;
		case INSERT:
			return INSERT_TAG;
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
	//	setContentAreaFilled(false);
		buttonType = type;
		isUp = true;

		this.loadImages();
		this.setIcon(new ImageIcon(staticButtonImage));
		this.reDraw();
	}

	public GraphEditorButtonType getQuestButtonType() {
		return buttonType;
	}

	@Override
	protected void loadImages() {
		String typeString = getStringTag();

		try {
			staticButtonImage = ImageIO.read(FileOp
					.getFileResource("scriptease/resources/icons/buttonicons/"
							+ typeString + ".png"));
		} catch (IOException e) {
		
		}
	}

	@Override
	protected boolean setButtonImage(JLabel img) {
		return false;
	}
}
