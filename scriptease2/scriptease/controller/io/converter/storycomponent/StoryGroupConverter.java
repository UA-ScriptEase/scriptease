package scriptease.controller.io.converter.storycomponent;

import scriptease.model.StoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter for a StoryGroup {@link StoryGroup}.
 * 
 * @author jyuen
 */
public class StoryGroupConverter extends StoryNodeConverter {

	public static final String TAG_STORYGROUP = "StoryGroup";

	public static final String TAG_START_NODE = "StartNode";
	public static final String TAG_EXIT_NODE = "ExitNode";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final StoryGroup storyGroup = (StoryGroup) source;
		super.marshal(source, writer, context);

		// start node
		writer.startNode(StoryGroupConverter.TAG_START_NODE);
		context.convertAnother(storyGroup.getStartNode());
		writer.endNode();

		// exit node
		writer.startNode(StoryGroupConverter.TAG_EXIT_NODE);
		context.convertAnother(storyGroup.getExitNode());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final StoryGroup storyGroup = (StoryGroup) super.unmarshal(reader,
				context);

		StoryNode startNode = null;
		StoryNode exitNode = null;

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			// TODO STORYNODES
			if (nodeName.equals(StoryGroupConverter.TAG_START_NODE)) {
				//startNode = 
			} else if (nodeName.equals(StoryGroupConverter.TAG_EXIT_NODE)) {
				//startNode = 
			}

			reader.moveUp();
		}

		storyGroup.setStartNode(startNode);
		storyGroup.setExitNode(exitNode);
		
		return storyGroup;
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
