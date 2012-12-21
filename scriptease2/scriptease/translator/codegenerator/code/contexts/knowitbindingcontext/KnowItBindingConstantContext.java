package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import scriptease.model.CodeBlock;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.CodeGenerationException;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.GameObject;
import scriptease.util.StringOp;

/**
 * KnowItBindingConstantContext is Context for a KnowItBindingConstant object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItBindingConstantContext extends KnowItBindingContext {

	public KnowItBindingConstantContext(StoryPoint model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInformation) {
		super(model, indent, existingNames, translator, locationInformation);
	}

	public KnowItBindingConstantContext(Context other) {
		this(other.getStartStoryPoint(), other.getIndent(),
				other.getNamifier(), other.getTranslator(), other
						.getLocationInfo());
	}

	public KnowItBindingConstantContext(Context other, KnowItBinding source) {
		this(other);
		this.binding = source;
	}

	/**
	 * Get the KnowItBinding's Type Formatted GameConstant Value, if no format
	 * is specified the ScriptValue of the binding is returned
	 */
	@Override
	public String getFormattedValue() {
		Collection<AbstractFragment> typeFormat = new ArrayList<AbstractFragment>();
		if (this.binding instanceof KnowItBindingConstant) {
			final String type = ((KnowItBindingConstant) this.binding)
					.getFirstType();
			typeFormat = this.translator.getGameTypeManager().getFormat(type);
			if (typeFormat == null || typeFormat.isEmpty()) {
				if (((KnowItBindingConstant) this.binding).getValue() instanceof GameObject) {
					final List<CodeBlock> codeBlocks = this
							.getBindingCodeBlocks();
					if (codeBlocks.size() <= 0)
						throw new CodeGenerationException(
								"Couldn't find code block for "
										+ this.binding
										+ ". Maybe it's missing from the API library?");

					final Collection<AbstractFragment> codeFragments;
					codeFragments = codeBlocks.get(0).getCode();
					String bindingCode = AbstractFragment.resolveFormat(
							codeFragments, this);
					return bindingCode;
				} else {
					return this.getValue();
				}
			}
		}
		return AbstractFragment.resolveFormat(typeFormat, this);
	}

	@Override
	public KnowIt getParameter(String parameter) {
		return new KnowIt(((KnowItBindingConstant) this.binding).getTag());
	}

	/**
	 * Get the KnowItBinding's ScriptValue
	 */
	@Override
	public String getValue() {
		String scriptValue = ((KnowItBindingConstant) this.binding)
				.getScriptValue();
		final String type = ((KnowItBindingConstant) this.binding)
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
		final Pattern regexPattern = Pattern.compile(regex);
		scriptValue = StringOp.removeIllegalCharacters(scriptValue,
				regexPattern);

		return scriptValue;
	}

	/**
	 * Get the KnowItBinding's Name
	 */
	@Override
	public String getUniqueName(Pattern legalFormat) {
		return ((KnowItBindingConstant) this.binding).getValue().getName();
	}
}
