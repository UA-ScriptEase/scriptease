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
import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;
import scriptease.model.complex.behaviours.Task;

/**
 * A JPanel that allows multiple effects to be dragged into it.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class TaskEffectsPanel extends JPanel {

	private final Task task;
	private final TaskEffectsPanel.TYPE type;
	private final StoryComponentPanelManager panelManager;

	public enum TYPE {
		INDEPENDENT, COLLABORATIVE_INIT, COLLABORATIVE_REACT
	}

	/**
	 * Creates a new TaskEffectsPanel
	 * 
	 * @param task
	 *            the task to create the effect panel for.
	 */
	public TaskEffectsPanel(String name, Task task, TaskEffectsPanel.TYPE type) {
		super();

		this.type = type;
		this.task = task;
		this.panelManager = new StoryComponentPanelManager();

		if (type == TaskEffectsPanel.TYPE.INDEPENDENT
				&& task instanceof IndependentTask) {
			for (StoryComponent component : task.getChildren()) {
				this.addEffect((ScriptIt) component);
			}

		} else if (type == TaskEffectsPanel.TYPE.COLLABORATIVE_INIT
				&& task instanceof CollaborativeTask) {
			final CollaborativeTask collabTask = (CollaborativeTask) task;

			for (StoryComponent child : collabTask
					.getInitiatorEffectsContainer().getChildren()) {
				this.addEffect((ScriptIt) child);
			}

		} else if (type == TaskEffectsPanel.TYPE.COLLABORATIVE_REACT
				&& task instanceof CollaborativeTask) {
			final CollaborativeTask collabTask = (CollaborativeTask) task;

			for (StoryComponent child : collabTask
					.getCollaboratorEffectsContainer().getChildren()) {
				this.addEffect((ScriptIt) child);
			}
		}

		this.setBackground(Color.WHITE);
		this.setBorder(BorderFactory.createTitledBorder(name));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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

		final StoryComponentPanel panel;

		panel = StoryComponentPanelFactory.getInstance()
				.buildStoryComponentPanel(effect);

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
		for (Component component : this.getComponents()) {
			if (component instanceof StoryComponentPanel) {
				final StoryComponentPanel panel = (StoryComponentPanel) component;

				if (panel.getStoryComponent() == effect) {
					this.remove(component);
					break;
				}
			}
		}

		return true;
	}

	/**
	 * @return the type for this panel.
	 */
	public TaskEffectsPanel.TYPE getType() {
		return this.type;
	}
	
	/**
	 * @return the task for this panel.
	 */
	public Task getTask() {
		return this.task;
	}

	public StoryComponentPanelManager getPanelManager() {
		return this.panelManager;
	}

	@Override
	public Dimension getMinimumSize() {
		final Dimension dimension = super.getMinimumSize();
		dimension.width = 500;
		dimension.height = 200;
		return dimension;
	}
}
