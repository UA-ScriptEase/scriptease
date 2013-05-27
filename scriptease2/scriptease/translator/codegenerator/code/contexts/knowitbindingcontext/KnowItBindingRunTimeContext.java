package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.semodel.StoryModel;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.contexts.Context;

/**
 * KnowItBindingReferenceContext is Context for a KnowItBindingReference object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItBindingRunTimeContext extends KnowItBindingContext {

	public KnowItBindingRunTimeContext(StoryModel model, String indent,
			CodeGenerationNamifier existingNames,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, locationInfo);
	}

	public KnowItBindingRunTimeContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getLocationInfo());
	}

	public KnowItBindingRunTimeContext(Context other, KnowItBinding source) {
		this(other);
		this.binding = source;
	}
}