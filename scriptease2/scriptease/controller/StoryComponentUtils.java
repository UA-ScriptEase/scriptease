package scriptease.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockReference;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.Task;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.io.model.Resource;

/**
 * Provides some utilities for {@link StoryComponent}s, including methods that
 * get specific types of children, etc.
 * 
 * @author mfchurch
 * @author kschenk
 * 
 */
public class StoryComponentUtils {
	public static void bindUninitialized(final ScriptIt scriptIt ) {
		final List<StoryComponent> children = scriptIt.getChildren();

		for (StoryComponent child : children) {

			child.process(new StoryAdapter() {

				@Override
				public void processScriptIt(ScriptIt scriptIt) {
					this.defaultProcessComplex(scriptIt);
					scriptIt.processParameters(this);
				}

				@Override
				public void processKnowIt(KnowIt knowIt) {
					final KnowItBinding binding = knowIt.getBinding();

					if (binding instanceof KnowItBindingFunction) {
						final KnowItBindingFunction function = (KnowItBindingFunction) binding;

						function.getValue().process(this);

					} else if (binding instanceof KnowItBindingUninitialized) {
						KnowItBindingUninitialized uninitialized = (KnowItBindingUninitialized) binding;

						for (KnowIt parameter : scriptIt.getParameters()) {
							if (uninitialized.getValue().getDisplayText()
									.equals(parameter.getDisplayText())) {
								uninitialized = new KnowItBindingUninitialized(
										new KnowItBindingReference(parameter));

								knowIt.setBinding(uninitialized);
								break;
							}
						}
					}
				}

				@Override
				protected void defaultProcessComplex(
						ComplexStoryComponent complex) {
					for (StoryComponent child : complex.getChildren()) {
						child.process(this);
					}
				}
			});
		}
	}
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
			public void processCauseIt(CauseIt causeIt) {
				super.processCauseIt(causeIt);
				this.defaultProcessComplex(causeIt);
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
	 * Returns the story point that contains the parent somewhere.
	 * 
	 * @param component
	 * @return
	 */
	public static StoryPoint getParentStoryPoint(StoryComponent component) {
		StoryComponent parent = component.getOwner();

		while (parent != null && !(parent instanceof StoryPoint)) {
			parent = parent.getOwner();
		}

		return (StoryPoint) parent;
	}

	/**
	 * Gets all code blocks from descendants of and from the root component.
	 * 
	 * @param component
	 * @return
	 */
	public static Collection<CodeBlock> getCodeBlocks(StoryComponent component) {
		final Collection<CodeBlock> codeBlocks = new ArrayList<CodeBlock>();

		final DescendantCollector collector = new DescendantCollector() {
			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				super.processScriptIt(scriptIt);

				if (!codeBlocks.contains(codeBlocks))
					codeBlocks.addAll(scriptIt.getCodeBlocks());
			}
		};

		component.process(collector);

		return codeBlocks;
	}

	/**
	 * Returns all knowits bound to the value of a particular resource
	 * 
	 * @param selected
	 * @return
	 */
	public static Collection<KnowIt> getKnowItsWithResource(
			final Resource selected, StoryNode node) {
		final Collection<KnowIt> matched = new ArrayList<KnowIt>();
		final DescendantCollector collector = new DescendantCollector() {
			@Override
			public void processKnowIt(final KnowIt knowIt) {
				super.processKnowIt(knowIt);
				knowIt.getBinding().process(new BindingAdapter() {
					@Override
					public void processResource(KnowItBindingResource resource) {
						if (selected == resource.getValue()) {
							matched.add(knowIt);
						}
					}
				});
			}
		};
		node.process(collector);
		return matched;
	}

	/**
	 * Returns all scriptIts descended from the ComplexStoryComponent passed in.
	 * 
	 * @param complex
	 * @return
	 */
	public static Collection<ScriptIt> getDescendantScriptIts(
			ComplexStoryComponent complex) {
		final Collection<ScriptIt> scriptIts = new HashSet<ScriptIt>();
		final DescendantCollector collector = new DescendantCollector() {
			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				super.processScriptIt(scriptIt);
				if (!scriptIts.contains(scriptIt))
					scriptIts.add(scriptIt);
			}
		};

		complex.process(collector);

