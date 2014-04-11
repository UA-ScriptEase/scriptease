package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.XMLNode;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.librarymodel.LibraryModel;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts only ScriptIts to/from XML.
 * 
 * @author remiller
 * @author mfchurch
 * @author kschenk
 * @author jyuen
 * 
 * @see ComplexStoryComponentConverter
 */
public class ScriptItConverter extends ComplexStoryComponentConverter {
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final ScriptIt scriptIt = (ScriptIt) source;

		super.marshal(source, writer, context);

		XMLNode.CODEBLOCKS.writeObject(writer, context,
				scriptIt.getCodeBlocks());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final ScriptIt scriptIt;

		scriptIt = (ScriptIt) super.unmarshal(reader, context);

		reader.moveDown();
		if (reader.hasMoreChildren()) {
			if (reader.getNodeName().equalsIgnoreCase(
					XMLNode.CODEBLOCKS.getName())) {
				final Collection<CodeBlock> codeBlocks;

				codeBlocks = ((Collection<CodeBlock>) context.convertAnother(
						scriptIt, ArrayList.class));

				if (codeBlocks.isEmpty())
					throw new IllegalStateException(
							"Unable to read CodeBlocks for " + scriptIt);

				scriptIt.setCodeBlocks(codeBlocks);
			}
		}
		reader.moveUp();

		return scriptIt;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(ScriptIt.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context, LibraryModel library, int id) {
		return new ScriptIt(library, id, "");
	}
}
