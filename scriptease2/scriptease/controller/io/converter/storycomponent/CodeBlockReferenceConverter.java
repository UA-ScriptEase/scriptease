package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.FileIO;
import scriptease.controller.io.FileIO.IoMode;
import scriptease.model.CodeBlockReference;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
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
 * allowed to exist in Stories as well as in the API dictionary.<br>
 * <br>
 * Story-side References store their target's ID number, but Translator-side
 * References store the actual target.
 * 
 * @author remiller
 */
public class CodeBlockReferenceConverter extends StoryComponentConverter
		implements Converter {
	public static final String TAG_CODE_BLOCK_REF = "CodeBlockReference";

	private static final String TAG_PARAMETERS = "Parameters";
	private static final String TAG_TARGET = "Target";
	private static final String TAG_TARGET_ID = "TargetId";

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
		final IoMode ioMode = FileIO.getInstance().getMode();

		super.marshal(source, writer, context);

		if (ioMode == IoMode.STORY) {
			writer.startNode(TAG_TARGET_ID);
			writer.setValue(Integer.toString(codeBlock.getId()));
			writer.endNode();
		} else if (ioMode == IoMode.LIBRARY) {
			writer.startNode(TAG_TARGET);
			writer.startNode(CodeBlockSourceConverter.TAG_CODE_BLOCK_SOURCE);
			context.convertAnother(codeBlock.getTarget());
			writer.endNode();
			writer.endNode();
		} else
			throw new XStreamException(
					"IO Mode is not a story or API dictionary while writing code block reference.");

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
		final CodeBlockReference block = (CodeBlockReference) super.unmarshal(
				reader, context);
		final Collection<KnowIt> parameters;
		final int targetId;
		final CodeBlockSource target;
		final Translator translator;
		final IoMode ioMode = FileIO.getInstance().getMode();
		String nodeName;

		translator = TranslatorManager.getInstance().getActiveTranslator();

		// CodeBlockSource Target
		reader.moveDown();
		nodeName = reader.getNodeName();

		if (ioMode == IoMode.STORY) {
			if (!nodeName.equals(TAG_TARGET_ID))
				dieMissingTarget(TAG_TARGET_ID, nodeName);

			targetId = Integer.parseInt(reader.getValue());

			target = translator.getApiDictionary().getCodeBlockByID(targetId);
		} else if (ioMode == IoMode.LIBRARY) {
			if (!nodeName.equals(TAG_TARGET))
				dieMissingTarget(TAG_TARGET, nodeName);

			target = (CodeBlockSource) context.convertAnother(block,
					CodeBlockSource.class);
		} else
			throw new XStreamException(
					"Reading CodeBlock but not in story or API mode.");

		if (target == null)
			System.err.println("Failed to read target information for \""
					+ block + "\", nulling the reference.");

		block.setTarget(target);
		reader.moveUp();

		// the parameter list doesn't exist if there aren't any, so look out for
		// that.
		if (reader.hasMoreChildren()) {
			parameters = new ArrayList<KnowIt>();

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
