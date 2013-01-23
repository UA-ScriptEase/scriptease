package scriptease.controller.observer.storycomponent;

import scriptease.model.StoryComponent;

/**
 * Defines an object that encapsulates information about a change made to a
 * property of a StoryComponent.<br>
 * <br>
 * When a new subclass of StoryComponent is created, this event should be
 * expanded to be able to represent a change in the new properties defined in
 * that new class. For example, when defining StoryPoints, expand
 * {@link StoryComponentChangeEnum} to be able to represent changes in a
 * StoryPoint's <i>Journal Entry</i> property.
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
		CHANGE_PARAMETER_TYPES_SET,
		CHANGE_PARAMETER_DEFAULT_TYPE_SET,
		CHANGE_PARAMETER_NAME_SET,
		CHANGE_CHILD_ADDED,
		CHANGE_CHILD_REMOVED,
		CHANGE_CODEBLOCK_ADDED,
		CHANGE_CODEBLOCK_REMOVED,
		CHANGE_KNOW_IT_BOUND,
		CHANGE_KNOW_IT_TYPE,
		CHANGE_REFERENCE,
		CHANGE_REMOVED,
		CHANGE_CONDITION_BOUND,
		CHANGE_START_IT_SUBJECT_BOUND,
		CHANGE_START_IT_SLOT,
		CHANGE_START_IT_TYPE,
		CHANGE_DOIT_TYPE,
		CHANGE_VISIBILITY,
		CODE_BLOCKS_SET,
		CHANGE_CODE_BLOCK_TYPES,
		CODE_BLOCK_SUBJECT_SET,
		CODE_BLOCK_SLOT_SET,
		//CHANGE_CODE_BLOCK_PARAMETERS,
		CODE_BLOCK_INCLUDES_SET,
		CODE_BLOCK_CODE_SET,
		CHANGE_LABELS_CHANGED,
		CHANGE_CODEBLOCK_CODE,
		CHANGE_FAN_IN,
		STORY_POINT_SUCCESSOR_ADDED,
		STORY_POINT_SUCCESSOR_REMOVED, 
		DESCRIBEIT_NODE_SUCCESSOR_ADDED, 
		DESCRIBEIT_NODE_SUCCESSOR_REMOVED
	}

	@Override
	public String toString() {
		return "StoryComponentEvent [" + this.type + " : " + this.source + "]";
	}
}
