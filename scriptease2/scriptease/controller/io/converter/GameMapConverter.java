package scriptease.controller.io.converter;

import java.util.HashMap;
import java.util.Map;

import scriptease.translator.io.model.GameMap;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter for GameMap objects.
 * 
 * @author mfchurch
 */
public class GameMapConverter implements Converter {
	private static final String TAG_ID = "id";
	private static final String TAG_DEFAULT = "default";
	private static final String TAG_ENTRY = "entry";
	private static final String TAG_KEY = "key";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final GameMap map = (GameMap) source;

		// ID
		writer.addAttribute(TAG_ID, map.getID());

		// Default
		writer.addAttribute(TAG_DEFAULT, map.getDefault());

		// Entries
		for (String entryKey : map.getEntryMap().keySet()) {
			String entry = map.resolveEntry(entryKey);
			writer.startNode(TAG_ENTRY);
			writer.addAttribute(TAG_KEY, entryKey);
			writer.setValue(entry);
			writer.endNode();
		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final GameMap map;
		final String id;
		final String defaultValue;
		final Map<String, String> entryMap;

		// Read ID
		id = reader.getAttribute(TAG_ID);

		// Read Default
		defaultValue = reader.getAttribute(TAG_DEFAULT);

		// Read Entries
		entryMap = new HashMap<String, String>();
		reader.moveDown();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			String key = reader.getAttribute(TAG_KEY);
			String value = reader.getValue();
			entryMap.put(key, value);
			reader.moveUp();
		}
		reader.moveUp();

		map = new GameMap(id, defaultValue, entryMap);
		return map;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(GameMap.class);
	}
}
