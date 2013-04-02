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
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.EventSlotManager;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

public class ScriptItContext extends ComplexStoryComponentContext {

	public ScriptItContext(StoryPoint model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public ScriptItContext(Context other) {
		this(other.getStartStoryPoint(), other.getIndent(),
				other.getNamifier(), other.getTranslator(), other
						.getLocationInfo());
	}

	public ScriptItContext(Context other, ScriptIt source) {
		this(other);
		this.setComponent(source);
	}

	@Override
	public CodeBlock getMainCodeBlock() {
		return this.getComponent().getMainCodeBlock();
	}

	@Override
	public Iterator<CodeBlock> getCodeBlocks() {
		return this.getComponent().getCodeBlocksForLocation(this.locationInfo)
				.iterator();
	}

	@Override
	public String getType() {
		final Context codeBlockContext;

		codeBlockContext = ContextFactory.getInstance().createContext(this,
				this.getMainCodeBlock());

		return codeBlockContext.getType();

	}

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

	@Override
	public Object getCause() {
		return this.getMainCodeBlock().getCause();
	}

	/**
	 * Namification needs to be done at a CodeBlock level instead of a
	 * StoryComponent level
	 */
	@Override
	public String getUniqueName(Pattern legalFormat) {
		final CodeBlock mainCodeBlock = this.getMainCodeBlock();
		final Context createContext = ContextFactory.getInstance()
				.createContext(this, mainCodeBlock);
		return createContext.getUniqueName(legalFormat);
	}

	@Override
	public Iterator<String> getIncludes() {
		final Collection<CodeBlock> codeBlocks = this.getComponent()
				.getCodeBlocksForLocation(this.locationInfo);

		final List<String> includes = new ArrayList<String>();
		// Grab the includes of all the codeBlocks
		for (CodeBlock codeBlock : codeBlocks) {
			includes.addAll(codeBlock.getIncludes());
		}

		return includes.iterator();
	}

	/**
	 * Get the Collection of FormatFragments which represent the method body
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

	@Override
	public Iterator<KnowIt> getVariables() {
		final Collection<KnowIt> parameters = new ArrayList<KnowIt>();
		for (CodeBlock codeBlock : this.getComponent()
				.getCodeBlocksForLocation(this.locationInfo)) {
			parameters.addAll(codeBlock.getParameters());
		}

		return parameters.iterator();
	}

	/**
	 * Get the StoryComponent's parameter by displayName returns null if a
	 * parameter with that displayName is not found
	 */
	@Override
	public KnowIt getParameter(String displayName) {
		return this.getComponent().getParameter(displayName);
	}

	@Override
	public Iterator<KnowIt> getParametersWithSlot() {
		final Collection<KnowIt> parameters = new ArrayList<KnowIt>();

		parameters.addAll(this.getSlotParameterCollection());
		parameters.addAll(this.getComponent().getParameters());

		return parameters.iterator();
	}

	@Override
	public Iterator<KnowIt> getParameters() {
		return this.getComponent().getParameters().iterator();
	}

	@Override
	public StoryItemSequence getActiveChild() {
		return this.getComponent().getActiveBlock();
	}

	@Override
	public StoryItemSequence getInactiveChild() {
		return this.getComponent().getInactiveBlock();
	}

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

	@Override
	public Iterator<KnowIt> getSlotParameters() {
		return this.getSlotParameterCollection().iterator();
	}

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
