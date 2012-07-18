package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.FileIO;
import scriptease.controller.io.FileIO.IoMode;
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
 * Converts {@link CodeBlock}s in File I/O. This is kept around as legacy
 * support, and should be removed when we are certain that there are no
 * important Stories left that have old CodeBlocks (from before July 2012) in
 * their Story file. - remiller
 * 
 * @author mfchurch
 * @author remiller
 */
@Deprecated
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
		throw new XStreamException(
				"CodeBlocks Should not get written directly, but rather should be written as a Source or Reference",
				new UnsupportedOperationException(
						"Can't write CodeBlocks, only their subclasses."));
	}

	private static int tempId = 0;

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		CodeBlock codeBlock = null;
		String subject;
		String slot;
		Collection<String> includes = new ArrayList<String>(0);
		Collection<KnowIt> parameters = new ArrayList<KnowIt>(0);
		Collection<FormatFragment> code = new ArrayList<FormatFragment>(0);
		Collection<String> types = new ArrayList<String>(0);

		subject = reader.getAttribute(TAG_SUBJECT);
		if (subject == null)
			subject = "";

		slot = reader.getAttribute(TAG_SLOT);
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
			}
			// Parameters
			else if (nodeName.equals(TAG_PARAMETERS)) {
				parameters = ((Collection<KnowIt>) context.convertAnother(
						codeBlock, ArrayList.class));
			}
			// Includes (optional since stories don't need to store includes)
			else if (nodeName.equals(TAG_INCLUDES)) {
				while (reader.hasMoreChildren()) {
					includes.add(FileIO.readValue(reader, TAG_INCLUDE));
				}
			}
			// Code (optional since stories don't need to store code)
			else if (nodeName.equals(TAG_CODE)) {
				code = ((Collection<FormatFragment>) context.convertAnother(
						codeBlock, ArrayList.class));
			}
			reader.moveUp();
		}

		if (FileIO.getInstance().getMode() == IoMode.STORY) {
			// we can't determine precisely enough which codeblock should be
			// referenced. There just isn't enough uniqueness in the data stored
			// in stories. - remiller
			throw new UnsupportedOperationException(
					"Can't fix stories with old CodeBlocks - not enough data. Sorry!");
		} else if (FileIO.getInstance().getMode() == IoMode.API_DICTIONARY) {
			codeBlock = new CodeBlockSource(subject, slot, types, parameters,
					includes, code, tempId++);
		} else {
			throw new XStreamException(
					"Why are there code blocks in something that isn't a story or API dictionary? "
							+ "How did this even happen?");
		}

		return codeBlock;
	}
}
