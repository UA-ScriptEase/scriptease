package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import scriptease.controller.io.FileIO;
import scriptease.controller.io.XMLNode;
import scriptease.gui.WindowFactory;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockReference;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.Note;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.util.StringOp;

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
	protected static final Map<CodeBlockReference, Integer> refMap = new HashMap<CodeBlockReference, Integer>();

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
		final ScriptIt scriptIt = (ScriptIt) super.unmarshal(reader, context);

		reader.moveDown();
		if (reader.hasMoreChildren()) {
			if (reader.getNodeName().equalsIgnoreCase(
					XMLNode.CODEBLOCKS.getName())) {
				final Collection<CodeBlock> codeBlocks;

				CodeBlockReferenceConverter.owner = scriptIt;

				codeBlocks = ((Collection<CodeBlock>) context.convertAnother(
						scriptIt, ArrayList.class));

				CodeBlockReferenceConverter.owner = null;

				if (codeBlocks.isEmpty())
					throw new IllegalStateException(
							"Unable to read CodeBlocks for " + scriptIt);

				// This checks if the code blocks all belong to the scriptIt's
				// library. May need to add more cases as we go on.
				for (CodeBlock codeBlock : codeBlocks) {
					final LibraryModel codeBlockLibrary;
					final LibraryModel scriptItLibrary;

					scriptItLibrary = scriptIt.getLibrary();
					codeBlockLibrary = codeBlock.getLibrary();

					if ((FileIO.getInstance().getMode() == FileIO.IoMode.STORY)
							&& scriptItLibrary != codeBlockLibrary
							&& codeBlock instanceof CodeBlockReference
							&& ((CodeBlockReference) codeBlock).getTarget()
									.getOwner().isEquivalent(scriptIt)) {
						final String scriptItName;
						scriptItName = scriptIt.getDisplayText();

						if (WindowFactory
								.getInstance()
								.showYesNoConfirmDialog(
										"<html>The component <b>\""
												+ StringOp
														.makeXMLSafe(scriptItName)
												+ "\"</b> was not found in <b>"
												+ scriptItLibrary
												+ "</b>. <br>However, a component with the same name was found in <b>"
												+ codeBlockLibrary
												+ "</b>.<br><br> Would you like to replace the missing component with this one? "
												+ "<br>"
												+ codeBlockLibrary
												+ " will be added to your story if it is not already attached.</html>",
										"Component Not Found"))

							scriptIt.setLibrary(codeBlockLibrary);
						else {
							reader.moveUp();
							return new Note(LibraryModel.getNonLibrary(),
									"Missing Component in Library: "
											+ scriptIt.getDisplayText());
						}
					}
				}
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
			UnmarshallingContext context, LibraryModel library) {
		return new ScriptIt(library, "");
	}
}
