package scriptease.controller.io.converter.storycomponent;

import scriptease.model.StoryComponent;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryNode;

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

	// TODO See LibraryModelConverter class for an example of how to refactor
	// this class. 
	
	public static final String TAG_STORYGROUP = "StoryGroup";

	public static final String TAG_START_NODE = "StartNode";
	public static final String TAG_EXIT_NODE = "ExitNode";
	
	public static final String TAG_EXPANDED = "Expanded";

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
		
		// expanded
		writer.startNode(StoryGroupConverter.TAG_EXPANDED);
		writer.setValue(storyGroup.isExpanded().toString());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final StoryGroup storyGroup = (StoryGroup) super.unmarshal(reader,
				context);
		
		StoryNode startNode = null;
		StoryNode exitNode = null;

		boolean expanded = false;
		
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			if (nodeName.equals(StoryGroupConverter.TAG_START_NODE)) {
				startNode = (StoryNode) context.convertAnother(storyGroup, StoryNode.class);
			} else if (nodeName.equals(StoryGroupConverter.TAG_EXIT_NODE)) {
				exitNode = (StoryNode) context.convertAnother(storyGroup, StoryNode.class);
			} else if (nodeName.equals(StoryGroupConverter.TAG_EXPANDED)) {
				expanded = reader.getValue().equalsIgnoreCase("true");
			}

			reader.moveUp();
		}

		storyGroup.setStartNode(startNode);
		storyGroup.setExitNode(exitNode);
		storyGroup.setExpanded(expanded);
		
		return storyGroup;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(StoryGroup.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new StoryGroup();
	}
}
