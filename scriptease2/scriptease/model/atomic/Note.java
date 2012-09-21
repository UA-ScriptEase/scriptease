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

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processNote(this);

	}
}
