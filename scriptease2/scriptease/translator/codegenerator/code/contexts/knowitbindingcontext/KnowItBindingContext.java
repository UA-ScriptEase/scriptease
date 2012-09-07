package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import scriptease.gui.quests.StoryPoint;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.contexts.Context;

/**
 * KnowItBindingContext is Context for a KnowItBinding object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItBindingContext extends Context {
	protected KnowItBinding binding;

	public KnowItBindingContext(StoryPoint model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator);
		this.setLocationInfo(locationInfo);
	}

	public KnowItBindingContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public KnowItBindingContext(Context other, KnowItBinding source) {
		this(other);
		this.binding = source;
	}

	/**
	 * Get the KnowItBinding's Type
	 */
	@Override
	public String getType() {
		return this.getTranslator().getApiDictionary().getGameTypeManager()
				.getCodeSymbol(this.binding.getFirstType());
	}

	@Override
	public String getValue() {
		return this.binding.getScriptValue();
	}

	@Override
	public String getFormattedValue() {
		return this.getValue();
	}
}
