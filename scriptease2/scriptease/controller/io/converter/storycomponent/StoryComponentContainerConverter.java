package scriptease.controller.io.converter.storycomponent;

import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * Converts only {@link StoryComponentContainer}s to/from XML.
 * StoryComponentContainers are special in that they need to be configured with
 * what they accept as children. This converter produces a container that will
 * allow anything as a child, and relies on the model to provide a properly
 * configured container. This means that whenever this converter produces a
 * container, that container's properties should be copied (if possible) into
 * the one that sohuld already exist within the model.
 * 
 * @author remiller
 * 
 * @see StoryComponentConverter
 * @see ComplexStoryComponentConverter
 */
public class StoryComponentContainerConverter extends
		ComplexStoryComponentConverter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(StoryComponentContainer.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final StoryComponentContainer container;
		int max = ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE;

		container = new StoryComponentContainer();

		container.registerChildType(StoryComponentContainer.class, max);
		container.registerChildType(KnowIt.class, max);
		container.registerChildType(ScriptIt.class, max);
		container.registerChildType(AskIt.class, max);
		container.registerChildType(StoryItemSequence.class, max);
		container.registerChildType(Note.class, max);

		return container;
	}
}
