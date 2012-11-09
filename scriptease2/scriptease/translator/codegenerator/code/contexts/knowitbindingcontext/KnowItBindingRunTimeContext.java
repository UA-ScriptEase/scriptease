package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.Translator;
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

	public KnowItBindingRunTimeContext(StoryPoint model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public KnowItBindingRunTimeContext(Context other) {
		this(other.getStartStoryPoint(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public KnowItBindingRunTimeContext(Context other, KnowItBinding source) {
		this(other);
		this.binding = source;
	}
}