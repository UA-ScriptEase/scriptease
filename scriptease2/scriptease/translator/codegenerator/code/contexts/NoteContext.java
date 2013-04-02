package scriptease.translator.codegenerator.code.contexts;

import scriptease.model.atomic.Note;

/**
 * NoteContext is Context for a Note object.
 * 
 * 
 * @see Context
 * @author kschenk
 * 
 */
public class NoteContext extends StoryComponentContext {

	public NoteContext(Context other, Note source) {
		super(other, source);
	}

	@Override
	public String toString() {
		return "NoteContext [" + this.getComponent() + "]";
	}

	@Override
	protected Note getComponent() {
		return (Note) super.getComponent();
	}
}
