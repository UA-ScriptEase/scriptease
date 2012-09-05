package scriptease.translator.codegenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.controller.get.QuestPointNodeGetter;
import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.controller.modelverifier.rule.ParameterBoundRule;
import scriptease.controller.modelverifier.rule.StoryRule;
import scriptease.gui.quests.QuestNode;
import scriptease.gui.quests.QuestPoint;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.TypeConverter;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.FileContext;

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
 */
public class SemanticAnalyzer extends StoryAdapter {
	private final QuestNode model;
	private final Translator translator;
	private final Collection<StoryRule> rules;

	/**
	 * List of code generation errors and warnings
	 */
	private final List<StoryProblem> problems;

	/**
	 * Creates a new instance that will recursively analyze the StoryComponent
	 * tree.
	 */
	public SemanticAnalyzer(QuestNode model, Translator translator) {
		this.problems = new ArrayList<StoryProblem>();
		this.translator = translator;
		this.rules = new ArrayList<StoryRule>();
		this.model = model;

		// Make sure all parameters are bound before generating code
		this.rules.add(new ParameterBoundRule());
		// Get all the QuestPoints in the model
		Collection<QuestPoint> questPoints = QuestPointNodeGetter
				.getQuestPoints(this.model);
		for (QuestPoint questPoint : questPoints) {
			// Process all the components from each QuestPoint
			for (StoryComponent child : questPoint.getChildren()) {
				child.process(this);
			}
		}
	}

	public Context buildContext(LocationInformation locationInfo) {
		return new FileContext(this.model, "", new CodeGenerationNamifier(
				this.translator.getLanguageDictionary()), this.translator, locationInfo);
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

		this.verifyRules(askIt);

		askIt.getCondition().process(this);

		askIt.processChildren(this);
	}

	@Override
	public void processScriptIt(ScriptIt scriptIt) {
		this.verifyRules(scriptIt);

		scriptIt.processChildren(this);
		scriptIt.processParameters(this);
		scriptIt.processSubjects(this);
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
			final TypeConverter converter = TranslatorManager.getInstance()
					.getActiveTranslator().getGameTypeManager()
					.getTypeConverter();
			final ScriptIt doIt = converter.convert(knowIt);
			if (doIt != null)
				doIt.process(SemanticAnalyzer.this);
		}
	}

	@Override
	public void processStoryComponentContainer(StoryComponentContainer container) {
		this.verifyRules(container);

		container.processChildren(this);
	}

	@Override
	public void processStoryItemSequence(StoryItemSequence sequence) {
		this.verifyRules(sequence);

		sequence.processChildren(this);
	}

	private void verifyRules(StoryComponent component) {
		for (StoryRule rule : this.rules) {
			this.problems.addAll(rule.validate(null, component));
		}
	}
}
