package scriptease.gui.libraryeditor;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.gui.storycomponentpanel.StoryComponentPanelManager;
import scriptease.gui.transfer.StoryComponentPanelTransferHandler;
import scriptease.model.StoryComponent;
import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;
import scriptease.model.complex.behaviours.Task;

/**
 * A JPanel that allows multiple effects to be dragged into it.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class TaskPanel extends JPanel {

	private final Task task;
	private final TaskPanel.TYPE type;
	private final StoryComponentPanelManager panelManager;
	private final boolean editable;

	public enum TYPE {
		INDEPENDENT, COLLABORATIVE_INIT, COLLABORATIVE_RESPOND
	}

	/**
	 * Creates a new TaskEffectsPanel
	 * 
	 * @param task
	 *            the task to create the effect panel for.
	 */
	public TaskPanel(String name, Task task, TaskPanel.TYPE type,
			boolean editable) {
		this.type = type;
		this.task = task;
		this.editable = editable;
		this.panelManager = new StoryComponentPanelManager();

		if (type == TaskPanel.TYPE.INDEPENDENT
				&& task instanceof IndependentTask) {
			final IndependentTask independentTask = (IndependentTask) task;

			for (StoryComponent child : independentTask.getChildren()) {
				this.addComponent(child);
			}

		} else if (type == TaskPanel.TYPE.COLLABORATIVE_INIT
				&& task instanceof CollaborativeTask) {
			final CollaborativeTask collabTask = (CollaborativeTask) task;

			for (StoryComponent child : collabTask.getInitiatorContainer().getChildren()) {
				this.addComponent(child);
			}

		} else if (type == TaskPanel.TYPE.COLLABORATIVE_RESPOND
				&& task instanceof CollaborativeTask) {
			final CollaborativeTask collabTask = (CollaborativeTask) task;

			for (StoryComponent child : collabTask.getResponderContainer().getChildren()) {
				this.addComponent(child);
			}
		}

		this.setBorder(BorderFactory.createTitledBorder(name));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setTransferHandler(StoryComponentPanelTransferHandler
				.getInstance());
	}

	/**
	 * Adds a component to the panel;
	 * 
	 * @param component
	 */
	public boolean addComponent(StoryComponent component) {
		final StoryComponentPanel panel;

		panel = StoryComponentPanelFactory.getInstance()
				.buildStoryComponentPanel(component);

		panel.setSelectable(true);
		panel.setRemovable(true);

		this.panelManager.addPanel(panel, false);

		this.add(panel);
		this.add(Box.createVerticalStrut(5));

		this.repaint();
		this.revalidate();

		return true;
	}

	/**
	 * Removes a component from the panel
	 * 
	 * @param component
	 * @return
	 */
	public boolean removeComponent(StoryComponent component) {
		// hack to remove the vertical strut since JPanel can't get indexes of
		// components
		boolean removeNextComponent = false;

		for (Component comp : this.getComponents()) {
			if (removeNextComponent) {
				this.remove(comp);
				break;
			}
			
			if (comp instanceof StoryComponentPanel) {
				final StoryComponentPanel panel = (StoryComponentPanel) comp;

				if (panel.getStoryComponent() == component) {
					this.remove(comp);
					removeNextComponent = true;
				}
			}
		}

		return true;
	}

	/**
	 * @return the type for this panel.
	 */
	public TaskPanel.TYPE getType() {
		return this.type;
	}

	/**
	 * @return the task for this panel.
	 */
	public Task getTask() {
		return this.task;
	}

	/**
	 * @return whether the task panel is editable.
	 */
	public boolean isEditable() {
		return this.editable;
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
