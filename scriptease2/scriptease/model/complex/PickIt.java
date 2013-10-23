package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;

/**
 * A PickIt represents a ComplexStoryComponent {@link ComplexStoryComponent}
 * with a probability attached to each of its children.
 * 
 * PickIt's have StoryComponentContainers {@link StoryComponentContainer} as
 * children. During execution, it 'picks' from one of these
 * StoryComponentContainers based on the normalized probability amongst each of
 * them. Everything in the chosen StoryComponentContainer is then executed,
 * while the rest are ignored.
 * 
 * @author jyuen
 * 
 */
public class PickIt extends ComplexStoryComponent {

	private Collection<StoryComponentContainer> choices;

	/**
	 * Builds a new PickIt.
	 */
	public PickIt() {
		super("<Pick>");

		final StoryComponentContainer choiceOne;
		final StoryComponentContainer choiceTwo;

		choiceOne = new StoryComponentContainer("Choice 1");
		choiceTwo = new StoryComponentContainer("Choice 2");

		this.choices = new ArrayList<StoryComponentContainer>();
		this.choices.add(choiceOne);
		this.choices.add(choiceTwo);

		this.registerChildType(StoryComponentContainer.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		this.addStoryChild(choiceOne);
		this.addStoryChild(choiceTwo);
	}

	/**
	 * Get the choices for this PickIt
	 * 
	 * @return
	 */
	public Collection<StoryComponentContainer> getChoices() {
		return this.choices;
	}

	@Override
	public void process(StoryVisitor processController) {
		processController.processPickIt(this);
	}

	@Override
	public PickIt clone() {
		final PickIt clone = (PickIt) super.clone();

		clone.choices = new ArrayList<StoryComponentContainer>();

		for (StoryComponentContainer choice : this.choices) {
			clone.choices.add(choice.clone());
		}

		return clone;
	}

	@Override
	public String toString() {
		return "PickIt [" + this.getDisplayText() + "]";
	}

	@Override
	public void revalidateKnowItBindings() {
		for (StoryComponent child : this.getChildren()) {
			child.revalidateKnowItBindings();
		}
	}
}
