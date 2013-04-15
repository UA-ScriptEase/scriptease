package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ControlIt;
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

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		super.marshal(source, writer, context);
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
		validTypes.add(Note.class);
		validTypes.add(ControlIt.class);
		return validTypes;
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		StoryItemSequence storyItemSequence = null;

		// read data
		storyItemSequence = (StoryItemSequence) super
				.unmarshal(reader, context);

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
