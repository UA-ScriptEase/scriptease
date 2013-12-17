package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.model.semodel.dialogue.DialogueLine.Speaker;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.Resource;
import scriptease.util.StringOp;

/**
 * Context for DialogueLines.
 * 
 * @author kschenk
 */
public class DialogueLineContext extends Context {
	private final DialogueLine line;

	public DialogueLineContext(Context other, DialogueLine source) {
		super(other);

		this.line = source;
	}

	@Override
	public String getFormattedValue() {
		final StoryModel model = this.getModel();
		final String dialogueType = model.getModule().getDialogueType();
		final String dialogueLineType = model.getModule().getDialogueLineType();

		final Collection<AbstractFragment> typeFormat;

		if (this.line.isRoot() && StringOp.exists(dialogueType))
			typeFormat = model.getType(dialogueType).getFormat();
		else if (StringOp.exists(dialogueLineType))
			typeFormat = model.getType(dialogueLineType).getFormat();
		else
			typeFormat = null;

		if (typeFormat == null || typeFormat.isEmpty())
			return this.getValue();

		return AbstractFragment.resolveFormat(typeFormat, this);
	}

	@Override
	public Collection<DialogueLine> getChildLines() {
		return this.line.getChildren();
	}

	@Override
	public String getUniqueID() {
		return Integer.toString(this.line.getUniqueID());
	}

	@Override
	public String getText() {
		return this.line.getName();
	}

	@Override
	public String getSpeaker() {
		final Speaker speaker = this.line.getSpeaker();

		if (speaker == Speaker.NPC)
			return "2";
		else
			return "1";
	}

	@Override
	public String getEnabled() {
		return Boolean.toString(this.line.isEnabled());
	}

	@Override
	public KnowIt getAudio() {
		return this.line.getAudio();
	}

	@Override
	public KnowIt getImage() {
		return this.line.getImage();
	}

	@Override
	public String getValue() {
		return this.getUniqueID();
	}

	@Override
	public Collection<DialogueLine> getOrderedDialogueLines() {
		final Collection<DialogueLine> descendants;

		descendants = new ArrayList<DialogueLine>();

		for (Resource resource : this.line.getOrderedDescendants()) {
			if (resource instanceof DialogueLine)
				descendants.add((DialogueLine) resource);
		}

		return descendants;
	}
}
