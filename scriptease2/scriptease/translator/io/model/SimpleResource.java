package scriptease.translator.io.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A simple resource has only one String associated with it. All properties are
 * set to this String. A common use for a Simple Resource is primitive types in
 * code.
 * 
 * @author kschenk
 * 
 */
public class SimpleResource extends Resource {
	private final Collection<String> types;
	private final String codeValue;

	public SimpleResource(Collection<String> types, String value) {
		this.types = new ArrayList<String>(types);
		this.codeValue = value;
	}

	@Override
	public Collection<String> getTypes() {
		return this.types;
	}

	@Override
	public String getCodeText() {
		return this.codeValue;
	}

	@Override
	public String getName() {
		return this.codeValue;
	}

	@Override
	public String toString() {
		return this.codeValue;
	}

	@Override
	public String getTag() {
		return this.codeValue;
	}

	@Override
	public String getTemplateID() {
		return this.codeValue;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SimpleResource) {
			final SimpleResource resource = (SimpleResource) obj;

			return this.codeValue.equals(resource.getCodeText())
					&& this.types.equals(resource.getTypes());
		}
		return false;
	}

	/**
	 * Builds a new {@link SimpleResource} with the passed in types and
	 * resolution code.
	 * 
	 * @param types
	 * @param resolutionCode
	 * @return
	 */
	public static SimpleResource buildSimpleResource(
			final Collection<String> types, final String resolutionCode) {
		if (resolutionCode == null)
			throw new IllegalArgumentException(
					"Cannot create a GameConstant with a null resolutionCode");

		return new SimpleResource(types, resolutionCode);
	}

	/**
	 * Builds a new {@link SimpleResource} with the passed in type and
	 * resolution code.
	 * 
	 * @param type
	 * @param resolutionCode
	 * @return
	 */
	public static SimpleResource buildSimpleResource(String type,
			String resolutionCode) {
		final ArrayList<String> types = new ArrayList<String>(1);
		types.add(type);
		return SimpleResource.buildSimpleResource(types, resolutionCode);
	}

	/**
	 * Builds a new Simple Resource with the passed in type and no resolution
	 * code.
	 * 
	 * @param type
	 * @return
	 */
	public static SimpleResource buildSimpleResource(final String type) {
		return SimpleResource.buildSimpleResource(type, "");
	}
}
