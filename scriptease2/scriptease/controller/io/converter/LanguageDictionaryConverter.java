package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import scriptease.controller.io.FileIO;
import scriptease.translator.LanguageDictionary;
import scriptease.translator.codegenerator.code.fragments.FormatIDFragment;
import scriptease.translator.io.model.GameMap;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter for the two types of Pattern Model converters.
 * 
 * @author remiller
 */
public class LanguageDictionaryConverter implements Converter {
	private static final String TAG_NAME = "name";
	private static final String TAG_FORMATS = "Formats";
	private static final String TAG_INDENTED_STRING = "IndentString";
	private static final String TAG_RESERVED_WORDS = "ReservedWords";
	private static final String TAG_RESERVED_WORD = "Word";
	private static final String TAG_MAPS = "Maps";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final LanguageDictionary languageDictionary = (LanguageDictionary) source;

		// name
		writer.addAttribute(TAG_NAME, languageDictionary.getName());

		// indentation
		writer.startNode(TAG_INDENTED_STRING);
		writer.setValue(languageDictionary.getIndent());
		writer.endNode();

		// reserved words
		writer.startNode(TAG_RESERVED_WORDS);
		for (String word : languageDictionary.getReservedWords()) {
			writer.startNode(TAG_RESERVED_WORD);
			writer.setValue(word);
			writer.endNode();

		}
		writer.endNode();

		// maps
		writer.startNode(TAG_MAPS);
		Map<String, GameMap> maps = languageDictionary.getMaps();
		for (String id : maps.keySet()) {
			GameMap map = maps.get(id);
			context.convertAnother(map);
		}
		writer.endNode();

		// formats
		writer.startNode(TAG_FORMATS);
		context.convertAnother(languageDictionary.getFormats());
		writer.endNode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String name;
		final String indentString;
		final Collection<String> reservedWords;
		final Map<String, GameMap> maps;
		final Map<String, FormatIDFragment> formatMap;
		LanguageDictionary languageDictionary = null;

		System.err.println("Unmarshaling LanguageDictionary");

		// name
		name = reader.getAttribute(TAG_NAME);

		// indentation
		indentString = FileIO.readValue(reader, TAG_INDENTED_STRING);

		// reserved words
		reservedWords = new ArrayList<String>();
		reader.moveDown();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			reservedWords.add(reader.getValue());
			reader.moveUp();
		}
		reader.moveUp();

		// maps
		maps = new HashMap<String, GameMap>();
		reader.moveDown();
		while (reader.hasMoreChildren()) {
			GameMap gameMap = (GameMap) context.convertAnother(
					languageDictionary, GameMap.class);
			maps.put(gameMap.getID(), gameMap);
		}
		reader.moveUp();

		// formats
		Collection<FormatIDFragment> fragments = new ArrayList<FormatIDFragment>();
		formatMap = new HashMap<String, FormatIDFragment>();

		reader.moveDown();
		if (reader.hasMoreChildren())
			fragments.addAll(((Collection<FormatIDFragment>) context
					.convertAnother(languageDictionary, ArrayList.class)));
		reader.moveUp();

		for (FormatIDFragment fragment : fragments) {
			formatMap.put(fragment.getDirectiveText().toUpperCase(), fragment);
		}

		languageDictionary = new LanguageDictionary(name, indentString,
				reservedWords, formatMap, maps);
		return languageDictionary;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(LanguageDictionary.class);
	}
}
