package scriptease.gui.pane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.libraryeditor.TaskEffectsPanel;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;
import scriptease.model.complex.behaviours.Task;

/**
 * Panel that allows you to modify a behaviour in the story model.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class BehaviourEditorPanel extends JPanel {

	private final JButton backToStory;
	private final JPanel layoutPanel;
	private Behaviour behaviour;

	/**
	 * Creates a new editor panel for behaviours. To update the behaviour being
	 * edited, call {@link #setBehaviour(Behaviour)}.
	 * 
	 * @param model
	 * @param backToStory
	 */
	public BehaviourEditorPanel(JButton backToStory) {
		this.backToStory = backToStory;
		this.behaviour = null;

		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		if (this.backToStory != null)
			this.add(this.backToStory, BorderLayout.EAST);

		this.layoutPanel = new JPanel();
		this.layoutPanel
				.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));
		this.add(layoutPanel);
	}

	/**
	 * Set this panel's behaviour.
	 * 
	 * @param behaviour
	 */
	public void setBehaviour(Behaviour behaviour) {
		this.behaviour = behaviour;
		this.createLayout();
	}

	/**
	 * Get this panel's behaviour.
	 * 
	 * @return
	 */
	public Behaviour getBehaviour() {
		return this.behaviour;
	}

	/**
	 * Create the layout for BehaviourEditorPanel, so that users can drag and
	 * drop in game objects.
	 */
	private void createLayout() {
		this.layoutPanel.removeAll();

		final SEGraph<Task> graphPanel;

		graphPanel = this.buildBehaviourGraph();

		this.layoutPanel.add(graphPanel);

		this.repaint();
		this.revalidate();
	}

	private SEGraph<Task> buildBehaviourGraph() {
		final SEGraph<Task> graph;
		final Task startTask;

		startTask = behaviour.getStartTask();

		graph = SEGraphFactory.buildTaskGraph(startTask, true);
		graph.setAlignmentY(JPanel.LEFT_ALIGNMENT);

		graph.addSEGraphObserver(new SEGraphAdapter<Task>() {

			@Override
			public void nodesSelected(final Collection<Task> nodes) {
				final JPanel effectsPanel = new JPanel();

				effectsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

				// Remove the previous task's effects panel if there is one.
				final Component lastComponent = layoutPanel
						.getComponent(layoutPanel.getComponents().length - 1);

				if (lastComponent instanceof JPanel) {
					final JPanel panel = (JPanel) lastComponent;

					if (panel.getComponentCount() > 0
							&& panel.getComponent(0) instanceof TaskEffectsPanel) {
						layoutPanel.remove(lastComponent);
					}
				}

				// Set up the effects panel for the task we selected.
				final Task task = nodes.iterator().next();

				if (task instanceof IndependentTask) {
					layoutPanel.add(new TaskEffectsPanel("Task Panel", task,
							TaskEffectsPanel.TYPE.INDEPENDENT, false));

				} else if (task instanceof CollaborativeTask) {
					layoutPanel.add(new TaskEffectsPanel(
							"Initiator Task Panel", task,
							TaskEffectsPanel.TYPE.COLLABORATIVE_INIT, false));
					layoutPanel.add(new TaskEffectsPanel("Reactor Task Panel",
							task, TaskEffectsPanel.TYPE.COLLABORATIVE_REACT,
							false));
				}

				layoutPanel.add(effectsPanel);
				layoutPanel.repaint();
				layoutPanel.revalidate();
			}

			@Override
			public void nodeOverwritten(Task task) {
				task.revalidateKnowItBindings();
			}

			@Override
			public void nodeRemoved(Task task) {
				task.revalidateKnowItBindings();
			}
		});

		return graph;
	}
}
