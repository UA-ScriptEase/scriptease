package scriptease.controller;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;

/**
 * Provides some utilities for {@link StoryComponent}s, including methods that
 * get specific types of children, etc.
 * 
 * @author mfchurch
 * @author kschenk
 * 
 */
public class StoryComponentUtils {

	/**
	 * Get all variables for the component.
	 * 
	 * @param component
	 * @return
	 */
	public static Collection<KnowIt> getVariables(StoryComponent component) {
		final Collection<KnowIt> variables = new ArrayList<KnowIt>();

		component.process(new StoryAdapter() {

			/**
			 * Get dependant variables. Process bindings before processing
			 * dependant KnowIts, so the order of resolution is correct
			 */
			@Override
			public void processKnowIt(KnowIt knowIt) {
				knowIt.getBinding().process(new BindingAdapter() {
					@Override
					public void processFunction(KnowItBindingFunction function) {
						final ScriptIt referenced = function.getValue();

						for (KnowIt parameter : referenced.getParameters()) {
							parameter.getBinding().process(this);
							variables.add(parameter);
						}
					}
				});
				variables.add(knowIt);
			}

			@Override
			public void processAskIt(AskIt questionIt) {
				KnowIt condition = questionIt.getCondition();
				variables.add(condition);
			}

			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				scriptIt.processParameters(this);
			}

			@Override
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				super.defaultProcessComplex(complex);
				complex.processChildren(this);
			}
		});

		return variables;
	}

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
