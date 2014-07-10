package scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.scriptit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.codegenerator.code.contexts.CodeBlockContext;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;
import scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.ComplexStoryComponentContext;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * ScriptItContexts are contexts for scriptits.
 * 
 * @author remiller
 * @author mfchurch
 * @author kschenk
 */
public class ScriptItContext extends ComplexStoryComponentContext {

	/**
	 * Creates a new ScriptItContext with the passed in source based on the
	 * other passed in context.
	 * 
	 * @param other
	 * @param source
	 */
	public ScriptItContext(Context other, ScriptIt source) {
		super(other, source);
	}

	/**
	 * Returns the main code block.
	 * 
	 * @see {@link ScriptIt#getMainCodeBlock()}.
	 */
	@Override
	public CodeBlock getMainCodeBlock() {
		return this.getComponent().getMainCodeBlock();
	}

	/**
	 * Returns an iterator over codeblocks.
	 * 
	 * @see ScriptIt#getCodeBlocks()
	 */
	@Override
	public Collection<CodeBlock> getCodeBlocks() {
		return this.getComponent().getCodeBlocksForLocation(this.locationInfo);
	}

	/**
	 * Returns the return type of the ScriptIt, which is the first type of the
	 * main code block.
	 * 
	 * @see CodeBlockContext#getType()
	 */
	@Override
	public String getType() {
		final Context codeBlockContext;

		codeBlockContext = ContextFactory.getInstance().createContext(this,
				this.getMainCodeBlock());

		return codeBlockContext.getType();

	}

	/**
	 * Returns the implicits of the ScriptIt, which are all known by the main
	 * codeblock.
	 * 
	 * @see CodeBlock#getImplicits()
	 */
	@Override
	public Collection<KnowIt> getImplicits() {
		return this.getMainCodeBlock().getImplicits();
	}

	/**
	 * Returns the parent case for the ScriptIt. May be the ScriptIt itself if
	 * it is a cause.
	 * 
	 * @see CodeBlock#getCause()
	 */
	@Override
	public CauseIt getCause() {
		return this.getMainCodeBlock().getCause();
	}

	/**
	 * Returns the unique name of the main codeblock associated with the
	 * ScriptIt.
	 */
	@Override
	public String getUniqueName(Pattern legalFormat) {
		final CodeBlock mainCodeBlock = this.getMainCodeBlock();
		final Context createContext = ContextFactory.getInstance()
				.createContext(this, mainCodeBlock);

		return createContext.getUniqueName(legalFormat);
	}

	/**
	 * Generates the code for the ScriptIt and returns it as a String.
	 * 
	 * @see CodeBlock#getCode()
	 */
	@Override
	public String getCode() {
		final Collection<AbstractFragment> code;

		code = new ArrayList<AbstractFragment>();

		// Combine codeBlocks with the same slot
		for (CodeBlock codeBlock : this.getCodeBlocks()) {
			code.addAll(codeBlock.getCode());
		}

		return AbstractFragment.resolveFormat(code, this);
	}

	/**
	 * Returns the variables of the ScriptIt. Variables are all of the
	 * parameters of all of the code blocks in the same location as this
	 * ScriptIt. <br>
	 * <br>
	 * An example of the difference between this and {@link #getParameters()} is
	 * if we have a ScriptIt with two codeblocks in different locations, such as
	 * the Enable Dialogue Line effect in the NWN translator.
	 * 
	 * @see CodeBlock#getParameters()
	 * @see #getCodeBlocks()
	 */
	@Override
	public Collection<KnowIt> getVariables() {
		final Collection<KnowIt> parameters = new ArrayList<KnowIt>();

		for (CodeBlock codeBlock : this.getCodeBlocks()) {
			parameters.addAll(codeBlock.getParameters());
		}

		return parameters;
	}

	/**
	 * Get one of the StoryComponent's parameters by displayName.
	 */
	@Override
	public KnowIt getParameter(String displayName) {
		return this.getComponent().getParameter(displayName);
	}

	/**
	 * Gets all parameters and slots together.
	 */
	@Override
	public Collection<KnowIt> getParametersWithSlot() {
		final Collection<KnowIt> parameters = new ArrayList<KnowIt>();

		parameters.addAll(this.getSlotParameters());
		parameters.addAll(this.getParameters());

		return parameters;
	}

	/**
	 * Returns all of the parameters of all of the CodeBlocks.
	 * 
	 * @see ScriptIt#getParameter(String)
	 */
	@Override
	public Collection<KnowIt> getParameters() {
		return this.getComponent().getParameters();
	}

	/**
	 * Returns all of the parameters in the slot.
	 * 
	 * @see ScriptItContext#getSlotParameterCollection()
	 */
	@Override
	public Collection<KnowIt> getSlotParameters() {
		final CodeBlock mainBlock = this.getMainCodeBlock();
		final CauseIt cause = mainBlock.getCause();
		final String currentSlot = mainBlock.getSlot();

		if (cause == null || currentSlot == null)
			throw new NullPointerException("Encountered null slot in a Cause! "
					+ "Call may be incorrect in Language Dictionary.");

		return cause.getMainCodeBlock().getLibrary().getSlot(currentSlot)
				.getParameters();
	}

	/**
	 * Conditions
	 */
	@Override
	public String getSlotConditional() {
		final CodeBlock mainBlock = this.getMainCodeBlock();
		final CauseIt cause = mainBlock.getCause();
		final String currentSlot = mainBlock.getSlot();

		if (currentSlot == null)
			throw new NullPointerException("Encountered null slot in a Cause! "
					+ "Call may be incorrect in Language Dictionary.");

		return cause.getMainCodeBlock().getLibrary().getSlot(currentSlot)
				.getCondition();
	};

	@Override
	public String getUnique32CharName() {
		StoryComponent owner = this.getOwner();
		while (owner != null) {
			if (owner instanceof StoryPoint)
				return ((StoryPoint) owner).getUnique32CharName();
		}
		throw new NullPointerException("Could not find Story Point for " + this);
	}

	@Override
	public ScriptIt getComponent() {
		return (ScriptIt) super.getComponent();
	}

	@Override
	public String toString() {
		return "ScriptItContext[" + this.getComponent() + "]";
	}
}
