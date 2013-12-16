package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

import scriptease.controller.StoryComponentUtils;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.codegenerator.CodeGenerationException;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * Context that is created for all CodeBlocks. Enables all relevant information
 * to be read from CodeBlocks.
 * 
 */
public class CodeBlockContext extends Context {
	private CodeBlock codeBlock;

	// Lazy loaded variables.
	private Collection<CauseIt> identicalCauses = null;
	private Collection<KnowIt> parameters = null;
	private Collection<KnowIt> parametersWithSlot = null;
	private Collection<KnowIt> variables = null;

	public CodeBlockContext(Context other, CodeBlock source) {
		super(other);
		this.setLocationInfo(other.getLocationInfo());
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
			return this.getModel().getType(type).getCodeSymbol();

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
		final Context scriptItContext;

		scriptItContext = ContextFactory.getInstance().createContext(this,
				this.codeBlock.getOwner());

		return scriptItContext.getParameter(keyword);
	}

	@Override
	public Collection<KnowIt> getVariables() {
		if (this.variables == null) {
			this.variables = new ArrayList<KnowIt>();

			for (StoryComponent child : this.codeBlock.getOwner().getChildren()) {
				this.variables.addAll(StoryComponentUtils.getVariables(child));
			}
		}

		return this.variables;
	}

	@Override
	public Collection<KnowIt> getParameters() {
		if (this.parameters == null) {
			final ScriptIt owner = this.codeBlock.getOwner();

			this.parameters = new ArrayList<KnowIt>();

			if (owner instanceof ControlIt) {
				this.parameters.addAll(((ControlIt) owner)
						.getRequiredParameters());
			} else {
				this.parameters.addAll(this.codeBlock.getParameters());
			}
		}

		return parameters;
	}

	@Override
	public Collection<KnowIt> getParametersWithSlot() {
		if (this.parametersWithSlot == null) {
			this.parametersWithSlot = new ArrayList<KnowIt>();
			final CodeBlock mainBlock;

			mainBlock = this.codeBlock.getCause().getMainCodeBlock();

			this.parametersWithSlot.addAll(mainBlock.getLibrary()
					.getSlot(mainBlock.getSlot()).getParameters());

			this.parametersWithSlot.addAll(this.getParameters());
		}
		return this.parametersWithSlot;
	}

	/**
	 * Gets the implicit KnowIts of the CodeBlock in context.
	 */
	@Override
	public Collection<KnowIt> getImplicits() {
		return this.codeBlock.getImplicits();
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
	public Set<String> getIncludeFiles() {
		return this.getIncludeFiles();
	}

	@Override
	public KnowIt getSubject() {
		return this.codeBlock.getSubject();
	}

	@Override
	public String toString() {
		return "CodeBlockContext [" + this.codeBlock + "]";
	}

	@Override
	public KnowIt getSlotParameter(String keyword) {
		final CodeBlock mainBlock;
		final Collection<KnowIt> parameters;

		mainBlock = this.codeBlock.getCause().getMainCodeBlock();
		parameters = mainBlock.getLibrary().getSlot(mainBlock.getSlot())
				.getParameters();

		for (KnowIt parameter : parameters) {
			if (parameter.getDisplayText().equalsIgnoreCase(keyword))
				return parameter;
		}

		return null;
	}

	@Override
	public Collection<CauseIt> getIdenticalCauses() {
		if (this.identicalCauses == null) {
			final CauseIt causeIt;

			causeIt = this.codeBlock.getCause();
			this.identicalCauses = new ArrayList<CauseIt>();

			for (StoryNode node : this.getStoryNodes()) {
				for (StoryComponent child : node.getChildren()) {
					if (child instanceof StoryPoint) {
						for (StoryComponent storyPointChild : ((StoryPoint) child)
								.getChildren()) {
							if (storyPointChild instanceof CauseIt) {
								if (causeIt
										.isEquivalentToCause((CauseIt) storyPointChild))
									this.identicalCauses
											.add((CauseIt) storyPointChild);
							}
						}

					} else if (child instanceof CauseIt) {
						if (causeIt.isEquivalentToCause((CauseIt) child)) {
							this.identicalCauses.add((CauseIt) child);
						}
					}
				}
			}
		}

		return this.identicalCauses;
	}

	/**
	 * Return the Story Component this <code>CodeBlock</code> is attached to
	 * 
	 * @return
	 */
	public StoryComponent getComponent() {
		return this.codeBlock.ownerComponent;
	}
}
