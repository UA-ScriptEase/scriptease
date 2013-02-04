package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.FileIO;
import scriptease.model.atomic.KnowIt;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.Slot;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter for {@link Slot} objects.
 * 
 * @author mfchurch
 */
public class SlotConverter implements Converter {
	private static final String TAG_NAME = "Name";
	private static final String TAG_KEYWORD = "Keyword";
	private static final String TAG_PARAMETERS = "Parameters";
	private static final String TAG_IMPLICITS = "Implicits";
	private static final String TAG_CONDITION = "Condition";
	private static final String TAG_FORMAT = "format";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final Slot slot = (Slot) source;

		if (!slot.getFormatKeyword().equals(
				TranslatorManager.getInstance().getActiveAPIDictionary()
						.getEventSlotManager().getDefaultFormatKeyword())) {
			writer.addAttribute(TAG_FORMAT, slot.getFormatKeyword());
		}

		// Write Name
		writer.startNode(TAG_NAME);
		writer.setValue(slot.getDisplayName());
		writer.endNode();

		// Write Keyword
		writer.startNode(TAG_KEYWORD);
		writer.setValue(slot.getKeyword());
		writer.endNode();

		// Write Parameters
		writer.startNode(TAG_PARAMETERS);
		if (slot.getParameters() != null && !slot.getParameters().isEmpty())
			context.convertAnother(slot.getParameters());
		writer.endNode();

		// Write Implicits
		writer.startNode(TAG_IMPLICITS);
		if (slot.getImplicits() != null && !slot.getImplicits().isEmpty())
			context.convertAnother(slot.getImplicits());
		writer.endNode();

		// Write Condition
		writer.startNode(TAG_CONDITION);
		writer.setValue(slot.getCondition());
		writer.endNode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String name;
		final String keyword;

		final Collection<KnowIt> parameters = new ArrayList<KnowIt>();
		final Collection<KnowIt> implicits = new ArrayList<KnowIt>();
		String condition = null;
		Slot slot = null;
		String formatKeyword = null;

		// Read Format
		formatKeyword = reader.getAttribute(TAG_FORMAT);

		// Read Name
		name = FileIO.readValue(reader, TAG_NAME);

		// Read Keyword
		keyword = FileIO.readValue(reader, TAG_KEYWORD);

		while (reader.hasMoreChildren()) {
			reader.moveDown();

			final String node = reader.getNodeName();
			// Read Parameters
			if (node.equals(TAG_PARAMETERS)) {
				if (reader.hasMoreChildren())
					parameters.addAll((Collection<KnowIt>) context
							.convertAnother(slot, ArrayList.class));
			} else

			// Read Implicits
			if (node.equals(TAG_IMPLICITS)) {
				if (reader.hasMoreChildren())
					implicits.addAll((Collection<KnowIt>) context
							.convertAnother(slot, ArrayList.class));
			}

			// Read Condition
			if (node.equals(TAG_CONDITION))
				condition = reader.getValue();

			reader.moveUp();
		}

		slot = new Slot(name, keyword, parameters, implicits, formatKeyword,
				condition);

		return slot;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(Slot.class);
	}
}