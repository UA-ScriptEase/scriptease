package scriptease.translator.codegenerator.code.contexts;

import scriptease.translator.io.model.Resource;

/**
 * ResourceContext is a context for a Resource object.
 * 
 * @author jyuen
 */
public class ResourceContext extends Context{
	
	private final Resource resource;
	
	/**
	 * Creates a new AskItContext with the source AskIt based on the context
	 * passed in.
	 * 
	 * @param other
	 * @param source
	 */
	public ResourceContext(Context other, Resource source) {
		super(other);

		this.setLocationInfo(other.getLocationInfo());
		this.resource = source;
	}
	
	@Override
	public String getTemplateID() {
		return resource.getTemplateID();
	}
}
