package scriptease.translator.codegenerator;

import java.util.List;

import scriptease.translator.LanguageDictionary;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;
import scriptease.translator.codegenerator.code.fragments.SimpleFragment;
import scriptease.translator.codegenerator.code.fragments.series.SeriesFragment;
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

	public ScriptInfo generateScriptFile(Context context) {

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
		fileFormat = languageDictionary.getFormat(slot.getFormatKeyword());

		// resolve the format into code
		return FormatFragment.resolveFormat(fileFormat, context);
	}
}
