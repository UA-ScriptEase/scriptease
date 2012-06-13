package scriptease.translator.codegenerator.code.contexts;

import java.util.Iterator;
import java.util.regex.Pattern;

import scriptease.gui.quests.QuestNode;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;

/**
 * StringContext is Context for a String object. *
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class StringContext {/*extends Context {
	private String value = "";
	private static Iterator<String> includeFragment;

	public StringContext(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator);

		this.setLocationInfo(locationInfo);
	}

	public StringContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public StringContext(Context other, String source) {
		this(other);
		value = source;
	}

	
	@Override
	public String getInclude() {	
		
		
		if(includeFragment == null)
			includeFragment = this.getIncludes();

		if(includeFragment.hasNext())
			return includeFragment.next();
		else
			return "<include not found>";
	}
	
	@Override
	public String getUniqueName(Pattern legalRange) {
		return this.value;
	}*/
}
