package scriptease.model.complex;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.translator.io.model.GameType;

/**
 * Represents the "if/else" programming construct.<br>
 * <Br>
 * AskIts are sort of a special case of ComplexStoryComponent in that they have
 * two distinct groups of children. That distinction is very important, so the
 * standard rules for children no longer properly apply. Calls to child-related
 * methods on an AskIt will give results based on the two containers for those
 * groups, not their contents. For example,
 * {@link ComplexStoryComponent#getChildren()} always returns a list of size 2
 * that whose contents are the two <code>StoryComponentContainer</code>s that
 * contain the sub-groups. To add children to either group, get the container
 * for that group first, via either {@link AskIt#getIfBlock()} or
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
 * @author jyuen
 */
public final class AskIt extends ComplexStoryComponent {
	private KnowIt condition;

	/**
	 * The group of children that are in the If part of the AskIt.
	 */
	private StoryComponentContainer ifBlock;

	/**
	 * The group of children that are in the Else part of the AskIt
	 */
	private StoryComponentContainer elseBlock;

	/************* CONSTRUCTORS ********************/
	/**
	 * Builds a new AskIt.
	 * 
	 */
	public AskIt() {
		super("<Question>");

		final List<Class<? extends StoryComponent>> ifElseValidTypes;

		ifElseValidTypes = new ArrayList<Class<? extends StoryComponent>>();

		this.setCondition(new KnowIt("Question", GameType.DEFAULT_BOOL_TYPE));

		// AskIts can have two children of type StoryComponentContainer. These
		// function as containers for the If/Else blocks
		this.registerChildType(StoryComponentContainer.class, 2);
		
		// Define the valid types for the two sub-groups
		ifElseValidTypes.add(AskIt.class);
		ifElseValidTypes.add(ScriptIt.class);
		ifElseValidTypes.add(KnowIt.class);
		ifElseValidTypes.add(StoryComponentContainer.class);
		ifElseValidTypes.add(Note.class);
		ifElseValidTypes.add(ControlIt.class);

		// now we can Initialize the StoryComponentContainer
		this.ifBlock = new StoryComponentContainer(ifElseValidTypes);
		this.ifBlock.setDisplayText("Yes:");
		this.elseBlock = new StoryComponentContainer(ifElseValidTypes);
		this.elseBlock.setDisplayText("No:");

		this.addStoryChild(this.ifBlock);
		this.addStoryChild(this.elseBlock);
		
		this.setDisplayText("<Question>");
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
		clone.setIfBlock((StoryComponentContainer) clone.childComponents.get(0));
		clone.setElseBlock((StoryComponentContainer) clone.childComponents
				.get(1));

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
		if (this.condition != null) {
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
	public StoryComponentContainer getIfBlock() {
		return this.ifBlock;
	}

	/**
	 * Gets the container for children that are in the Else part of the AskIt.
	 * 
	 * @return the container for children that are in the Else part of the
	 *         AskIt.
	 */
	public StoryComponentContainer getElseBlock() {
		return this.elseBlock;
	}

	public void setIfBlock(StoryComponentContainer ifBlock) {
		this.ifBlock = ifBlock;
		ifBlock.setOwner(this);
	}

	public void setElseBlock(StoryComponentContainer elseBlock) {
		this.elseBlock = elseBlock;
		elseBlock.setOwner(this);
	}

	@Override
	public boolean addStoryChildBefore(StoryComponent newChild,
			StoryComponent sibling) {
		boolean success = super.addStoryChildBefore(newChild, sibling);
		if (success) {
			if (this.getChildren().iterator().next() == newChild)
				this.setIfBlock((StoryComponentContainer) newChild);
			else
				this.setElseBlock((StoryComponentContainer) newChild);
		}
		return success;
	}

	@Override
	public String toString() {
		return "AskIt [" + this.getDisplayText() + "]";
	}

	@Override
	public void revalidateKnowItBindings() {
		this.getCondition().revalidateKnowItBindings();
		this.getIfBlock().revalidateKnowItBindings();
		this.getElseBlock().revalidateKnowItBindings();
	}

	@Override
	public void setEnabled(Boolean enabled) {
		super.setEnabled(enabled);

		condition.setEnabled(enabled);

		// Enable the descriptions that are used as bindings if this
		// is enabled
		if (enabled) {
			final KnowIt condition = this.getCondition();
			final KnowItBinding binding = condition.getBinding();
			
			if (binding instanceof KnowItBindingReference) {
				final KnowItBindingReference reference = (KnowItBindingReference) binding;

				final KnowIt value = reference.getValue();

				if (!value.isEnabled())
					value.setEnabled(true);
			}
		}
	}
}
