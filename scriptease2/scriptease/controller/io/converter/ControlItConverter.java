package scriptease.controller.io.converter;

import scriptease.model.StoryComponent;
import scriptease.model.complex.ControlIt;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts only ScriptIts to/from XML.
 * 
 * @author remiller
 * @author mfchurch
 * @author kschenk
 * 
 * @see ComplexStoryComponentConverter
 */
public class ControlItConverter extends ScriptItConverter {
	private static final String ATTRIBUTE_FORMAT_FLAVOUR = "control";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {

		final ControlIt controlIt = (ControlIt) source;

		writer.addAttribute(ATTRIBUTE_FORMAT_FLAVOUR, controlIt.getFormat());

		super.marshal(source, writer, context);
	}
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final ControlIt controlIt;
		final String format;

		format = reader.getAttribute(ATTRIBUTE_FORMAT_FLAVOUR);

		controlIt = (ControlIt) super.unmarshal(reader, context);

		controlIt.setFormat(format);

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
		return new ControlIt("", "");
	}
}
