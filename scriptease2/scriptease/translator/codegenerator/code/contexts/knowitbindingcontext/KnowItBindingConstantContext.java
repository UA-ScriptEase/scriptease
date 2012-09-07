package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import scriptease.gui.quests.QuestPoint;
import scriptease.model.CodeBlock;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.CodeGenerationException;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.GameObject;

/**
 * KnowItBindingConstantContext is Context for a KnowItBindingConstant object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class KnowItBindingConstantContext extends KnowItBindingContext {

	public KnowItBindingConstantContext(QuestPoint model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInformation) {
		super(model, indent, existingNames, translator, locationInformation);
	}

	public KnowItBindingConstantContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
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
		final Collection<AbstractFragment> typeFormat;

		typeFormat = this.translator.getGameTypeManager().getFormat(
				((KnowItBindingConstant) this.binding).getFirstType());

		if (typeFormat == null || typeFormat.isEmpty()
				&& this.binding instanceof KnowItBindingConstant) {
			if (((KnowItBindingConstant) this.binding).getValue() instanceof GameObject) {
				List<CodeBlock> codeBlocks = this.getBindingCodeBlocks();

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
		return ((KnowItBindingConstant) this.binding).getScriptValue();
	}

	/**
	 * Get the KnowItBinding's Name
	 */
	@Override
	public String getUniqueName(Pattern legalFormat) {
		return ((KnowItBindingConstant) this.binding).getValue().getName();
	}
}
