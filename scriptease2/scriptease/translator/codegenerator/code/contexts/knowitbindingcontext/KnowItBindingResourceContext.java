package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
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
	public KnowItBindingResourceContext(Context other, KnowItBinding source) {
		super(other, source);
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

		typeFormat = this.getModel().getTypeFormat(type);

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
		final Set<Entry<String, String>> entrySet = this.getModel()
				.getTypeEscapes(type).entrySet();

		for (Entry<String, String> escape : entrySet) {
			final String key = escape.getKey();
			final String value = escape.getValue();
			scriptValue = scriptValue.replace(key, value + key);
		}

		// Handle Legal Values the type can take
		final String regex = this.getModel().getTypeRegex(type);
		if (StringOp.exists(regex) && !scriptValue.isEmpty()) {
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
