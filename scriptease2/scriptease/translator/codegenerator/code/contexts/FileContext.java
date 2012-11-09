package scriptease.translator.codegenerator.code.contexts;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import scriptease.model.CodeBlock;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;

/**
 * FileContext is Context for a File object.
 * 
 * This is the highest level of implemented Context and should be the first
 * instantiated Context in the chain of context.
 * 
 * In addition to being a context for the file object, the file context sets up
 * the include/imported files needed in a script.
 * 
 * @see Context
 * @author mfchurch
 * @author kschenk
 * 
 */
public class FileContext extends Context {

	private Set<String> includeFiles;
	private Iterator<String> includeFilesIterator;

	public FileContext(StoryPoint model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator);
		this.setLocationInfo(locationInfo);

		this.includeFiles = new HashSet<String>();

		this.includeFilesIterator = this.includeFiles.iterator();
	}

	@Override
	public Set<String> getIncludeFiles() {
		Iterator<CodeBlock> codeBlocks = this.getCodeBlocks();

		while (codeBlocks.hasNext()) {
			this.includeFiles.addAll(codeBlocks.next().getIncludes());
		}

		List<CodeBlock> bindingCodeBlocks = this.getBindingCodeBlocks();

		for (CodeBlock bindingCodeBlock : bindingCodeBlocks) {
			this.includeFiles.addAll(bindingCodeBlock.getIncludes());
		}

		this.includeFilesIterator = this.includeFiles.iterator();

		return this.includeFiles;
	}

	@Override
	public String getInclude() {
		if (this.includeFilesIterator.hasNext())
			return this.includeFilesIterator.next();
		else
			return "ERROR while getting Include File in FileContext.java";
	}

	public FileContext(Context other) {
		this(other.getStartStoryPoint(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	@Override
	public String toString() {
		return "FileContext";
	}
}
