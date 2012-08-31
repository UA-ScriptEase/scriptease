package scriptease.controller.observer;

import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;

/*****************************************************
 * Visitor component for removing the given observer as an observer of the given
 * observable, it's immediate components and it's children
 * 
 * @author mfchurch
 *****************************************************/

public class StoryComponentObserverRemover extends AbstractNoOpStoryVisitor {
	private StoryComponentObserver observer; 

	public static void removeObservers(StoryComponentObserver observer,
			StoryComponent observable) {
		StoryComponentObserverRemover remover = new StoryComponentObserverRemover(
				observer);
		observable.process(remover);
	}

	private StoryComponentObserverRemover(StoryComponentObserver observer) {
		this.observer = observer;
	}

	private void processComplexStoryComponent(ComplexStoryComponent component) {
		for (StoryComponent child : component.getChildren()) {
			child.process(this);
		}
	}

	@Override
	public void processStoryComponentContainer(
			StoryComponentContainer storyComponentContainer) {
		storyComponentContainer.removeStoryComponentObserver(this.observer);
		this.processComplexStoryComponent(storyComponentContainer);
	}

	@Override
	public void processScriptIt(ScriptIt scriptIt) {
		scriptIt.removeStoryComponentObserver(this.observer);
		scriptIt.processParameters(this);
		scriptIt.processChildren(this);
		scriptIt.processSubjects(this);
	}

	@Override
	public void processKnowIt(KnowIt knowIt) {
		knowIt.removeStoryComponentObserver(this.observer);
	}

	@Override
	public void processAskIt(AskIt askIt) {
		askIt.removeStoryComponentObserver(this.observer);
		askIt.getCondition().process(this);
		this.processComplexStoryComponent(askIt);
	}

	@Override
	public void processStoryItemSequence(StoryItemSequence sequence) {
		sequence.removeStoryComponentObserver(this.observer);
		this.processComplexStoryComponent(sequence);
	}
}