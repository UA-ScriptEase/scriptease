package scriptease.controller.io.converter.storycomponent;

import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;

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
	// TODO See LibraryModelConverter class for an example of how to refactor
	// this class. However, since we're moving to YAML eventually, we don't need
	// to waste anymore time on refactoring these.
	private static final String TAG_CONDITION = "Condition";
	private static final String TAG_KNOWIT = "KnowIt";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final AskIt askIt = (AskIt) source;

		super.marshal(source, writer, context);

		writer.startNode(TAG_CONDITION);
		writer.startNode(TAG_KNOWIT);
		context.convertAnother(askIt.getCondition());
		writer.endNode();
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final AskIt askIt;
		KnowIt condition;
		String nodeName;

		// Read the AskIt like a ComplexStoryComponent.
		askIt = (AskIt) super.unmarshal(reader, context);

		// Read and assign the AskIt's condition.
		reader.moveDown();
		nodeName = reader.getNodeName();
		if (nodeName.equalsIgnoreCase(TAG_CONDITION)) {
			reader.moveDown();
			condition = (KnowIt) context.convertAnother(askIt, KnowIt.class);
			reader.moveUp();
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
			UnmarshallingContext context) {
		AskIt askIt = new AskIt();
		// Remove the default generated If and Else blocks.
		askIt.removeStoryChild(askIt.getIfBlock());
		askIt.removeStoryChild(askIt.getElseBlock());
		return askIt;
	}
}
