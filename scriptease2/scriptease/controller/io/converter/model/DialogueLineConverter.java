package scriptease.controller.io.converter.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.io.XMLNode;
import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.model.semodel.dialogue.DialogueLine.Speaker;
import scriptease.translator.io.model.EditableResource;
import scriptease.translator.io.model.Resource;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This converts {@link DialogueLine}s. Eventually we may need a converter for
 * {@link EditableResource}s, which will use parts of this. This converter will
 * then extend that one.
 * 
 * @author kschenk
 * 
 */
public class DialogueLineConverter implements Converter {
	public static final String TAG_DIALOGUE = "DialogueLine";
	private static final String TAG_CHILDREN = "Children";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final DialogueLine line = (DialogueLine) source;

		XMLNode.NAME.writeString(writer, line.getName());
		XMLNode.ID.writeInteger(writer, line.getUniqueID());
		XMLNode.CHILDREN.writeObject(writer, context, line.getChildren());
		XMLNode.ENABLED.writeBoolean(writer, line.isEnabled());
		XMLNode.IMAGE.writeObject(writer, context, line.getImage());
		XMLNode.AUDIO.writeObject(writer, context, line.getAudio());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final DialogueLine line;
		final String name;
		final int id;
		final List<Resource> children;
		final boolean enabled;

		final KnowIt image;
		final KnowIt audio;

		children = new ArrayList<Resource>();

		name = XMLNode.NAME.readString(reader);
		id = Integer.parseInt(XMLNode.ID.readString(reader));

		// TODO This isn't the way we should be loading stuff from XML. We need
		// to use XMLNode methods
		reader.moveDown();
		if (reader.hasMoreChildren())
			if (!reader.getNodeName().equalsIgnoreCase(TAG_CHILDREN)) {
				throw new ConversionException("Expected child list but found "
						+ reader.getNodeName());
			} else {
				children.addAll((Collection<DialogueLine>) context
						.convertAnother(null, ArrayList.class));
			}
		reader.moveUp();

		enabled = Boolean.parseBoolean(XMLNode.ENABLED.readString(reader));

		image = XMLNode.IMAGE.readObject(reader, context, KnowIt.class);
		audio = XMLNode.AUDIO.readObject(reader, context, KnowIt.class);

		line = new DialogueLine(StoryModelConverter.currentStory, Speaker.PC,
				name, id, enabled, image, audio, children);

		return line;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(DialogueLine.class);
	}
}
