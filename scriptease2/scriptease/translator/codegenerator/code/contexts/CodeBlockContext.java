package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import scriptease.controller.get.VariableGetter;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.StoryModel;
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

	public CodeBlockContext(StoryModel model, String indent,
			CodeGenerationNamifier existingNames,
			LocationInformation locationInfo) {
		super(model, indent, existingNames);
		this.setLocationInfo(locationInfo);
	}

	public CodeBlockContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getLocationInfo());
	}

	public CodeBlockContext(Context other, CodeBlock source) {
		this(other);
		this.codeBlock = source;
	}

	@Override
	public String getUnique32CharName() {
		StoryComponent component = this.codeBlock.getOwner();

		while (!(component instanceof StoryPoint) && component != null)
			component = component.getOwner();

		if (component != null)
			return ((StoryPoint) component).getUnique32CharName();
		else
			throw new CodeGenerationException(
					"Failed to find a Story Point parent for CodeBlock: "
							+ this.codeBlock);
	}

	@Override
	public Collection<StoryComponent> getChildren() {
		return this.codeBlock.getOwner().getChildren();
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
			return this.getModel().getTypeCodeSymbol(type);

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
	public Collection<KnowIt> getVariables() {
		VariableGetter knowItGetter = new VariableGetter();
		Collection<StoryComponent> children = ((ComplexStoryComponent) this.codeBlock
				.getOwner()).getChildren();
		for (StoryComponent child : children) {
			child.process(knowItGetter);
		}
		return knowItGetter.getObjects();
	}

	private Collection<KnowIt> getParameterCollection() {
		final ScriptIt owner = this.codeBlock.getOwner();
		final Collection<KnowIt> parameters;

		parameters = new ArrayList<KnowIt>();

		if (owner instanceof ControlIt) {
			parameters.addAll(((ControlIt) owner).getRequiredParameters());
		} else {
			parameters.addAll(this.codeBlock.getParameters());
		}

		return parameters;
	}

	@Override
	public Collection<KnowIt> getParameters() {
		return this.getParameterCollection();
	}

	@Override
	public Collection<KnowIt> getParametersWithSlot() {
		final Collection<KnowIt> parameters = new ArrayList<KnowIt>();
		final CodeBlock mainBlock;

		mainBlock = this.codeBlock.getCause().getMainCodeBlock();

		parameters.addAll(mainBlock.getLibrary().getSlotParameters(
				mainBlock.getSlot()));

		parameters.addAll(this.getParameterCollection());

		return parameters;
	}

	/**
	 * Gets the implicit KnowIts of the CodeBlock in context.
	 */
	@Override
	public Collection<KnowIt> getImplicits() {
		final Collection<KnowIt> used = new ArrayList<KnowIt>();

		// Only return implicits that are used in this Context
		for (KnowIt implicit : this.codeBlock.getImplicits()) {
			// TODO Get components does not return all implciits.
			if (getComponents().contains(implicit))
				used.add(implicit);
		}
		return used;
	}

	@Override
	public ScriptIt getCause() {
		return this.codeBlock.getCause();
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
	 * Get the ScriptIt's Story Point Active Child
	 */
	@Override
	public StoryComponentContainer getActiveChild() {
		CauseIt cause = this.codeBlock.getCause();
		if (cause != null)
			return cause.getActiveBlock();
		else
			throw new CodeGenerationException(
					"Attempted to get Story Point Active Block for a "
							+ "CodeBlock without a Cause: " + this.codeBlock);
	}

	/**
	 * Get the ScriptIt's Story Point Inactive Child
	 */
	@Override
	public StoryComponentContainer getInactiveChild() {
		CauseIt cause = this.codeBlock.getCause();
		if (cause != null)
			return cause.getInactiveBlock();
		else
			throw new CodeGenerationException(
					"Attempted to get Story Point Inactive Block for a "
							+ "CodeBlock without a Cause: " + this.codeBlock);
		// CodeBlock Without A Cause. (Not) Starring James Dean
	}

	@Override
	public StoryComponentContainer getAlwaysChild() {
		CauseIt cause = this.codeBlock.getCause();
		if (cause != null)
			return cause.getAlwaysBlock();
		else
			throw new CodeGenerationException(
					"Attempted to get Story Point Inactive Block for a "
							+ "CodeBlock without a Cause: " + this.codeBlock);

	}

	@Override
	public KnowIt getSlotParameter(String keyword) {
		final CodeBlock mainBlock;
		final Collection<KnowIt> parameters;

		mainBlock = this.codeBlock.getCause().getMainCodeBlock();
		parameters = mainBlock.getLibrary().getSlotParameters(
				mainBlock.getSlot());

		for (KnowIt parameter : parameters) {
			if (parameter.getDisplayText().equalsIgnoreCase(keyword))
				return parameter;
		}

		return null;
	}

	@Override
	public Collection<ScriptIt> getIdenticalCauses() {
		final Collection<ScriptIt> identicalCauses;
		final CauseIt causeIt;

		causeIt = this.codeBlock.getCause();
		identicalCauses = new ArrayList<ScriptIt>();

		for (StoryPoint point : this.getStartStoryPoint().getDescendants()) {
			for (StoryComponent child : point.getChildren()) {
				if (child instanceof ScriptIt) {
					if (causeIt.isEquivalentToCause((CauseIt) child)) {
						identicalCauses.add((ScriptIt) child);
					}
				}
			}
		}

		return identicalCauses;
	}
}
