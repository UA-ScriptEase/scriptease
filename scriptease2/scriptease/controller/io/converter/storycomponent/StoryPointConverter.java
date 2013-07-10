package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.model.StoryComponent;
import scriptease.model.complex.StoryPoint;

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
	// TODO See LibraryModelConverter class for an example of how to refactor
	// this class. However, since we're moving to YAML eventually, we don't need
	// to waste anymore time on refactoring these.
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

		writer.startNode(TAG_SUCCESSORS);
		context.convertAnother(storyPoint.getSuccessors());
		writer.endNode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final StoryPoint storyPoint = (StoryPoint) super.unmarshal(reader,
				context);
		final Set<StoryPoint> successors;

		String fanIn = null;
		successors = new HashSet<StoryPoint>();

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			if (nodeName.equals(TAG_FAN_IN)) {
				fanIn = reader.getValue();
			} else if (nodeName.equals(TAG_SUCCESSORS)) {
				successors.addAll((Collection<StoryPoint>) context
						.convertAnother(storyPoint, ArrayList.class));
			}
			reader.moveUp();
		}

		storyPoint.setFanIn(new Integer(fanIn));
		storyPoint.addSuccessors(successors);

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
