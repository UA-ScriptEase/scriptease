package scriptease.controller.io.converter.model;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.io.XMLNode;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.translator.io.model.EditableResource;
import scriptease.translator.io.model.GameModule;
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
		final Resource image = line.getImage();
		final Resource audio = line.getAudio();
		final String imageName;
		final String audioName;

		if (image != null)
			imageName = image.getName();
		else
			imageName = null;
		if (audio != null)
			audioName = audio.getName();
		else
			audioName = null;

		XMLNode.NAME.writeString(writer, line.getName());
		XMLNode.CHILDREN.writeObject(writer, context, line.getChildren());
		XMLNode.ENABLED.writeBoolean(writer, line.isEnabled());
		XMLNode.IMAGE.writeString(writer, imageName);
		XMLNode.AUDIO.writeString(writer, audioName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final DialogueLine line;
		final String name;
		final Collection<DialogueLine> children;
		final boolean enabled;
		final String imageTemplateID;
		final String audioTemplateID;
		final GameModule currentModule;

		final Resource image;
		final Resource audio;

		line = new DialogueLine(StoryModelConverter.currentStory);

		name = XMLNode.NAME.readString(reader);

		// TODO This isn't the way we should be loading stuff from XML. We need
		// to use XMLNode methods
		reader.moveDown();
		if (reader.hasMoreChildren())
			if (!reader.getNodeName().equalsIgnoreCase(TAG_CHILDREN)) {
				throw new ConversionException("Expected child list but found "
						+ reader.getNodeName());
			} else {
				children = (Collection<DialogueLine>) context.convertAnother(
						line, ArrayList.class);
				line.addChildren(children);
			}
		reader.moveUp();

		enabled = Boolean.parseBoolean(XMLNode.ENABLED.readString(reader));

		imageTemplateID = XMLNode.IMAGE.readString(reader);
		audioTemplateID = XMLNode.AUDIO.readString(reader);

		currentModule = StoryModelConverter.currentStory.getModule();

		image = currentModule.getInstanceForObjectIdentifier(imageTemplateID);
		audio = currentModule.getInstanceForObjectIdentifier(audioTemplateID);

		line.setName(name);
		if (audio != null)
			line.setAudio(audio);
		if (image != null)
			line.setImage(image);
		line.setEnabled(enabled);

		return line;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(DialogueLine.class);
	}
}
