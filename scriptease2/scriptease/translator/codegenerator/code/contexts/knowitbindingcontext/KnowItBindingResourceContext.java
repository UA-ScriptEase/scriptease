package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.util.StringOp;

/**
 * KnowItBindingConstantContext is Context for a KnowItBindingConstant object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItBindingResourceContext extends KnowItBindingContext {

	public KnowItBindingResourceContext(StoryPoint model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInformation) {
		super(model, indent, existingNames, translator, locationInformation);
	}

	public KnowItBindingResourceContext(Context other) {
		this(other.getStartStoryPoint(), other.getIndent(),
				other.getNamifier(), other.getTranslator(), other
						.getLocationInfo());
	}

	public KnowItBindingResourceContext(Context other, KnowItBinding source) {
		this(other);
		this.binding = source;
	}

	/**
	 * Get the KnowItBinding's Type Formatted GameConstant Value, if no format
	 * is specified the ScriptValue of the binding is returned
	 */
	@Override
	public String getFormattedValue() {
		final Collection<AbstractFragment> typeFormat;
		final String type;

		type = this.binding.getFirstType();
		typeFormat = this.translator.getLibrary().getTypeFormat(type);

		if (typeFormat == null || typeFormat.isEmpty()) {
			return this.getValue();
		}

		return AbstractFragment.resolveFormat(typeFormat, this);
	}

	/**
	 * Get the KnowItBinding's ScriptValue
	 */
	@Override
	public String getValue() {
		String scriptValue = ((KnowItBindingResource) this.binding)
				.getScriptValue();
		final String type = ((KnowItBindingResource) this.binding)
				.getFirstType();

		// Handles Escaped Characters
		final Set<Entry<String, String>> entrySet = this.translator
				.getGameTypeManager().getEscapes(type).entrySet();

		for (Entry<String, String> escape : entrySet) {
			final String key = escape.getKey();
			final String value = escape.getValue();
			scriptValue = scriptValue.replace(key, value + key);
		}

		// Handle Legal Values the type can take
		final String regex = this.translator.getGameTypeManager().getReg(type);
		if (regex != null && !regex.isEmpty() && !scriptValue.isEmpty()) {
			final Pattern regexPattern = Pattern.compile(regex);
			scriptValue = StringOp.removeIllegalCharacters(scriptValue,
					regexPattern, false);
		}

		return scriptValue;
	}

	/**
	 * Get the KnowItBinding's Name
	 */
	@Override
	public String getUniqueName(Pattern legalFormat) {
		return ((KnowItBindingResource) this.binding).getValue().getName();
	}
}
