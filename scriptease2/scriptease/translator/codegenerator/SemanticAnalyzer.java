package scriptease.translator.codegenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.controller.modelverifier.rule.ParameterBoundRule;
import scriptease.controller.modelverifier.rule.StoryRule;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryPoint;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.model.semodel.librarymodel.TypeConverter;
import scriptease.translator.TranslatorManager;

/**
 * Code generation phase where recursive analysis of the StoryComponent tree
 * takes place and the symbol table is built. The symbol table is populated with
 * CodeContextSets, which are the groupings of the three ways a code concept can
 * appear: declaration, definition, and reference. <br>
 * <br>
 * This phase also checks for semantic problems like unbound KnowIts or empty
 * ComplexStoryComponents. Furthermore, it can report a list of include file
 * names required by processed DoIts with the {@link #buildContext()} method.
 * 
 * @author remiller
 * @author mfchurch
 * @author jyuen
 */
public class SemanticAnalyzer extends StoryAdapter {
	private final Collection<StoryRule> rules;

	/**
	 * List of code generation errors and warnings
	 */
	private final List<StoryProblem> problems;

	/**
	 * Creates a new instance that will recursively analyze the StoryComponent
	 * tree.
	 */
	public SemanticAnalyzer(Collection<StoryPoint> storyPoints) {
		this.problems = new ArrayList<StoryProblem>();
		this.rules = new ArrayList<StoryRule>();

		// Make sure all parameters are bound before generating code
		this.rules.add(new ParameterBoundRule());
		// Get all the StoryNodes in the model
		for (StoryPoint storyPoint : storyPoints) {
			// Process all the components from each StoryPoint
			final int initialSize = this.problems.size();

			for (StoryComponent child : storyPoint.getChildren()) {
				child.process(this);
			}

			storyPoint.setHasProblems(this.problems.size() > initialSize);
		}
	}

	/**
	 * Gets the problems encountered by the semantic analysis phase of Code
	 * Generation.
	 * 
	 * @return the code generation problems
	 */
	public List<StoryProblem> getProblems() {
		return new ArrayList<StoryProblem>(this.problems);
	}

	@Override
	public void processAskIt(AskIt askIt) {
		this.defaultProcessComplex(askIt);

		askIt.getCondition().process(this);
	}

	@Override
	public void processScriptIt(ScriptIt scriptIt) {
		this.defaultProcessComplex(scriptIt);

		scriptIt.processParameters(this);
		scriptIt.processSubjects(this);
	}

	@Override
	public void processBehaviour(Behaviour behaviour) {
		this.processScriptIt(behaviour);

		// final Task startTask = behaviour.getStartTask();
		// startTask.process(this);
		// for (Task task : startTask.getSuccessors())
		// task.process(this);
	}

	@Override
	public void processKnowIt(final KnowIt knowIt) {
		final KnowItBinding binding = knowIt.getBinding();
		binding.process(new BindingAdapter() {
			@Override
			public void processReference(KnowItBindingReference reference) {
				final KnowIt referenced = reference.getValue();
				referenced.process(SemanticAnalyzer.this);
			}

			@Override
			public void processFunction(KnowItBindingFunction function) {
				final ScriptIt referenced = function.getValue();
				referenced.process(SemanticAnalyzer.this);
			}
		});

		/*
		 * check if we need to convert the binding to be an acceptable type at
		 * codegen time
		 */
		if (!binding.explicitlyCompatibleWith(knowIt)) {
			ScriptIt scriptIt = null;

			for (LibraryModel library : TranslatorManager.getInstance()
					.getActiveTranslator().getLibraries()) {
				final TypeConverter converter = library.getTypeConverter();
				scriptIt = converter.convert(knowIt);

				if (scriptIt != null)
					break;
			}
			// TODO we aren't checking optional libraries that may be loaded.
			if (scriptIt != null)
				scriptIt.process(SemanticAnalyzer.this);
		}
	}

	@Override
	protected void defaultProcessComplex(ComplexStoryComponent complex) {
		this.verifyRules(complex);

		complex.processChildren(this);
	}

	private void verifyRules(StoryComponent component) {
		for (StoryRule rule : this.rules) {
			final Collection<StoryProblem> problems;

			problems = rule.validate(null, component);

			this.problems.addAll(problems);
			component.setHasProblems(!problems.isEmpty());
		}
	}
}
