package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.StoryComponentClassNameConverter;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts only StoryItemSequences to/from XML.
 * 
 * @author remiller
 * 
 * @see StoryComponentConverter
 */
public class StoryItemSequenceConverter extends ComplexStoryComponentConverter {
	private static final String TAG_VALID_TYPES = "ValidTypes";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final StoryItemSequence storyItemSequence = (StoryItemSequence) source;
		super.marshal(source, writer, context);

		Collection<Class<? extends StoryComponent>> validTypes = new ArrayList<Class<? extends StoryComponent>>(
				storyItemSequence.getValidChildTypes());
		writer.startNode(TAG_VALID_TYPES);
		context.convertAnother(classToName(validTypes));
		writer.endNode();

	}

	private Collection<String> classToName(
			Collection<Class<? extends StoryComponent>> classes) {
		Collection<String> names = new ArrayList<String>();

		for (Class<? extends StoryComponent> aClass : classes) {
			String name = aClass.getSimpleName();
			names.add(name);
		}
		return names;
	}

	/**
	 * Populates validTypes with all possible classes, so that it can accept any
	 * child as a valid child for initialization.
	 * 
	 * @return
	 */
	private Collection<Class<? extends StoryComponent>> populateValidTypes() {
		Collection<Class<? extends StoryComponent>> validTypes = new ArrayList<Class<? extends StoryComponent>>();
		validTypes.add(ScriptIt.class);
		validTypes.add(KnowIt.class);
		validTypes.add(StoryComponentContainer.class);
		validTypes.add(AskIt.class);
		return validTypes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		StoryItemSequence storyItemSequence = null;

		// reading data
		storyItemSequence = (StoryItemSequence) super
				.unmarshal(reader, context);

		/*
		 * clear default of all class types as children @see
		 * populateValidTypes(), the read in the real allowable children
		 */
		storyItemSequence.clearAllowableChildren();

		reader.moveDown();
		if (!reader.getNodeName().equalsIgnoreCase(TAG_VALID_TYPES))
			System.err
					.println("Failed to read valid type list for StoryItemSequence ");
		else {
			for (String name : (Collection<String>) context.convertAnother(
					storyItemSequence, ArrayList.class)) {
				storyItemSequence.registerChildType(
						StoryComponentClassNameConverter
								.convertToModelClass(name),
						ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
			}
		}
		reader.moveUp();

		return storyItemSequence;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(StoryItemSequence.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final StoryItemSequence storyItemSequence;
		Collection<Class<? extends StoryComponent>> validTypes = populateValidTypes();

		storyItemSequence = new StoryItemSequence(validTypes);

		return storyItemSequence;
	}
}
