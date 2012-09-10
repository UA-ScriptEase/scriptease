package scriptease.controller.observer.storycomponent;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;

/*****************************************************
 * Helper Class used for applying different levels of StoryComponent observation
 * on a given observable
 * 
 * @author mfchurch
 *****************************************************/

public class StoryComponentObserverAdder {

	/**
	 * Visitor component for adding the given observer as an observer of the
	 * given observable, it's immediate components and it's children
	 * 
	 * @param observer
	 * @param observable
	 */
	public void observeEverything(StoryComponentObserver observer,
			StoryComponent observable) {
		EverythingObserver everythingObserver = new EverythingObserver(observer);
		observable.process(everythingObserver);
	}

	/**
	 * Visitor component for adding the given observer as an observer of the
	 * given observable and it's immediate parameters and children
	 * 
	 * @param observer
	 * @param observable
	 */
	public void observeRelated(StoryComponentObserver observer,
			StoryComponent observable) {
		RelatedObserver relatedObserver = new RelatedObserver(observer);
		observable.process(relatedObserver);
	}

	private class EverythingObserver extends StoryAdapter {
		private StoryComponentObserver observer;

		public EverythingObserver(StoryComponentObserver observer) {
			this.observer = observer;
		}

		@Override
		protected void defaultProcessComplex(ComplexStoryComponent complex) {
			complex.addStoryComponentObserver(this.observer);
			for (StoryComponent child : complex.getChildren()) {
				child.process(this);
			}
		}

		@Override
		public void processStoryComponentContainer(
				StoryComponentContainer storyComponentContainer) {
			storyComponentContainer.addStoryComponentObserver(this.observer);
			this.defaultProcessComplex(storyComponentContainer);
		}

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			scriptIt.addStoryComponentObserver(this.observer);
			scriptIt.processSubjects(this);
			scriptIt.processParameters(this);
			this.defaultProcessComplex(scriptIt);
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			knowIt.addStoryComponentObserver(this.observer);
			KnowItBinding binding = knowIt.getBinding();
			binding.process(new BindingAdapter() {
				@Override
				public void processReference(KnowItBindingReference reference) {
					KnowIt referenced = reference.getValue();
					referenced.process(EverythingObserver.this);
				}

				@Override
				public void processFunction(KnowItBindingFunction function) {
					ScriptIt referenced = function.getValue();
					referenced.process(EverythingObserver.this);
				}
			});
		}

		@Override
		public void processAskIt(AskIt askIt) {
			askIt.addStoryComponentObserver(this.observer);
			askIt.getCondition().process(this);
			this.defaultProcessComplex(askIt);
		}

		@Override
		public void processStoryItemSequence(StoryItemSequence sequence) {
			sequence.addStoryComponentObserver(this.observer);
			this.defaultProcessComplex(sequence);
		}
	}

	private class RelatedObserver extends StoryAdapter {
		private StoryComponentObserver observer; 

		public RelatedObserver(StoryComponentObserver observer) {
			this.observer = observer;
		}

		@Override
		public void processStoryComponentContainer(
				StoryComponentContainer storyComponentContainer) {
			storyComponentContainer.addStoryComponentObserver(this.observer);
		}
		
		@Override
		protected void defaultProcessComplex(ComplexStoryComponent complex) {
			complex.addStoryComponentObserver(this.observer);
		}

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			scriptIt.addStoryComponentObserver(this.observer);
			scriptIt.processParameters(this);
			scriptIt.processSubjects(this);
			scriptIt.processChildren(this);
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			knowIt.addStoryComponentObserver(this.observer);
		}

		@Override
		public void processAskIt(AskIt askIt) {
			askIt.addStoryComponentObserver(this.observer);
			askIt.getCondition().process(this);
		}

		@Override
		public void processStoryItemSequence(StoryItemSequence sequence) {
			sequence.addStoryComponentObserver(this.observer);
		}
	}
}
