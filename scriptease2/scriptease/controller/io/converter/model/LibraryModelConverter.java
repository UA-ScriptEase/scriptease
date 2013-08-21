package scriptease.controller.io.converter.model;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.XMLAttribute;
import scriptease.controller.io.XMLNode;
import scriptease.controller.io.XMLNode.XMLNodeData;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
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
 * Converter for Library Models.
 * 
 * @author mfchurch
 * @author kschenk
 * @author jyuen
 */
public class LibraryModelConverter implements Converter {
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final LibraryModel library = (LibraryModel) source;

		final Collection<CauseIt> causes = new ArrayList<CauseIt>();

		// Remove all the children of the causes we save. We need to make a
		// clone of every cause so we don't remove the children from the
		// originals.
		for (StoryComponent component : library.getCausesCategory()
				.getChildren()) {
			final CauseIt cause = (CauseIt) component;
			final Collection<StoryComponent> children;

			children = new ArrayList<StoryComponent>(cause.getChildren());

			for (StoryComponent child : children) {
				// Notes are fine
				if (!(child instanceof Note)) {
					cause.removeStoryChild(child);
				}
			}

			causes.add(cause);
		}

		XMLAttribute.NAME.write(writer, library.getTitle());
		XMLAttribute.AUTHOR.write(writer, library.getAuthor());
		XMLAttribute.DESCRIPTION.write(writer, library.getInformation());

		XMLNode.INCLUDE_FILES.writeChildren(writer,
				library.getIncludeFilePaths());
		XMLNode.TYPES.writeObject(writer, context, library.getGameTypes());
		XMLNode.SLOTS.writeObject(writer, context, library.getSlots(),
				XMLAttribute.DEFAULT_FORMAT, library.getSlotDefaultFormat());

		XMLNode.CAUSES.writeObject(writer, context, causes);
		XMLNode.EFFECTS.writeObject(writer, context, library
				.getEffectsCategory().getChildren());
		XMLNode.DESCRIBEITS.writeObject(writer, context,
				library.getDescribeIts());
		XMLNode.CONTROLITS.writeObject(writer, context, library
				.getControllersCategory().getChildren());
		XMLNode.TYPECONVERTERS.writeObject(writer, context, library
				.getTypeConverter().getConverterDoIts());

		// Add the defaults back in case we're playing with the libraries.
		this.addDefaultCauseChildren(library, causes);
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final LibraryModel library = new LibraryModel();

		final Collection<String> includeFilePaths;
		final Collection<GameType> types;
		final XMLNodeData<Collection<Slot>> slots;
		final Collection<CauseIt> causes;
		final Collection<ScriptIt> effects;
		final Collection<DescribeIt> descriptions;
		final Collection<ControlIt> controls;
		final Collection<ScriptIt> typeConvertors;

		System.out.println("Unmarshalling Library Model");

		// Read everything from XML

		library.setTitle(XMLAttribute.NAME.read(reader));
		library.setAuthor(XMLAttribute.AUTHOR.read(reader));
		library.setInformation(XMLAttribute.DESCRIPTION.read(reader));

		includeFilePaths = XMLNode.INCLUDE_FILES.readStringCollection(reader);
		types = XMLNode.TYPES.readCollection(reader, context, GameType.class);
		slots = XMLNode.SLOTS.readAttributedCollection(reader, context,
				Slot.class, XMLAttribute.DEFAULT_FORMAT);
		causes = XMLNode.CAUSES.readCollection(reader, context, CauseIt.class);
		effects = XMLNode.EFFECTS.readCollection(reader, context,
				ScriptIt.class);
		descriptions = XMLNode.DESCRIBEITS.readCollection(reader, context,
				DescribeIt.class);
		controls = XMLNode.CONTROLITS.readCollection(reader, context,
				ControlIt.class);
		typeConvertors = XMLNode.TYPECONVERTERS.readCollection(reader, context,
				ScriptIt.class);

		// Construct the library
		library.setIncludeFilePaths(includeFilePaths);

		library.setSlotDefaultFormat(slots
				.getAttribute(XMLAttribute.DEFAULT_FORMAT));
		library.addSlots(slots.getData());

		library.addGameTypes(types);

		library.addAll(causes);
		library.addAll(effects);

		/*
		 * We can't add this as usual since our LibraryModel is still getting
		 * created right here. The add(DescribeIt) method would thus cause a
		 * null pointer exception to be thrown. Besides, we'd be doing things
		 * twice. -kschenk
		 */
		for (DescribeIt describeIt : descriptions) {
			final KnowIt knowIt = library.createKnowItForDescribeIt(describeIt);

			library.add(knowIt);
			library.addDescribeIt(describeIt, knowIt);
		}

		library.addAll(controls);

		library.getTypeConverter().addConverterScriptIts(typeConvertors);

		this.addDefaultCauseChildren(library, causes);

		return library;
	}

	/**
	 * Add the default children for causes.
	 * 
	 * @param library
	 * @param causes
	 */
	private void addDefaultCauseChildren(LibraryModel library,
			Collection<CauseIt> causes) {

		if (library.getTitle().contains("Path")) {
			System.out.println(library.getTitle());
		}
		final Collection<CauseIt> automatics = library.getAutomatics();

		for (CauseIt cause : causes) {
			if (automatics.contains(cause))
				continue;
			for (StoryComponent description : library.getDescriptionsCategory()
					.getChildren()) {
				if (description instanceof KnowIt
						&& description.getDisplayText().contains("Is Active")) {
					final KnowIt knowIt = ((KnowIt) description).clone();
					final AskIt askIt = new AskIt();

					cause.addStoryChild(knowIt);
					cause.addStoryChild(askIt);
					askIt.getCondition().setBinding(knowIt);

					break;
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(LibraryModel.class);
	}
}
