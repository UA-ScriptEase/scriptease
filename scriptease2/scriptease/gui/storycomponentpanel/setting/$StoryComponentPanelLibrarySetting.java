package scriptease.gui.storycomponentpanel.setting;

import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;

/* XXX $$$$$$$$$$$$$$$ XXX
 * $ MARKED FOR DELETION $
 * XXX $$$$$$$$$$$$$$$ XXX
 */

/**
 * Every non-root StoryComponentPanel is selectable, but none are editable or
 * removable.
 * 
 * @author mfchurch
 * 
 */
public class $StoryComponentPanelLibrarySetting extends
		AbstractNoOpStoryVisitor implements $StoryComponentPanelSetting {
	private StoryComponentPanel panel;

	@Override
	public void updateSettings(StoryComponentPanel panel) {
		this.panel = panel;
		panel.getStoryComponent().process(this);
		this.panel.setRemovable(false);
		this.panel.setEditable(false);
		this.panel = null;
	}

	@Override
	public void updateComplexSettings(StoryComponentPanel rootPanel) {
		updateSettings(rootPanel);
		for (StoryComponentPanel panel : rootPanel
				.getDescendantStoryComponentPanels()) {
			updateSettings(panel);
		}
	}

	@Override
	protected void defaultProcess(StoryComponent component) {
		this.panel.setSelectable(true);
	}

	/**
	 * Everything but the root is selectable
	 */
	@Override
	protected void defaultProcessComplex(ComplexStoryComponent complex) {
		boolean notRoot = (complex.getOwner() != null);
		this.panel.setSelectable(notRoot);
	}
}
