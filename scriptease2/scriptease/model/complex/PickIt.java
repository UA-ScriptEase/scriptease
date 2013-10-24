package scriptease.model.complex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;

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

	private int choiceCounter;

	/**
	 * Builds a new PickIt.
	 */
	public PickIt() {
		super("<Pick>");

		final List<Class<? extends StoryComponent>> choiceValidTypes;

		final StoryComponentContainer choiceOne;
		final StoryComponentContainer choiceTwo;

		choiceValidTypes = new ArrayList<Class<? extends StoryComponent>>();

		this.choiceCounter = 2;

		this.setDisplayText("Pick");

		this.registerChildType(StoryComponentContainer.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		// Define the valid types for the choices
		choiceValidTypes.add(AskIt.class);
		choiceValidTypes.add(ScriptIt.class);
		choiceValidTypes.add(KnowIt.class);
		choiceValidTypes.add(StoryComponentContainer.class);
		choiceValidTypes.add(Note.class);
		choiceValidTypes.add(ControlIt.class);
		choiceValidTypes.add(PickIt.class);

		choiceOne = new StoryComponentContainer("Choice 1", choiceValidTypes);
		choiceTwo = new StoryComponentContainer("Choice 2", choiceValidTypes);

		this.choices = new WeakHashMap<StoryComponentContainer, Integer>();
		this.choices.put(choiceOne, 50);
		this.choices.put(choiceTwo, 50);

		this.addStoryChild(choiceOne);
		this.addStoryChild(choiceTwo);
	}

	public void addChoice(int probability) {
		final List<Class<? extends StoryComponent>> choiceValidTypes;
		final StoryComponentContainer choice;

		choiceValidTypes = new ArrayList<Class<? extends StoryComponent>>();

		choiceValidTypes.add(AskIt.class);
		choiceValidTypes.add(ScriptIt.class);
		choiceValidTypes.add(KnowIt.class);
		choiceValidTypes.add(StoryComponentContainer.class);
		choiceValidTypes.add(Note.class);
		choiceValidTypes.add(ControlIt.class);
		choiceValidTypes.add(PickIt.class);

		choice = new StoryComponentContainer("Choice " + choiceCounter++,
				choiceValidTypes);

		this.choices.put(choice, probability);
		this.addStoryChild(choice);
	}

	/**
	 * Get the choices for this PickIt
	 * 
	 * @return
	 */
	public Map<StoryComponentContainer, Integer> getChoices() {
		return this.choices;
	}

	/**
	 * Set the choices for this PickIt
	 * 
	 * @param choices
	 */
	public void setChoices(Map<StoryComponentContainer, Integer> choices) {
		this.choices = choices;
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
