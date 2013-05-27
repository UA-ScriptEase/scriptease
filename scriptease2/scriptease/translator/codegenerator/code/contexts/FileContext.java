package scriptease.translator.codegenerator.code.contexts;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import scriptease.model.CodeBlock;
import scriptease.model.semodel.StoryModel;
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

	public FileContext(StoryModel model, LocationInformation locationInfo) {
		this(model, "", new CodeGenerationNamifier(model.getTranslator()
				.getLanguageDictionary()), locationInfo);
	}

	public FileContext(StoryModel model, String indent,
			CodeGenerationNamifier existingNames,
			LocationInformation locationInfo) {
		super(model, indent, existingNames);
		this.setLocationInfo(locationInfo);

		this.includeFiles = new HashSet<String>();

		this.includeFilesIterator = this.includeFiles.iterator();
	}

	@Override
	public Set<String> getIncludeFiles() {
		for (CodeBlock codeBlock : this.getCodeBlocks()) {
			this.includeFiles.addAll(codeBlock.getIncludes());
		}

		final List<CodeBlock> bindingCodeBlocks = this.getBindingCodeBlocks();

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

	@Override
	public String toString() {
		return "FileContext";
	}
}
