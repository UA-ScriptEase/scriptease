package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryItemSequence;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.EventSlotManager;
import scriptease.translator.codegenerator.LocationInformation;
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
	public Iterator<CodeBlock> getCodeBlocks() {
		return this.getComponent().getCodeBlocksForLocation(this.locationInfo)
				.iterator();
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
	public Iterator<KnowIt> getImplicits() {
		final Collection<KnowIt> used = new ArrayList<KnowIt>();

		// Only return implicits that are used in this Context
		for (KnowIt implicit : this.getMainCodeBlock().getImplicits()) {
			if (getComponents().contains(implicit))
				used.add(implicit);
		}
		return used.iterator();
	}

	/**
	 * Returns the parent case for the ScriptIt. May be the ScriptIt itself if
	 * it is a cause.
	 * 
	 * @see CodeBlock#getCause()
	 */
	@Override
	public ScriptIt getCause() {
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
	 * Gets all of the include files required by a ScriptIt's CodeBlocks.
	 * 
	 * @see CodeBlock#getIncludes()
	 */
	@Override
	public Iterator<String> getIncludes() {
		final Collection<CodeBlock> codeBlocks;
		final List<String> includes = new ArrayList<String>();

		codeBlocks = this.getComponent().getCodeBlocksForLocation(
				this.locationInfo);

		// Grab the includes of all the codeBlocks
		for (CodeBlock codeBlock : codeBlocks) {
			includes.addAll(codeBlock.getIncludes());
		}

		return includes.iterator();
	}

	/**
	 * Generates the code for the ScriptIt and returns it as a String.
	 * 
	 * @see CodeBlock#getCode()
	 */
	@Override
	public String getCode() {
		final Collection<CodeBlock> codeBlocks;
		final Collection<AbstractFragment> code;

		codeBlocks = this.getComponent().getCodeBlocksForLocation(
				this.locationInfo);
		code = new ArrayList<AbstractFragment>();

		// Combine codeBlocks with the same slot
		for (CodeBlock codeBlock : codeBlocks) {
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
	 * @see ScriptIt#getCodeBlocksForLocation(LocationInformation)
	 */
	@Override
	public Iterator<KnowIt> getVariables() {
		final Collection<KnowIt> parameters = new ArrayList<KnowIt>();
		final Collection<CodeBlock> codeBlocks;

		codeBlocks = this.getComponent().getCodeBlocksForLocation(
				this.locationInfo);

		for (CodeBlock codeBlock : codeBlocks) {
			parameters.addAll(codeBlock.getParameters());
		}

		return parameters.iterator();
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
	public Iterator<KnowIt> getParametersWithSlot() {
		final Collection<KnowIt> parameters = new ArrayList<KnowIt>();

		parameters.addAll(this.getSlotParameterCollection());
		parameters.addAll(this.getComponent().getParameters());

		return parameters.iterator();
	}

	/**
	 * Returns all of the parameters of all of the CodeBlocks.
	 * 
	 * @see ScriptIt#getParameter(String)
	 */
	@Override
	public Iterator<KnowIt> getParameters() {
		return this.getComponent().getParameters().iterator();
	}

	/**
	 * Returns the active block of a Cause.
	 * 
	 * @see ScriptIt#getActiveBlock()
	 */
	@Override
	public StoryItemSequence getActiveChild() {
		return this.getComponent().getActiveBlock();
	}

	/**
	 * Returns the inactive block of a Cause.
	 * 
	 * @see ScriptIt#getInactiveBlock()
	 */
	@Override
	public StoryItemSequence getInactiveChild() {
		return this.getComponent().getInactiveBlock();
	}

	/**
	 * Returns all of the parameters in the slot. Used mainly for implicit
	 * gathering.
	 * 
	 * @return
	 */
	protected Collection<KnowIt> getSlotParameterCollection() {
		final EventSlotManager manager;
		final String currentSlot;

		manager = TranslatorManager.getInstance().getActiveAPIDictionary()
				.getEventSlotManager();
		currentSlot = this.getMainCodeBlock().getSlot();

		if (currentSlot == null)
			throw new NullPointerException("Encountered null slot in a Cause! "
					+ "Call may be incorrect in Language Dictionary.");

		return manager.getParameters(currentSlot);
	}

	/**
	 * Returns all of the parameters in the slot.
	 * 
	 * @see ScriptItContext#getSlotParameterCollection()
	 */
	@Override
	public Iterator<KnowIt> getSlotParameters() {
		return this.getSlotParameterCollection().iterator();
	}

	/**
	 * Conditions 
	 */
	@Override
	public String getSlotConditional() {
		final EventSlotManager manager;
		final String currentSlot;

		manager = TranslatorManager.getInstance().getActiveAPIDictionary()
				.getEventSlotManager();
		currentSlot = this.getMainCodeBlock().getSlot();

		if (currentSlot == null)
			throw new NullPointerException("Encountered null slot in a Cause! "
					+ "Call may be incorrect in Language Dictionary.");

		return manager.getCondition(currentSlot);
	};

	@Override
	public String getUnique32CharName() {
		StoryComponent owner = this.getComponent().getOwner();
		while (owner != null) {
			if (owner instanceof StoryPoint)
				return ((StoryPoint) owner).getUnique32CharName();
		}
		throw new NullPointerException("Could not find Story Point for " + this);
	}

	@Override
	protected ScriptIt getComponent() {
		return (ScriptIt) super.getComponent();
	}

	@Override
	public String toString() {
		return "ScriptItContext[" + this.getComponent() + "]";
	}
}
