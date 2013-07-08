package scriptease.controller.io.converter.model;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.XMLNode;
import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.SEModelManager;
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
	// TODO See LibraryModelConverter class for an example of how to refactor
	// this class. However, since we're moving to YAML eventually, we don't need
	// to waste anymore time on refactoring these.
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
				SEModelManager.getInstance().getActiveModel()
						.getSlotDefaultFormat())) {
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

		// Write Condition
		final String condition = slot.getCondition();
		if (condition != null && !condition.isEmpty()) {
			writer.startNode(TAG_CONDITION);
			writer.setValue(slot.getCondition());
			writer.endNode();
		}

		// Write Parameters
		final Collection<KnowIt> parameters = slot.getParameters();
		writer.startNode(TAG_PARAMETERS);
		if (parameters != null && !parameters.isEmpty())
			context.convertAnother(parameters);
		writer.endNode();

		// Write Implicits
		final Collection<KnowIt> implicits = slot.getImplicits();
		writer.startNode(TAG_IMPLICITS);
		if (implicits != null && !implicits.isEmpty())
			context.convertAnother(implicits);
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

		name = XMLNode.NAME.readString(reader);
		keyword = XMLNode.KEYWORD.readString(reader);

		while (reader.hasMoreChildren()) {
			reader.moveDown();

			final String node = reader.getNodeName();

			if (node.equals(TAG_PARAMETERS)) {
				if (reader.hasMoreChildren())
					parameters.addAll((Collection<KnowIt>) context
							.convertAnother(slot, ArrayList.class));

			} else if (node.equals(TAG_IMPLICITS)) {
				if (reader.hasMoreChildren())
					implicits.addAll((Collection<KnowIt>) context
							.convertAnother(slot, ArrayList.class));

			} else if (node.equals(TAG_CONDITION))
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