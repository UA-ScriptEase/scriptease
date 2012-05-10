package scriptease.gui.action;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.imageio.ImageIO;
import javax.swing.Action;

import scriptease.util.FileOp;

/**
 * Abstract class for toolbar buttons. Adds an image for the button. The name of
 * the image must be the same as the
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractToolBarButtonAction extends
		ActiveModelSensitiveAction {
	private String actionName;

	/**
	 * Constructor. Creates the icon for the ToolBar button.
	 * 
	 * @param name
	 *            The file name for the icon and name of the action.
	 */
	protected AbstractToolBarButtonAction(String name) {
		super(name);
		this.actionName = name;
		this.putValue(Action.LARGE_ICON_KEY, loadImages(name));
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
}
