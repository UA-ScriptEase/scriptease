package scriptease.controller.io.converter.storycomponent;

import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import scriptease.model.StoryComponent;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.PickIt;
import scriptease.model.complex.StoryComponentContainer;

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

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();
//
//			if (nodeName.equals(PickItConverter.TAG_CHOICES)) {
//				choices.put((Entry<StoryComponentContainer, Integer>) context
//						.convertAnother(pickIt, Entry.class));
//			} else {
//				System.err.println("Trying to read a (" + reader.getNodeName()
//						+ ") successor from " + storyNode);
//			}
			reader.moveUp();
		}

		pickIt.setChoices(choices);

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