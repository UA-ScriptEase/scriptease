package scriptease.translator.io.model;

import scriptease.model.TypedComponent;

/**
 * Representation of constant game data. GameConstants are immutable (that is,
 * to 'change' them, they must be replaced with a new instance).
 * 
 * @author remiller
 */
public interface GameConstant extends TypedComponent {
	/**
	 * The displayable name of the object
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Gets a tag for the object.
	 * 
	 * @return
	 */
	public String getTag();

	/**
	 * Gets the template ID for the object. This is usually a stronger version
	 * of a tag, although some games may not have both types.
	 * 
	 * @return
	 */
	public String getTemplateID();
	
	/**
	 * Gets the code that will resolve to an object as specified by the user.
	 * 
	 * @return The code string that represents the method of object resolution
	 */
	public String getCodeText();
}
