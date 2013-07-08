package scriptease.controller.io.converter.model;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.XMLAttribute;
import scriptease.controller.io.XMLNode;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.librarymodel.LibraryModel;
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
public class LibraryModelConverter implements Converter {
	private static final String TAG_CAUSES = "Causes";
	private static final String TAG_DESCRIBE_ITS = "DescribeIts";
	private static final String TAG_CONTROL_ITS = "ControlIts";
	private static final String TAG_EFFECTS = "Effects";
	private static final String TAG_TYPE_CONVERTERS = "TypeConverters";
	private static final String TAG_SLOT_DEFAULT_FORMAT_KEYWORD = "defaultFormat";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final LibraryModel library = (LibraryModel) source;

		XMLAttribute.NAME.write(writer, library.getTitle());
		XMLAttribute.AUTHOR.write(writer, library.getAuthor());

		XMLNode.TYPES.writeObject(writer, context, library.getGameTypes());
		XMLNode.SLOTS.writeObject(writer, context, library.getSlots(),
				XMLAttribute.DEFAULT_FORMAT, library.getSlotDefaultFormat());

		XMLNode.CAUSES.writeObject(writer, context, library.getCausesCategory()
				.getChildren());
		XMLNode.EFFECTS.writeObject(writer, context, library
				.getEffectsCategory().getChildren());
		XMLNode.DESCRIBEITS.writeObject(writer, context,
				library.getDescribeIts());
		XMLNode.CONTROLITS.writeObject(writer, context, library
				.getControllersCategory().getChildren());
		XMLNode.TYPECONVERTERS.writeObject(writer, context, library
				.getTypeConverter().getConverterDoIts());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final LibraryModel library = new LibraryModel();

		System.out.println("Unmarshalling Library Model");

		library.setTitle(XMLAttribute.NAME.read(reader));
		library.setAuthor(XMLAttribute.AUTHOR.read(reader));

		library.addGameTypes(XMLNode.TYPES.readObjectCollection(reader,
				context, GameType.class));

		// slots
		reader.moveDown();
		library.setSlotDefaultFormat(reader
				.getAttribute(TAG_SLOT_DEFAULT_FORMAT_KEYWORD));
		if (reader.hasMoreChildren()) {
			library.addSlots(((Collection<Slot>) context.convertAnother(
					library, ArrayList.class)));
		}
		reader.moveUp();

		// causes
		reader.moveDown();
		if (reader.hasMoreChildren()) {
			if (!reader.getNodeName().equalsIgnoreCase(TAG_CAUSES))
				System.err.println("Expected " + TAG_CAUSES + ", but found "
						+ reader.getNodeName());
			else {
				library.addAll((Collection<? extends StoryComponent>) context
						.convertAnother(library, ArrayList.class));
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
				library.addAll((Collection<? extends StoryComponent>) context
						.convertAnother(library, ArrayList.class));
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
				final Collection<DescribeIt> describeIts;

				describeIts = (Collection<DescribeIt>) context.convertAnother(
						library, ArrayList.class);

				for (DescribeIt describeIt : describeIts) {
					/*
					 * We can't add this as usual since our LibraryModel is
					 * still getting created right here. The add(DescribeIt)
					 * method would thus cause a null pointer exception to be
					 * thrown. Besides, we'd be doing things twice. -kschenk
					 */
					final KnowIt knowIt;

					knowIt = library.createKnowItForDescribeIt(describeIt);

					library.add(knowIt);
					library.addDescribeIt(describeIt, knowIt);
				}
			}
		}
		reader.moveUp();

		// controls
		reader.moveDown();
		if (reader.hasMoreChildren()) {
			if (!reader.getNodeName().equalsIgnoreCase(TAG_CONTROL_ITS))
				System.err.println("Expected " + TAG_CONTROL_ITS
						+ ", but found " + reader.getNodeName());
			else {
				library.addAll(((Collection<ControlIt>) context.convertAnother(
						library, ArrayList.class)));
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
				library.getTypeConverter().addConverterScriptIts(
						((Collection<ScriptIt>) context.convertAnother(library,
								ArrayList.class)));
			}
		}
		reader.moveUp();

		return library;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(LibraryModel.class);
	}
}
