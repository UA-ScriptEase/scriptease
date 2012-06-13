package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.FileIO;
import scriptease.model.CodeBlock;
import scriptease.model.atomic.KnowIt;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts Codeblocks for File I/O.
 * 
 * @author mfchurch
 * 
 */
public class CodeBlockConverter implements Converter {
	private static final String TAG_SUBJECT = "subject";
	private static final String TAG_SLOT = "slot";
	private static final String TAG_CODE = "Code";
	private static final String TAG_INCLUDES = "Includes";
	private static final String TAG_INCLUDE = "Include";
	private static final String TAG_PARAMETERS = "Parameters";
	private static final String TAG_TYPES = "Types";
	private static final String TAG_TYPE = "Type";

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(CodeBlock.class);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final CodeBlock codeBlock = (CodeBlock) source;
		final String subject;
		if (codeBlock.hasSubject())
			subject = codeBlock.getSubject().getDisplayText();
		else
			subject = null;
		final String slot;
		if (codeBlock.hasSlot())
			slot = codeBlock.getSlot();
		else
			slot = null;
		final Collection<String> types = codeBlock.getTypes();
		final Collection<String> includes = codeBlock.getIncludes();
		final Collection<KnowIt> parameters = codeBlock.getParameters();
		
		//TODO This doesn't do anything. Should it? Could it? Would it?
		final Collection<FormatFragment> code = codeBlock.getCode();

		// Subject
		if (subject != null)
			writer.addAttribute(TAG_SUBJECT, subject);

		// Slot
		if (slot != null)
			writer.addAttribute(TAG_SLOT, slot);

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

		// No need to write code and includes to Stories since they are already
		// stored in the ApiDictionary <-- This is wrong. 
		//if (FileIO.getInstance().getMode() == IoMode.API_DICTIONARY) {
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
/*
			writer.startNode(TAG_CODE);
			context.convertAnother(code);
			writer.endNode();*/
	//	}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		CodeBlock codeBlock = null;
		Collection<String> includes = new ArrayList<String>(0);
		Collection<KnowIt> parameters = new ArrayList<KnowIt>(0);
		Collection<FormatFragment> code = new ArrayList<FormatFragment>(0);
		Collection<String> types = new ArrayList<String>(0);

		String subject = reader.getAttribute(TAG_SUBJECT);
		if (subject == null)
			subject = "";
		String slot = reader.getAttribute(TAG_SLOT);
		if (slot == null)
			slot = "";

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final String nodeName = reader.getNodeName();

			// Types
			if (nodeName.equals(TAG_TYPES)) {
				while (reader.hasMoreChildren()) {
					types.add(FileIO.readValue(reader, TAG_TYPE));
				}
			} else
			// Parameters
			if (nodeName.equals(TAG_PARAMETERS)) {
				parameters = ((Collection<KnowIt>) context.convertAnother(
						codeBlock, ArrayList.class));
			} else

			// Includes (optional since stories don't need to store includes)
			if (nodeName.equals(TAG_INCLUDES)) {
				while (reader.hasMoreChildren()) {
					includes.add(FileIO.readValue(reader, TAG_INCLUDE));
				}
			} else
			// Code (optional since stories don't need to store code)
			if (nodeName.equals(TAG_CODE)) {
				code = ((Collection<FormatFragment>) context.convertAnother(
						codeBlock, ArrayList.class));
			}
			reader.moveUp();
		}

		codeBlock = new CodeBlock(subject, slot, types, includes, parameters,
				code);

		return codeBlock;
	}
}
