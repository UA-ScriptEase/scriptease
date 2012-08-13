package scriptease.gui.storycomponentpanel.setting;

import scriptease.gui.storycomponentpanel.StoryComponentPanel;

/* XXX $$$$$$$$$$$$$$$ XXX
 * $ MARKED FOR DELETION $
 * XXX $$$$$$$$$$$$$$$ XXX
 */

/**
 * Interface for defining StoryComponentPanel drag and drop and selection
 * behavior. Typically should be used in combination with a StoryVisitor, and
 * resides in a StoryComponentPanelTree.
 * 
 * @author mfchurch
 * 
 */
public interface $StoryComponentPanelSetting {
	/**
	 * Sets the editable, removable and selectable settings of a panel to
	 * coincide with the desired tree settings
	 * 
	 * @param panel
	 */
	public void updateSettings(StoryComponentPanel panel);

	/**
	 * Sets the editable, removable and selectable settings of a root panel and
	 * all it's descendants to coincide with the desired tree settings
	 * 
	 * @param rootPanel
	 */
	public void updateComplexSettings(StoryComponentPanel rootPanel);
}
