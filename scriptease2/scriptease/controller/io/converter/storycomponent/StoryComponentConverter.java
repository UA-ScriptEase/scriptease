package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.FileIO;
import scriptease.controller.io.FileIO.IoMode;
import scriptease.controller.io.XMLAttribute;
import scriptease.controller.io.XMLNode;
import scriptease.controller.io.converter.model.LibraryModelConverter;
import scriptease.controller.io.converter.model.StoryModelConverter;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.librarymodel.LibraryModel;

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
		final LibraryModel library = comp.getLibrary();
		final String description = comp.getDescription();
		final boolean visible = comp.isVisible();
		final boolean enabled = comp.isEnabled();
		final Collection<String> labels = comp.getLabels();

		if (library != LibraryModelConverter.currentLibrary) {
			XMLAttribute.LIBRARY.write(writer, library.getTitle());
		}

		XMLNode.NAME.writeString(writer, comp.getDisplayText());

		if (!description.isEmpty())
			XMLNode.DESCRIPTION.writeString(writer, description);
		if (!visible)
			XMLNode.VISIBLE.writeBoolean(writer, visible);
		if (!enabled)
			XMLNode.ENABLED.writeBoolean(writer, enabled);

		// We need to write these so we break at the right moment when we load.
		XMLNode.LABELS.writeChildren(writer, labels);
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

		String displayText = "";
		String description = "";

		boolean visible = true;
		boolean enabled = true;

		final Collection<String> labels = new ArrayList<String>();
		final String libraryStr = XMLAttribute.LIBRARY.read(reader);

		final LibraryModel library;
		if (libraryStr == null
				|| libraryStr.equals(LibraryModelConverter.currentLibrary
						.getTitle()))
			library = LibraryModelConverter.currentLibrary;
		else if (libraryStr.equals(LibraryModel.NON_LIBRARY_NAME))
			library = LibraryModel.getNonLibrary();
		else if (libraryStr.equals(LibraryModel.COMMON_LIBRARY_NAME))
			library = LibraryModel.getCommonLibrary();
		else {
			final IoMode ioMode = FileIO.getInstance().getMode();

			if (ioMode == IoMode.LIBRARY)
				library = FileIO.getInstance().getTranslator()
						.findLibrary(libraryStr);
			else if (ioMode == IoMode.STORY)
				library = StoryModelConverter.currentStory.getTranslator()
						.findLibrary(libraryStr);
			else
				throw new IllegalStateException("Unsupported IOMode " + ioMode
						+ ". Could not find a library called " + libraryStr);
		}

		comp = this.buildComponent(reader, context, library);

		while (reader.hasMoreChildren()) {
			reader.moveDown();

			String node = reader.getNodeName();
			if (node.equals(XMLNode.NAME.getName())) {
				displayText = reader.getValue();
			}

			if (node.equals(XMLNode.VISIBLE.getName())) {
				visible = reader.getValue().equalsIgnoreCase("true");
			}

			if (node.equals(XMLNode.ENABLED.getName())) {
				enabled = reader.getValue().equalsIgnoreCase("true");
			}

			if (node.equals(XMLNode.DESCRIPTION.getName())) {
				description = reader.getValue();
			}

			if (node.equals(XMLNode.LABELS.getName())) {
				while (reader.hasMoreChildren()) {
					reader.moveDown();
					labels.add(reader.getValue());
					reader.moveUp();
				}

				// Labels are always the last element and are always present.
				reader.moveUp();
				break;
			}

			reader.moveUp();
		}

		// Actually init the StoryComponent.
		comp.setDisplayText(displayText);
		comp.setDescription(description);
		comp.addLabels(labels);
		comp.setVisible(visible);
		comp.setEnabled(enabled);

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
	 * @param library
	 *            the library that the story component belongs to
	 * @param id
	 *            the unique id of the story component referenced to the library
	 * @return instance a StoryComponent subclass
	 * @see #unmarshal(HierarchicalStreamReader, UnmarshallingContext)
	 */
	protected abstract StoryComponent buildComponent(
			HierarchicalStreamReader reader, UnmarshallingContext context,
			LibraryModel library);
}
