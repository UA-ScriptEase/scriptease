package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.XMLNode;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts the StoryNode class to and from XML.
 * 
 * @author jyuen
 */
public abstract class StoryNodeConverter extends ComplexStoryComponentConverter {
	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final StoryNode node = (StoryNode) source;

		super.marshal(source, writer, context);

		XMLNode.SUCCESSORS.writeObject(writer, context, node.getSuccessors());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final StoryNode storyNode = (StoryNode) super
				.unmarshal(reader, context);

		final Collection<StoryNode> successors = new ArrayList<StoryNode>();

		reader.moveDown();
		if (reader.hasMoreChildren()) {
			if (!reader.getNodeName().equalsIgnoreCase(
					XMLNode.SUCCESSORS.getName()))
				System.err.println("Expected successors list, but found "
						+ reader.getNodeName());
			else {
				while (reader.hasMoreChildren()) {
					reader.moveDown();
					final String nodeName = reader.getNodeName();

					if (nodeName.equals(XMLNode.STORY_POINT.getName())) {
						successors.add((StoryPoint) context.convertAnother(
								storyNode, StoryPoint.class));
					} else if (nodeName.equals(XMLNode.STORY_GROUP.getName())) {
						successors.add((StoryGroup) context.convertAnother(
								storyNode, StoryGroup.class));
					} else {
						System.err
								.println("Trying to read a non StoryGroup or non StoryPoint ("
										+ reader.getNodeName()
										+ ") successor from " + storyNode);
					}
					reader.moveUp();
				}
				storyNode.addSuccessors(successors);
			}
		}
		reader.moveUp();

		return storyNode;
	}
}
