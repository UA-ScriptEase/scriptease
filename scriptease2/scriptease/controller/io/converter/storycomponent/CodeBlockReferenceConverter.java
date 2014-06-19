package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.FileIO;
import scriptease.controller.io.FileIO.IoMode;
import scriptease.controller.io.XMLAttribute;
import scriptease.controller.io.XMLNode;
import scriptease.model.CodeBlockReference;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.util.StringOp;

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

	// Sorry about this awful, terrible hack, but we had no other choice.
	protected static ScriptIt owner = null;

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(CodeBlockReference.class);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final CodeBlockReference codeBlock = (CodeBlockReference) source;
		final Collection<KnowIt> parameters = codeBlock.getParameters();

		final int id = codeBlock.getID();

		if (id > 0)
			XMLAttribute.ID.write(writer, Integer.toString(id));

		super.marshal(source, writer, context);

		if (!parameters.isEmpty()) {
			XMLNode.PARAMETERS.writeObject(writer, context, parameters);
		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String idStr = XMLAttribute.ID.read(reader);

		final int id;
		final CodeBlockReference ref;
		final CodeBlockSource target;
		final LibraryModel library;

		ref = (CodeBlockReference) super.unmarshal(reader, context);

		if (StringOp.exists(idStr))
			id = Integer.parseInt(idStr);
		else
			id = 0;

		library = ref.getLibrary();
		target = library.findCodeBlockSource(owner, id);

		if (target == null) {
			CodeBlockSource newTarget = null;

			if (FileIO.getInstance().getMode() == IoMode.STORY) {
				final Translator translator = library.getTranslator();
				newTarget = translator.findSimilarTarget(owner, id);
			} // TODO May have to do something here if we have missing
				// references in a different mode.

			if (newTarget == null) {
				throw new NullPointerException("Could not find CodeBlock "
						+ ref.getDisplayText() + " in \"" + owner + "\" in "
						+ library);
			}

			ref.setTarget(newTarget);
		} else
			ref.setTarget(target);

		if (reader.hasMoreChildren()) {
			final Collection<KnowIt> parameters = new ArrayList<KnowIt>();

			parameters.addAll(XMLNode.PARAMETERS.readCollection(XMLNode.KNOWIT,
					reader, context, KnowIt.class));

			ref.setParameters(parameters);
		}

		return ref;
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context, LibraryModel library) {
		return new CodeBlockReference(library);
	}
}
