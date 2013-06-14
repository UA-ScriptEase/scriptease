package scriptease.model.semodel.dialogue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;

/**
 * 
 * Represents a dialogue line resource. This is translator independent. The
 * translator handles these in its {@link GameModule} implementation.
 * 
 * TODO Comment this class furtehr
 * 
 * @author kschenk
 * 
 */
public final class DialogueLine extends Resource {
	private static final String DEFAULT_DIALOGUE = "New Dialogue Line";

	private final List<DialogueLine> parents;
	private final List<DialogueLine> children;
	private final GameModule module;

	private String dialogue;
	private boolean enabled;
	private Resource image;
	private Resource audio;

	public DialogueLine(GameModule module) {
		this(module, new ArrayList<DialogueLine>());
	}

	public DialogueLine(GameModule module, List<DialogueLine> parents) {
		this(DEFAULT_DIALOGUE, true, null, null, new ArrayList<DialogueLine>(),
				module, parents);
	}

	public DialogueLine(String dialogue, boolean enabled, Resource image,
			Resource audio, List<DialogueLine> children, GameModule module,
			List<DialogueLine> parents) {
		this.dialogue = dialogue;
		this.enabled = enabled;
		this.image = image;
		this.audio = audio;
		this.children = children;
		this.module = module;
		this.parents = parents;
	}

	public boolean removeChild(DialogueLine dialogueLine) {
		dialogueLine.parents.remove(this);
		return this.children.remove(dialogueLine);
	}

	public boolean addChild(DialogueLine dialogueLine) {
		dialogueLine.parents.add(this);
		return this.children.add(dialogueLine);
	}

	public boolean isRoot() {
		return this.parents == null || this.parents.isEmpty();
	}

	public Collection<DialogueLine> getParents() {
		return this.parents;
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

		setImage = image.getTypes().contains(this.module.getImageType());

		if (setImage)
			this.image = image;

		return setImage;
	}

	public boolean setAudio(Resource audio) {
		final boolean setAudio;

		setAudio = audio.getTypes().contains(this.module.getAudioType());

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

	@Override
	public Collection<String> getTypes() {
		final Collection<String> type = new ArrayList<String>();

		if (this.isRoot())
			type.add(this.module.getDialogueType());
		else
			type.add(this.module.getDialogueLineType());

		System.out.println(type);
		return type;
	}

	@Override
	public Resource getOwner() {
		// TODO Auto-generated method stub
		return super.getOwner();
	}

	@Override
	public String getOwnerName() {
		// TODO Return the speaker name
		return super.getOwnerName();
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
