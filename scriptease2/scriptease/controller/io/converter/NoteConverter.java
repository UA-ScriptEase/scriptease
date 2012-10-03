package scriptease.controller.io.converter;

import scriptease.model.StoryComponent;
import scriptease.model.atomic.Note;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts Notes to/from XML. This is very simple.
 * 
 * @author kschenk
 * 
 * @see ComplexStoryComponentConverter
 */
public class NoteConverter extends StoryComponentConverter {

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		super.marshal(source, writer, context);
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final Note note;

		note = (Note) super.unmarshal(reader, context);

		return note;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(Note.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new Note("");
	}
}
