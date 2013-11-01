package scriptease.gui.pane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
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
		final JPanel namePanel;
		final JPanel graphPanel;

		final SEGraph<Task> graph;
		final Task startTask;
		
		this.layoutPanel.removeAll();

		// create the name panel.
		namePanel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				final Dimension dimension = super.getPreferredSize();
				dimension.height = 70;
				return dimension;
			}

			@Override
			public Dimension getMaximumSize() {
				final Dimension dimension = super.getMaximumSize();
				dimension.height = 70;
				return dimension;
			}

			@Override
			public Dimension getMinimumSize() {
				final Dimension dimension = super.getMinimumSize();
				dimension.height = 70;
				return dimension;
			}
		};
		namePanel.setBorder(BorderFactory.createTitledBorder("Behaviour"));
		namePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		// Create the graph panel.
		graphPanel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				final Dimension dimension = super.getPreferredSize();
				dimension.height = 180;
				return dimension;
			}

			@Override
			public Dimension getMaximumSize() {
				final Dimension dimension = super.getMaximumSize();
				dimension.height = 180;
				return dimension;
			}

			@Override
			public Dimension getMinimumSize() {
				final Dimension dimension = super.getMinimumSize();
				dimension.height = 180;
				return dimension;
			}
		};
		graphPanel.setBorder(BorderFactory
				.createTitledBorder("Behaviour Tasks Graph"));
		graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.X_AXIS));

		startTask = behaviour.getStartTask();

		graph = SEGraphFactory.buildTaskGraph(startTask, true);
		graph.setAlignmentY(JPanel.LEFT_ALIGNMENT);

		graph.addSEGraphObserver(new SEGraphAdapter<Task>() {

			@Override
			public void nodesSelected(final Collection<Task> nodes) {
				final JPanel effectsPanel = new JPanel();
				
				final FlowLayout layout = new FlowLayout(FlowLayout.LEADING);
				layout.setAlignOnBaseline(true);
				
				effectsPanel.setLayout(layout);

				// Remove the previous task's effects panel if there is one.
				final Component lastComponent = layoutPanel
						.getComponent(layoutPanel.getComponents().length - 1);

				if (lastComponent instanceof JPanel
						&& layoutPanel.getComponents().length > 2) {
					layoutPanel.remove(lastComponent);
				}

				// Set up the effects panel for the task we selected.
				final Task task = nodes.iterator().next();

				if (task instanceof IndependentTask) {

					effectsPanel.add(new TaskEffectsPanel("Task Panel", task,
							TaskEffectsPanel.TYPE.INDEPENDENT, false));

				} else if (task instanceof CollaborativeTask) {

					effectsPanel.add(new TaskEffectsPanel(
							"Initiator Task Panel", task,
							TaskEffectsPanel.TYPE.COLLABORATIVE_INIT, false));

					effectsPanel.add(new TaskEffectsPanel("Reactor Task Panel",
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

		namePanel.add(StoryComponentPanelFactory.getInstance().buildStoryComponentPanel(behaviour));
		graphPanel.add(graph);

		this.layoutPanel.add(namePanel);
		this.layoutPanel.add(graphPanel);

		graph.setSelectedNode(startTask);

		this.repaint();
		this.revalidate();
	}
}
