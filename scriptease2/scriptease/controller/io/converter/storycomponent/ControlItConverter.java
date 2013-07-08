package scriptease.controller.io.converter.storycomponent;

import scriptease.model.StoryComponent;
import scriptease.model.complex.ControlIt;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts ControlIts to/from XML.
 * 
 * @author kschenk
 * 
 * @see ScriptItConverter
 */
public class ControlItConverter extends ScriptItConverter {

	// TODO See LibraryModelConverter class for an example of how to refactor
	// this class. However, since we're moving to YAML eventually, we don't need
	// to waste anymore time on refactoring these.
	private static final String ATTRIBUTE_FORMAT_FLAVOUR = "control";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {

		final ControlIt controlIt = (ControlIt) source;

		writer.addAttribute(ATTRIBUTE_FORMAT_FLAVOUR, controlIt.getFormat()
				.name());

		super.marshal(source, writer, context);
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final ControlIt controlIt;
		final String format;

		format = reader.getAttribute(ATTRIBUTE_FORMAT_FLAVOUR);

		controlIt = (ControlIt) super.unmarshal(reader, context);

		controlIt.setFormat(ControlIt.ControlItFormat.valueOf(format
				.toUpperCase()));

		return controlIt;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(ControlIt.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new ControlIt("", ControlIt.ControlItFormat.NONE);
	}
}
