package scriptease.model.semodel.dialogue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.translator.io.model.Resource;

public final class DialogueLine extends Resource {
	private final List<DialogueLine> children;
	private final String type;

	private String dialogue;
	private boolean enabled;
	private Resource image;
	private Resource audio;

	public DialogueLine(String type) {
		this("", true, null, null, new ArrayList<DialogueLine>(), type);
	}

	public DialogueLine(String dialogue, boolean enabled, Resource image,
			Resource audio, List<DialogueLine> children, String type) {
		this.dialogue = dialogue;
		this.enabled = enabled;
		this.image = image;
		this.audio = audio;
		this.children = children;
		this.type = type;
	}

	public boolean removeChild(DialogueLine dialogueLine) {
		return this.children.remove(dialogueLine);
	}

	public boolean addChild(DialogueLine dialogueLine) {
		return this.children.add(dialogueLine);
	}

	@Override
	public List<DialogueLine> getChildren() {
		return this.children;
	}

	public void setDialogue(String dialogue) {
		this.dialogue = dialogue;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean setImage(Resource image) {
		final boolean setImage;

		setImage = image.getTypes() // TODO Get the thing below
				.contains("Get the translator's image type.");

		if (setImage)
			this.image = image;

		return setImage;
	}

	public boolean setAudio(Resource audio) {
		final boolean setAudio;

		setAudio = audio.getTypes() // TODO Get the thing below
				.contains("Get the translator's audio type.");

		if (setAudio)
			this.audio = audio;

		return setAudio;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public String getDialogue() {
		return this.dialogue;
	}

	public Resource getAudio() {
		return this.audio;
	}

	public Resource getImage() {
		return this.image;
	}

	// TODO TODO TODO!!!!!!!
	// One or more of these methods may need to be changed.

	@SuppressWarnings("serial")
	@Override
	public Collection<String> getTypes() {
		return new ArrayList<String>() {
			{
				this.add(DialogueLine.this.type);
			}
		};
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.getDialogue();
	}

	@Override
	public String getTag() {
		// TODO Auto-generated method stub
		return this.getName();
	}

	@Override
	public String getTemplateID() {
		// TODO Auto-generated method stub
		return this.getName();
	}

	@Override
	public String getCodeText() {
		// TODO Auto-generated method stub
		return this.getName();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return this == obj;
	}
}
