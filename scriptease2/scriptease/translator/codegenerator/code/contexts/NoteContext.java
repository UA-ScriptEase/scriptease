package scriptease.translator.codegenerator.code.contexts;

import scriptease.model.atomic.Note;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;

/**
 * NoteContext is Context for a Note object.
 * 
 * 
 * @see Context
 * @author kschenk
 * 
 */
public class NoteContext extends StoryComponentContext {

	public NoteContext(StoryPoint model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInformation) {
		super(model, indent, existingNames, translator, locationInformation);
	}

	public NoteContext(Context other) {
		this(other.getStartStoryPoint(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public NoteContext(Context other, Note source) {
		this(other);
		this.component = source;
	}

	@Override
	public String toString() {
		return "NoteContext [" + this.component + "]";
	}
}
