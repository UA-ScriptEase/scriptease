package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.XMLAttribute;
import scriptease.controller.io.XMLNode;
import scriptease.controller.io.converter.model.LibraryModelConverter;
import scriptease.gui.WindowFactory;
import scriptease.model.CodeBlockReference;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts {@link CodeBlockReference}s for file I/O. References have their own
 * parameter list, and a target that they must rebind to on load. References are
 * allowed to exist in Stories as well as in the LibraryModel.<br>
 * <br>
 * Story-side References store their target's ID number, but Translator-side
 * References store the actual target.
 * 
 * @author remiller
 * @author kschenk
 */
public class CodeBlockReferenceConverter extends StoryComponentConverter
		implements Converter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(CodeBlockReference.class);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final CodeBlockReference codeBlock = (CodeBlockReference) source;
		final LibraryModel library = codeBlock.getLibrary();
		final Collection<KnowIt> parameters = codeBlock.getParameters();

		if (library != null)
			XMLAttribute.LIBRARY.write(writer, library.getTitle());
		else
			System.err.println("No library found for " + source
					+ ". Library attribute will be left blank.");

		super.marshal(source, writer, context);

		if (!parameters.isEmpty()) {
			XMLNode.PARAMETERS.writeObject(writer, context, parameters);
		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {

		// TODO Setting target here may be wrong. Check it out

		final String libraryName = XMLAttribute.LIBRARY.read(reader);

		final CodeBlockReference block;
		final Translator translator;

		block = (CodeBlockReference) super.unmarshal(reader, context);
		translator = TranslatorManager.getInstance().getActiveTranslator();

		// We use this hack to load in any codeblock references to the existing
		// library model before the library model is read from XStream.
		LibraryModel library;
		if (LibraryModelConverter.currentLibrary != null
				&& libraryName.equals(LibraryModelConverter.currentLibrary
						.getTitle())) {
			library = LibraryModelConverter.currentLibrary;
		} else
			library = translator.findLibrary(libraryName);

		CodeBlockSource target = null;
		if (library != null) {
			target = library.getCodeBlockByID(block.getID());
			block.setLibrary(library);
		}

		if (target == null) {
			final String msg = "Failed to read target information for \""
					+ block + "\" in the library, " + library
					+ ", nulling the reference.";
			System.err.println(msg);
			WindowFactory.getInstance().showWarningDialog("Library Error", msg);
		}

		block.setTarget(target);

		if (reader.hasMoreChildren()) {
			final Collection<KnowIt> parameters = new ArrayList<KnowIt>();

			parameters.addAll(XMLNode.PARAMETERS.readCollection(XMLNode.KNOWIT,
					reader, context, KnowIt.class));

			block.setParameters(parameters);
		}

		return block;
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context, LibraryModel library, int id) {
		return new CodeBlockReference(library, id);
	}
}
