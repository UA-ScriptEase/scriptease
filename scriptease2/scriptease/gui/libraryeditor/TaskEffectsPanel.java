package scriptease.gui.libraryeditor;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

	private List<ScriptIt> effects;

	private StoryComponentPanelManager panelManager;

	/**
	 * Creates a new TaskEffectsPanel
	 * 
	 * @param task
	 *            the task to create the effect panel for.
	 */
	public TaskEffectsPanel(Task task) {
		super();

		this.effects = new ArrayList<ScriptIt>();
		this.panelManager = new StoryComponentPanelManager();

		for (StoryComponent child : task.getChildren()) {
			this.effects.add((ScriptIt) child);
		}

		this.setBackground(Color.WHITE);
		this.setBorder(BorderFactory.createTitledBorder("Effects Panel"));
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setTransferHandler(StoryComponentPanelTransferHandler
				.getInstance());
	}

	/**
	 * Add all given effects to the panel.
	 * 
	 * @param effects
	 * @return
	 */
	public boolean addAllEffects(Collection<ScriptIt> effects) {
		for (ScriptIt effect : effects) {
			if (!this.addEffect(effect))
				return false;
		}
		
		return true;
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

		this.effects.add(effect);

		final StoryComponentPanel panel;

		panel = StoryComponentPanelFactory.getInstance()
				.buildStoryComponentPanel(effect);
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
		if (!this.effects.contains(effect))
			return false;

		for (Component component : this.getComponents()) {
			if (component instanceof StoryComponentPanel) {
				final StoryComponentPanel panel = (StoryComponentPanel) component;

				if (panel.getStoryComponent() == effect) {
					this.effects.remove(effect);
					this.remove(component);
					break;
				}
			}
		}

		return true;
	}

	/**
	 * Returns the effects inside the panel.
	 * 
	 * @return
	 */
	public List<ScriptIt> getEffects() {
		return this.effects;
	}

	public StoryComponentPanelManager getPanelManager() {
		return this.panelManager;
	}
}
