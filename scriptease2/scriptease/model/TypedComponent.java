package scriptease.model;

import java.util.Collection;

/**
 * Has typing information associated with it.
 * 
 * @author remiller
 */
public interface TypedComponent {
	/**
	 * Gets the type for this typed component.
	 * 
	 * @return The type.
	 */
	public Collection<String> getTypes();
}
