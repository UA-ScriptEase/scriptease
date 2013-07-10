package scriptease.controller.io.converter.storycomponent;

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
 * @author kschenk
 * @author jyuen
 * 
 * @see ComplexStoryComponentConverter
 */
public class ScriptItConverter extends ComplexStoryComponentConverter {

	// TODO See LibraryModelConverter class for an example of how to refactor
	// this class. However, since we're moving to YAML eventually, we don't need
	// to waste anymore time on refactoring these.
	public static final String TAG_SCRIPTIT = "ScriptIt";
	private static final String TAG_CODEBLOCKS = "CodeBlocks";

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

	/**
	 * This goes through the entire ScriptIt, looking for
	 * KnowItBindingReferences in all of the nooks and crannies. Then we check
	 * if these are supposed to be implicits, in which case we replace them with
	 * actual implicits. Before, we were generating new knowits to be bound
	 * here, which obliterated code gen..
	 * 
	 * @param scriptIt
	 */

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
