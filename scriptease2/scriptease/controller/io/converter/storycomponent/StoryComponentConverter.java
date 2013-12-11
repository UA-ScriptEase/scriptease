package scriptease.controller.io.converter.storycomponent;

import java.util.Collection;

import scriptease.controller.io.XMLNode;
import scriptease.model.StoryComponent;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter superclass for all converters that convert StoryComponents.
 * Subclasses are required to implement {@link #buildComponent()}, in which they
 * build the specific instance of StoryComponent that they are responsible for
 * converting. They are also expected to call super.marshal(...) and
 * super.unmarshall(...).
 * 
 * @author remiller
 */
public abstract class StoryComponentConverter implements Converter {
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final StoryComponent comp = (StoryComponent) source;

		XMLNode.NAME.writeString(writer, comp.getDisplayText());
		XMLNode.VISIBLE.writeBoolean(writer, comp.isVisible());
		XMLNode.ENABLED.writeBoolean(writer, comp.isEnabled());
		XMLNode.LABELS.writeChildren(writer, comp.getLabels());
	}

	/**
	 * Unmarshalls A StoryComponent from XML. The exact StoryComponent that is
	 * unmarshalled is determined by the implementation of
	 * {@link #buildComponent(HierarchicalStreamReader, UnmarshallingContext)},
	 * which is called before any generic StoryComponent properties are read in.
	 */
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final StoryComponent comp;
		final String displayText;
		final String visibility;
		final String enabled;
		final Collection<String> labels;

		comp = this.buildComponent(reader, context);

		displayText = XMLNode.NAME.readString(reader);
		visibility = XMLNode.VISIBLE.readString(reader);
		enabled = XMLNode.ENABLED.readString(reader);
		labels = XMLNode.LABELS.readStringCollection(reader);

		// Actually init the StoryComponent.
		comp.setDisplayText(displayText);
		comp.addLabels(labels);
		comp.setVisible(visibility.equalsIgnoreCase("true"));
		comp.setEnabled(enabled.equalsIgnoreCase("true"));

		return comp;
	}

	/**
	 * Builds an instance of the specific StoryComponent subclass that the
	 * converter is intended to convert. This is used as part of the
	 * unmarshalling process, before all other StoryComponent properties have
	 * been read. The unmarshalling parameters can be used to read any
	 * information that is necessary for constructing the object.
	 * 
	 * @param reader
	 *            the reader to read from
	 * @param context
	 *            the context to read in
	 * 
	 * @return instance a StoryComponent subclass
	 * @see #unmarshal(HierarchicalStreamReader, UnmarshallingContext)
	 */
	protected abstract StoryComponent buildComponent(
			HierarchicalStreamReader reader, UnmarshallingContext context);
}
