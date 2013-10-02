package scriptease.model.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.StoryVisitor;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.complex.behaviours.Behaviour;

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
	public CauseIt(String name) {
		super(name);
		final List<Class<? extends StoryComponent>> validTypes = new ArrayList<Class<? extends StoryComponent>>();

		validTypes.add(ScriptIt.class);
		validTypes.add(KnowIt.class);
		validTypes.add(AskIt.class);
		validTypes.add(StoryComponentContainer.class);
		validTypes.add(Note.class);
		validTypes.add(ControlIt.class);
		validTypes.add(Behaviour.class);

		this.registerChildTypes(validTypes, MAX_NUM_OF_ONE_TYPE);
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
	public CauseIt clone() {
		return (CauseIt) super.clone();
	}

	@Override
	public void process(StoryVisitor processController) {
		processController.processCauseIt(this);
	}

	@Override
	public String toString() {
		return "CauseIt [" + this.getDisplayText() + "]";
	}
}
