package scriptease.model.complex;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;

/**
 * Represents the "if/else" programming construct.<br>
 * <Br>
 * AskIts are sort of a special case of ComplexStoryComponent in that they have
 * two distinct groups of children. That distinction is very important, so the
 * standard rules for children no longer properly apply. Calls to child-related
 * methods on an AskIt will give results based on the two containers for those
 * groups, not their contents. For example,
 * {@link ComplexStoryComponent#getChildren()} always returns a list of size 2
 * that whose contents are the two <code>StoryItemSequence</code>s that contain
 * the sub-groups. To add children to either group, get the container for that
 * group first, via either {@link AskIt#getIfBlock()} or
 * {@link AskIt#getElseBlock()}. <br>
 * <br>
 * The If and Else blocks of an AskIt will only accept DoIts and AskIts as
 * children.<br>
 * <br>
 * <u>Implementation Details</u><br>
 * The If and Else blocks have getters (and no setters) by design. Because
 * AskIts have two groups of children, a choice arose: either create a bunch of
 * methods in AskIt that conceptually duplicate ComplexStoryComponent methods (
 * <code>addChildToIfBlock(...)</code>, <code>addChildToElseBlock(...)</code>,
 * <code>addChildToIfBlockBefore(...)</code>, etc), or expose the two separate
 * child groups to allow clients to directly say which sub-group they mean to
 * add it to. <br>
 * <br>
 * The latter option was deemed to be better overall. Clients are the best ones
 * to decide where something should go, it creates less method bloat, and it
 * makes little semantic sense to blindly add a child to the AskIt. - remiller
 * 
 * @author friesen
 * @author remiller
 */
public final class AskIt extends ComplexStoryComponent {
	private KnowIt condition;

	/**
	 * The group of children that are in the If part of the AskIt.
	 */
	private StoryItemSequence ifBlock;

	/**
	 * The group of children that are in the Else part of the AskIt
	 */
	private StoryItemSequence elseBlock;

	/************* CONSTRUCTORS ********************/
	/**
	 * Builds a new AskIt.
	 * 
	 * @param condition
	 *            The KnowIt that defines the condition that evaluates this
	 *            AskIt.
	 */
	public AskIt(KnowIt condition) {
		super("<No Name>");

		final List<Class<? extends StoryComponent>> ifElseValidTypes;
		ifElseValidTypes = new ArrayList<Class<? extends StoryComponent>>();

		this.setCondition(condition);
		this.setDisplayText("<question>");

		// AskIts can have two children of type StoryItemSequence. These
		// function as containers for the If/Else blocks
		this.registerChildType(StoryItemSequence.class, 2);

		// Define the valid types for the two sub-groups
		ifElseValidTypes.add(AskIt.class);
		ifElseValidTypes.add(ScriptIt.class);
		ifElseValidTypes.add(KnowIt.class);
		ifElseValidTypes.add(StoryComponentContainer.class);

		// now we can Initialize the StoryItemSequences
		this.ifBlock = new StoryItemSequence(ifElseValidTypes);
		this.ifBlock.setDisplayText("Yes:");
		this.elseBlock = new StoryItemSequence(ifElseValidTypes);
		this.elseBlock.setDisplayText("No:");

		this.addStoryChild(this.ifBlock);
		this.addStoryChild(this.elseBlock);
	}

	/* ================== IMPORTANT CODE ================== */
	@Override
	public void process(StoryVisitor processController) {
		processController.processAskIt(this);
	}

	@Override
	public AskIt clone() {
		final AskIt clone = (AskIt) super.clone();

		clone.setCondition(this.condition.clone());

		// super.clone() clones the if/else blocks for us already because
		// they're children. We just need to reassign the variables to point to
		// the right ones.
		clone.setIfBlock((StoryItemSequence) clone.childComponents.get(0));
		clone.setElseBlock((StoryItemSequence) clone.childComponents.get(1));

		return clone;
	}

	/* ================== GETTERS/SETTERS ================== */
	/**
	 * Gets the KnowIt that stores the condition.
	 * 
	 * @return The boolean condition KnowIt.
	 */
	public KnowIt getCondition() {
		return this.condition;
	}

	/**
	 * Sets the KnowIt that stores the condition.
	 * 
	 * @param newCondition
	 *            The new condition to use.
	 */
	public void setCondition(KnowIt newCondition) {
		this.condition = newCondition;
		if (condition != null) {
			this.condition.setOwner(this);
		}
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_CONDITION_BOUND));
	}

	/**
	 * Gets the container for children that are in the If part of the AskIt.
	 * 
	 * @return the container for children that are in the If part of the AskIt.
	 */
	public StoryItemSequence getIfBlock() {
		return this.ifBlock;
	}

	public void setIfBlock(StoryItemSequence ifBlock) {
		this.ifBlock = ifBlock;
		ifBlock.setOwner(this);
	}

	/**
	 * Handles these stupid If/ElseBlock pointers.. we need to fix this TODO:
	 * NOT THIS
	 */
	@Override
	public boolean addStoryChildBefore(StoryComponent newChild,
			StoryComponent sibling) {
		boolean success = super.addStoryChildBefore(newChild, sibling);
		if (success) {
			if (this.getChildren().iterator().next() == newChild)
				this.setIfBlock((StoryItemSequence) newChild);
			else
				this.setElseBlock((StoryItemSequence) newChild);
		}
		return success;
	}

	/**
	 * Gets the container for children that are in the Else part of the AskIt.
	 * 
	 * @return the container for children that are in the Else part of the
	 *         AskIt.
	 */
	public StoryItemSequence getElseBlock() {
		return this.elseBlock;
	}

	public void setElseBlock(StoryItemSequence elseBlock) {
		this.elseBlock = elseBlock;
		elseBlock.setOwner(this);
	}

	@Override
	public String toString() {
		return "AskIt [" + this.getDisplayText() + "]";
	}
}
