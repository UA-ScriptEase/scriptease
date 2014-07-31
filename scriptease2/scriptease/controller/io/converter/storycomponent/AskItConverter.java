package scriptease.controller.io.converter.storycomponent;

import scriptease.controller.io.XMLNode;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.semodel.librarymodel.LibraryModel;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts only AskIts to/from XML.
 * 
 * @author remiller
 * 
 * @see StoryComponentConverter
 */
public class AskItConverter extends ComplexStoryComponentConverter {
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final AskIt askIt = (AskIt) source;

		super.marshal(source, writer, context);

		writer.startNode(XMLNode.CONDITION.getName());
		XMLNode.KNOWIT.writeObject(writer, context, askIt.getCondition());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final AskIt askIt = (AskIt) super.unmarshal(reader, context);

		// Read and assign the AskIt's condition.
		reader.moveDown();
		if (reader.getNodeName().equalsIgnoreCase(XMLNode.CONDITION.getName())) {
			final KnowIt condition;

			condition = XMLNode.KNOWIT
					.readObject(reader, context, KnowIt.class);
			askIt.setCondition(condition);
		} else {
			System.err.println("Missing condition for AskIt \""
					+ askIt.getDisplayText() + "\"");
		}
		reader.moveUp();

		return askIt;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(AskIt.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context, LibraryModel library) {
		AskIt askIt = new AskIt(library);
		// Remove the default generated If and Else blocks.
		askIt.removeStoryChild(askIt.getIfBlock());
		askIt.removeStoryChild(askIt.getElseBlock());
		return askIt;
	}
}
