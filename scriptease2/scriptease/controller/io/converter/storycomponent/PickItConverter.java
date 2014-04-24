package scriptease.controller.io.converter.storycomponent;

import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import scriptease.controller.io.XMLAttribute;
import scriptease.controller.io.XMLNode;
import scriptease.model.StoryComponent;
import scriptease.model.complex.PickIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.semodel.librarymodel.LibraryModel;

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
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final PickIt pickIt = (PickIt) source;

		super.marshal(source, writer, context);

		XMLNode.CHOICE_COUNTER.writeInteger(writer, pickIt.getChoiceCounter());

		writer.startNode(XMLNode.CHOICES.getName());
		for (Entry<StoryComponentContainer, Integer> choice : pickIt
				.getChoices().entrySet()) {
			XMLNode.CHOICE.writeObject(writer, context, choice.getKey(),
					XMLAttribute.PROBABILITY,
					Integer.toString(choice.getValue()));
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

			if (nodeName.equals(XMLNode.CHOICE_COUNTER.getName())) {
				choiceCounter = Integer.parseInt(reader.getValue());
			} else if (nodeName.equals(XMLNode.CHOICES.getName())) {

				while (reader.hasMoreChildren()) {
					reader.moveDown();

					final String choiceNodeName = reader.getNodeName();

					StoryComponentContainer choice = null;
					Integer probability = null;

					if (choiceNodeName.equals(XMLNode.CHOICE.getName())) {
						probability = Integer.parseInt(XMLAttribute.PROBABILITY
								.read(reader));

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
			UnmarshallingContext context, LibraryModel library) {
		final PickIt pickIt = new PickIt(library);

		// Remove the default generated choices.
		for (StoryComponent child : pickIt.getChildren())
			pickIt.removeStoryChild(child);

		return pickIt;
	}
}