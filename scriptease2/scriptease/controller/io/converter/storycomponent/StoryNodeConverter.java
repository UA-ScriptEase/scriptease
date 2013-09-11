package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.model.complex.StoryNode;

import com.thoughtworks.xstream.converters.ConversionException;
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

	// TODO See LibraryModelConverter class for an example of how to refactor
	// this class.
	public static final String TAG_SUCCESSORS = "Successors";

	@Override
	public void marshal(Object source, final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final StoryNode storyNode = (StoryNode) source;
		super.marshal(source, writer, context);

		writer.startNode(TAG_SUCCESSORS);
		context.convertAnother(storyNode.getSuccessors());
		writer.endNode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final StoryNode storyNode = (StoryNode) super
				.unmarshal(reader, context);

		final Collection<StoryNode> successors = new ArrayList<StoryNode>();

		reader.moveDown();
		if (reader.hasMoreChildren()) {
			if (!reader.getNodeName().equalsIgnoreCase(TAG_SUCCESSORS))
				System.err.println("Expected successors list, but found "
						+ reader.getNodeName());
			else {
				try {
					successors.addAll((Collection<StoryNode>) context
							.convertAnother(storyNode, ArrayList.class));

					storyNode.addSuccessors(successors);
				} catch (ConversionException e) {
					System.err.println("Problems converting story node "
							+ storyNode.getDisplayText() + "'s successors: "
							+ e);
				}
			}
		}
		reader.moveUp();

		return storyNode;
	}
}
