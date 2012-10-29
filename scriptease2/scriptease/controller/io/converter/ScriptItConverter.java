package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ScriptIt;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts only ScriptIts to/from XML.
 * 
 * @author remiller
 * @author mfchurch
 * 
 * @see ComplexStoryComponentConverter
 */
public class ScriptItConverter extends ComplexStoryComponentConverter {
	public static final String TAG_SCRIPTIT = "ScriptIt";
	private static final String TAG_CODEBLOCKS = "CodeBlocks";

	// Other tags: Storychild scope
	// always child scope
	// functioncall

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final ScriptIt scriptIt = (ScriptIt) source;

		super.marshal(source, writer, context);

		// CodeBlocks
		writer.startNode(TAG_CODEBLOCKS);
		context.convertAnother(scriptIt.getCodeBlocks());
		writer.endNode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final ScriptIt scriptIt;

		scriptIt = (ScriptIt) super.unmarshal(reader, context);

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final Collection<CodeBlock> codeBlocks;

			codeBlocks = ((Collection<CodeBlock>) context.convertAnother(
					scriptIt, ArrayList.class));
			
			if (codeBlocks.isEmpty())
				throw new IllegalStateException(
						"Unable to read CodeBlocks for " + scriptIt);
			scriptIt.setCodeBlocks(codeBlocks);
			reader.moveUp();
		}

		return scriptIt;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(ScriptIt.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new ScriptIt("");
	}
}
