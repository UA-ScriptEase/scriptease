package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.gui.WindowFactory;
import scriptease.model.CodeBlockReference;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

import com.thoughtworks.xstream.XStreamException;
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
	public static final String TAG_CODE_BLOCK_REF = "CodeBlockReference";

	private static final String TAG_PARAMETERS = "Parameters";
	private static final String TAG_TARGET_ID = "TargetId";

	private static final String ATTRIBUTE_LIBRARY = "library";

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

		if (library != null)
			writer.addAttribute(ATTRIBUTE_LIBRARY, library.getName());
		else
			System.err.println("No library found for " + source
					+ ". Library attribute will be left blank.");

		final Collection<KnowIt> parameters = codeBlock.getParameters();

		super.marshal(source, writer, context);

		writer.startNode(TAG_TARGET_ID);
		writer.setValue(Integer.toString(codeBlock.getId()));
		writer.endNode();

		// Parameters
		if (!parameters.isEmpty()) {
			writer.startNode(TAG_PARAMETERS);
			context.convertAnother(parameters);
			writer.endNode();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String libraryName;
		final CodeBlockReference block;
		final CodeBlockSource target;
		String nodeName;

		libraryName = reader.getAttribute(ATTRIBUTE_LIBRARY);

		block = (CodeBlockReference) super.unmarshal(reader, context);

		reader.moveDown();
		nodeName = reader.getNodeName();

		if (!nodeName.equals(TAG_TARGET_ID))
			this.dieMissingTarget(TAG_TARGET_ID, nodeName);

		final Translator translator;
		final int targetId;
		final LibraryModel library;

		translator = TranslatorManager.getInstance().getActiveTranslator();
		targetId = Integer.parseInt(reader.getValue());
		library = translator.findLibrary(libraryName);

		if (library != null) {
			target = library.getCodeBlockByID(targetId);
			block.setLibrary(library);
		} else
			target = null;

		if (target == null) {
			final String msg = "Failed to read target information for \""
					+ block + "\" in the library, " + library
					+ ", nulling the reference.";
			System.err.println(msg);
			WindowFactory.getInstance().showWarningDialog("Library Error", msg);
		}

		block.setTarget(target);
		reader.moveUp();

		// the parameter list doesn't exist if there aren't any, so look out for
		// that.
		if (reader.hasMoreChildren()) {
			final Collection<KnowIt> parameters = new ArrayList<KnowIt>();

			reader.moveDown();
			nodeName = reader.getNodeName();

			// Parameters
			if (!nodeName.equals(TAG_PARAMETERS)) {
				throw new XStreamException(
						"CodeBlockReference missing parameter information, or data is in the wrong order. Expected "
								+ TAG_PARAMETERS + ", but found " + nodeName);
			}

			parameters.addAll((Collection<KnowIt>) context.convertAnother(
					block, ArrayList.class));

			reader.moveUp();

			block.setParameters(parameters);
		}

		return block;
	}

	private void dieMissingTarget(String expected, String found) {
		throw new XStreamException(
				"CodeBlockReference missing target information, or data is in the wrong order. Expected "
						+ expected + ", but found " + found);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new CodeBlockReference();
	}
}
