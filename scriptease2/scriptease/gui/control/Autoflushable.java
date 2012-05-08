package scriptease.gui.control;

/**
 * An Autoflushable GUI widget automatically updates (or "flushes to") a model
 * object with data entered into it. This is accomplished via the
 * {@link #tryFlush()} method, and is called <i>Autoflushing</i> GUI widgets
 * that both display model data and allow users to directly edit it are ideal
 * candidates for Autoflushing behaviour. Widgets that do not directly support
 * editing (like a JLabel) should not be autoflushable, since it would be
 * pointless.<br>
 * <br>
 * <strong>Warning:&nbsp;</strong> Implementing classes should disabled
 * Autoflushing when they are responding to updates in the model by updating the
 * display. Autoflushing is only intended to function when the when the field's
 * data is changing because of user edits, not because of model updates. If
 * Autoflushing is enabled when the model updates, then there will be an
 * infinite loop:
 * <ol>
 * <li>the user enters data, which causes an autoflush</li>
 * <li>the flush updates the model. The model notifies its observers of a change
 * </li>
 * <li>the observer update causes the text field to update its text, which
 * causes an autoflush. Proceed to step 2.</li>
 * </ol>
 * This class is based on ScriptEase 1 file
 * <code>ca.ualberta.cs.games.gui.Autoflushable</code>.
 * 
 * @author Unknown
 * @author remiller
 */
public interface Autoflushable {
	/**
	 * Gets whether the autoflush feature is enabled or not.
	 * 
	 * @return True if autoflush is enabled.
	 */
	public boolean isAutoflushEnabled();

	/**
	 * Enables or disables the autoflush feature.
	 * 
	 * @param enabled
	 *            Will enable autoflush if <code>true</code>, disable if
	 *            <code>false</code>.
	 */
	public void setAutoflushEnabled(boolean enabled);

	/**
	 * Pushes data from the implementing class to the model if and only if
	 * {@link #isAutoflushEnabled()} returns true.
	 */
	public void tryFlush();
}
