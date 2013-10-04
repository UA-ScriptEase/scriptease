package scriptease.gui.libraryeditor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
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

	/**
	 * Creates a new TaskEffectsPanel
	 * 
	 * @param task
	 *            the task to create the effect panel for.
	 */
	public TaskEffectsPanel(Task task) {
		super();

		this.effects = new ArrayList<ScriptIt>();

		for (StoryComponent child : task.getChildren()) {
			this.effects.add((ScriptIt) child);
		}

		this.setBackground(Color.WHITE);
		this.setBorder(BorderFactory.createTitledBorder("Effects Panel"));
		
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

		this.effects.add(effect);

		final StoryComponentPanel panel;
		
		panel = StoryComponentPanelFactory.getInstance()
				.buildStoryComponentPanel(effect);
		panel.setBackground(Color.WHITE);
		
		this.add(panel);
		
		this.repaint();
		this.revalidate();

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
}
