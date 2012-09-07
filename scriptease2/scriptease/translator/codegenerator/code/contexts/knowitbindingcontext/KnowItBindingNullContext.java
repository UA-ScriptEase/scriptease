package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import scriptease.gui.quests.StoryPoint;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.translator.Translator;
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

	public KnowItBindingNullContext(StoryPoint model,
			String indent, CodeGenerationNamifier existingNames,
			Translator translator, LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public KnowItBindingNullContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(),
				other.getTranslator(), other.getLocationInfo());
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
