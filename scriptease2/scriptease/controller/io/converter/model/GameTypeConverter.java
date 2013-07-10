package scriptease.controller.io.converter.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	// TODO See LibraryModelConverter class for an example of how to refactor
	// this class. However, since we're moving to YAML eventually, we don't need
	// to waste anymore time on refactoring these.

	private static final String TAG_ENUM = "Enum";
	private static final String TAG_LEGAL_VALUES = "LegalValues";
	private static final String TAG_SLOTS = "Slots";
	private static final String TAG_FORMAT = "Format";
	private static final String TAG_GUI = "GUI";
	private static final String TAG_WIDGETNAME = "WidgetName";
	private static final String TAG_ESCAPE = "Escape";
	private static final String TAG_ESCAPES = "Escapes";

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

		// TODO
		if (escapes != null && !escapes.isEmpty()) {
			writer.startNode(TAG_ESCAPES);
			for (Entry<String, String> entry : escapes.entrySet()) {
				final String key = entry.getKey();
				final String value = entry.getValue();
				writer.startNode(TAG_ESCAPE);
				writer.addAttribute("value", key);
				writer.setValue(value);
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

		// Read Optional
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			// Read Format
			String node = reader.getNodeName();
			if (node.equals(TAG_FORMAT)) {
				fragments.addAll((Collection<AbstractFragment>) context
						.convertAnother(type, ArrayList.class));
			}

			// Read Enum
			if (node.equals(TAG_ENUM)) {
				enums = reader.getValue();
			}

			// Read Reg
			if (node.equals(TAG_LEGAL_VALUES)) {
				reg = reader.getValue();
			}

			// Read Slots
			if (node.equals(TAG_SLOTS)) {
				slots.addAll(XMLNode.SLOTS.readStringCollection(reader,
						XMLNode.SLOT));
			}

			// Read Escapes
			if (node.equals(TAG_ESCAPES)) {
				while (reader.hasMoreChildren()) {
					final String value = reader.getAttribute("value");
					reader.moveDown();
					final String key = reader.getValue();
					escapes.put(key, value);
					reader.moveUp();
				}
			}

			// Read GUI
			if (node.equals(TAG_GUI)) {
				String guiStr = reader.getValue();

				if (guiStr.equalsIgnoreCase(GUIType.JCOMBOBOX.toString()))
					gui = GUIType.JCOMBOBOX;
				else if (guiStr.equalsIgnoreCase(GUIType.JSPINNER.toString()))
					gui = GUIType.JSPINNER;
				else if (guiStr.equalsIgnoreCase(GUIType.JTEXTFIELD.toString()))
					gui = GUIType.JTEXTFIELD;
			}

			// Read widget name
			if (node.equals(TAG_WIDGETNAME)) {
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
