package scriptease.translator.io.model;

import java.util.ArrayList;
import java.util.List;

import scriptease.model.TypedComponent;

/**
 * Representation of constant game data. Resources are immutable (that is, to
 * 'change' them, they must be replaced with a new instance).
 * 
 * Note that you may need to implement the {@link #hashCode()} and
 * {@link #equals(Object)} methods properly in order for intended functionality.
 * 
 * @author remiller (Created the original GameConstant class)
 * @author mchurch (Created the subclasses of GameConstant)
 * @author kschenk
 */
public abstract class Resource implements TypedComponent {
	/**
	 * The displayable name of the object
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * Gets a tag for the object.
	 * 
	 * @return
	 */
	public abstract String getTag();

	/**
	 * Gets the template ID for the object. This is usually a stronger version
	 * of a tag, although some games may not have both types.
	 * 
	 * @return
	 */
	public abstract String getTemplateID();

	/**
	 * Gets the code that will resolve to an object as specified by the user.
	 * 
	 * @return The code string that represents the method of object resolution
	 */
	public abstract String getCodeText();

	/**
	 * Determines whether an object is equal to this Resource. The equals
	 * implementation should check for unique identifiers; the return value from
	 * {@link #getIdentifier()} should be useful here.
	 */
	public abstract boolean equals(Object obj);

	/**
	 * Returns a list of all of the game object's children. This should never
	 * return null; if there are no children, an empty list will be returned.
	 * This method returns an empty list be default and must be overridden in
	 * order for children to be returned.
	 * 
	 * @return
	 */
	public List<Resource> getChildren() {
		return new ArrayList<Resource>(0);
	}

	/**
	 * Determines if this resource is a reference placeholder for another
	 * resource. This method by default returns false and so must be overridden
	 * to be useful.
	 * 
	 * TODO We should return the link instead of checking for one.
	 * Ticket: 48086075
	 * 
	 * @return <code>true</code> if the node is a link.
	 */
	public boolean isLink() {
		return false;
	}

	/**
	 * Returns the owner of the resource. Not all translators necessarily need
	 * to implement this.
	 * 
	 * @return
	 */
	public Resource getOwner() {
		return null;
	}

	/**
	 * Returns the name of the owner of the Resource. This method returns an
	 * empty string by default and so must be overridden to provide any
	 * functionality.
	 * 
	 * @return
	 */
	public String getOwnerName() {
		return "";
	}
}
