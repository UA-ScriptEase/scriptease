package scriptease.controller.exceptionhandler;

import java.awt.Toolkit;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import scriptease.gui.WindowFactory;

/**
 * Error handling class that will perform the following duties when an exception
 * is thrown:
 * 
 * <ol>
 * <li>Pop open the relevant dialog informing the user a problem has occurred.
 * The dialog will take control from there.
 * </ol>
 * 
 * @author remiller
 * @author ds3
 * @author mfchurch - refactored to remove logging as it is now handled by
 *         Logging.aj
 * @author jyuen
 */
public class ScriptEaseExceptionHandler implements UncaughtExceptionHandler {

	// Singleton
	private static ScriptEaseExceptionHandler instance = null;

	/**
	 * Gets the sole instance of this Exception Handler
	 * 
	 * @return
	 */
	public static ScriptEaseExceptionHandler getInstance() {
		if (instance == null) {
			instance = new ScriptEaseExceptionHandler();
		}

		return ScriptEaseExceptionHandler.instance;
	}

	protected ScriptEaseExceptionHandler() {
	}

	@Override
	public void uncaughtException(final Thread t, final Throwable e) {
		this.handle(e);
	}

	/**
	 * Sorts the Throwable object by its subtype (Error or Exception) and calls
	 * the appropriate handler method.
	 * 
	 * @param thrown
	 *            The Throwable object to be handled.
	 */
	private void handle(final Throwable thrown) {
		final String title = "Internal Error";
		final String messageBrief = "ScriptEase has encountered an internal error.";
		final String message = "It may be possible to continue past this error.<br>Would you like to help make ScriptEase better by reporting the problem?";
		final Icon icon = UIManager.getIcon("OptionPane.errorIcon");

		Toolkit.getDefaultToolkit().beep();
		
		if (thrown instanceof java.lang.Error) {
			// Very Bad Things are happening. Duck and cover.
			try {
				WindowFactory.getInstance().showErrorDialog();
			} catch (Throwable t) {
				JOptionPane.showMessageDialog(
						null,
						"ScriptEase has encountered a critical error: "
								+ t.getMessage());
			} finally {
				System.exit(-1);
			}
		} else if (thrown instanceof java.lang.Exception) {
			System.out.println("WE HIT THIS PART");
			
			WindowFactory.getInstance().showExceptionDialog(title,
					messageBrief, message, icon, null);
		} else {
			// This should never ever happen. If it does, take a good look at
			// the Throwable thrown and why it is not an Exception or Error
			// subclass. - remiller
			throw new IllegalArgumentException(
					"Caught a throwable, but it is not an Error or Exception.");
		}
	}
}
