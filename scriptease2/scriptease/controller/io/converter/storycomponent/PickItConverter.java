package scriptease.controller.io.converter.storycomponent;

import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import scriptease.model.StoryComponent;
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
	public static final String TAG_CHOICE_COUNTER = "ChoiceCounter";
	public static final String TAG_CHOICE = "Choice";
	public static final String ATTRIBUTE_PROBABILITY = "Probability";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final PickIt pickIt = (PickIt) source;

		super.marshal(source, writer, context);

		// choice counter
		writer.startNode(TAG_CHOICE_COUNTER);
		writer.setValue(Integer.toString(pickIt.getChoiceCounter()));
		writer.endNode();

		// choices
		writer.startNode(TAG_CHOICES);
		for (Entry<StoryComponentContainer, Integer> choice : pickIt
				.getChoices().entrySet()) {

			// choice
			writer.startNode(TAG_CHOICE);
			writer.addAttribute(ATTRIBUTE_PROBABILITY,
					Integer.toString(choice.getValue()));
			context.convertAnother(choice.getKey());
			writer.endNode();
		}
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final PickIt pickIt;

		pickIt = (PickIt) super.unmarshal(reader, context);

		Integer choiceCounter = null;
		Map<StoryComponentContainer, Integer> choices = new WeakHashMap<StoryComponentContainer, Integer>();

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			if (nodeName.equals(TAG_CHOICE_COUNTER)) {
				choiceCounter = Integer.parseInt(reader.getValue());

			} else if (nodeName.equals(PickItConverter.TAG_CHOICES)) {

				while (reader.hasMoreChildren()) {
					reader.moveDown();

					final String choiceNodeName = reader.getNodeName();

					StoryComponentContainer choice = null;
					Integer probability = null;

					if (choiceNodeName.equals(TAG_CHOICE)) {
						probability = Integer.parseInt(reader
								.getAttribute(ATTRIBUTE_PROBABILITY));

						choice = (StoryComponentContainer) context
								.convertAnother(pickIt,
										StoryComponentContainer.class);
					} else {
						System.err.println("Read a invalid choice ("
								+ choiceNodeName + ") from " + pickIt);
					}

					choices.put(choice, probability);

					reader.moveUp();
				}

			} else {
				System.err.println("Trying to read a invalid node name("
						+ nodeName + ") from " + pickIt);
			}

			reader.moveUp();
		}

		pickIt.setChoiceCounter(choiceCounter);
		pickIt.setChoices(choices);

		return pickIt;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(PickIt.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final PickIt pickIt = new PickIt();
		
		// Remove the default generated choices.
		for (StoryComponent child : pickIt.getChildren())
			pickIt.removeStoryChild(child);
			
		return pickIt;
	}
}