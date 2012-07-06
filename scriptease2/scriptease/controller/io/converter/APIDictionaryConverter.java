package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.apimanagers.EventSlotManager;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.APIDictionary;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.Slot;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter for the two types of Pattern Model converters.
 * 
 * @author mfchurch
 */
public class APIDictionaryConverter implements Converter {
	private static final String TAG_NAME = "name";
	private static final String TAG_AUTHOR = "author";
	private static final String TAG_CAUSES = "Causes";
	private static final String TAG_DESCRIBE_ITS = "DescribeIts";
	private static final String TAG_EFFECTS = "Effects";
	private static final String TAG_TYPES = "Types";
	private static final String TAG_TYPE_CONVERTERS = "TypeConverters";
	private static final String TAG_SLOTS = "Slots";
	private static final String TAG_SLOT_DEFAULT_FORMAT_KEYWORD = "defaultFormat";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final APIDictionary apiDictionary = (APIDictionary) source;

		// name
		writer.addAttribute(TAG_NAME, apiDictionary.getName());

		// author
		writer.addAttribute(TAG_AUTHOR, apiDictionary.getAuthor());

		// types
		writer.startNode(TAG_TYPES);
		context.convertAnother(apiDictionary.getGameTypeManager()
				.getGameTypes());
		writer.endNode();

		// slots
		final EventSlotManager eventSlotManager = apiDictionary
				.getEventSlotManager();
		writer.startNode(TAG_SLOTS);
		writer.addAttribute(TAG_SLOT_DEFAULT_FORMAT_KEYWORD,
				eventSlotManager.getDefaultFormatKeyword());
		context.convertAnother(eventSlotManager.getEventSlots());
		writer.endNode();

		// causes
		writer.startNode(TAG_CAUSES);
		context.convertAnother(apiDictionary.getLibrary().getCausesCategory()
				.getChildren());
		writer.endNode();

		// effects
		writer.startNode(TAG_EFFECTS);
		context.convertAnother(apiDictionary.getLibrary().getEffectsCategory()
				.getChildren());
		writer.endNode();

		// descriptions
		writer.startNode(TAG_DESCRIBE_ITS);
		context.convertAnother(apiDictionary.getLibrary()
				.getDescriptionsCategory().getChildren());
		writer.endNode();

		// typeconverters
		writer.startNode(TAG_TYPE_CONVERTERS);
		context.convertAnother(apiDictionary.getGameTypeManager()
				.getTypeConverter().getConverterDoIts());
		writer.endNode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final APIDictionary apiDictionary = new APIDictionary();

		System.err.println("Unmarshaling APIDictionary");

		// name
		apiDictionary.setName(reader.getAttribute(TAG_NAME));

		// author
		apiDictionary.setAuthor(reader.getAttribute(TAG_AUTHOR));

		// types
		reader.moveDown();
		if (reader.hasMoreChildren()) {
			if (!reader.getNodeName().equalsIgnoreCase(TAG_TYPES))
				System.err.println("Expected " + TAG_TYPES + ", but found "
						+ reader.getNodeName());
			else {
				apiDictionary.getGameTypeManager().addGameTypes(
						((Collection<GameType>) context.convertAnother(
								apiDictionary, ArrayList.class)));
			}
		}
		reader.moveUp();

		// slots
		final EventSlotManager eventSlotManager = apiDictionary
				.getEventSlotManager();
		reader.moveDown();
		eventSlotManager.setDefaultFormatKeyword(reader
				.getAttribute(TAG_SLOT_DEFAULT_FORMAT_KEYWORD));
		if (reader.hasMoreChildren()) {
			eventSlotManager.addEventSlots(((Collection<Slot>) context
					.convertAnother(apiDictionary, ArrayList.class)));
		}
		reader.moveUp();

		// causes
		reader.moveDown();
		if (reader.hasMoreChildren()) {
			if (!reader.getNodeName().equalsIgnoreCase(TAG_CAUSES))
				System.err.println("Expected " + TAG_CAUSES + ", but found "
						+ reader.getNodeName());
			else {
				apiDictionary
						.getLibrary()
						.addAll(((Collection<? extends StoryComponent>) context
								.convertAnother(apiDictionary, ArrayList.class)));
			}
		}
		reader.moveUp();

		// effects
		reader.moveDown();
		if (reader.hasMoreChildren()) {
			if (!reader.getNodeName().equalsIgnoreCase(TAG_EFFECTS))
				System.err.println("Expected " + TAG_EFFECTS + ", but found "
						+ reader.getNodeName());
			else {
				apiDictionary
						.getLibrary()
						.addAll(((Collection<? extends StoryComponent>) context
								.convertAnother(apiDictionary, ArrayList.class)));
			}
		}
		reader.moveUp();

		// descriptions
		reader.moveDown();
		if (reader.hasMoreChildren()) {
			if (!reader.getNodeName().equalsIgnoreCase(TAG_DESCRIBE_ITS))
				System.err.println("Expected " + TAG_DESCRIBE_ITS
						+ ", but found " + reader.getNodeName());
			else {
				apiDictionary
						.getLibrary()
						.addAll(((Collection<? extends StoryComponent>) context
								.convertAnother(apiDictionary, ArrayList.class)));
			}
		}
		reader.moveUp();

		// typeconverters
		reader.moveDown();
		if (reader.hasMoreChildren()) {
			if (!reader.getNodeName().equalsIgnoreCase(TAG_TYPE_CONVERTERS))
				System.err.println("Expected " + TAG_TYPE_CONVERTERS
						+ ", but found " + reader.getNodeName());
			else {
				apiDictionary
						.getGameTypeManager()
						.getTypeConverter()
						.addConverterScriptIts(
								((Collection<ScriptIt>) context.convertAnother(
										apiDictionary, ArrayList.class)));
			}
		}
		reader.moveUp();

		return apiDictionary;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(APIDictionary.class);
	}
}
