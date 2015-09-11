package scriptease.translator.codegenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.UIManager;

import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.gui.WindowFactory;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.StoryModel;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.FileContext;
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
 * @author jyuen
 * 
 * @see AbstractFragment
 * @see SimpleDataFragment
 * @see SeriesFragment
 */
public class CodeGenerator {

	private final Collection<StoryPoint> generatingStoryPoints;

	private final static CodeGenerator instance = new CodeGenerator();

	/**
	 * Returns the sole instance of CodeGenerator.
	 * 
	 * @return
	 */
	public static CodeGenerator getInstance() {
		return instance;
	}

	private CodeGenerator() {
		this.generatingStoryPoints = new HashSet<StoryPoint>();
	}

	/**
	 * Returns the story nodes currently getting generated. This is faster than
	 * getting the descendants of the root. This should only be used while we're
	 * actually generating code, since it won't reset until
	 * {@link #generateCode(StoryModel, Collection)} is called.
	 * 
	 * @return
	 */
	public Collection<StoryPoint> getGeneratingStoryPoints() {
		return this.generatingStoryPoints;
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

		final long initialTime = System.currentTimeMillis();

		final GameModule module;
		final Translator translator;
		final Collection<ScriptInfo> scriptInfos;
		final StoryPoint root;
		final SemanticAnalyzer analyzer;

		translator = model.getTranslator();
		module = model.getModule();
		scriptInfos = new ArrayList<ScriptInfo>();
		root = model.getRoot();

		this.generatingStoryPoints.clear();
		this.generatingStoryPoints.addAll(root.getStoryPointDescendants());

		// do the first pass (semantic analysis) for the given story
		analyzer = new SemanticAnalyzer(this.generatingStoryPoints);

		// Find problems with code gen, such as slots missing bindings, etc.
		problems.addAll(analyzer.getProblems());
		// If no problems were detected, generate the scripts
		if (problems.isEmpty()) {
			final Collection<StoryComponent> automaticCauses;
			final Collection<Set<CodeBlock>> scriptBuckets;

			// Set the automatic bindings for any causes that require one.
			automaticCauses = model.generateAutomaticCauses();

			// Temporarily add automatics.
			root.addStoryChildren(automaticCauses);

			// aggregate the scripts based on the storyPoints
			scriptBuckets = module
					.aggregateScripts(new ArrayList<StoryComponent>(
							this.generatingStoryPoints));
			if (scriptBuckets.size() > 0) {
				scriptInfos.addAll(this.compile(scriptBuckets, model));
			}

			// Remove the automatics from the story again.
			root.removeStoryChildren(automaticCauses);
		} else {
			
			WindowFactory.getInstance().showCompileProblems(problems);
		}

		final File compiler = translator.getCompiler();

		if (compiler != null
				&& !compiler.exists()
				&& !compiler.getName().equalsIgnoreCase(
						Translator.DescriptionKeys.FALSE)) {

			System.err.println("Compiler at "
					+ translator.getCompiler().getAbsolutePath()
					+ " could not be found.");
		}

		final long secondsToWrite = System.currentTimeMillis() - initialTime;

		System.out.println("It took " + secondsToWrite
				+ " milliseconds to write code.");

		return scriptInfos;
	}

	private ScriptInfo generateScript(Context context) {
		// generate the script file
		final LocationInformation location;
		final Translator translator;

		final String slotName;
		final Slot slot;
		final String format;
		final List<AbstractFragment> fileFormat;

		location = context.getLocationInfo();
		translator = context.getTranslator();
		slotName = location.getSlot();
		slot = context.getModel().getSlot(slotName);

		if (slot == null) {
			throw new IllegalStateException("Unable to find slot " + slotName
					+ ". Check that it exists in the Library.");
		}

		// Get the format keyword from the slot and get the format from the
		// language dictionary
		format = slot.getFormatKeyword().toUpperCase();
		fileFormat = translator.getLanguageDictionary().getFormat(format);
		// resolve the format into code

		try {
			final String scriptContent;

			scriptContent = AbstractFragment.resolveFormat(fileFormat, context);

			return new ScriptInfo(scriptContent, location);
		} catch (Exception e) {
			final String scriptContent;

			e.printStackTrace();

			WindowFactory
					.getInstance()
					.showExceptionDialog(
							"Code Generation Exception",
							"Code did not write correctly.",
							"ScriptEase II encountered an error while writing code.<br>"
									+ "The code has either not been written, or written incorrectly.<br>"
									+ "Please send us an error report so we can fix the issue.",
							UIManager.getIcon("OptionPane.errorIcon"), e);

			scriptContent = "Code Generation Exception";
			return new ScriptInfo(scriptContent, location);
		}
	}

	/**
	 * Compiles the script buckets. Note that multithreading was tried at one
	 * point but ultimately led to more problems than we could fix.
	 * 
	 * @param scriptSets
	 * @param analyzer
	 * @param translator
	 * @return
	 */
	private Collection<ScriptInfo> compile(
			Collection<Set<CodeBlock>> scriptSets, StoryModel story) {

		final Collection<ScriptInfo> scriptInfos = new ArrayList<ScriptInfo>();
		/*
		 * This method is called so that we load the Language Dictionary if it
		 * has not been loaded before.
		 */
		story.getTranslator().getLanguageDictionary();

		for (final Set<CodeBlock> bucket : scriptSets) {
			final CodeBlock codeBlock;
			final LocationInformation location;
			final Context context;
			final ScriptInfo generated;

			// All CodeBlocks of a given bucket share slot and
			// subject, so we can just use the first one
			codeBlock = bucket.iterator().next();
			location = new LocationInformation(codeBlock);
			context = new FileContext(story, this.generatingStoryPoints,
					location);
			generated = this.generateScript(context);

			scriptInfos.add(generated);
		}

		return scriptInfos;
	}
}
