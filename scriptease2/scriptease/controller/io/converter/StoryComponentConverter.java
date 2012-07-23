package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.VisibilityManager;
import scriptease.controller.io.FileIO;
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
 * converting. They are also expected to call super.marshal(...) and super.unmarshall(...).
 * 
 * @author remiller
 */
public abstract class StoryComponentConverter implements Converter {
	private static final String TAG_NAME = "Name";
	private static final String TAG_LABELS = "Labels";
	private static final String TAG_LABEL = "Label";
	private final String TAG_VISIBLE = "visible";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final StoryComponent comp = (StoryComponent) source;

		// Visibility
		writer.addAttribute(TAG_VISIBLE, VisibilityManager.getInstance()
				.isVisible(comp).toString());

		// Name
		writer.startNode(TAG_NAME);
		writer.setValue(comp.getDisplayText());
		writer.endNode();

		// Labels
		writer.startNode(TAG_LABELS);
		for (String label : comp.getLabels()) {
			writer.startNode(TAG_LABEL);
			writer.setValue(label);
			writer.endNode();
		}
		writer.endNode();
	}

	/**
	 * Unmarshalls A StoryComponent from XML. The exact StoryComponent that is
	 * unmarshalled is determined by the implementation of
	 * {@link #buildComponent(HierarchicalStreamReader, UnmarshallingContext)},
	 * which is called before any generic StroyComponent properties are read in.
	 */
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final StoryComponent comp;
		final String displayText;
		String visibility;

		comp = this.buildComponent(reader, context);

		// Default to true if not found
		visibility = reader.getAttribute(TAG_VISIBLE);
		if (visibility == null)
			visibility = "true";

		displayText = FileIO.readValue(reader, TAG_NAME);

		// Labels
		final Collection<String> labels = new ArrayList<String>();
		reader.moveDown();
		if (!reader.getNodeName().equalsIgnoreCase(TAG_LABELS))
			System.err
					.println("Failed to read labels for StoryComponent with displayText ["
							+ displayText + "]");
		else {
			while (reader.hasMoreChildren()) {
				// read all of the labels
				labels.add(FileIO.readValue(reader, TAG_LABEL));
			}
		}
		reader.moveUp();

		// Actually init the StoryComponent.
		comp.setDisplayText(displayText);
		comp.addLabels(labels);

		// Set the visibility
		VisibilityManager.getInstance().setVisibility(comp,
				visibility.equalsIgnoreCase("true"));

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
