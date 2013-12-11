package scriptease.controller.io.converter.model;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.XMLAttribute;
import scriptease.controller.io.XMLNode;
import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.SEModelManager;
import scriptease.translator.io.model.Slot;
import scriptease.util.StringOp;

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
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final Slot slot = (Slot) source;
		final String condition = slot.getCondition();
		final Collection<KnowIt> parameters = slot.getParameters();
		final Collection<KnowIt> implicits = slot.getImplicits();

		if (!slot.getFormatKeyword().equals(
				SEModelManager.getInstance().getActiveModel()
						.getSlotDefaultFormat())) {
			XMLAttribute.FORMAT.write(writer, slot.getFormatKeyword());
		}

		XMLNode.NAME.writeString(writer, slot.getDisplayName());
		XMLNode.KEYWORD.writeString(writer, slot.getKeyword());

		// Write Condition
		if (StringOp.exists(condition)) {
			XMLNode.CONDITION.writeString(writer, slot.getCondition());
		}

		XMLNode.PARAMETERS.writeObject(writer, context, parameters);
		XMLNode.IMPLICITS.writeObject(writer, context, implicits);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String format = XMLAttribute.FORMAT.read(reader);

		final String name = XMLNode.NAME.readString(reader);
		final String keyword = XMLNode.KEYWORD.readString(reader);

		final Collection<KnowIt> parameters = new ArrayList<KnowIt>();
		final Collection<KnowIt> implicits = new ArrayList<KnowIt>();

		String condition = null;

		while (reader.hasMoreChildren()) {
			reader.moveDown();

			final String node = reader.getNodeName();

			if (node.equals(XMLNode.PARAMETERS.getName())) {
				if (reader.hasMoreChildren())
					parameters.addAll((Collection<KnowIt>) context
							.convertAnother(null, ArrayList.class));

			} else if (node.equals(XMLNode.IMPLICITS.getName())) {
				if (reader.hasMoreChildren())
					implicits.addAll((Collection<KnowIt>) context
							.convertAnother(null, ArrayList.class));

			} else if (node.equals(XMLNode.CONDITION.getName()))
				condition = reader.getValue();

			reader.moveUp();
		}

		return new Slot(name, keyword, parameters, implicits, format, condition);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(Slot.class);
	}
}