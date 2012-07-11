package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.FileIO;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.atomic.KnowIt;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts {@link CodeBlockSource}s for File I/O. Code block sources have all
 * the information about a code block, and as such they must store all of it in
 * the file.
 * 
 * @author remiller
 */
public class CodeBlockSourceConverter implements Converter {
	private static final String TAG_SUBJECT = "subject";
	private static final String TAG_SLOT = "slot";
	private static final String TAG_CODE = "Code";
	private static final String TAG_INCLUDES = "Includes";
	private static final String TAG_INCLUDE = "Include";
	private static final String TAG_PARAMETERS = "Parameters";
	private static final String TAG_TYPES = "Types";
	private static final String TAG_TYPE = "Type";
	private static final String TAG_ID = "Id";

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(CodeBlock.class);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final CodeBlock block = (CodeBlock) source;
		final Collection<String> types = block.getTypes();
		final Collection<KnowIt> parameters = block.getParameters();
		final Collection<String> includes = block.getIncludes();
		final Collection<FormatFragment> code = block.getCode();

		// make sure Very Bad Things aren't happening.
		if (FileIO.getInstance().getMode() != FileIO.IoMode.API_DICTIONARY)
			throw new XStreamException(
					"CodeBlockSources can only live in the Translator! Aaaaaaaaugh!");

		// Subject
		if (block.hasSubject())
			writer.addAttribute(TAG_SUBJECT, block.getSubject()
					.getDisplayText());

		// Slot
		if (block.hasSlot())
			writer.addAttribute(TAG_SLOT, block.getSlot());

		writer.startNode(TAG_ID);
		writer.setValue(Integer.toString(block.getId()));
		writer.endNode();

		// Types
		writer.startNode(TAG_TYPES);
		for (String type : types) {
			writer.startNode(TAG_TYPE);
			writer.setValue(type);
			writer.endNode();
		}
		writer.endNode();

		// Parameters
		if (!parameters.isEmpty()) {
			writer.startNode(TAG_PARAMETERS);
			context.convertAnother(parameters);
			writer.endNode();
		}

		// Includes
		if (!includes.isEmpty()) {
			writer.startNode(TAG_INCLUDES);
			for (String include : includes) {
				writer.startNode(TAG_INCLUDE);
				writer.setValue(include);
				writer.endNode();
			}
			writer.endNode();
		}

		// code
		if (!code.isEmpty()) {
			writer.startNode(TAG_CODE);
			context.convertAnother(code);
			writer.endNode();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		CodeBlock block = null;
		String subject;
		String slot;
		int id = -1;
		final Collection<String> includes;
		final Collection<KnowIt> parameters;
		final Collection<FormatFragment> code;
		final Collection<String> types;

		if (FileIO.getInstance().getMode() != FileIO.IoMode.API_DICTIONARY)
			throw new XStreamException(
					"CodeBlockSources can only live in the Translator! Aaaaaaaaugh!");

		includes = new ArrayList<String>();
		parameters = new ArrayList<KnowIt>();
		code = new ArrayList<FormatFragment>();
		types = new ArrayList<String>();

		subject = reader.getAttribute(TAG_SUBJECT);
		slot = reader.getAttribute(TAG_SLOT);

		if (subject == null)
			subject = "";

		if (slot == null)
			slot = "";

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			/*
			 * ID. Cannot appear in Stories; ID is for CodeBlockSources, and
			 * those must only exist in the Translator.
			 */
			if (nodeName.equals(TAG_ID)) {
				id = Integer.parseInt(reader.getValue());
			}
			// Types
			else if (nodeName.equals(TAG_TYPES)) {
				while (reader.hasMoreChildren()) {
					types.add(FileIO.readValue(reader, TAG_TYPE));
				}
			}
			// Parameters
			else if (nodeName.equals(TAG_PARAMETERS)) {
				parameters.addAll(((Collection<KnowIt>) context.convertAnother(
						block, ArrayList.class)));
			}
			/*
			 * Includes. Cannot appear in Stories; includes are game-specific
			 * and must be in the translator only.
			 */
			else if (nodeName.equals(TAG_INCLUDES)) {
				while (reader.hasMoreChildren()) {
					includes.add(FileIO.readValue(reader, TAG_INCLUDE));
				}
			}
			/*
			 * Code. Cannot appear in Stories; code is game-specific and must be
			 * in the translator only.
			 */
			else if (nodeName.equals(TAG_CODE)) {
				code.addAll(((Collection<FormatFragment>) context
						.convertAnother(block, ArrayList.class)));
			}

			reader.moveUp();
		}

		block = new CodeBlockSource(slot, slot, types, parameters, types, code,
				id);

		return block;
	}
}
