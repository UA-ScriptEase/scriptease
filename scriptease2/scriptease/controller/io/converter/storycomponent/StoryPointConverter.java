package scriptease.controller.io.converter.storycomponent;

import scriptease.controller.io.XMLNode;
import scriptease.model.StoryComponent;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.librarymodel.LibraryModel;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts the StoryPoint class to and from XML.
 * 
 * @author mfchurch
 * @author kschenk
 */
public class StoryPointConverter extends StoryNodeConverter {
	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final StoryPoint storyPoint = (StoryPoint) source;

		super.marshal(source, writer, context);

		XMLNode.FAN_IN.writeInteger(writer, storyPoint.getFanIn());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {

		final StoryPoint storyPoint = (StoryPoint) super.unmarshal(reader,
				context);
		final String fanIn = XMLNode.FAN_IN.readString(reader);

		storyPoint.setFanIn(new Integer(fanIn));

		return storyPoint;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(StoryPoint.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context, LibraryModel library, int id) {
		return new StoryPoint("");
	}
}
