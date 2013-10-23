package scriptease.controller.io.converter.storycomponent;

import java.util.Map;
import java.util.WeakHashMap;

import scriptease.model.StoryComponent;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.PickIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryPoint;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts PickIts to/from XML.
 * 
 * @author jyuen
 * 
 * @see StoryComponentConverter
 */
public class PickItConverter extends ComplexStoryComponentConverter {

	public static final String TAG_PICKIT = "PickIt";
	public static final String TAG_CHOICES = "Choices";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final PickIt pickIt = (PickIt) source;

		super.marshal(source, writer, context);

		// CodeBlocks
		writer.startNode(TAG_CHOICES);
		context.convertAnother(pickIt.getChoices());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final PickIt pickIt;

		pickIt = (PickIt) super.unmarshal(reader, context);

		final Map<StoryComponentContainer, Integer> choices = new WeakHashMap<StoryComponentContainer, Integer>();

		reader.moveDown();
		
		if (reader.hasMoreChildren()) {
//			if (!reader.getNodeName().equalsIgnoreCase(TAG_CHOICES))
//				System.err.println("Expected choices list, but found "
//						+ reader.getNodeName());
//			else {
//				while (reader.hasMoreChildren()) {
//					reader.moveDown();
//					final String nodeName = reader.getNodeName();
//
//					if (nodeName.equals(StoryPointConverter.TAG_STORYPOINT)) {
//						successors.add((StoryPoint) context.convertAnother(
//								storyNode, StoryPoint.class));
//					} else if (nodeName
//							.equals(StoryGroupConverter.TAG_STORYGROUP)) {
//						successors.add((StoryGroup) context.convertAnother(
//								storyNode, StoryGroup.class));
//					} else {
//						System.err
//								.println("Trying to read a non StoryGroup or non StoryPoint ("
//										+ reader.getNodeName()
//										+ ") successor from " + storyNode);
//					}
//					reader.moveUp();
//				}
//				storyNode.addSuccessors(successors);
//			}
		}
		reader.moveUp();

		return pickIt;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(AskIt.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		PickIt pickIt = new PickIt();
		return pickIt;
	}
}