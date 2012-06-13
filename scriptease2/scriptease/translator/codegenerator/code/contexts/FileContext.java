package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import scriptease.gui.quests.QuestNode;
import scriptease.model.CodeBlock;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;

/**
 * FileContext is Context for a File object.
 * 
 * This is the highest level of implemented Context and should be the first
 * instantiated Context in the chain of context.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class FileContext extends Context {
	
	private Set<String> includeFiles; //= new HashSet<String>();
	private Iterator<String> includeFilesIterator;

	public FileContext(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator);
		this.setLocationInfo(locationInfo);
		
		includeFiles = new HashSet<String>();
	
		includeFilesIterator = includeFiles.iterator();

		//for(String include : this.codeBlockIncludes(codeBlocks))
		//	this.addIncludeFile(include);
	}

	@Override
	public Set<String> getIncludeFiles() {
		Iterator<CodeBlock> codeBlocks = this.getCodeBlocks();
				
		System.out.println("Including things!");
		
		while(codeBlocks.hasNext()) {
			this.includeFiles.addAll(codeBlocks.next().getIncludes());
		}
		this.includeFilesIterator = includeFiles.iterator();

		return this.includeFiles;
	}
	
	@Override
	public String getInclude() {
		if(this.includeFilesIterator.hasNext())
			return this.includeFilesIterator.next();
		else
			return "";
	}
	
	public FileContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	@Override
	public String toString() {
		return "FileContext";
	}
}
