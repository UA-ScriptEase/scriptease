package scriptease.controller.io.converter.storycomponent;

import scriptease.controller.io.XMLNode;
import scriptease.model.StoryComponent;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryNode;
import scriptease.model.semodel.librarymodel.LibraryModel;

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
	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final StoryGroup group = (StoryGroup) source;

		super.marshal(source, writer, context);

		XMLNode.START_NODE.writeObject(writer, context, group.getStartNode());
		XMLNode.EXIT_NODE.writeObject(writer, context, group.getExitNode());
		XMLNode.EXPANDED.writeBoolean(writer, group.isExpanded());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final StoryGroup storyGroup = (StoryGroup) super.unmarshal(reader,
				context);

		final StoryNode start;
		final StoryNode exit;
		final boolean expanded;

		start = XMLNode.START_NODE.readObject(reader, context, StoryNode.class);
		exit = XMLNode.EXIT_NODE.readObject(reader, context, StoryNode.class);
		expanded = Boolean.valueOf(XMLNode.EXPANDED.readString(reader));

		storyGroup.setStartNode(start);
		storyGroup.setExitNode(exit);
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
			UnmarshallingContext context, LibraryModel library) {
		return new StoryGroup();
	}
}
