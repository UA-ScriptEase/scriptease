package scriptease.controller;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;

/**
 * 
 * @author mfchurch
 * @author kschenk
 * 
 */
public class StoryComponentUtils {

	/**
	 * Returns all of the decendant StoryComponents of a given
	 * ComplexStoryComponent.
	 */
	public static Collection<StoryComponent> getAllDescendants(
			ComplexStoryComponent complex) {
		final Collection<StoryComponent> children = new ArrayList<StoryComponent>();

		for (StoryComponent child : complex.getChildren())
			child.process(new StoryAdapter() {

				@Override
				protected void defaultProcessComplex(
						ComplexStoryComponent complex) {
					children.add(complex);
					for (StoryComponent child : complex.getChildren())
						child.process(this);
				}

				@Override
				public void processScriptIt(ScriptIt scriptIt) {
					scriptIt.processParameters(this);
					this.defaultProcessComplex(scriptIt);
				}

				@Override
				public void processControlIt(ControlIt controlIt) {
					super.processControlIt(controlIt);
				}

				@Override
				public void processKnowIt(KnowIt knowIt) {
					final StoryAdapter adapter = this;
					children.add(knowIt);
					knowIt.getBinding().process(new BindingAdapter() {
						@Override
						public void processFunction(
								KnowItBindingFunction function) {
							function.getValue().process(adapter);
						}

						@Override
						public void processReference(
								KnowItBindingReference reference) {
							reference.getValue().process(adapter);
						}
					});
				}

				@Override
				public void processAskIt(AskIt questionIt) {
					children.add(questionIt);
					questionIt.getCondition().process(this);
					questionIt.getIfBlock().process(this);
					questionIt.getElseBlock().process(this);
				}
			});

		return children;
	}
}
