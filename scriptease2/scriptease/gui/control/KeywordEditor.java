package scriptease.gui.control;

import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.StoryComponentObserver;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;

/**
 * FlushableTextField for editing the name of a parameter.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public class KeywordEditor extends FlushableTextField implements
		StoryComponentObserver {
	protected StoryComponent edited;

	/**
	 * Creates a new Name Editor
	 */
	public KeywordEditor() {
		super(50);
	}

	public void setEditedComponent(StoryComponent edited) {
		StoryComponent oldComponent = this.edited;
		if (oldComponent != null)
			oldComponent.removeStoryComponentObserver(this);
		this.edited = edited;
		this.updateDisplay();
		this.edited.addStoryComponentObserver(this);
	}

	@Override
	protected String getModelText() {
		return this.edited.getDisplayText();
	}

	@Override
	protected void updateKnowItBinding(String newValue) {
		this.edited.setDisplayText(newValue);
	}

	@Override
	public void componentChanged(StoryComponentEvent event) {
		if (event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME) {
			this.updateDisplay();
		}
	}

	public void updateModelKeyword(String newValue) {
		((KnowIt) this.edited).setDisplayText(newValue);
	}
}