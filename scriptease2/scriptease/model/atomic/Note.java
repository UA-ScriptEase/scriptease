package scriptease.model.atomic;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;

/**
 * A story component that contains text and nothing else. Can be inserted
 * anywhere.
 * 
 * @author kschenk
 * 
 */
public final class Note extends StoryComponent {

	public Note() {
		this("");
	}

	public Note(String string) {
		super();
		this.setDisplayText(string);
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processNote(this);

	}

	@Override
	public void revalidateKnowItBindings() {
		// Do nothing. Notes don't have any bindings.
	}
}
