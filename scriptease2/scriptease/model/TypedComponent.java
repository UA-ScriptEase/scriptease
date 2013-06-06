package scriptease.model;

import java.util.Collection;

/**
 * Has typing information associated with it.
 * 
 * @author remiller
 */
public interface TypedComponent {
	// TODO This doesn't belong here.
	public static final String TAG_TYPE = "Type";
	public static final String TAG_TYPES = "Types";

	/**
	 * Gets the type for this typed component.
	 * 
	 * @return The type.
	 */
	public Collection<String> getTypes();
}
