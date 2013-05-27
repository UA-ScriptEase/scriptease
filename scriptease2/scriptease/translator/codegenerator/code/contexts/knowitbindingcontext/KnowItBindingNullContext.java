package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.semodel.StoryModel;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.contexts.Context;

/**
 * KnowItBindingNullContext is Context for a KnowItBindingNull object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItBindingNullContext extends KnowItBindingContext {

	public KnowItBindingNullContext(StoryModel model, String indent,
			CodeGenerationNamifier existingNames,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, locationInfo);
	}

	public KnowItBindingNullContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getLocationInfo());
	}

	public KnowItBindingNullContext(Context other, KnowItBinding source) {
		this(other);
		this.binding = source;
	}

	/**
	 * Get the KnowItBinding's GameConstant Value
	 */
	@Override
	public String getValue() {
		return ((KnowItBindingNull) this.binding).getScriptValue();
	}

}
