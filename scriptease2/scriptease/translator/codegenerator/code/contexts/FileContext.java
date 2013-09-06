package scriptease.translator.codegenerator.code.contexts;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import scriptease.model.CodeBlock;
import scriptease.model.complex.StoryNode;
import scriptease.model.semodel.StoryModel;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.io.model.Resource;

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
 * @author jyuen
 * 
 */
public class FileContext extends Context {

	private Set<String> includeFiles;
	private Iterator<String> includeFilesIterator;

	public FileContext(StoryModel model, Collection<StoryNode> storyNodes,
			LocationInformation locationInfo) {
		super(model, storyNodes, "", new CodeGenerationNamifier(model
				.getTranslator().getLanguageDictionary()));

		this.setLocationInfo(locationInfo);

		this.includeFiles = new HashSet<String>();

		this.includeFilesIterator = this.includeFiles.iterator();
	}

	@Override
	public Set<String> getIncludeFiles() {
		for (CodeBlock codeBlock : this.getCodeBlocks()) {
			this.includeFiles.addAll(codeBlock.getIncludes());
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
	public Resource getResource() {
		return this.locationInfo.getSubject();
	}
	
	@Override
	public String toString() {
		return "FileContext";
	}
}
