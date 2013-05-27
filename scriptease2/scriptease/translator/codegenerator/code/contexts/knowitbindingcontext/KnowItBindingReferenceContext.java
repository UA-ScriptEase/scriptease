package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.semodel.StoryModel;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;

/**
 * KnowItBindingReferenceContext is Context for a KnowItBindingReference object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItBindingReferenceContext extends KnowItBindingContext {

	public KnowItBindingReferenceContext(StoryModel model, String indent,
			CodeGenerationNamifier existingNames,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, locationInfo);
	}

	public KnowItBindingReferenceContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getLocationInfo());
	}

	public KnowItBindingReferenceContext(Context other, KnowItBinding source) {
		this(other);
		this.binding = source;
	}

	/**
	 * Get the KnowItBinding's Reference Name
	 */
	@Override
	public String getValue() {
		Context knowItContext = ContextFactory.getInstance().createContext(
				this, ((KnowItBindingReference) this.binding).getValue());
		return knowItContext.getName();
	}
}