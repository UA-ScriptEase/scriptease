package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.TypedComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.translator.codegenerator.LocationInformation;

/**
 * A ScriptIt represents a StoryComponent which is used to generate functions in
 * code. It contains codeBlocks, which are able to have parameters, implicits,
 * subjects and slots and return types.
 * 
 * @author mfchurch
 * @author kschenk
 * 
 */
public class ScriptIt extends ComplexStoryComponent implements TypedComponent,
		StoryComponentObserver {
	private static final String PREFIX = "<"
			+ StoryComponentPanelFactory.CURRENT_STORY_POINT_TAG + "> ";

	private static final String ACTIVE_BLOCK_TEXT = PREFIX + "Active:";
	private static final String INACTIVE_BLOCK_TEXT = PREFIX + "Inactive:";
	private static final String ALWAYS_BLOCK_TEXT = "Always:";

	/*
	 * TODO We should move out all of the cause specific stuff and call it a
	 * "CauseIt". We are removing functionality when we create the ControlIt
	 * subclass from ScriptIt, which violates the Liskov principle.
	 * 
	 * Ticket: 42583119
	 */

	// The group of children that are in the Story Point Active block
	private StoryItemSequence activeBlock;

	// The group of children that are in the Story Point Inactive block
	private StoryItemSequence inactiveBlock;

	// The group of children that are in the Always block .
	private StoryItemSequence alwaysBlock;

	protected Collection<CodeBlock> codeBlocks;

	public ScriptIt(String name) {
		super(name);
		final int NUMBER_OF_BLOCKS = 3;

		this.codeBlocks = new ArrayList<CodeBlock>();

		final List<Class<? extends StoryComponent>> validTypes;

		validTypes = new ArrayList<Class<? extends StoryComponent>>();

		this.registerChildType(StoryItemSequence.class, NUMBER_OF_BLOCKS);

		validTypes.add(ScriptIt.class);
		validTypes.add(KnowIt.class);
		validTypes.add(AskIt.class);
		validTypes.add(StoryComponentContainer.class);
		validTypes.add(Note.class);
		validTypes.add(ControlIt.class);

		this.activeBlock = new StoryItemSequence(validTypes);
		this.activeBlock.setDisplayText(ACTIVE_BLOCK_TEXT);
		this.inactiveBlock = new StoryItemSequence(validTypes);
		this.inactiveBlock.setDisplayText(INACTIVE_BLOCK_TEXT);
		this.alwaysBlock = new StoryItemSequence(validTypes);
		this.alwaysBlock.setDisplayText(ALWAYS_BLOCK_TEXT);

	}

	public Collection<CodeBlock> getCodeBlocks() {
		return new ArrayList<CodeBlock>(this.codeBlocks);
	}

	/**
	 * Retrieves only the code blocks which apply to the given location
	 * (subject, slot)
	 * 
	 * @param locationInfo
	 * @return
	 */
	public Collection<CodeBlock> getCodeBlocksForLocation(
			LocationInformation locationInfo) {
		Collection<CodeBlock> matching = new ArrayList<CodeBlock>(1);
		for (CodeBlock codeBlock : this.codeBlocks) {
			if (locationInfo.matchesLocation(codeBlock))
				matching.add(codeBlock);
		}
		return matching;
	}

	/**
	 * Gets the cause that contains this ScriptIt.
	 * 
	 * @return
	 */
	public ScriptIt getCause() {
		if (this.isCause())
			return this;
		else
			for (CodeBlock block : this.codeBlocks) {
				final ScriptIt cause = block.getCause();
				if (cause != null)
					return cause;
			}
		return null;
	}

	@Override
	public boolean equals(Object other) {
		if (super.equals(other) && other instanceof ScriptIt) {
			return this.codeBlocks.equals(((ScriptIt) other).codeBlocks);
		}
		return false;
	}

	/**
	 * A cause is a ScriptIt where all CodeBlocks have a subject and a slot.
	 * 
	 * @return true if all CodeBlocks have both subjects and slots.
	 * 
	 */
	public boolean isCause() {
		if (this.codeBlocks.size() == 0)
			return false;

		for (CodeBlock codeBlock : this.codeBlocks) {
			if (!codeBlock.hasSubject() || !codeBlock.hasSlot())
				return false;
		}
		return true;
	}

	@Override
	public void process(StoryVisitor processController) {
		processController.processScriptIt(this);
	}

	@Override
	public String toString() {
		return "ScriptIt [" + this.getDisplayText() + "]";
	}

	@Override
	public ScriptIt clone() {
		final ScriptIt component = (ScriptIt) super.clone();

		// clone the code blocks
		component.codeBlocks = new ArrayList<CodeBlock>(this.codeBlocks.size());
		for (CodeBlock codeBlock : this.codeBlocks) {
			component.addCodeBlock(codeBlock.clone());
		}

		if (component.isCause()) {
			component
					.setActiveBlock((StoryItemSequence) component.childComponents
							.get(0));
			component
					.setInactiveBlock((StoryItemSequence) component.childComponents
							.get(1));

			component
					.setAlwaysBlock((StoryItemSequence) component.childComponents
							.get(2));
		}

		return component;
	}

	/**
	 * Gets the container for children that are in the Story Point Active block
	 * of a cause.
	 * 
	 * @return
	 */
	public StoryItemSequence getActiveBlock() {
		return this.activeBlock;
	}

	/**
	 * Gets the container for children that are in the Story Point Inactive
	 * block of a cause.
	 * 
	 * @return
	 */
	public StoryItemSequence getInactiveBlock() {
		return this.inactiveBlock;
	}

	/**
	 * Gets the container for children that are in the Always block of a cause.
	 * 
	 * @return
	 */
	public StoryItemSequence getAlwaysBlock() {
		return this.alwaysBlock;
	}

	private void setActiveBlock(StoryItemSequence activeBlock) {
		// Change text for backwards compatibility
		activeBlock.setDisplayText(ACTIVE_BLOCK_TEXT);
		this.activeBlock = activeBlock;

		if (this.activeBlock != null)
			activeBlock.setOwner(this);
	}

	private void setInactiveBlock(StoryItemSequence inactiveBlock) {
		// Change text for backwards compatibility
		inactiveBlock.setDisplayText(INACTIVE_BLOCK_TEXT);
		this.inactiveBlock = inactiveBlock;

		if (this.inactiveBlock != null)
			inactiveBlock.setOwner(this);
	}

	private void setAlwaysBlock(StoryItemSequence alwaysBlock) {
		alwaysBlock.setDisplayText(ALWAYS_BLOCK_TEXT);
		this.alwaysBlock = alwaysBlock;

		if (this.alwaysBlock != null) {
			alwaysBlock.setOwner(this);
		}
	}

	@Override
	public boolean addStoryChildBefore(StoryComponent newChild,
			StoryComponent sibling) {
		boolean success = super.addStoryChildBefore(newChild, sibling);
		// TODO See this? This "instanceof my subclass"? This is bad. This is
		// everything that is wrong with this class.
		if (success && !(this instanceof ControlIt)) {
			final Iterator<StoryComponent> it = this.getChildren().iterator();
			if (it.next() == newChild)
				this.setActiveBlock((StoryItemSequence) newChild);
			else if (it.next() == newChild)
				this.setInactiveBlock((StoryItemSequence) newChild);
			else
				this.setAlwaysBlock((StoryItemSequence) newChild);
		}
		return success;
	}

	@Override
	public Collection<String> getTypes() {
		return this.getMainCodeBlock().getTypes();
	}

	/**
	 * Get the parameters for all of the codeBlocks
	 * 
	 * @return
	 */
	public Collection<KnowIt> getParameters() {
		final List<KnowIt> parameters = new ArrayList<KnowIt>();
		for (CodeBlock codeBlock : this.codeBlocks) {
			parameters.addAll(codeBlock.getParameters());
		}
		return parameters;
	}

	/**
	 * Get a specific parameter from one of the codeBlocks. Returns null if a
	 * parameter with that displayName is not found.
	 * 
	 * @param displayName
	 * @return
	 */
	public KnowIt getParameter(String displayName) {
		for (KnowIt parameter : this.getParameters()) {
			if (parameter.getDisplayText().equalsIgnoreCase(displayName))
				return parameter;
		}
		return null;
	}

	/**
	 * Get the implicits for all of the codeBlocks
	 * 
	 * @return
	 */
	public Collection<KnowIt> getImplicits() {
		final Collection<KnowIt> implicits = new CopyOnWriteArraySet<KnowIt>();
		for (CodeBlock codeBlock : this.codeBlocks) {
			implicits.addAll(codeBlock.getImplicits());
		}
		return implicits;
	}

	/**
	 * Get the main CodeBlock for the ScriptIt. Defaults to the first CodeBlock.
	 * 
	 * @return
	 */
	public CodeBlock getMainCodeBlock() {
		for (CodeBlock codeBlock : this.codeBlocks)
			return codeBlock;

		throw new NoSuchElementException(
				"Cannot get main CodeBlock because there are none! Did "
						+ "you remember to add a CodeBlock when you "
						+ "created the ScriptIt?");
	}

	public void removeCodeBlock(CodeBlock codeBlock) {
		if (this.codeBlocks.remove(codeBlock)) {
			codeBlock.removeStoryComponentObserver(this);
			codeBlock.setOwner(null);
			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.CHANGE_CODEBLOCK_REMOVED));
		}

		this.updateStoryChildren();
	}

	public void addCodeBlock(CodeBlock codeBlock) {
		if (this.codeBlocks.add(codeBlock)) {
			codeBlock.addStoryComponentObserver(this);
			codeBlock.setOwner(this);
			this.notifyObservers(new StoryComponentEvent(this,
					StoryComponentChangeEnum.CHANGE_CODEBLOCK_ADDED));
		}

		this.updateStoryChildren();
	}

	/**
	 * Updates the ScriptIt to display the Story Point Active and Inactive
	 * blocks, depending on if it is a cause or not.
	 */
	public void updateStoryChildren() {
		if (this.isCause()) {
			if (!this.childComponents.contains(this.activeBlock))
				this.addStoryChild(this.activeBlock);
			if (!this.childComponents.contains(this.inactiveBlock))
				this.addStoryChild(this.inactiveBlock);
			if (!this.childComponents.contains(this.alwaysBlock))
				this.addStoryChild(this.alwaysBlock);
		} else {
			this.removeStoryChild(this.activeBlock);
			this.removeStoryChild(this.inactiveBlock);
			this.removeStoryChild(this.alwaysBlock);
		}
	}

	public void setCodeBlocks(Collection<CodeBlock> codeBlocks) {
		for (CodeBlock codeBlock : this.codeBlocks) {
			this.removeCodeBlock(codeBlock);
		}
		for (CodeBlock codeBlock : codeBlocks) {
			this.addCodeBlock(codeBlock);
		}

		this.updateStoryChildren();

		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CODE_BLOCKS_SET));
	}

	public void processParameters(StoryVisitor processController) {
		for (StoryComponent parameter : getParameters()) {
			parameter.process(processController);
		}
	}

	/**
	 * Double-dispatch for the subjects of the ScriptIt
	 * 
	 * @param processController
	 */
	public void processSubjects(StoryVisitor processController) {
		Collection<KnowIt> subjects = new ArrayList<KnowIt>();
		for (CodeBlock codeBlock : this.codeBlocks)
			if (codeBlock.hasSubject())
				subjects.add(codeBlock.getSubject());
		for (KnowIt subject : subjects) {
			subject.process(processController);
		}
	}

	/**
	 * Double-dispatch for the implicits of the ScriptIt
	 * 
	 * @param processController
	 */
	public void processImplicits(StoryVisitor processController) {
		for (KnowIt implicit : this.getImplicits())
			implicit.process(processController);
	}

	/**
	 * Returns the bindings on the ScriptIt's parameters
	 * 
	 * @return
	 */
	public Collection<KnowItBinding> getBindings() {
		final Collection<KnowItBinding> bindings;

		bindings = new ArrayList<KnowItBinding>();

		for (KnowIt parameter : this.getParameters()) {
			bindings.add(parameter.getBinding());
		}

		return bindings;
	}

	/**
	 * Returns whether the two causes are equivalent. That is, whether they have
	 * the same display text and the same bindings. If one of these ScriptIts is
	 * not a cause, this returns false.
	 * 
	 * @param cause
	 * @return
	 */
	public boolean isEquivalentToCause(ScriptIt cause) {
		boolean equality = true;

		equality &= cause.isCause() && this.isCause();
		equality &= cause.getDisplayText().equals(this.getDisplayText());
		equality &= cause.getBindings().equals(this.getBindings());

		return equality;
	}

	@Override
	public void revalidateKnowItBindings() {
		for (KnowIt parameter : this.getParameters()) {
			final KnowItBinding binding;

			binding = parameter.getBinding();

			if (!binding.compatibleWith(parameter))
				parameter.setBinding(new KnowItBindingNull());
		}

		if (this.isCause()) {
			this.getAlwaysBlock().revalidateKnowItBindings();
			this.getInactiveBlock().revalidateKnowItBindings();
			this.getActiveBlock().revalidateKnowItBindings();
		}
	}

	@Override
	public void componentChanged(StoryComponentEvent event) {
		final StoryComponentChangeEnum type = event.getType();
		final StoryComponent source = event.getSource();
		// The ScriptIt hijacks the event and sends it to it's observers
		if (this.codeBlocks.contains(source)) {
			if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_ADD) {
				this.notifyObservers(new StoryComponentEvent(this,
						StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_ADD));
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_REMOVE) {
				this.notifyObservers(new StoryComponentEvent(this,
						StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_REMOVE));
			} else if (type == StoryComponentChangeEnum.CHANGE_CODE_BLOCK_TYPES) {
				this.notifyObservers(new StoryComponentEvent(this,
						StoryComponentChangeEnum.CHANGE_CODE_BLOCK_TYPES));
			}
		}
	}
}
