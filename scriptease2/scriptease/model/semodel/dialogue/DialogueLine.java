package scriptease.model.semodel.dialogue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.StoryModel;
import scriptease.translator.io.model.EditableResource;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;

/**
 * 
 * Represents a dialogue line resource. This is translator independent. The
 * translator handles these in its {@link GameModule} implementation. The first
 * line in a Dialogue Line tree will always be a Dialogue that represents the
 * conversation as a whole. The rest of the lines alternate between true and
 * false.
 * 
 * 
 * @author kschenk
 * 
 */
public final class DialogueLine extends EditableResource {
	public static enum Speaker {
		PC, NPC
	}

	private static int uniqueNumberCount = 0;

	private static final String DEFAULT_DIALOGUE = "New Dialogue Line";

	private final int uniqueID;

	private Speaker speaker;
	private final StoryModel story;

	private boolean enabled;
	private KnowIt image;
	private KnowIt audio;

	/**
	 * Creates a new dialogue line for the story.
	 * 
	 * @param story
	 */
	public DialogueLine(StoryModel story) {
		this(story, null);
	}

	/**
	 * Creates a new dialogue line for the story with the speaker.
	 * 
	 * @param story
	 * @param speaker
	 */
	public DialogueLine(StoryModel story, Speaker speaker) {
		this(story, speaker, DEFAULT_DIALOGUE, true, null, null,
				new ArrayList<Resource>());
	}

	/**
	 * Creates a new dialouge line with the passed in parameters.
	 * 
	 * @param story
	 *            The story to create the dialogue line for * @param speaker The
	 *            speaker of the line.
	 * @param dialogue
	 *            The text to be displayed
	 * 
	 * @param enabled
	 *            Whether the dialogue line is enabled or disabled by default
	 * @param image
	 *            The image attached to the line
	 * @param audio
	 *            Audio attached to the line
	 * @param children
	 *            Any dialogue line children attached.
	 */
	public DialogueLine(StoryModel story, Speaker speaker, String dialogue,
			boolean enabled, KnowIt image, KnowIt audio, List<Resource> children) {
		super(dialogue, children);

		this.enabled = enabled;
		this.speaker = speaker;
		this.story = story;

		if (audio == null)
			this.audio = new KnowIt("Audio", this.story.getModule()
					.getAudioType());
		else
			this.audio = audio;
		if (image == null)
			this.image = new KnowIt("Image", this.story.getModule()
					.getImageType());
		else
			this.image = image;

		this.uniqueID = uniqueNumberCount++;

		// Set up the speakers
		for (Resource child : children) {
			if (child instanceof DialogueLine)
				this.setChildSpeaker((DialogueLine) child);
		}
	}

	public int getUniqueID() {
		return this.uniqueID;
	}

	private void setChildSpeaker(DialogueLine child) {
		if (this.speaker == Speaker.PC)
			child.speaker = Speaker.NPC;
		else if (this.speaker == Speaker.NPC)
			child.speaker = Speaker.PC;
		else
			throw new IllegalStateException("Dialogue Line " + this
					+ " has illegal speaker: " + this.speaker);

		for (Resource childChild : child.getChildren()) {
			if (childChild instanceof DialogueLine)
				child.setChildSpeaker((DialogueLine) childChild);
		}
	}

	@Override
	public List<DialogueLine> getChildren() {
		// TODO There has to be a better way to do this..
		final List<DialogueLine> children = new ArrayList<DialogueLine>();

		for (Resource resource : super.getChildren()) {
			if (resource instanceof DialogueLine) {
				children.add((DialogueLine) resource);
			} else
				throw new IllegalStateException(
						"Encountered non-dialogue line " + resource
								+ " in children of " + this);
		}

		return children;
	}

	/**
	 * Set the default enabled state of the dialogue line.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Set the image that will be displayed in game by the dialogue line.
	 * 
	 * @param image
	 * @return
	 */
	public boolean setImage(Resource image) {
		final boolean setImage;

		setImage = image.getTypes().contains(
				this.story.getModule().getImageType());

		if (setImage)
			this.image.setBinding(image);

		return setImage;
	}

	/**
	 * Set the audio that will be played in game by the dialogue line.
	 * 
	 * @param audio
	 * @return
	 */
	public boolean setAudio(Resource audio) {
		final boolean setAudio;

		setAudio = audio.getTypes().contains(
				this.story.getModule().getAudioType());

		if (setAudio)
			this.audio.setBinding(audio);

		return setAudio;
	}

	/**
	 * Returns whether the dialogue line is enabled by default.
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	public Speaker getSpeaker() {
		return this.speaker;
	}

	/**
	 * Returns the audio that will be played in game by the dialogue line.
	 * 
	 * @return
	 */
	public KnowIt getAudio() {
		return this.audio;
	}

	/**
	 * Returns the image that will be displayed in game by the dialogue line.
	 * 
	 * @return
	 */
	public KnowIt getImage() {
		return this.image;
	}

	@Override
	public boolean isRoot() {
		return this.story.getDialogueRoots().contains(this);
	}

	@Override
	public boolean addChild(Resource child) {
		if (!(child instanceof DialogueLine))
			return false;

		final DialogueLine childLine = (DialogueLine) child;

		if (childLine.speaker == null)
			this.setChildSpeaker(childLine);

		return ((DialogueLine) child).speaker != this.speaker
				&& super.addChild(child);
	}

	@Override
	public Collection<String> getTypes() {
		final Collection<String> type = new ArrayList<String>();

		if (this.isRoot())
			type.add(this.story.getModule().getDialogueType());
		else
			type.add(this.story.getModule().getDialogueLineType());
		return type;
	}

	// TODO TODO TODO!!!!!!!
	// One or more of these methods may need to be changed.

	@Override
	public Resource getOwner() {
		// TODO Return the speaker? Or don't use it?
		return super.getOwner();
	}

	@Override
	public String getOwnerName() {
		// TODO Should return alternating Speaker 1 and Speaker 2.
		return super.getOwnerName();
	}

	@Override
	public String getTag() {
		// TODO Will tags need to be different..?
		return this.getName();
	}

	@Override
	public String getTemplateID() {
		// TODO Does this need to be uniquer? Yes it does!
		return this.getName();
	}

	@Override
	public String getCodeText() {
		return this.getName();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO This is wrong. Do we need to override equals, though..?
		return this == obj;
	}
}
