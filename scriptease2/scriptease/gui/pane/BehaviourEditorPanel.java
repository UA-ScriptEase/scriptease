package scriptease.gui.pane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EtchedBorder;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.gui.storycomponentpanel.StoryComponentPanelManager;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.model.atomic.KnowIt;
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
	private final StoryComponentPanelManager panelManager;
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
		this.panelManager = new StoryComponentPanelManager();

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
		
		final List<KnowIt> implicitList = new ArrayList<KnowIt>();
		final Iterator<KnowIt> iterator = behaviour.getImplicits().iterator();

		if (behaviour.getType() == Behaviour.Type.INDEPENDENT) {
			implicitList.add(iterator.next());
		} else {
			implicitList.add(iterator.next());
			implicitList.add(iterator.next());
		}
		
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
				final JPanel taskPanel = new JPanel();

				final FlowLayout layout = new FlowLayout(FlowLayout.LEADING);
				layout.setAlignOnBaseline(true);

				taskPanel.setLayout(layout);

				// Remove the previous task's effects panel if there is one.
				final Component lastComponent = layoutPanel
						.getComponent(layoutPanel.getComponents().length - 1);

				if (lastComponent instanceof JPanel
						&& layoutPanel.getComponents().length > 3) {
					layoutPanel.remove(lastComponent);
				}

				// Set up the effects panel for the task we selected.
				final Task task = nodes.iterator().next();
				
				if (task instanceof IndependentTask) {
					
					final StoryComponentPanelTree storyComponentPanelTree;

					StoryComponentPanel initiatorTaskPanel = StoryComponentPanelFactory
							.getInstance().buildStoryComponentPanel(
									((IndependentTask) task)
											.getInitiatorContainer());

					storyComponentPanelTree = new StoryComponentPanelTree(
							initiatorTaskPanel);

					storyComponentPanelTree.setBorder(BorderFactory
							.createEmptyBorder());
					
					taskPanel.add(storyComponentPanelTree);

				} else if (task instanceof CollaborativeTask) {

					final StoryComponentPanelTree initiatorPanelTree;
					final StoryComponentPanelTree responderPanelTree;

					StoryComponentPanel initiatorTaskPanel = StoryComponentPanelFactory
							.getInstance().buildStoryComponentPanel(
									((CollaborativeTask) task)
											.getInitiatorContainer());

					StoryComponentPanel responderTaskPanel = StoryComponentPanelFactory
							.getInstance().buildStoryComponentPanel(
									((CollaborativeTask) task)
											.getResponderContainer());

					initiatorPanelTree = new StoryComponentPanelTree(
							initiatorTaskPanel);
					responderPanelTree = new StoryComponentPanelTree(
							responderTaskPanel);

					initiatorPanelTree.setBorder(BorderFactory
							.createEmptyBorder());
					responderPanelTree.setBorder(BorderFactory
							.createEmptyBorder());

					taskPanel.setLayout(new BoxLayout(taskPanel,BoxLayout.PAGE_AXIS));

					JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,initiatorPanelTree, responderPanelTree);
					splitPane.setResizeWeight(0.5);
					taskPanel.setLayout(new BoxLayout(taskPanel,BoxLayout.X_AXIS));
					
					//Provide minimum sizes for the two components in the split pane
					//Should be enough space for each panel to have 3 effects event at low resolutions.
					Dimension minimumSize = new Dimension(500, 300);
					splitPane.setMinimumSize(minimumSize);
					splitPane.setPreferredSize(minimumSize);
					
					taskPanel.add(splitPane);
					
				}

				layoutPanel.add(taskPanel);
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

		final StoryComponentPanel behaviourComponentPanel = StoryComponentPanelFactory
				.getInstance().buildStoryComponentPanel(behaviour);

		this.panelManager.addPanel(behaviourComponentPanel, false);

		namePanel.add(behaviourComponentPanel);
		graphPanel.add(graph);

		this.layoutPanel.add(namePanel);
		this.layoutPanel.add(graphPanel);
		this.layoutPanel.add(this.buildBehaviourImplicitPanel(implicitList));

		graph.setSelectedNode(startTask);

		this.repaint();
		this.revalidate();
	}
	
	private JPanel buildBehaviourImplicitPanel(List<KnowIt> implicitList) {
		final JPanel implicitPanel;

		implicitPanel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				final Dimension dimension = super.getPreferredSize();
				dimension.height = 60;
				return dimension;
			}

			@Override
			public Dimension getMaximumSize() {
				final Dimension dimension = super.getMaximumSize();
				dimension.height = 60;
				return dimension;
			}

			@Override
			public Dimension getMinimumSize() {
				final Dimension dimension = super.getMinimumSize();
				dimension.height = 60;
				return dimension;
			}
		};

		implicitPanel.setBorder(BorderFactory.createTitledBorder("Implicits"));
		implicitPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		for (KnowIt implicit : implicitList) {
			implicitPanel.add(ScriptWidgetFactory.buildBindingWidget(implicit,
					false));
		}

		return implicitPanel;
	}

	public StoryComponentPanelManager getPanelManager() {
		return this.panelManager;
	}
}
