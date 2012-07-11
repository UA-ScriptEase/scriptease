package scriptease.controller.observer;

import scriptease.model.StoryComponent;

/**
 * Defines an object that encapsulates information about a change made to a
 * property of a StoryComponent.<br>
 * <br>
 * When a new subclass of StoryComponent is created, this event should be
 * expanded to be able to represent a change in the new properties defined in
 * that new class. For example, when defining Quest Points, expand
 * {@link StoryComponentChangeEnum} to be able to represent changes in a Quest
 * Point's <i>Journal Entry</i> property.
 * 
 * @author remiller
 */
public final class StoryComponentEvent {
	private final StoryComponent source;
	private final StoryComponentChangeEnum type;

	public StoryComponentEvent(StoryComponent source,
			StoryComponentChangeEnum type) {
		this.source = source;
		this.type = type;
	}

	public StoryComponent getSource() {
		return this.source;
	}

	public StoryComponentChangeEnum getType() {
		return this.type;
	}

	public enum StoryComponentChangeEnum {
		CHANGE_TEXT_NAME, 
		CHANGE_PARAMETER_LIST_ADD, 
		CHANGE_PARAMETER_LIST_REMOVE, 
		CHANGE_CHILD_ADDED, 
		CHANGE_CHILD_REMOVED,
		CHANGE_CODEBLOCK_ADDED, 
		CHANGE_CODEBLOCK_REMOVED, 
		CHANGE_KNOW_IT_BOUND,
		CHANGE_KNOW_IT_TYPE,
		CHANGE_REMOVED,
		CHANGE_CONDITION_BOUND,
		LABEL_ADDED, 
		LABEL_REMOVED,
	}

	@Override
	public String toString() {
		return "StoryComponentEvent [" + type + " : " + source + "]";
	}
}
