package scriptease.translator.codegenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import scriptease.controller.get.QuestPointNodeGetter;
import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.gui.WindowManager;
import scriptease.gui.quests.QuestNode;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.translator.LanguageDictionary;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;
import scriptease.translator.codegenerator.code.fragments.SimpleFragment;
import scriptease.translator.codegenerator.code.fragments.series.SeriesFragment;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Slot;

/**
 * Generates script code for the target game based off of data gathered from the
 * game-specific translator. In many ways, this is a multi-pass, single language
 * to multi-platform compiler. The StoryComponent tree is treated like a
 * compiler treats a parse tree, except we have the added benefit of knowing
 * that it is already valid in several ways.<br>
 * <br>
 * Furthermore, it uses the Interpreter pattern to accomplish its task.
 * CodeGenerator is the client, and FormatFragment is the AbstractExpression.
 * The Context is stored in CodeGenerationContext. <br>
 * <br>
 * It may be helpful to understand the basics of those concepts before editing
 * this system. <br>
 * <br>
 * Code generation consists of two passes. The first pass is Semantic Analysis,
 * where a map of StoryComponents to abstract representations of their code is
 * built. The second pass is the phase where the abstract code fragments are
 * resolved into concrete strings which are concatenated into what is then the
 * contents of the script file.<br>
 * <br>
 * Code Generation is a good candidate for parallelization. To implement this,
 * make a ThreadPool of threads and assign each thread the task of running a
 * code generator on a bag of pattern trees. <br>
 * <br>
 * See SimpleFragment and SeriesFragment for lists of what data labels those
 * fragments will accept.
 * 
 * @author remiller
 * @author mfchurch
 * @see FormatFragment
 * @see SimpleFragment
 * @see SeriesFragment
 */
public class CodeGenerator {

	private ScriptInfo generateScriptFile(Context context) {

		// generate the script file
		String scriptContent = this.generateScript(context);
		return new ScriptInfo(scriptContent, context.getLocationInfo());
	}

	/**
	 * Generates the actual final script code that will reside in the script
	 * file.
	 * 
	 * @return the code for the script.
	 */
	private String generateScript(Context context) {
		final List<FormatFragment> fileFormat;
		final Translator translator = context.getTranslator();
		final LanguageDictionary languageDictionary = translator
				.getLanguageDictionary();
		final String slotName = context.getLocationInfo().getSlot();
		final Slot slot = translator.getApiDictionary().getEventSlotManager()
				.getEventSlot(slotName);

		if (slot == null) {
			throw new IllegalStateException("Unable to find slot " + slotName
					+ ". Check that it exists in the ApiDictionary.");
		}

		// Get the format keyword from the slot and get the format from the
		// language dictionary
		fileFormat = languageDictionary.getFormat(slot.getFormatKeyword()
				.toUpperCase());

		// resolve the format into code
		try {
			return FormatFragment.resolveFormat(fileFormat, context);
		} catch (CodeGenerationException e) {
			return "CodeGenerationException occured at the script generating level with message: "
					+ e.getMessage();
		}
	}

	/**
	 * Generates the script files based on the current state of the model.
	 * Returns the generated scriptInfo files, and any problems that may have
	 * arisen during the process.
	 * 
	 * @param model
	 * @param problems
	 * @return
	 */
	public static Collection<ScriptInfo> generateCode(StoryModel model,
			final Collection<StoryProblem> problems) {
		final GameModule module = model.getModule();
		final Translator translator = model.getTranslator();
		final Collection<ScriptInfo> scriptInfos = new ArrayList<ScriptInfo>();
		final QuestNode root = model.getRoot();

		// do the first pass (semantic analysis) for the given quest
		final SemanticAnalyzer analyzer = new SemanticAnalyzer(root,
				translator, module.getCodeGenerationRules());

		// check for problems
		problems.addAll(analyzer.getProblems());

		// If no problems were detected, generate the scripts
		if (problems.isEmpty()) {
			// aggregate the scripts based on the questPoints
			final Collection<Set<CodeBlock>> scriptBuckets = module
					.aggregateScripts(new ArrayList<StoryComponent>(
							QuestPointNodeGetter.getQuestPoints(root)));

			// Multithreaded
			final ExecutorService executor = Executors
					.newFixedThreadPool(scriptBuckets.size());
			for (final Set<CodeBlock> bucket : scriptBuckets) {
				// All CodeBlocks of a given bucket share slot and subject, so
				// take the first one
				final CodeBlock codeBlock = bucket.iterator().next();
				final LocationInformation locationInfo = new LocationInformation(
						codeBlock);
				final Context context = analyzer.buildContext(locationInfo);
				// Spawn a new thread to compile the code
				Runnable worker = new Runnable() {
					@Override
					public void run() {
						final CodeGenerator generator;
						generator = new CodeGenerator();
						scriptInfos.add(generator.generateScriptFile(context));
					}
				};
				executor.execute(worker);
			}
			// This will make the executor accept no new threads
			// and finish all existing threads in the queue
			executor.shutdown();
			// Wait until all threads are finish
			while (!executor.isTerminated())
				;
		} else {
			WindowManager.getInstance().showCompileProblems(problems);
		}

		if (translator.getCompiler() != null
				&& !translator.getCompiler().exists()) {
			System.err.println("Compiler: "
					+ translator.getCompiler().getName()
					+ " could not be found.");
		}
		return scriptInfos;
	}
}
