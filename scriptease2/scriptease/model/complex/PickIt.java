package scriptease.model.complex;

import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

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

	/**
	 * A WeakHashMap for the choices and their probabilities.
	 */
	private Map<StoryComponentContainer, Integer> choices;

	/**
	 * Builds a new PickIt.
	 */
	public PickIt() {
		super("<Pick>");

		this.setDisplayText("Pick");

		this.registerChildType(StoryComponentContainer.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		final StoryComponentContainer choiceOne;
		final StoryComponentContainer choiceTwo;

		choiceOne = new StoryComponentContainer("Choice 1");
		choiceTwo = new StoryComponentContainer("Choice 2");

		this.choices = new WeakHashMap<StoryComponentContainer, Integer>();
		this.choices.put(choiceOne, 50);
		this.choices.put(choiceTwo, 50);

		this.addStoryChild(choiceOne);
		this.addStoryChild(choiceTwo);
	}

	/**
	 * Get the choices for this PickIt
	 * 
	 * @return
	 */
	public Map<StoryComponentContainer, Integer> getChoices() {
		return this.choices;
	}

	@Override
	public void process(StoryVisitor processController) {
		processController.processPickIt(this);
	}

	@Override
	public PickIt clone() {
		final PickIt clone = (PickIt) super.clone();

		clone.choices = new WeakHashMap<StoryComponentContainer, Integer>();

		for (Entry<StoryComponentContainer, Integer> entry : this.choices
				.entrySet()) {
			clone.choices.put(entry.getKey().clone(), entry.getValue());
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
