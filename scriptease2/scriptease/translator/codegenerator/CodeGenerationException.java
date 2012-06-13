package scriptease.translator.codegenerator;
/**
 * Exception class for code generation exceptions.
 * 
 * @author kschenk
 *
 */
@SuppressWarnings("serial")
public class CodeGenerationException extends RuntimeException {
	
	/**
	 * Constructor that calls super with a message.
	 * 
	 * @param message
	 */
	public CodeGenerationException(String message) {
		super(message);
	}
	
	/**
	 * Constructor that calls super with a message and the cause.
	 * 
	 * @param message
	 * @param cause
	 */
	public CodeGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
