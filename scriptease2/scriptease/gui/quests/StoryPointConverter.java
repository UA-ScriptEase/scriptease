package scriptease.gui.quests;

import scriptease.controller.io.converter.ComplexStoryComponentConverter;
import scriptease.model.StoryComponent;

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
public class StoryPointConverter extends ComplexStoryComponentConverter {
	public static final String TAG_STORYPOINT = "StoryPoint";
	public static final String TAG_FAN_IN = "FanIn";
	public static final String TAG_SUCCESSORS = "Successors";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final StoryPoint storyPoint = (StoryPoint) source;
		super.marshal(source, writer, context);

		// fan in
		writer.startNode(TAG_FAN_IN);
		writer.setValue(storyPoint.getFanIn().toString());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final StoryPoint storyPoint = (StoryPoint) super.unmarshal(reader,
				context);
		String fanIn = null;

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			if (nodeName.equals(TAG_FAN_IN)) {
				fanIn = reader.getValue();
			}
			else if (nodeName.equals(TAG_SUCCESSORS)) {
				//TODO Handle successors
			}
			reader.moveUp();
		}

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
			UnmarshallingContext context) {
		return new StoryPoint("");
	}
}
