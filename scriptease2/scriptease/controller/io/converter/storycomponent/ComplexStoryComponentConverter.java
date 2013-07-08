package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts a ComplexStoryComponent to/from XML. This includes all additional
 * non-volatile properties of a ComplexStoryComponent over a StoryComponent,
 * namely the child list.
 * 
 * @author remiller
 * 
 */
public abstract class ComplexStoryComponentConverter extends
		StoryComponentConverter implements Converter {

	// TODO See LibraryModelConverter class for an example of how to refactor
	// this class. However, since we're moving to YAML eventually, we don't need
	// to waste anymore time on refactoring these.
	private static final String TAG_CHILDREN = "Children";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final ComplexStoryComponent comp;

		comp = (ComplexStoryComponent) source;

		super.marshal(source, writer, context);

		writer.startNode(TAG_CHILDREN);
		context.convertAnother(comp.getChildren());

		writer.endNode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final ComplexStoryComponent comp;
		final Collection<StoryComponent> children;

		comp = (ComplexStoryComponent) super.unmarshal(reader, context);

		// Read children.
		reader.moveDown();
		if (reader.hasMoreChildren()) {
			if (!reader.getNodeName().equalsIgnoreCase(TAG_CHILDREN))
				System.err.println("Expected child list, but found "
						+ reader.getNodeName());
			else {
				children = (Collection<StoryComponent>) context.convertAnother(
						comp, ArrayList.class);

				comp.addStoryChildren(children);
			}
		}
		reader.moveUp();

		return comp;
	}
}
