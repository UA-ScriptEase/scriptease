package scriptease.controller.io.converter.storycomponent;

import scriptease.model.StoryComponent;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.semodel.librarymodel.LibraryModel;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * Converts only {@link StoryComponentContainer}s to/from XML.
 * StoryComponentContainers are special in that they need to be configured with
 * what they accept as children. This converter produces a container that will
 * allow anything as a child, and relies on the model to provide a properly
 * configured container. This means that whenever this converter produces a
 * container, that container's properties should be copied (if possible) into
 * the one that should already exist within the model.
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
			UnmarshallingContext context, LibraryModel library) {
		return new StoryComponentContainer();
	}
}
