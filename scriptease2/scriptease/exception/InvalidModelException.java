package scriptease.exception;

/**
 * Represents a problem with loading a wrong model. This is a standard exception
 * class, and {@link RuntimeException} may be consulted for details.
 * 
 * @author kschenk
 */
@SuppressWarnings("serial")
public class InvalidModelException extends RuntimeException {
	/**
	 * Builds a new exception that was caused by the given Throwable problem.
	 * 
	 * @param cause
	 *            The Throwable problem. May be <code>null</code>.
	 */
	public InvalidModelException(Throwable cause) {
		this("", cause);
	}

	/**
	 * Builds a new exception that contains the given message.
	 * 
	 * @param message
	 *            The detailed message that described the situation the
	 *            exception was created in.
	 */
	public InvalidModelException(String message) {
		this(message, null);
	}

	/**
	 * Builds a new exception that contains the given message and was caused by
	 * the given Throwable problem.
	 * 
	 * @param message
	 *            The detailed message that described the situation the
	 *            exception was created in.
	 * @param cause
	 *            The Throwable problem that caused this exception to exist. May
	 *            be <code>null</code>.
	 */
	public InvalidModelException(String message, Throwable cause) {
		super(message, cause);
	}
}
