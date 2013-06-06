package scriptease.model.semodel.dialogue;

import java.util.ArrayList;
import java.util.List;

import scriptease.translator.io.model.Resource;

public abstract class DialogueLine extends Resource {
	private final List<DialogueLine> children;

	private String dialogue;
	private boolean enabled;
	private Resource image;
	private Resource audio;

	public DialogueLine() {
		this("", true, null, null, new ArrayList<DialogueLine>());
	}

	public DialogueLine(String dialogue, boolean enabled, Resource image,
			Resource audio, List<DialogueLine> children) {
		this.dialogue = dialogue;
		this.enabled = enabled;
		this.image = image;
		this.audio = audio;
		this.children = children;
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
}
