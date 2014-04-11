package scriptease.controller.io.converter.storycomponent;

import scriptease.model.StoryComponent;
import scriptease.model.atomic.Note;
import scriptease.model.semodel.librarymodel.LibraryModel;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * Converts Notes to/from XML. This is very simple.
 * 
 * @author kschenk
 * 
 * @see ComplexStoryComponentConverter
 */
public class NoteConverter extends StoryComponentConverter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(Note.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context, LibraryModel library, int id) {
		return new Note(library, id, "");
	}
}
