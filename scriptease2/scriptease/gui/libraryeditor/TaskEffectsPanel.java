package scriptease.gui.libraryeditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.gui.storycomponentpanel.StoryComponentPanelManager;
import scriptease.gui.transfer.StoryComponentPanelTransferHandler;
import scriptease.model.StoryComponent;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.behaviours.Task;

/**
 * A JPanel that allows multiple effects to be dragged into it.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class TaskEffectsPanel extends JPanel {

	private Task task;

	private StoryComponentPanelManager panelManager;

	/**
	 * Creates a new TaskEffectsPanel
	 * 
	 * @param task
	 *            the task to create the effect panel for.
	 */
	public TaskEffectsPanel(Task task) {
		super();

		this.task = task;
		this.panelManager = new StoryComponentPanelManager();

		for (StoryComponent child : task.getChildren()) {
			this.addEffect((ScriptIt) child);
		}
		
		this.setBackground(Color.WHITE);
		this.setBorder(BorderFactory.createTitledBorder("Task Panel"));
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setTransferHandler(StoryComponentPanelTransferHandler
				.getInstance());
	}

	/**
	 * Adds a effect to the panel;
	 * 
	 * @param component
	 */
	public boolean addEffect(ScriptIt effect) {
		// Don't want to be setting causes.
		if (effect != null && (effect instanceof CauseIt))
			return false;

		final ScriptIt clone = effect.clone();
		final StoryComponentPanel panel;

		if (!this.task.getChildren().contains(effect)) {
			this.task.addStoryChild(clone);
			panel = StoryComponentPanelFactory.getInstance()
					.buildStoryComponentPanel(clone);
		} else {
			panel = StoryComponentPanelFactory.getInstance()
					.buildStoryComponentPanel(effect);
		}

		panel.setBackground(Color.WHITE);
		panel.setMaximumSize(panel.getPreferredSize());
		panel.setSelectable(true);
		panel.setRemovable(true);

		this.panelManager.addPanel(panel, false);

		this.add(panel);

		this.repaint();
		this.revalidate();

		return true;
	}

	/**
	 * Removes a effect from the panel
	 * 
	 * @param effect
	 * @return
	 */
	public boolean removeEffect(ScriptIt effect) {
		if (!this.task.getChildren().contains(effect))
			return false;

		for (Component component : this.getComponents()) {
			if (component instanceof StoryComponentPanel) {
				final StoryComponentPanel panel = (StoryComponentPanel) component;

				if (panel.getStoryComponent() == effect) {
					this.task.removeStoryChild(effect);
					this.remove(component);
					break;
				}
			}
		}

		return true;
	}

	public StoryComponentPanelManager getPanelManager() {
		return this.panelManager;
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension dimension = super.getPreferredSize();
		dimension.height = 200;
		return dimension;
	}

	@Override
	public Dimension getMinimumSize() {
		final Dimension dimension = super.getMinimumSize();
		dimension.height = 200;
		return dimension;
	}
}