		return scriptIts;
	}

	/**
	 * Returns all scriptIts descended from the ComplexStoryComponent passed in.
	 * 
	 * @param complex
	 * @return
	 */
	public static Collection<Behaviour> getDescendantBehaviours(
			ComplexStoryComponent complex) {
		final Collection<Behaviour> behaviours = new HashSet<Behaviour>();
		final DescendantCollector collector = new DescendantCollector() {

			@Override
			public void processBehaviour(Behaviour behaviour) {
				super.processBehaviour(behaviour);
				if (!behaviours.contains(behaviour))
					behaviours.add(behaviour);
			}
		};

		complex.process(collector);

		return behaviours;
	}

	/**
	 * Returns all knowIts with descriptions descended from the
	 * ComplexStoryComponent passed in.
	 * 
	 * @param complex
	 * @return
	 */
	public static Collection<KnowIt> getDescendantDescriptions(
			ComplexStoryComponent complex) {
		final Collection<KnowIt> knowIts = new HashSet<KnowIt>();
		final DescendantCollector collector = new DescendantCollector() {
			@Override
			public void processKnowIt(KnowIt knowIt) {
				super.processKnowIt(knowIt);
				if (knowIt.getLibrary().getDescribeIt(knowIt) != null)
					knowIts.add(knowIt);
			}
		};

		complex.process(collector);

		return knowIts;
	}

	public static void duplicate(final StoryComponent component,
			final LibraryModel library) {

		component.process(new StoryAdapter() {
			@Override
			public void processKnowIt(KnowIt knowIt) {
				knowIt.setLibrary(library);
			}

			// Clone ScriptIts, then replace the referenced codeBlocks
			// with modifiable duplicates since we want them to be
			// unique
			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				final ScriptIt clone = scriptIt.clone();
				final Collection<CodeBlock> codeBlocks = clone.getCodeBlocks();
				for (CodeBlock codeBlock : codeBlocks) {
					clone.removeCodeBlock(codeBlock);
					codeBlock.process(new StoryAdapter() {

						@Override
						public void processCodeBlockSource(
								CodeBlockSource codeBlockSource) {
							clone.addCodeBlock(codeBlockSource
									.duplicate(library));
						}

						@Override
						public void processCodeBlockReference(
								CodeBlockReference codeBlockReference) {
							codeBlockReference.getTarget().process(this);
						}
					});
				}
				clone.processParameters(this);
				clone.setLibrary(library);
				library.add(clone);
			}

			@Override
			protected void defaultProcess(StoryComponent component) {
				final StoryComponent clone = component.clone();
				clone.setLibrary(library);
				library.add(clone);
			}
		});
	}

	/**
	 * Returns all of the decendant StoryComponents of a given
	 * ComplexStoryComponent. This is quite computationally expensive,
	 * especially if you intend to go over all of them afterwards. It's
	 * recommended to use a method like
	 * {@link #getDescendantScriptIts(ComplexStoryComponent)} instead to find
	 * specific story components.
	 */
	public static Collection<StoryComponent> getAllDescendants(
			ComplexStoryComponent complex) {
		final DescendantCollector collector = new DescendantCollector();

		complex.process(collector);

		return collector.getChildren();
	}
}

/**
 * Collects all descendants of a complex story component. Note that by
 * descendants we mean children of children of children. So this will not return
 * other story points known by a story point.
 * 
 * @author kschenk
 * 
 */
class DescendantCollector extends StoryAdapter {
	private final Collection<StoryComponent> children = new HashSet<StoryComponent>();

	protected Collection<StoryComponent> getChildren() {
		return this.children;
	}

	@Override
	protected void defaultProcessComplex(ComplexStoryComponent complex) {
		if (!this.children.contains(complex)) {
			this.children.add(complex);
			for (StoryComponent child : complex.getChildren())
				child.process(this);
		}
	}

	@Override
	public void processCauseIt(CauseIt causeIt) {
		if (!this.children.contains(causeIt)) {
			causeIt.processImplicits(this);
			this.processScriptIt(causeIt);
		}
	}

	@Override
	public void processScriptIt(ScriptIt scriptIt) {
		if (!this.children.contains(scriptIt)) {
			scriptIt.processParameters(this);
			this.defaultProcessComplex(scriptIt);
		}
	}

	@Override
	public void processKnowIt(KnowIt knowIt) {
		if (!this.children.contains(knowIt)) {
			final StoryAdapter adapter = this;
			this.children.add(knowIt);
			knowIt.getBinding().process(new BindingAdapter() {
				@Override
				public void processFunction(KnowItBindingFunction function) {
					function.getValue().process(adapter);
				}

				@Override
				public void processReference(KnowItBindingReference reference) {
					reference.getValue().process(adapter);
				}
			});
		}
	}

	@Override
	public void processAskIt(AskIt questionIt) {
		if (!this.children.contains(questionIt)) {
			questionIt.getCondition().process(this);
			this.defaultProcessComplex(questionIt);
		}
	}

	@Override
	public void processTask(Task task) {
		if (!this.children.contains(task)) {
			for (Task successor : task.getSuccessors()) {
				if (!this.children.contains(task))
					this.processTask(successor);
			}
			this.defaultProcessComplex(task);
		}
	}
}
