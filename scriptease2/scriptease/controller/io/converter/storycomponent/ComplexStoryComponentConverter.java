package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.XMLNode;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;

import com.thoughtworks.xstream.converters.ConversionException;
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

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final ComplexStoryComponent comp = (ComplexStoryComponent) source;

		super.marshal(source, writer, context);

		XMLNode.CHILDREN.writeObject(writer, context, comp.getChildren());
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
			final String node = reader.getNodeName();

			if (!node.equalsIgnoreCase(XMLNode.CHILDREN.getName()))
				System.err.println("Expected child list, but found " + node);
			else {
				try {
					children = (Collection<StoryComponent>) context
							.convertAnother(comp, ArrayList.class);

					comp.addStoryChildren(children);
				} catch (ConversionException e) {
					System.err.println("Problems converting the children of "
							+ comp + " from xml: " + e);
				}
			}
		}
		reader.moveUp();

		return comp;
	}
}
