package scriptease.translator.io.model;

import scriptease.model.TypedComponent;

/**
 * A GameConstant 
 *
 */
public interface GameConstant extends TypedComponent {
	/**
	 * Gets the code that will resolve to an object as specified by the user.
	 * 
	 * @return The code string that represents the method of object resolution
	 */
	public String getResolutionText();

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
}
