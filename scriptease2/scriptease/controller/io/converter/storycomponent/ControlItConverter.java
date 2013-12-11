package scriptease.controller.io.converter.storycomponent;

import scriptease.controller.io.XMLAttribute;
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
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {

		final ControlIt controlIt = (ControlIt) source;

		XMLAttribute.CONTROL.write(writer, controlIt.getFormat().name());

		super.marshal(source, writer, context);
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String format = XMLAttribute.CONTROL.read(reader);

		final ControlIt controlIt;

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
