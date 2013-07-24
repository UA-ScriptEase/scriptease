package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import scriptease.controller.StoryVisitor;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;

/**
 * A CauseIt represents a StoryComponent. It acts as a trigger and a cause /
 * pre-req. to effects.
 * 
 * It contains codeBlocks, which are able to have parameters, implicits,
 * subjects and slots and return types. It also has 3 blocks - Active, Inactive,
 * and Always corresponding to the appropriate state of execution.
 * 
 * @author mfchurch
 * @author kschenk
 * @author jyuen
 */
public class CauseIt extends ScriptIt {

	private static final String PREFIX = "<"
			+ StoryComponentPanelFactory.CURRENT_STORY_POINT_TAG + "> ";

	private static final String ACTIVE_BLOCK_TEXT = PREFIX + "Active:";
	private static final String INACTIVE_BLOCK_TEXT = PREFIX + "Inactive:";
	private static final String ALWAYS_BLOCK_TEXT = "Always:";

	// The group of children that are in the Story Point Active block
	private StoryComponentContainer activeBlock;

	// The group of children that are in the Story Point Inactive block
	private StoryComponentContainer inactiveBlock;

	// The group of children that are in the Always block .
	private StoryComponentContainer alwaysBlock;

	public CauseIt(String name) {
		super(name);
		final int NUMBER_OF_BLOCKS = 3;
		final List<Class<? extends StoryComponent>> validTypes = new ArrayList<Class<? extends StoryComponent>>();

		this.registerChildType(StoryComponentContainer.class, NUMBER_OF_BLOCKS);

		validTypes.add(ScriptIt.class);
		validTypes.add(KnowIt.class);
		validTypes.add(AskIt.class);
		validTypes.add(StoryComponentContainer.class);
		validTypes.add(Note.class);
		validTypes.add(ControlIt.class);

		this.activeBlock = new StoryComponentContainer(validTypes);
		this.inactiveBlock = new StoryComponentContainer(validTypes);
		this.alwaysBlock = new StoryComponentContainer(validTypes);

		this.activeBlock.setDisplayText(ACTIVE_BLOCK_TEXT);
		this.inactiveBlock.setDisplayText(INACTIVE_BLOCK_TEXT);
		this.alwaysBlock.setDisplayText(ALWAYS_BLOCK_TEXT);
	}

	/**
	 * Gets the container for children that are in the Story Point Active block
	 * of a cause.
	 * 
	 * @return
	 */
	public StoryComponentContainer getActiveBlock() {
		return this.activeBlock;
	}

	/**
	 * Gets the container for children that are in the Story Point Inactive
	 * block of a cause.
	 * 
	 * @return
	 */
	public StoryComponentContainer getInactiveBlock() {
		return this.inactiveBlock;
	}

	/**
	 * Gets the container for children that are in the Always block of a cause.
	 * 
	 * @return
	 */
	public StoryComponentContainer getAlwaysBlock() {
		return this.alwaysBlock;
	}

	private void setActiveBlock(StoryComponentContainer activeBlock) {
		// Change text for backwards compatibility
		activeBlock.setDisplayText(ACTIVE_BLOCK_TEXT);
		this.activeBlock = activeBlock;

		if (this.activeBlock != null)
			activeBlock.setOwner(this);
	}

	private void setInactiveBlock(StoryComponentContainer inactiveBlock) {
		// Change text for backwards compatibility
		inactiveBlock.setDisplayText(INACTIVE_BLOCK_TEXT);
		this.inactiveBlock = inactiveBlock;

		if (this.inactiveBlock != null)
			inactiveBlock.setOwner(this);
	}

	private void setAlwaysBlock(StoryComponentContainer alwaysBlock) {
		alwaysBlock.setDisplayText(ALWAYS_BLOCK_TEXT);
		this.alwaysBlock = alwaysBlock;

		if (this.alwaysBlock != null) {
			alwaysBlock.setOwner(this);
		}
	}

	public void removeCodeBlock(CodeBlock codeBlock) {
		super.removeCodeBlock(codeBlock);
		this.updateStoryChildren();
	}

	public void addCodeBlock(CodeBlock codeBlock) {
		super.addCodeBlock(codeBlock);
		this.updateStoryChildren();
	}

	public void setCodeBlocks(Collection<CodeBlock> codeBlocks) {
		super.setCodeBlocks(codeBlocks);
		this.updateStoryChildren();
	}

	/**
	 * Updates the CauseIt to display the Story Point Active and Inactive
	 * blocks, depending on if it is a cause or not.
	 */
	public void updateStoryChildren() {
		if (!this.childComponents.contains(this.activeBlock))
			this.addStoryChild(this.activeBlock);
		if (!this.childComponents.contains(this.inactiveBlock))
			this.addStoryChild(this.inactiveBlock);
		if (!this.childComponents.contains(this.alwaysBlock))
			this.addStoryChild(this.alwaysBlock);
	}

	@Override
	public CauseIt clone() {
		final CauseIt component = (CauseIt) super.clone();

		component
				.setActiveBlock((StoryComponentContainer) component.childComponents
						.get(0));
		component
				.setInactiveBlock((StoryComponentContainer) component.childComponents
						.get(1));

		component
				.setAlwaysBlock((StoryComponentContainer) component.childComponents
						.get(2));

		return component;
	}

	@Override
	public void process(StoryVisitor processController) {
		processController.processCauseIt(this);
	}

	/**
	 * Returns whether the two causes are equivalent. That is, whether they have
	 * the same display text and the same bindings. If one of these CauseIts is
	 * not a cause, this returns false.
	 * 
	 * @param cause
	 * @return
	 */
	public boolean isEquivalentToCause(CauseIt cause) {
		boolean equality = true;

		equality &= cause.getDisplayText().equals(this.getDisplayText());

		final Collection<String> thisSlots = new ArrayList<String>();
		final Collection<String> otherSlots = new ArrayList<String>();

		for (CodeBlock codeBlock : this.getCodeBlocks()) {
			thisSlots.add(codeBlock.getSlot());
		}

		for (CodeBlock codeBlock : cause.getCodeBlocks()) {
			otherSlots.add(codeBlock.getSlot());
		}
		
		equality &= thisSlots.equals(otherSlots);

		// This automatically checks if they have the same number of bindings.
		equality &= cause.getBindings().equals(this.getBindings());

		return equality;
	}

	@Override
	public boolean addStoryChildBefore(StoryComponent newChild,
			StoryComponent sibling) {

		boolean success = super.addStoryChildBefore(newChild, sibling);

		if (success) {
			final Iterator<StoryComponent> it = this.getChildren().iterator();
			if (it.next() == newChild)
				this.setActiveBlock((StoryComponentContainer) newChild);
			else if (it.next() == newChild)
				this.setInactiveBlock((StoryComponentContainer) newChild);
			else
				this.setAlwaysBlock((StoryComponentContainer) newChild);
		}
		return success;
	}

	@Override
	public void revalidateKnowItBindings() {
		super.revalidateKnowItBindings();

		this.getAlwaysBlock().revalidateKnowItBindings();
		this.getInactiveBlock().revalidateKnowItBindings();
		this.getActiveBlock().revalidateKnowItBindings();
	}

	@Override
	public String toString() {
		return "CauseIt [" + this.getDisplayText() + "]";
	}
}
