package scriptease.gui.action.graphs;

import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.util.FileOp;

/**
 * Abstract class for ToolBar buttons. Adds an image for the button. The name of
 * the image must be the same as the tool name. GraphToolBarModeActions should
 * only set the mode of the ToolBar and not act on the graph itself.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public abstract class GraphToolBarModeAction extends ActiveModelSensitiveAction {

	public static enum ToolBarMode {
		INSERT, SELECT, DELETE, CONNECT, DISCONNECT, GROUP, UNGROUP
	}

	private static final String CURSOR_DIRECTORY = "scriptease/resources/icons/buttonicons/";
	private static final String CURSOR_EXTENSION = ".png";

	private static ToolBarMode selectedMode = ToolBarMode.SELECT;

	private String actionName;

	private static Collection<WeakReference<JComponent>> activeComponents = new ArrayList<WeakReference<JComponent>>();

	/**
	 * Constructor. Creates the icon for the ToolBar button.
	 * 
	 * @param name
	 *            The file name for the icon and name of the action.
	 */
	protected GraphToolBarModeAction(String name, String iconName) {
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
	 * @return A BufferedImage for the loaded image, or null if image cannot be
	 *         loaded.
	 */
	protected BufferedImage loadImages(String actionName) {
		try {
			final BufferedImage buttonImage;

			buttonImage = ImageIO.read(FileOp.getFileResource(CURSOR_DIRECTORY
					+ actionName + CURSOR_EXTENSION));
			return buttonImage;
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
	public static void setMode(ToolBarMode newMode) {
		GraphToolBarModeAction.selectedMode = newMode;

		final Cursor cursor;

		cursor = GraphToolBarModeAction.getCursor(newMode);

		for (WeakReference<JComponent> reference : activeComponents)
			reference.get().setCursor(cursor);
	}

	/**
	 * Adds the JComponent to a weakly referenced list of JComponents that use
	 * the cursor created by the graph tool bar.
	 * 
	 * @param component
	 */
	public static void useGraphCursorForJComponent(JComponent component) {
		GraphToolBarModeAction.activeComponents
				.add(new WeakReference<JComponent>(component));
	}

	/**
	 * Returns the current mode selected for ToolBar Buttons..
	 * 
	 * @return The current mode for the ToolBar.
	 */
	public static ToolBarMode getMode() {
		return GraphToolBarModeAction.selectedMode;
	}

	/**
	 * Returns the cursor associated with the mode.
	 * 
	 * @param mode
	 * @return
	 */
	private static Cursor getCursor(ToolBarMode mode) {
		final Cursor cursor;

		if (mode == ToolBarMode.INSERT)
			cursor = ScriptEaseUI.CURSOR_NODE_ADD;
		else if (mode == ToolBarMode.DELETE)
			cursor = ScriptEaseUI.CURSOR_NODE_DELETE;
		else if (mode == ToolBarMode.CONNECT)
			cursor = ScriptEaseUI.CURSOR_PATH_DRAW;
		else if (mode == ToolBarMode.DISCONNECT)
			cursor = ScriptEaseUI.CURSOR_PATH_ERASE;
		else if (mode == ToolBarMode.GROUP)
			cursor = ScriptEaseUI.CURSOR_NODE_GROUP;
		else if (mode == ToolBarMode.UNGROUP)
			cursor = ScriptEaseUI.CURSOR_NODE_UNGROUP;
		else
			// Setting a cursor to null sets it to its parent, which is the
			// default cursor in this case.
			cursor = null;

		return cursor;
	}
}
