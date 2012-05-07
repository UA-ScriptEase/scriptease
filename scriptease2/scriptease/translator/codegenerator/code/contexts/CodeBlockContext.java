package scriptease.translator.codegenerator.code.contexts;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

import scriptease.controller.VariableGetter;
import scriptease.gui.quests.QuestNode;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

public class CodeBlockContext extends Context {
	private CodeBlock codeBlock;

	public CodeBlockContext(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator);
		this.setLocationInfo(locationInfo);
	}

	public CodeBlockContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public CodeBlockContext(Context other, CodeBlock source) {
		this(other);
		codeBlock = source;
	}

	@Override
	public Iterator<StoryComponent> getChildren() {
		return codeBlock.getOwner().getChildren().iterator();
	}

	@Override
	public String getUniqueName(Pattern legalFormat) {
		return this.getNamifier().getUniqueName(codeBlock, legalFormat);
	}

	@Override
	public String getType() {
		// Grab the first type of the first codeBlock - Assumes they will all
		// have matching codeSymbols
		String type = codeBlock.getTypes().iterator().next();

		return TranslatorManager.getInstance().getActiveTranslator()
				.getGameTypeManager().getCodeSymbol(type);
	}

	@Override
	public KnowIt getParameter(String keyword) {
		for (KnowIt parameter : codeBlock.getParameters()) {
			if (parameter.getDisplayText().equalsIgnoreCase(keyword))
				return parameter;
		}

		// if we have not found it locally, check the ScriptIt context. It could
		// be a parameter of a sibling CodeBlock
		final Context scriptItContext = ContextFactory.getInstance()
				.createContext(this, this.codeBlock.getOwner());
		return scriptItContext.getParameter(keyword);
	}

	@Override
	public Iterator<KnowIt> getVariables() {
		VariableGetter knowItGetter = new VariableGetter();
		Collection<StoryComponent> children = ((ComplexStoryComponent) codeBlock
				.getOwner()).getChildren();
		for (StoryComponent child : children) {
			child.process(knowItGetter);
		}
		return knowItGetter.getObjects().iterator();
	}

	@Override
	public Iterator<KnowIt> getParameters() {
		return codeBlock.getParameters().iterator();
	}

	@Override
	public Iterator<String> getIncludes() {
		return codeBlock.getIncludes().iterator();
	}

	/**
	 * Get the Collection of FormatFragments which represent the method body
	 */
	@Override
	public String getCode() {
		return FormatFragment.resolveFormat(codeBlock.getCode(), this);
	}

	@Override
	public KnowIt getSubject() {
		return codeBlock.getSubject();
	}

	@Override
	public String toString() {
		return "CodeBlockContext [" + this.codeBlock + "]";
	}
}
