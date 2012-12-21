package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import scriptease.controller.io.FileIO;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.GameType.TypeValueWidgets;

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
	private static final String TAG_NAME = "Name";
	private static final String TAG_KEYWORD = "Keyword";
	private static final String TAG_ENUM = "Enum";
	private static final String TAG_LEGAL_VALUES = "LegalValues";
	private static final String TAG_SLOTS = "Slots";
	private static final String TAG_SLOT = "Slot";
	private static final String TAG_FORMAT = "Format";
	private static final String TAG_GUI = "GUI";
	private static final String TAG_WIDGETNAME = "WidgetName";
	private static final String TAG_CODESYMBOL = "CodeSymbol";
	private static final String TAG_ESCAPE = "Escape";
	private static final String TAG_ESCAPES = "Escapes";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final GameType type = (GameType) source;

		// Write Name
		writer.startNode(TAG_NAME);
		writer.setValue(type.getDisplayName());
		writer.endNode();

		// Write Keyword
		writer.startNode(TAG_KEYWORD);
		writer.setValue(type.getKeyword());
		writer.endNode();

		// Write CodeSymbol
		writer.startNode(TAG_CODESYMBOL);
		writer.setValue(type.getCodeSymbol());
		writer.endNode();

		// Write Format
		if (type.getFormat() != null && !type.getFormat().isEmpty()) {
			writer.startNode(TAG_FORMAT);
			context.convertAnother(type.getFormat());
			writer.endNode();
		}
		// Write Reg
		if (type.getReg() != null && !type.getReg().isEmpty()) {
			writer.startNode(TAG_LEGAL_VALUES);
			writer.setValue(type.getReg());
			writer.endNode();
		}

		// Write Enum
		String enumString = type.getEnumString();
		if (enumString != null && !enumString.isEmpty()) {
			writer.startNode(TAG_ENUM);
			writer.setValue(enumString);
			writer.endNode();
		}

		// Write GUI
		if (type.hasGUI()) {
			writer.startNode(TAG_GUI);
			context.convertAnother(type.getGui());
			writer.endNode();
		}

		// Write Escapes
		if (type.getEscapes() != null && !type.getEscapes().isEmpty()) {
			writer.startNode(TAG_ESCAPES);
			final Map<String, String> escapes = type.getEscapes();
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
		if (type.getSlots() != null && !type.getSlots().isEmpty()) {
			writer.startNode(TAG_SLOTS);
			for (String slot : type.getSlots()) {
				writer.startNode(TAG_SLOT);
				writer.setValue(slot);
				writer.endNode();
			}
			writer.endNode();
		}

		if (type.hasWidgetName()) {
			writer.startNode(TAG_WIDGETNAME);
			context.convertAnother(type.getWidgetName());
			writer.endNode();
		}
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
		TypeValueWidgets gui = null;
		GameType type = null;

		// Read Name
		name = FileIO.readValue(reader, TAG_NAME);

		// Read Keyword
		keyword = FileIO.readValue(reader, TAG_KEYWORD);

		// Read CodeSymbol
		codeSymbol = FileIO.readValue(reader, TAG_CODESYMBOL);

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
				while (reader.hasMoreChildren()) {
					slots.add(FileIO.readValue(reader, TAG_SLOT));
				}
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

				if (guiStr.equalsIgnoreCase(TypeValueWidgets.JCOMBOBOX
						.toString()))
					gui = TypeValueWidgets.JCOMBOBOX;
				else if (guiStr.equalsIgnoreCase(TypeValueWidgets.JSPINNER
						.toString()))
					gui = TypeValueWidgets.JSPINNER;
				else if (guiStr.equalsIgnoreCase(TypeValueWidgets.JTEXTFIELD
						.toString()))
					gui = TypeValueWidgets.JTEXTFIELD;
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
