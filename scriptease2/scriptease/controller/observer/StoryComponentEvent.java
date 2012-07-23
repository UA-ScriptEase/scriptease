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

	public StoryComponentEvent(StoryComponent comp,
			StoryComponentChangeEnum type) {
		this.source = comp;
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
		CHANGE_KNOW_IT_BOUND,
		CHANGE_KNOW_IT_TYPE,
		CHANGE_SCRIPT_IT_TYPES,
		CHANGE_REFERENCE,
		CHANGE_REMOVED,
		CHANGE_CONDITION_BOUND,
		CHANGE_START_IT_SUBJECT_BOUND, 
		CHANGE_START_IT_SLOT, 
		CHANGE_START_IT_TYPE,
		CHANGE_DOIT_TYPE, 
		CODE_BLOCK_REMOVED,
		CODE_BLOCK_ADDED,
		CODE_BLOCKS_SET,
		CODE_BLOCK_TYPES_SET,
		CODE_BLOCK_SUBJECT_SET,
		CODE_BLOCK_SLOT_SET,
		CODE_BLOCK_PARAMETERS_SET,
		CODE_BLOCK_INCLUDES_SET,
		CODE_BLOCK_CODE_SET,
		LABEL_ADDED, 
		LABELS_ADDED,
		LABEL_REMOVED,
		LABEL_SET
	}

	@Override
	public String toString() {
		return "StoryComponentEvent [" + type + " : " + source + "]";
	}
}
