package scriptease.translator.codegenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.gui.WindowFactory;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.LanguageDictionary;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.SimpleDataFragment;
import scriptease.translator.codegenerator.code.fragments.container.SeriesFragment;
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
 * @author kschenk
 * 
 * @see AbstractFragment
 * @see SimpleDataFragment
 * @see SeriesFragment
 */
public class CodeGenerator {

	private static CodeGenerator instance = new CodeGenerator();

	/**
	 * Returns the sole instance of CodeGenerator.
	 * 
	 * @return
	 */
	public static CodeGenerator getInstance() {
		return instance;
	}

	private CodeGenerator() {
		// Privatized constructor
	}

	private ScriptInfo generateScriptFile(Context context) {

		// generate the script file
		String scriptContent = generateScript(context);
		return new ScriptInfo(scriptContent, context.getLocationInfo());
	}

	/**
	 * Generates the actual final script code that will reside in the script
	 * file.
	 * 
	 * @return the code for the script.
	 */
	private String generateScript(Context context) {
		final List<AbstractFragment> fileFormat;
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
			return AbstractFragment.resolveFormat(fileFormat, context);
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
	public Collection<ScriptInfo> generateCode(StoryModel model,
			final Collection<StoryProblem> problems) {
		final GameModule module;
		final Translator translator;
		final Collection<ScriptInfo> scriptInfos;
		final StoryPoint root;
		final SemanticAnalyzer analyzer;

		translator = model.getTranslator();
		module = model.getModule();
		scriptInfos = new ArrayList<ScriptInfo>();
		root = model.getRoot();
		// do the first pass (semantic analysis) for the given story
		analyzer = new SemanticAnalyzer(root, translator);

		// Find problems with code gen, such as slots missing bindings, etc.
		problems.addAll(analyzer.getProblems());

		// If no problems were detected, generate the scripts
		if (problems.isEmpty()) {
			// aggregate the scripts based on the storyPoints
			final Collection<Set<CodeBlock>> scriptBuckets;

			scriptBuckets = module
					.aggregateScripts(new ArrayList<StoryComponent>(root
							.getDescendants()));

			if (scriptBuckets.size() > 0) {
				// Multithreaded
				// XXX
				// Multi-threading has been commented out. There is a race
				// condition somewhere that causes code to compile wrong the
				// first time through on larger stories.
				// final ExecutorService executor;

				// executor =
				// Executors.newFixedThreadPool(scriptBuckets.size());

				/*
				 * This method is called so that we load the Language Dictionary
				 * if it has not been loaded before. Otherwise, the following
				 * multithreaded code will attempt to load the language
				 * dictionary in each thread, creating a race condition.
				 */
				translator.getLanguageDictionary();

				/*
				 * A note on debugging:
				 * 
				 * If for some reason we are running into issues with
				 * multithreading, make sure you do not print out or debug
				 * inside of the multithreaded code. This will slow down
				 * function calls, which may make everything work fine and give
				 * the false appearance that the bug is fixed. Instead, debug or
				 * print out debug statements after the multiple threads are
				 * finished.
				 */
				for (final Set<CodeBlock> bucket : scriptBuckets) {
					// Spawn a new thread to compile the code
					// Runnable worker = new Runnable() {
					// @Override
					// public void run() {
					final CodeBlock codeBlock;
					final LocationInformation locationInfo;
					final Context context;
					final ScriptInfo generated;

					// All CodeBlocks of a given bucket share slot and
					// subject, so we can just use the first one
					codeBlock = bucket.iterator().next();
					locationInfo = new LocationInformation(codeBlock);
					context = analyzer.buildContext(locationInfo);
					generated = generateScriptFile(context);

					scriptInfos.add(generated);
					// }
					// };
					// executor.execute(worker);
				}
				// This will make the executor accept no new threads and finish
				// all existing threads in the queue
				// executor.shutdown();
				// Wait until all threads are finish before continuing.
				// while (!executor.isTerminated())
				// ;
			}
		} else {
			WindowFactory.getInstance().showCompileProblems(problems);
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
