package scriptease.controller.io.converter.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import scriptease.controller.io.XMLAttribute;
import scriptease.controller.io.XMLNode;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.GameType.GUIType;
import scriptease.util.StringOp;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter for GameType objects.
 * 
 * @author mfchurch
 */
public class GameTypeConverter implements Converter {
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final GameType type = (GameType) source;
		final Collection<AbstractFragment> format = type.getFormat();
		final String regex = type.getReg();
		final String enumString = type.getEnumString();
		final Map<String, String> escapes = type.getEscapes();
		final Collection<String> slots = type.getSlots();
		final String widgetName = type.getWidgetName();

		XMLNode.NAME.writeString(writer, type.getDisplayName());
		XMLNode.KEYWORD.writeString(writer, type.getKeyword());
		XMLNode.CODESYMBOL.writeString(writer, type.getCodeSymbol());

		if (format != null && !format.isEmpty())
			XMLNode.FORMAT.writeObject(writer, context, type.getFormat());

		if (StringOp.exists(regex))
			XMLNode.LEGAL_VALUES.writeString(writer, regex);

		if (StringOp.exists(enumString))
			XMLNode.ENUM.writeString(writer, enumString);

		if (type.hasGUI())
			XMLNode.GUI.writeObject(writer, context, type.getGui());

		if (escapes != null && !escapes.isEmpty()) {
			writer.startNode(XMLNode.ESCAPES.getName());
			for (Entry<String, String> entry : escapes.entrySet()) {
				XMLNode.ESCAPE.writeObject(writer, context, entry.getValue(),
						XMLAttribute.VALUE, entry.getKey());
			}
			writer.endNode();
		}

		// Write Slots
		if (slots != null && !slots.isEmpty())
			XMLNode.SLOTS.writeChildren(writer, slots);

		if (StringOp.exists(widgetName))
			XMLNode.WIDGETNAME.writeString(writer, widgetName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String name;
		final String keyword;
		final String codeSymbol;
		final Collection<AbstractFragment> fragments = new ArrayList<AbstractFragment>();
		final Collection<String> slots = new ArrayList<String>();
		final Map<String, String> escapes = new HashMap<String, String>();
		String enums = "";
		String reg = "";
		String widgetName = "";
		GUIType gui = null;
		GameType type = null;

		name = XMLNode.NAME.readString(reader);
		keyword = XMLNode.KEYWORD.readString(reader);
		codeSymbol = XMLNode.CODESYMBOL.readString(reader);

		// Read Optional Data
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			// Read Format
			String node = reader.getNodeName();
			if (node.equals(XMLNode.FORMAT.getName())) {
				fragments.addAll((Collection<AbstractFragment>) context
						.convertAnother(type, ArrayList.class));
			}

			// Read Enum
			if (node.equals(XMLNode.ENUM.getName())) {
				enums = reader.getValue();
			}

			// Read Reg
			if (node.equals(XMLNode.LEGAL_VALUES.getName())) {
				reg = reader.getValue();
			}

			// Read Slots
			if (node.equals(XMLNode.SLOTS.getName())) {
				while (reader.hasMoreChildren()) {
					slots.add(XMLNode.SLOT.readString(reader));
				}
			}

			// Read Escapes
			if (node.equals(XMLNode.ESCAPES.getName())) {
				while (reader.hasMoreChildren()) {
					final String value = XMLAttribute.VALUE.read(reader);
					final String key = XMLNode.ESCAPE.readString(reader);

					escapes.put(key, value);
				}
			}

			// Read GUI
			if (node.equals(XMLNode.GUI.getName())) {
				String guiStr = reader.getValue();

				if (guiStr.equalsIgnoreCase(GUIType.JCOMBOBOX.toString()))
					gui = GUIType.JCOMBOBOX;
				else if (guiStr.equalsIgnoreCase(GUIType.JSPINNER.toString()))
					gui = GUIType.JSPINNER;
				else if (guiStr.equalsIgnoreCase(GUIType.JTEXTFIELD.toString()))
					gui = GUIType.JTEXTFIELD;
			}

			// Read widget name
			if (node.equals(XMLNode.WIDGETNAME.getName())) {
				widgetName = reader.getValue();
			}

			reader.moveUp();
		}

		type = new GameType(name, keyword, codeSymbol, fragments, slots, enums,
				reg, escapes, gui, widgetName);

		return type;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(GameType.class);
	}
}
