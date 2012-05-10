package scriptease.gui.action;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;

import scriptease.util.FileOp;

/**
 * Abstract class for toolbar buttons. Adds an image for the button. The name of
 * the image must be the same as the tool name.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public abstract class ToolBarAction extends
		ActiveModelSensitiveAction {
	
	public static enum ToolBarButtonMode{
		INSERT_QUEST_POINT,
		SELECT_QUEST_POINT,
		DELETE_QUEST_POINT,
		CONNECT_QUEST_POINT,
		DISCONNECT_QUEST_POINT,
		}
	
	private static ToolBarButtonMode selectedMode;
	
	private String actionName;

	/**
	 * Constructor. Creates the icon for the ToolBar button.
	 * 
	 * @param name
	 *            The file name for the icon and name of the action.
	 */
	protected ToolBarAction(String name) {
		super(name);
		this.actionName = name;
		this.putValue(Action.LARGE_ICON_KEY, new ImageIcon(loadImages(name)));
	}

	protected BufferedImage loadImages(String actionName) {
		try {
			BufferedImage staticButtonImage = ImageIO.read(FileOp
					.getFileResource("scriptease/resources/icons/buttonicons/"
							+ actionName + ".png"));
			return staticButtonImage;
		} catch (IOException e) {
			UncaughtExceptionHandler handler = Thread
					.getDefaultUncaughtExceptionHandler();
			handler.uncaughtException(Thread.currentThread(),
					new IllegalStateException("Exception " + e
							+ "while adding the icon for "
							+ "ToolBarButtonAction " + this.actionName));
			return null;
		}
	}
	
	public static void setMode(ToolBarButtonMode newMode){
		ToolBarAction.selectedMode = newMode;
	}
	
	public static ToolBarButtonMode getMode() {
		return ToolBarAction.selectedMode;
	}
}
