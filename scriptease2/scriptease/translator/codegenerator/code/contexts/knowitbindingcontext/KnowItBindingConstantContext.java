package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import java.util.Collection;
import java.util.regex.Pattern;

import scriptease.gui.quests.QuestNode;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

/**
 * KnowItBindingConstantContext is Context for a KnowItBindingConstant object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItBindingConstantContext extends KnowItBindingContext {

	public KnowItBindingConstantContext(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInformation) {
		super(model, indent, existingNames, translator,
				locationInformation);
	}

	public KnowItBindingConstantContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public KnowItBindingConstantContext(Context other, KnowItBinding source) {
		this(other);
		binding = source;
	}

	/**
	 * Get the KnowItBinding's Type Formatted GameConstant Value, if no format
	 * is specified the ScriptValue of the binding is returned
	 */
	@Override
	public String getFormattedValue() {
		final Collection<FormatFragment> typeFormat;

		typeFormat = this.translator.getGameTypeManager().getFormat(
				((KnowItBindingConstant) binding).getFirstType());
		if (typeFormat == null || typeFormat.isEmpty())
			return this.getValue();

		return FormatFragment.resolveFormat(typeFormat, this);
	}

	/**
	 * Get the KnowItBinding's ScriptValue
	 */
	@Override
	public String getValue() {
		return ((KnowItBindingConstant) binding).getScriptValue();
	}

	/**
	 * Get the KnowItBinding's Name
	 */
	@Override
	public String getUniqueName(Pattern legalFormat) {
		return ((KnowItBindingConstant) binding).getValue().getName();
	}
}
