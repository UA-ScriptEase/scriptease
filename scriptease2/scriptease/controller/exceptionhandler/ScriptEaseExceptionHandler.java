package scriptease.controller.exceptionhandler;

import java.awt.Toolkit;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.JOptionPane;

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
 * refactored by mfchurch to remove logging as it is now handled by Logging.aj
 * 
 * @author remiller
 * @author ds3
 * @author mfchurch
 */
public class ScriptEaseExceptionHandler implements UncaughtExceptionHandler {

	public ScriptEaseExceptionHandler() {
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
			thrown.printStackTrace(System.err);
			WindowFactory.getInstance().showExceptionDialog();
		} else {
			// This should never ever happen. If it does, take a good look at
			// the Throwable thrown and why it is not an Exception or Error
			// subclass. - remiller
			throw new IllegalArgumentException(
					"Caught a throwable, but it is not an Error or Exception.");
		}
	}
}
