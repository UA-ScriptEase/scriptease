package scriptease.model.atomic;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * A story component that contains text and nothing else. Can be inserted
 * anywhere.
 * 
 * @author kschenk
 * 
 */
public final class Note extends StoryComponent {

	/**
	 * Used to create the common note. If you're adding a note in code, it's
	 * recommended to use {@link LibraryModel#createNote()} instead.
	 * 
	 * @param library
	 * @param id
	 */
	public Note(LibraryModel library) {
		this(library, "");
	}

	/**
	 * Used to build notes from save files. If you're adding a note in code,
	 * it's recommended to use {@link LibraryModel#createNote()} instead.
	 * 
	 * @param library
	 * @param id
	 */
	public Note(LibraryModel library, String string) {
		super(library, string);
	}

	@Override
	public Note clone() {
		return (Note) super.clone();
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processNote(this);

	}

	@Override
	public void revalidateKnowItBindings() {
		// Do nothing. Notes don't have any bindings.
	}
}
