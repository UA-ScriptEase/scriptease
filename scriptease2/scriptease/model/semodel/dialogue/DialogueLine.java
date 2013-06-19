package scriptease.model.semodel.dialogue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.model.semodel.StoryModel;
import scriptease.translator.io.model.EditableResource;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;

/**
 * 
 * Represents a dialogue line resource. This is translator independent. The
 * translator handles these in its {@link GameModule} implementation.
 * 
 * TODO Comment this class further
 * 
 * @author kschenk
 * 
 */
public final class DialogueLine extends EditableResource {
	private static final String DEFAULT_DIALOGUE = "New Dialogue Line";

	private final StoryModel story;

	private boolean enabled;
	private Resource image;
	private Resource audio;

	public DialogueLine(StoryModel story) {
		this(story, new ArrayList<DialogueLine>());
	}

	public DialogueLine(StoryModel story, List<DialogueLine> parents) {
		this(DEFAULT_DIALOGUE, true, null, null, new ArrayList<DialogueLine>(),
				story, parents);
	}

	public DialogueLine(String dialogue, boolean enabled, Resource image,
			Resource audio, List<DialogueLine> children, StoryModel story,
			List<DialogueLine> parents) {
		this.enabled = enabled;
		this.image = image;
		this.audio = audio;
		this.story = story;

		this.setName(dialogue);
	}

	@Override
	public List<DialogueLine> getChildren() {
		// TODO There has to be a better way to do this..
		final List<DialogueLine> children = new ArrayList<DialogueLine>();

		for (EditableResource resource : super.getChildren()) {
			if (resource instanceof DialogueLine) {
				children.add((DialogueLine) resource);
			} else
				throw new IllegalStateException(
						"Encountered non-dialogue line " + resource
								+ " in children of " + this);
		}

		return children;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean setImage(Resource image) {
		final boolean setImage;

		setImage = image.getTypes().contains(
				this.story.getModule().getImageType());

		if (setImage)
			this.image = image;

		return setImage;
	}

	public boolean setAudio(Resource audio) {
		final boolean setAudio;

		setAudio = audio.getTypes().contains(
				this.story.getModule().getAudioType());

		if (setAudio)
			this.audio = audio;

		return setAudio;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public Resource getAudio() {
		return this.audio;
	}

	public Resource getImage() {
		return this.image;
	}

	@Override
	public Collection<String> getTypes() {
		final Collection<String> type = new ArrayList<String>();

		if (this.story.getDialogueRoots().contains(this))
			type.add(this.story.getModule().getDialogueType());
		else
			type.add(this.story.getModule().getDialogueLineType());
		return type;
	}

	// TODO TODO TODO!!!!!!!
	// One or more of these methods may need to be changed.

	@Override
	public Resource getOwner() {
		// TODO Return the speaker?
		return super.getOwner();
	}

	@Override
	public String getOwnerName() {
		// TODO Return the speaker name
		return super.getOwnerName();
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
