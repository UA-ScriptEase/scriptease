package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import scriptease.gui.quests.QuestNode;
import scriptease.model.CodeBlock;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.fragments.Fragment;

public class ScriptItContext extends ComplexStoryComponentContext {

	public ScriptItContext(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public ScriptItContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public ScriptItContext(Context other, ScriptIt source) {
		this(other);
		component = source;
	}

	@Override
	public CodeBlock getMainCodeBlock() {
		return ((ScriptIt) component).getMainCodeBlock();
	}

	@Override
	public Iterator<CodeBlock> getCodeBlocks() {
		return ((ScriptIt) component).getCodeBlocksForLocation(
				this.locationInfo).iterator();
	}

	@Override
	public String getType() {
		final CodeBlock mainCodeBlock = ((ScriptIt) this.component)
				.getMainCodeBlock();

		final Context createContext = ContextFactory.getInstance()
				.createContext(this, mainCodeBlock);

		return createContext.getType();

	}

	/**
	 * Namification needs to be done at a CodeBlock level instead of a
	 * StoryComponent level
	 */
	@Override
	public String getUniqueName(Pattern legalFormat) {
		final CodeBlock mainCodeBlock = ((ScriptIt) this.component)
				.getMainCodeBlock();
		final ContextFactory factory = ContextFactory.getInstance();
		final Context createContext = factory
				.createContext(this, mainCodeBlock);
		return createContext.getUniqueName(legalFormat);
	}

	@Override
	public Iterator<String> getIncludes() {
		final Collection<CodeBlock> codeBlocks = ((ScriptIt) component)
				.getCodeBlocksForLocation(this.locationInfo);

		final List<String> includes = new ArrayList<String>();
		// Grab the includes of all the codeBlocks
		for (CodeBlock codeBlock : codeBlocks) {
			includes.addAll(codeBlock.getIncludes());
		}

		return includes.iterator();
	}

	/**
	 * Get the Collection of FormatFragments which represent the method body
	 */
	@Override
	public String getCode() {
		final Collection<Fragment> code = new ArrayList<Fragment>();
		final Collection<CodeBlock> codeBlocks = ((ScriptIt) component)
				.getCodeBlocksForLocation(this.locationInfo);

		// Combine codeBlocks with the same slot
		for (CodeBlock codeBlock : codeBlocks) {
			code.addAll(codeBlock.getCode());
		}

		return Fragment.resolveFormat(code, this);
	}

	@Override
	public Iterator<KnowIt> getVariables() {
		final ScriptIt scriptIt = (ScriptIt) component;
		final Collection<KnowIt> parameters = new ArrayList<KnowIt>();
		for (CodeBlock codeBlock : scriptIt
				.getCodeBlocksForLocation(this.locationInfo)) {
			parameters.addAll(codeBlock.getParameters());
		}
		return parameters.iterator();
	}

	/**
	 * Get the StoryComponent's parameter by displayName returns null if a
	 * parameter with that displayName is not found
	 */
	@Override
	public KnowIt getParameter(String displayName) {
		return ((ScriptIt) component).getParameter(displayName);
	}

	@Override
	public Iterator<KnowIt> getParameters() {
		return ((ScriptIt) component).getParameters().iterator();
	}
}
