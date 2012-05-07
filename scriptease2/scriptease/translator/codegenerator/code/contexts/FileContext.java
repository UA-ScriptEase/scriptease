package scriptease.translator.codegenerator.code.contexts;

import scriptease.gui.quests.QuestNode;
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

	public FileContext(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator);
		this.setLocationInfo(locationInfo);
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
