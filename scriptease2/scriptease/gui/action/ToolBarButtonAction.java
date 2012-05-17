package scriptease.gui.action;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;

import scriptease.util.FileOp;

/**
 * Abstract class for ToolBar buttons. Adds an image for the button. The name of
 * the image must be the same as the tool name.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public abstract class ToolBarButtonAction extends
		ActiveModelSensitiveAction {
	
	public static enum ToolBarButtonMode {
		INSERT_GRAPH_NODE,
		SELECT_GRAPH_NODE,
		DELETE_GRAPH_NODE,
		CONNECT_GRAPH_NODE,
		DISCONNECT_GRAPH_NODE,
	}
	
	private static ToolBarButtonMode selectedMode;
	
	private String actionName;

	/**
	 * Constructor. Creates the icon for the ToolBar button.
	 * 
	 * @param name
	 *            The file name for the icon and name of the action.
	 */
	protected ToolBarButtonAction(String name, String iconName) {
		super(name);
		this.actionName = name;
		this.putValue(Action.LARGE_ICON_KEY, new ImageIcon(loadImages(iconName)));
	}

	/**
	 * Loads the image for the toolbar button from the path:
	 * "scriptease/resources/icons/buttonicons/actionName.png", where actionName
	 * refers to the name of the icon.
	 * 
	 * All images loaded in this way must be .png files, and must be located in
	 * the buttonicons folder.
	 * 
	 * @param actionName
	 *            The name of the image file being loaded, without the .png
	 *            extension.
	 * 
	 * @return staticButtonImage A BufferedImage for the loaded image.
	 * @return null Returns null if image cannot be loaded.
	 */
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

	/**
	 * Updates the icon for the ToolBarAction.
	 * 
	 * @param newIconName
	 *            Name of the new icon, as pertaining to the load images method.
	 */
	public void updateIcon(String newIconName) {
		BufferedImage newIcon = loadImages(newIconName);
		this.putValue(Action.LARGE_ICON_KEY, new ImageIcon(newIcon));
	}

	/**
	 * Changes the current mode of the ToolBar tools.
	 * 
	 * @param newMode
	 *            The ToolBarButtonMode associated with the tool.
	 */
	public static void setMode(ToolBarButtonMode newMode){
		ToolBarButtonAction.selectedMode = newMode;
	}
	
	/**
	 * Returns the current mode selected for ToolBar Buttons..
	 * 
	 * @return The current mode for the ToolBar.
	 */
	public static ToolBarButtonMode getMode() {
		return ToolBarButtonAction.selectedMode;
	}
}
