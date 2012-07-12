package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.FileIO;
import scriptease.model.CodeBlockReference;
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
 * parameter list, and a target that they must rebind to.
 * 
 * @author remiller
 */
public class CodeBlockReferenceConverter extends StoryComponentConverter
		implements Converter {
	private static final String TAG_PARAMETERS = "Parameters";
	private static final String TAG_TARGET = "Target";

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

		writer.startNode(TAG_TARGET);
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
		final CodeBlockReference block = (CodeBlockReference) super.unmarshal(
				reader, context);
		final Collection<KnowIt> parameters;
		int targetId = -1;
		final Translator translator;

		if (FileIO.getInstance().getMode() != FileIO.IoMode.STORY)
			throw new XStreamException(
					"CodeBlockReferences can only live in stories! Aaaaaaaaugh!");

		parameters = new ArrayList<KnowIt>();
		translator = TranslatorManager.getInstance().getActiveTranslator();

		// CodeBlock Source Target
		reader.moveDown();
		if (!reader.getNodeName().equals(TAG_TARGET))
			throw new XStreamException(
					"CodeBlockReference missing target information!");
		targetId = Integer.parseInt(reader.getValue());
		reader.moveUp();

		// the parameter list doesn't show if there aren't any, so look out for that.
		if (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			// Parameters
			if (nodeName.equals(TAG_PARAMETERS)) {
				parameters.addAll((Collection<KnowIt>) context.convertAnother(
						block, ArrayList.class));
			}
			reader.moveUp();
		}

		block.setTarget(translator.getApiDictionary()
				.getCodeBlockByID(targetId));
		block.setBindings(parameters);

		return block;
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new CodeBlockReference();
	}
}
