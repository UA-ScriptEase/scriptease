package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import scriptease.controller.get.VariableGetter;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryItemSequence;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.CodeGenerationException;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * Context that is created for all CodeBlocks. Enables all relevant information
 * to be read from CodeBlocks.
 * 
 */
public class CodeBlockContext extends Context {
	private CodeBlock codeBlock;

	public CodeBlockContext(StoryPoint model, String indent,
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
		this.codeBlock = source;
	}

	@Override
	public String getStoryPointName() {
		StoryComponent component = this.codeBlock.getOwner();

		while (!(component instanceof StoryPoint) && component != null)
			component = component.getOwner();

		if (component != null)
			return this.getNamifier().getUniqueName(component, null);
		else
			throw new CodeGenerationException(
					"Failed to find a Story Point parent for CodeBlock: "
							+ this.codeBlock);
	}

	@Override
	public Iterator<StoryComponent> getChildren() {
		return this.codeBlock.getOwner().getChildren().iterator();
	}

	@Override
	public String getUniqueName(Pattern legalFormat) {
		return this.getNamifier().getUniqueName(this.codeBlock, legalFormat);
	}

	@Override
	public String getType() {
		// Grab the first type of the first codeBlock - Assumes they will all
		// have matching codeSymbols
		for (String type : this.codeBlock.getTypes())
			return TranslatorManager.getInstance().getActiveTranslator()
					.getGameTypeManager().getCodeSymbol(type);

		throw new NoSuchElementException("Error: No type found for CodeBlock "
				+ this.codeBlock);
	}

	@Override
	public KnowIt getParameter(String keyword) {
		for (KnowIt parameter : this.codeBlock.getParameters()) {
			if (parameter.getDisplayText().equalsIgnoreCase(keyword)) {
				return parameter;
			}
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
		Collection<StoryComponent> children = ((ComplexStoryComponent) this.codeBlock
				.getOwner()).getChildren();
		for (StoryComponent child : children) {
			child.process(knowItGetter);
		}
		return knowItGetter.getObjects().iterator();
	}

	@Override
	public Iterator<KnowIt> getParameters() {
		return this.codeBlock.getParameters().iterator();
	}

	/**
	 * Gets the implicit KnowIts of the CodeBlock in context.
	 */
	@Override
	public Iterator<KnowIt> getImplicits() {
		final Collection<KnowIt> used = new ArrayList<KnowIt>();

		// Only return implicits that are used in this Context
		for (KnowIt implicit : this.codeBlock.getImplicits()) {
			if (getComponents().contains(implicit))
				used.add(implicit);
		}
		return used.iterator();
	}

	/**
	 * Get the Collection of FormatFragments which represent the method body
	 */
	@Override
	public String getCode() {
		final Collection<AbstractFragment> codeFragments;

		codeFragments = this.codeBlock.getCode();

		return AbstractFragment.resolveFormat(codeFragments, this);
	}

	@Override
	public KnowIt getSubject() {
		return this.codeBlock.getSubject();
	}

	@Override
	public String toString() {
		return "CodeBlockContext [" + this.codeBlock + "]";
	}

	/**
	 * Get the ScriptIt's Story Child
	 */
	@Override
	public StoryItemSequence getStoryChild() {
		ScriptIt cause = this.codeBlock.getCause();
		if (cause != null)
			return cause.getStoryBlock();
		else
			throw new CodeGenerationException(
					"Attempted to get Story Block for a CodeBlock without a Cause: "
							+ this.codeBlock);
	}

	/**
	 * Get the ScriptIt's Always Child
	 */
	@Override
	public StoryItemSequence getAlwaysChild() {
		ScriptIt cause = this.codeBlock.getCause();
		if (cause != null)
			return cause.getAlwaysBlock();
		else
			throw new CodeGenerationException(
					"Attempted to get Always Block for a CodeBlock without a Cause: "
							+ this.codeBlock);
	}
}
