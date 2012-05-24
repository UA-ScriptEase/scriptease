package scriptease.gui.action;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import scriptease.util.FileOp;

/**
 * Abstract class for ToolBar buttons. Adds an image for the button. The name of
 * the image must be the same as the tool name.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public abstract class ToolBarButtonAction extends ActiveModelSensitiveAction {

	public static enum ToolBarButtonMode {
		INSERT_GRAPH_NODE, SELECT_GRAPH_NODE, DELETE_GRAPH_NODE, CONNECT_GRAPH_NODE, DISCONNECT_GRAPH_NODE
	}

	private static ToolBarButtonMode selectedMode;

	private String actionName;

	private static ArrayList<JComponent> activeComponent = new ArrayList<JComponent>();

	/**
	 * Constructor. Creates the icon for the ToolBar button.
	 * 
	 * @param name
	 *            The file name for the icon and name of the action.
	 */
	protected ToolBarButtonAction(String name, String iconName) {
		super(name);
		this.actionName = name;
		this.putValue(Action.LARGE_ICON_KEY,
				new ImageIcon(loadImages(iconName)));
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
	 * Changes the current mode of the ToolBar tools.
	 * 
	 * @param newMode
	 *            The ToolBarButtonMode associated with the tool.
	 */
	public static void setMode(ToolBarButtonMode newMode) {
		ToolBarButtonAction.selectedMode = newMode;
	}

	public static void addJComponent(JComponent component) {
		ToolBarButtonAction.activeComponent.add(component);
	}

	/*
	 * public static JComponent getJComponent() { return
	 * ToolBarButtonAction.activeComponent; }
	 */

	/**
	 * Returns the current mode selected for ToolBar Buttons..
	 * 
	 * @return The current mode for the ToolBar.
	 */
	public static ToolBarButtonMode getMode() {
		return ToolBarButtonAction.selectedMode;
	}

	/**
	 * Sets the cursor to the image from "scriptease/resources/icons/cursors/" +
	 * cursorPath.
	 * 
	 * @param cursorPath
	 */
	public void setCursorToImageFromPath(String cursorPath) {

		String resultingCursorPath = "scriptease/resources/icons/cursors/"
				+ cursorPath + ".png";

		System.out.println(cursorPath);

		Toolkit toolkit = Toolkit.getDefaultToolkit();

		final Point CURSOR_HOTSPOT = new Point(0, 0);

		BufferedImage cursorImage;
		Cursor customCursor = null;
		try {
			cursorImage = ImageIO.read(FileOp
					.getFileResource(resultingCursorPath));

			customCursor = toolkit.createCustomCursor(cursorImage,
					CURSOR_HOTSPOT, resultingCursorPath);

		} catch (IOException e) {
			customCursor = null;
		} catch (IllegalArgumentException e) {
			customCursor = null;
		}

		for (JComponent component : activeComponent)
			component.setCursor(customCursor);
	}
}
