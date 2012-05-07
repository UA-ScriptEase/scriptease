package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.model.CodeBlock;
import scriptease.model.LibraryManager;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

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

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final ScriptIt scriptIt = (ScriptIt) source;
		super.marshal(source, writer, context);

		// CodeBlocks
		//regular codeblocks...they make up 99% of the codeblocks that are in ScriptEaseII
			if(scriptIt.getCodeBlocks().size() == 1){
				writer.startNode(TAG_CODEBLOCKS);
				context.convertAnother(scriptIt.getCodeBlocks());
				writer.endNode();
			}
		
			//special cases, in the current NWN their are a few scriptits like this
			//its possible their will be more later in future translators
			else{
				int i =0;
				writer.startNode(TAG_CODEBLOCKS);
				for(CodeBlock cb : scriptIt.getCodeBlocks()){
					if(i == 0){
						writer.startNode("CodeBlock");
						context.convertAnother(cb);
						writer.endNode();
					}
					if(i == 1){
						writer.startNode("CodeBlock");
						context.convertAnother(cb);
							writer.startNode("Code");
							context.convertAnother(cb.getCode());
							writer.endNode();
						writer.endNode();
					}
					i++;
				}
				writer.endNode();
			}
	
	
	
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final ScriptIt scriptIt;

		scriptIt = (ScriptIt) super.unmarshal(reader, context);
		
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			Collection<CodeBlock> codeBlocks = ((Collection<CodeBlock>) context
					.convertAnother(scriptIt, ArrayList.class));
			if (codeBlocks.isEmpty())
				throw new IllegalStateException(
						"Unable to read CodeBlocks for " + scriptIt);
			scriptIt.setCodeBlocks(codeBlocks);
			reader.moveUp();
		}
		
		//Does this need to be here? 
		for (CodeBlock codeBlock : scriptIt.getCodeBlocks()) {
			Collection<FormatFragment> code = codeBlock.getCode();
			// Story's don't save code, so grab the codeblocks from a similar
			// scriptIt in the library
			if (code.isEmpty()) {
				LibraryManager.getInstance().getCodeForScriptIt(scriptIt, codeBlock);
				break;
			}
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
