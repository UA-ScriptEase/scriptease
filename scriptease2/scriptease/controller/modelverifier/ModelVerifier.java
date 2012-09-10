package scriptease.controller.modelverifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import scriptease.controller.modelverifier.problem.ModelProblem;
import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.controller.modelverifier.rule.StoryRule;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.observer.storycomponent.StoryComponentObserverAdder;
import scriptease.controller.observer.storycomponent.StoryComponentObserverRemover;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WindowFactory;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.translator.TranslatorManager;

/**
 * ModelVerifier is a system designed to enforce specified rules on the given
 * Story at all times. The root given should be the highest level component in
 * the story (typically a StoryComponentContainer), and observers are added to
 * each StoryComponent in the tree. In the event of a change in the model, the
 * ModelVerifier will pass this change through it's rules, which will only fire
 * if the event matches that specified when the rule was added.
 * 
 * @author mfchurch
 * 
 */
public class ModelVerifier implements StoryComponentObserver {
	// Map the events to the visitors that should fire
	private Map<StoryComponentChangeEnum, Collection<StoryRule>> rules;

	// Root ComplexStoryComponent
	private ComplexStoryComponent root;

	// Boolean denoting if the ModelVerifier is currently solving problems, so
	// it knows not to verify while solving.
	private boolean isSolving;

	// The initial capacity for the rules map (adjust as more rules are added)
	private static final int INITIAL_CAPACITY = 4;

	public ModelVerifier(ComplexStoryComponent root) {
		this.root = root;
		this.rules = new HashMap<StoryComponentChangeEnum, Collection<StoryRule>>(
				ModelVerifier.INITIAL_CAPACITY);
		this.isSolving = false;
		// observe changes to the model
		StoryComponentObserverAdder adder = new StoryComponentObserverAdder();
		adder.observeEverything(this, this.root); 
	}

	/**
	 * Adds the given rule to the model verifier, so that it will fire the rule
	 * when the given events are triggered.
	 * 
	 * @param rule
	 *            - StoryVisitor that would act as a rule
	 * @param events
	 *            - Events that should trigger execution of the rule
	 */
	public void addRule(StoryRule rule,
			Collection<StoryComponentChangeEnum> events) {
		for (StoryComponentChangeEnum event : events) {
			Collection<StoryRule> visitors = this.rules.get(event);
			if (visitors == null) {
				visitors = new ArrayList<StoryRule>(ModelVerifier.INITIAL_CAPACITY);
			}
			visitors.add(rule);
			this.rules.put(event, visitors);
		}
	}

	/**
	 * When ModelVerifier is notified of a change in the model, validate the
	 * model with rules that are listening for the given eventType. Collect the
	 * problems with a string description of the solution and notify the user.
	 * If the user wishes to continue, apply the changes necessary to maintain
	 * model consistency otherwise undo the last action.
	 */
	@Override
	public void componentChanged(StoryComponentEvent event) {
		final StoryComponentChangeEnum eventType = event.getType();
		final StoryComponent source = event.getSource();

		// process addition, removal from the model
		if (eventType == StoryComponentChangeEnum.CHANGE_CHILD_ADDED) {
			StoryComponentObserverAdder adder = new StoryComponentObserverAdder();
			adder.observeEverything(this, source);
		} else if (eventType == StoryComponentChangeEnum.CHANGE_CHILD_REMOVED)
			StoryComponentObserverRemover.removeObservers(this, source);

		// ignore changes to the model while solving problems,
		// Undodoing/Redoing, or if there is no active translator
		if (this.isSolving
				|| UndoManager.getInstance().isUndoingOrRedoing()
				|| TranslatorManager.getInstance().getActiveTranslator() == null)
			return;

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ModelVerifier.this.assertModelRules(source, eventType);
			}
		});
	}

	private void assertModelRules(StoryComponent source,
			StoryComponentChangeEnum eventType) {
		// Find problems in the model
		final Collection<StoryRule> visitors = this.rules.get(eventType);
		final List<StoryProblem> storyProblems = new ArrayList<StoryProblem>();

		if (visitors != null) {
			for (StoryRule rule : visitors) {
				storyProblems.addAll(rule.validate(this.root, source));
			}
		}

		// Sort the model problems based on priority
		Collections.sort(storyProblems, new Comparator<StoryProblem>() {
			@Override
			public int compare(StoryProblem item1, StoryProblem item2) {
				if (item1 instanceof ModelProblem
						&& item2 instanceof ModelProblem) {
					int priority1 = ((ModelProblem) item1).getPriority();
					int priority2 = ((ModelProblem) item2).getPriority();

					return priority2 - priority1;
				}
				return 0;
			}
		});

		// If problems exist :
		if (storyProblems != null && !storyProblems.isEmpty()) {
			boolean notify = false;
			boolean accept = true;

			// if the user is supposed to be notified of any problems
			for (StoryProblem problem : storyProblems) {
				if (problem.shouldNotify())
					notify = true;
			}

			// only solve one problem set at a time
			if (!this.isSolving) {
				this.isSolving = true;

				if (notify) {
					accept = WindowFactory.getInstance().showSolvableProblems(
							storyProblems);
				}

				/*
				 * If the user accepts, they want to continue, so apply the
				 * solutions to all problems. Otherwise, undo the previous
				 * action to restore model state.
				 */
				if (accept) {
					if (!UndoManager.getInstance().hasOpenUndoableAction()
							&& UndoManager.getInstance().canUndo())
						UndoManager.getInstance().appendToLastAction();
					for (StoryProblem problem : storyProblems) {
						if (problem instanceof ModelProblem)
							((ModelProblem) problem).solve();
					}
					if (UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().endUndoableAction();
				} else {
					// SwingUtilities.invokeLater(new Runnable() {
					// @Override
					// public void run() {
					if (UndoManager.getInstance().hasOpenUndoableAction())
						UndoManager.getInstance().endUndoableAction();
					UndoManager.getInstance().undo();
					UndoManager.getInstance().clearRedo();
					// }
					// });
				}
				this.isSolving = false;
			}
		}
	}

	@Override
	public String toString() {
		return "ModelVerifier";
	}
}
