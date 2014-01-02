package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.FileIO;
import scriptease.controller.io.XMLNode;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

import com.thoughtworks.xstream.XStreamException;
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
public class CodeBlockSourceConverter extends StoryComponentConverter {
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final CodeBlock block = (CodeBlock) source;
		final Collection<String> types = block.getTypes();
		final Collection<KnowIt> parameters = block.getParameters();
		final Collection<String> includes = block.getIncludes();
		final Collection<AbstractFragment> code = block.getCode();

		// make sure Very Bad Things aren't happening.
		if (FileIO.getInstance().getMode() != FileIO.IoMode.LIBRARY)
			throw new XStreamException(
					"CodeBlockSources can only live in the Translator! Aaaaaaaaugh!");

		super.marshal(source, writer, context);

		if (block.hasSubject()) {
			XMLNode.SUBJECT.writeString(writer, block.getSubjectName());
		}

		if (block.hasSlot()) {
			XMLNode.SLOT.writeString(writer, block.getSlot());
		}

		XMLNode.ID.writeInteger(writer, block.getId());

		XMLNode.TYPES.writeChildren(writer, types);

		if (!parameters.isEmpty()) {
			XMLNode.PARAMETERS.writeObject(writer, context, parameters);
		}

		if (!includes.isEmpty()) {
			XMLNode.INCLUDES.writeChildren(writer, includes);
		}

		XMLNode.CODE.writeObject(writer, context, code);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final CodeBlockSource block = (CodeBlockSource) super.unmarshal(reader,
				context);
		String subject = "";
		String slot = "";
		int id = -1;
		final Collection<String> includes;
		final Collection<KnowIt> parameters;
		final Collection<AbstractFragment> code;
		final Collection<String> types;

		final FileIO.IoMode mode = FileIO.getInstance().getMode();
		if (mode != FileIO.IoMode.LIBRARY)
			throw new XStreamException(
					"CodeBlockSources can only live in the Translator! Aaaaaaaaugh!");

		includes = new ArrayList<String>();
		parameters = new ArrayList<KnowIt>();
		code = new ArrayList<AbstractFragment>();
		types = new ArrayList<String>();

		// Since all of these are optional, this is the most efficient way to
		// read the data.
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			// subject
			if (nodeName.equals(XMLNode.SUBJECT.getName())) {
				subject = reader.getValue();

				if (subject == null)
					subject = "";
			}
			// slot
			else if (nodeName.equals(XMLNode.SLOT.getName())) {
				slot = reader.getValue();

				if (slot == null)
					slot = "";
			}
			/*
			 * ID. Cannot appear in Stories; ID is for CodeBlockSources, and
			 * those must only exist in the Translator.
			 */
			else if (nodeName.equalsIgnoreCase(XMLNode.ID.getName())) {
				id = Integer.parseInt(reader.getValue());
			}
			// Types
			else if (nodeName.equals(XMLNode.TYPES.getName())) {
				while (reader.hasMoreChildren()) {
					types.add(XMLNode.TYPE.readString(reader));
				}
			}
			// Parameters
			else if (nodeName.equals(XMLNode.PARAMETERS.getName())) {
				parameters.addAll(((Collection<KnowIt>) context.convertAnother(
						block, ArrayList.class)));
			}
			/*
			 * Includes. Cannot appear in Stories; includes are game-specific
			 * and must be in the translator only.
			 */
			else if (nodeName.equals(XMLNode.INCLUDES.getName())) {
				while (reader.hasMoreChildren()) {
					includes.add(XMLNode.INCLUDE.readString(reader));
				}
			}
			/*
			 * Code. Cannot appear in Stories; code is game-specific and must be
			 * in the translator only.
			 */
			else if (nodeName.equals(XMLNode.CODE.getName())) {
				code.addAll(((Collection<AbstractFragment>) context
						.convertAnother(block, ArrayList.class)));
			}

			reader.moveUp();
		}

		block.setId(id);
		block.setSubject(subject);
		block.setSlot(slot);
		block.setTypesByName(types);
		for (KnowIt parameter : parameters) {
			block.addParameter(parameter);
		}
		block.setIncludes(includes);
		block.setCode(code);

		return block;
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new CodeBlockSource();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(CodeBlockSource.class);
	}

}
