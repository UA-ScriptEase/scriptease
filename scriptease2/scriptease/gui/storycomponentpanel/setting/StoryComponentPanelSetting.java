package scriptease.gui.storycomponentpanel.setting;

import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.StoryItemSequence;

/**
 * Every StoryComponentPanel is editable, and any non-root StoryComponentPanels
 * are selectable and removable
 * 
 * @author mfchurch
 * 
 */
public class StoryComponentPanelSetting extends AbstractNoOpStoryVisitor {
	private StoryComponentPanel panel;

	public void updateSettings(StoryComponentPanel panel) {
		this.panel = panel;
		panel.getStoryComponent().process(this);
		this.panel.setEditable(true);
		this.panel = null; //Hack used to pass in some information, then reset the variable.
	}

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
		this.panel.setRemovable(true);
	}

	/**
	 * Everything but the root is selectable
	 */
	@Override
	protected void defaultProcessComplex(ComplexStoryComponent complex) {
		boolean notRoot = (complex.getOwner() != null);
		this.panel.setSelectable(notRoot);
		this.panel.setRemovable(notRoot);
	}

	@Override
	public void processStoryItemSequence(StoryItemSequence sequence) {
		this.panel.setSelectable(true);
		this.panel.setRemovable(false);
	}
}
