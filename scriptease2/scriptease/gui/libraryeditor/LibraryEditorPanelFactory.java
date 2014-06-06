package scriptease.gui.libraryeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import scriptease.ScriptEase;
import scriptease.controller.observer.CodeBlockPanelObserver;
import scriptease.controller.observer.ParameterPanelObserver;
import scriptease.controller.observer.SetEffectObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.component.BindingWidget;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.libraryeditor.codeblocks.CodeBlockPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockReference;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.complex.ActivityIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;
import scriptease.model.complex.behaviours.Task;
import scriptease.model.semodel.ScriptEaseKeywords;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.io.model.SimpleResource;
import scriptease.util.ListOp;
import scriptease.util.StringOp;

/**
 * A factory used to create a library editor. This is a singleton class, so use
 * the {@link #getInstance()} method to work with it.
 * 
 * @author kschenk
 * @author jyuen
 */
public class LibraryEditorPanelFactory {
	private static LibraryEditorPanelFactory instance = new LibraryEditorPanelFactory();
	public static Font labelFont = new Font("SansSerif", Font.BOLD,
			Integer.parseInt(ScriptEase.getInstance().getPreference(
					ScriptEase.FONT_SIZE_KEY)) + 1);

	/**
	 * Returns the single instance of StoryComponentBuilderPanelFactory
	 * 
	 * @return
	 */
	public static LibraryEditorPanelFactory getInstance() {
		return instance;
	}

	/**
	 * Creates a JPanel with fields for Name, Labels, and a check box for
	 * Visibility. This JPanel is common to all library editor panes.
	 * 
	 * @param libraryPane
	 *            The LibraryPanel to be acted on.
	 * 
	 * @return
	 */
	public JPanel buildLibraryEditorPanel() {
		return new LibraryEditorPanel();
	}

	/**
	 * Panel used to edit ActivityIts.
	 * 
	 * @param activityIt
	 * @return
	 */
	public JPanel buildActivityItEditingPanel(final ActivityIt activityIt) {
		final JPanel activityPanel;
		final CodeBlockPanel codeBlockPanel;
		final StoryComponentPanel transferPanel;

		activityPanel = new JPanel();
		activityPanel.setLayout(new BoxLayout(activityPanel, BoxLayout.Y_AXIS));
		activityPanel.setOpaque(false);

		transferPanel = StoryComponentPanelFactory.getInstance()
				.buildStoryComponentPanel(activityIt);

		codeBlockPanel = new CodeBlockPanel(activityIt.getMainCodeBlock(),
				activityIt, true);

		activityPanel.add(codeBlockPanel);
		activityPanel.add(this.buildActivityItImplicitPanel(activityIt));
		activityPanel.add(new StoryComponentPanelTree(transferPanel));

		codeBlockPanel.addListener(new CodeBlockPanelObserver() {

			// Rebuilds the implicit panel and StoryComponentPanelTree when a
			// parameter has changed
			@Override
			public void codeBlockPanelChanged() {
				final StoryComponentPanel newTransferPanel = StoryComponentPanelFactory
						.getInstance().buildStoryComponentPanel(activityIt);

				// Should always be able to remove the tree regardless
				activityPanel.remove(2);
				activityPanel.add(buildActivityItImplicitPanel(activityIt), 2);

				// Checking if getComponent(3) is null results in an out of
				// bounds exception so we have to make sure we list is big
				// enough before doing our null check
				if (activityPanel.getComponentCount() > 3
						&& activityPanel.getComponent(3) != null) {
					activityPanel.remove(3);
				}
				activityPanel.add(
						new StoryComponentPanelTree(newTransferPanel), 3);
				activityPanel.repaint();
				activityPanel.revalidate();
			}
		});

		activityIt.addStoryComponentObserver(activityPanel,
				new StoryComponentObserver() {

					@Override
					public void componentChanged(StoryComponentEvent event) {
						if (event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME
								|| event.getType() == StoryComponentChangeEnum.CHANGE_PARAMETER_TYPE) {
							final StoryComponentPanel newTransferPanel = StoryComponentPanelFactory
									.getInstance().buildStoryComponentPanel(
											activityIt);

							if (activityPanel.getComponents().length >= 3
									&& activityPanel.getComponent(2) != null) {
								activityPanel.remove(2);
							}

							activityPanel
									.add(buildActivityItImplicitPanel(activityIt),
											2);

							if (activityPanel.getComponents().length >= 4
									&& activityPanel.getComponent(3) != null) {
								activityPanel.remove(3);
							}
							activityPanel.add(new StoryComponentPanelTree(
									newTransferPanel), 3);

							activityPanel.repaint();
							activityPanel.revalidate();
						}
					}
				});

		return activityPanel;
	}

	@SuppressWarnings("serial")
	private JPanel buildActivityItImplicitPanel(final ActivityIt activityIt) {
		final JPanel implicitPanel;

		/*
		 * Sets a fixed height for panels. Probably not the cleanest way to do
		 * this. Refactor if necessary.
		 */
		implicitPanel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				final Dimension dimension = super.getPreferredSize();
				dimension.height = 40;
				return dimension;
			}

			@Override
			public Dimension getMaximumSize() {
				final Dimension dimension = super.getMaximumSize();
				dimension.height = 40;
				return dimension;
			}

			@Override
			public Dimension getMinimumSize() {
				final Dimension dimension = super.getMinimumSize();
				dimension.height = 40;
				return dimension;
			}
		};

		implicitPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		final CodeBlock codeBlock = activityIt.getMainCodeBlock();

		for (KnowIt parameter : codeBlock.getParameters()) {
			implicitPanel.add(ScriptWidgetFactory.buildBindingWidget(parameter,
					false));
		}

		return implicitPanel;
	}

	/**
	 * Builds a panel used to edit a behaviour.
	 * 
	 * @param behaviour
	 * @return
	 */
	@SuppressWarnings("serial")
	public JPanel buildBehaviourEditingPanel(final Behaviour behaviour) {
		final JPanel behaviourPanel;

		final JPanel buttonsPanel;
		final JButton independentButton;
		final JButton collaborativeButton;
		final JLabel warningLabel;

		behaviourPanel = new JPanel();
		behaviourPanel
				.setLayout(new BoxLayout(behaviourPanel, BoxLayout.Y_AXIS));

		// Create buttons panel
		buttonsPanel = new JPanel() {
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

		buttonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		buttonsPanel.setBorder(BorderFactory
				.createTitledBorder("Behaviour Type"));

		independentButton = new JButton("Independent");
		collaborativeButton = new JButton("Collaborative");
		warningLabel = new JLabel(
				"Warning: By changing the behaviour type, you are removing any existing behaviour owner(s) and tasks.");
		warningLabel.setForeground(Color.RED);

		independentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (Component component : behaviourPanel.getComponents()) {
					if (component != buttonsPanel)
						behaviourPanel.remove(component);
				}

				behaviour.setType(Behaviour.Type.INDEPENDENT);
				behaviour.setStartTask(null);
				behaviour.getMainCodeBlock().clearParameters();
				LibraryEditorPanelFactory.this.buildIndependentBehaviourPanel(
						behaviour, behaviourPanel);
			}
		});

		collaborativeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (Component component : behaviourPanel.getComponents()) {
					if (component != buttonsPanel)
						behaviourPanel.remove(component);
				}

				behaviour.setType(Behaviour.Type.COLLABORATIVE);
				behaviour.setStartTask(null);
				behaviour.getMainCodeBlock().clearParameters();
				LibraryEditorPanelFactory.this
						.buildCollaborativeBehaviourPanel(behaviour,
								behaviourPanel);
			}
		});

		buttonsPanel.add(independentButton);
		buttonsPanel.add(collaborativeButton);
		buttonsPanel.add(warningLabel);

		behaviourPanel.add(buttonsPanel);

		if (behaviour.getType() == Behaviour.Type.INDEPENDENT) {
			LibraryEditorPanelFactory.this.buildIndependentBehaviourPanel(
					behaviour, behaviourPanel);
		} else {
			LibraryEditorPanelFactory.this.buildCollaborativeBehaviourPanel(
					behaviour, behaviourPanel);
		}

		return behaviourPanel;
	}

	@SuppressWarnings("serial")
	private JPanel buildBehaviourGraphPanel(String graphName,
			SEGraph<Task> graph) {
		final JPanel graphPanel;

		// Create the graph panel.
		graphPanel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				final Dimension dimension = super.getPreferredSize();
				dimension.height = 245;
				return dimension;
			}

			@Override
			public Dimension getMaximumSize() {
				final Dimension dimension = super.getMaximumSize();
				dimension.height = 245;
				return dimension;
			}

			@Override
			public Dimension getMinimumSize() {
				final Dimension dimension = super.getMinimumSize();
				dimension.height = 245;
				return dimension;
			}
		};

		graphPanel.setBorder(BorderFactory.createTitledBorder(graphName));
		graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.X_AXIS));

		graphPanel.add(graph.getToolBar());
		graphPanel.add(new JScrollPane(graph), BorderLayout.CENTER);

		return graphPanel;
	}

	private SEGraph<Task> buildBehaviourGraph(final Behaviour behaviour,
			final JPanel behaviourPanel, final String type) {
		final SEGraph<Task> graph;
		final Task startTask;
		final LibraryModel library = behaviour.getLibrary();

		if (behaviour.getStartTask() != null) {
			// If behaviour is defined, build its existing task graph
			startTask = behaviour.getStartTask();
		} else {
			// Else create a new one.
			if (type == ScriptEaseKeywords.INDEPENDENT)
				startTask = new IndependentTask(library);
			else
				startTask = new CollaborativeTask(library);

			behaviour.setStartTask(startTask);
		}

		graph = SEGraphFactory.buildTaskGraph(startTask, false);

		graph.setAlignmentY(JPanel.LEFT_ALIGNMENT);

		graph.addSEGraphObserver(new SEGraphAdapter<Task>() {

			@Override
			public void nodesSelected(final Collection<Task> nodes) {
				final JPanel taskPanel = new JPanel();
				final FlowLayout layout = new FlowLayout(FlowLayout.LEADING);
				layout.setAlignOnBaseline(true);

				taskPanel.setLayout(layout);
				taskPanel.setBorder(BorderFactory
						.createTitledBorder("Task Panel"));

				// Remove the previous task's effects panel if there is one.
				final Component lastComponent = behaviourPanel
						.getComponent(behaviourPanel.getComponents().length - 1);

				if (lastComponent instanceof JPanel) {
					final JPanel panel = (JPanel) lastComponent;

					if (panel.getComponentCount() > 0
							&& panel.getComponent(0) instanceof JScrollPane
							|| panel.getComponent(0) instanceof JSplitPane
							|| panel.getComponent(0) instanceof JLabel) {
						behaviourPanel.remove(lastComponent);
					}
				}

				// Set up the effects panel for the task we selected.
				final Task task = nodes.iterator().next();

				if (task instanceof IndependentTask && task != behaviour.getStartTask()) {

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

				} else if (task instanceof CollaborativeTask && task != behaviour.getStartTask()) {

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

					JSplitPane splitPane = new JSplitPane(
							JSplitPane.VERTICAL_SPLIT, initiatorPanelTree,
							responderPanelTree);
					splitPane.setResizeWeight(0.5);
					taskPanel.setLayout(new BoxLayout(taskPanel,
							BoxLayout.X_AXIS));

					// Provide minimum sizes for the two components in the split
					// pane
					// Should be enough space for each panel to have 3 effects
					// event at low resolutions.
					Dimension minimumSize = new Dimension(500, 300);
					splitPane.setMinimumSize(minimumSize);
					splitPane.setPreferredSize(minimumSize);

					taskPanel.add(splitPane);

				} else {
					//Here is what we do for start task nodes 
					
					final JLabel startLabel;
					
					startLabel = new JLabel("You cannot add any components to the start task node!");
					taskPanel.add(startLabel);
				}

				behaviourPanel.add(taskPanel);
				behaviourPanel.repaint();
				behaviourPanel.revalidate();

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

	private JPanel buildBehaviourImplicitPanel(final List<KnowIt> implicitList) {
		final JPanel implicitPanel;

		implicitPanel = new JPanel();
		implicitPanel.setBorder(BorderFactory.createTitledBorder("Implicits"));
		implicitPanel.setLayout(new BoxLayout(implicitPanel, BoxLayout.Y_AXIS));

		for (final KnowIt implicit : implicitList) {
			final BindingWidget bindingWidget = ScriptWidgetFactory
					.buildBindingWidget(implicit, false);
			final JPanel subPanel = new JPanel();
			final ParameterPanel parameterPanel;

			subPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			subPanel.add(bindingWidget);

			parameterPanel = buildParameterPanel(implicit);
			parameterPanel.addListener(new ParameterPanelObserver() {
				@Override
				public void parameterPanelChanged() {

					/**
					 * So this chunk is to make sure that the binding's code
					 * block itself has it's types updated in addition to just
					 * the KnowIt (which is not done by the ParameterPanel, but
					 * is what's sued when drawing the BindingWidget).
					 */

					final KnowItBinding implicitBinding = implicit.getBinding();
					final ScriptIt implicitBindingValue = (ScriptIt) implicitBinding
							.getValue();
					final CodeBlockReference implicitCodeBlock = (CodeBlockReference) ListOp
							.head(implicitBindingValue.getCodeBlocks());

					implicitCodeBlock.setTypesByName(implicit.getTypes());

					BindingWidget updatedWidget = ScriptWidgetFactory
							.buildBindingWidget(implicit, false);
					subPanel.removeAll();
					subPanel.add(updatedWidget);
					subPanel.add(parameterPanel);

				}
			});
			subPanel.add(parameterPanel, false);

			implicitPanel.add(subPanel, false);
		}

		final Dimension dimension = implicitPanel.getPreferredSize();
		dimension.height = implicitList.size() * 90;
		dimension.width = implicitPanel.getMaximumSize().width;
		implicitPanel.setMaximumSize(dimension);

		return implicitPanel;
	}

	private void buildIndependentBehaviourPanel(final Behaviour behaviour,
			final JPanel behaviourPanel) {
		final SEGraph<Task> graph = this.buildBehaviourGraph(behaviour,
				behaviourPanel, ScriptEaseKeywords.INDEPENDENT);

		final List<KnowIt> implicitList = new ArrayList<KnowIt>();

		final KnowIt initiatorImplicit = behaviour.getImplicits().iterator()
				.next();
		final LibraryModel library = behaviour.getLibrary();

		graph.setBorder(BorderFactory.createEmptyBorder());

		implicitList.add(initiatorImplicit);

		if (behaviour.getMainCodeBlock().getParameters().isEmpty()) {
			final KnowIt initiator = new KnowIt(library);
			final KnowIt priority = new KnowIt(library);

			behaviour
					.setDisplayText("<Initiator> does action with priority <Priority>");

			initiator.setDisplayText("Initiator");
			initiator.addType("Creature");

			priority.setDisplayText("Priority");
			priority.addType("Number");
			priority.setBinding(new SimpleResource(priority.getTypes(), Integer
					.toString(behaviour.getPriority())));

			behaviour.getMainCodeBlock().addParameter(initiator);
			behaviour.getMainCodeBlock().addParameter(priority);
		}

		behaviourPanel.add(this.buildIndependentBehaviourNamePanel(behaviour));
		behaviourPanel.add(this.buildBehaviourGraphPanel("Independent Graph",
				graph));
		behaviourPanel.add(this.buildBehaviourImplicitPanel(implicitList));

		graph.setSelectedNode(graph.getStartNode());

		behaviourPanel.repaint();
		behaviourPanel.revalidate();
	}

	private void buildCollaborativeBehaviourPanel(final Behaviour behaviour,
			final JPanel behaviourPanel) {
		final SEGraph<Task> graph = this.buildBehaviourGraph(behaviour,
				behaviourPanel, ScriptEaseKeywords.COLLABORATIVE);

		final List<KnowIt> implicitList = new ArrayList<KnowIt>();

		final Iterator<KnowIt> iterator = behaviour.getImplicits().iterator();

		final KnowIt initiatorImplicit = iterator.next();
		final KnowIt responderImplicit = iterator.next();
		final LibraryModel library = behaviour.getLibrary();

		graph.setBorder(BorderFactory.createEmptyBorder());

		implicitList.add(initiatorImplicit);
		implicitList.add(responderImplicit);

		if (behaviour.getMainCodeBlock().getParameters().isEmpty()) {
			final KnowIt initiator = new KnowIt(library);
			final KnowIt responder = new KnowIt(library);
			final KnowIt priority = new KnowIt(library);

			behaviour
					.setDisplayText("<Initiator> interacts with <Responder> with priority <Priority>");

			initiator.setDisplayText("Initiator");
			initiator.addType("Creature");

			responder.setDisplayText("Responder");
			responder.addType("Creature");

			priority.setDisplayText("Priority");
			priority.addType("Number");
			priority.setBinding(new SimpleResource(priority.getTypes(), Integer
					.toString(behaviour.getPriority())));

			behaviour.getMainCodeBlock().addParameter(initiator);
			behaviour.getMainCodeBlock().addParameter(responder);
			behaviour.getMainCodeBlock().addParameter(priority);
		}

		behaviourPanel
				.add(this.buildCollaborativeBehaviourNamePanel(behaviour));
		behaviourPanel.add(this.buildBehaviourGraphPanel("Collaborative Graph",
				graph));
		behaviourPanel.add(this.buildBehaviourImplicitPanel(implicitList));

		graph.setSelectedNode(graph.getStartNode());

		behaviourPanel.repaint();
		behaviourPanel.revalidate();
	}

	@SuppressWarnings("serial")
	private JPanel buildCollaborativeBehaviourNamePanel(
			final Behaviour behaviour) {
		final JPanel namePanel;

		final JLabel initiatorLabel;
		final JLabel priorityLabel;

		final JTextField actionField;
		final JTextField priorityField;

		final Runnable commitText;
		final Runnable commitPriority;

		namePanel = new JPanel() {
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
		namePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		namePanel.setBorder(BorderFactory.createTitledBorder("Behaviour Name"));

		initiatorLabel = new JLabel("Initiator ");
		initiatorLabel.setFont(LibraryEditorPanelFactory.labelFont);
		priorityLabel = new JLabel(" Responder with priority ");
		priorityLabel.setFont(LibraryEditorPanelFactory.labelFont);

		final String displayText = behaviour.getDisplayText();

		final String actionName = displayText.substring(
				displayText.indexOf("<") + 12,
				displayText.indexOf(" <Responder>"));
		actionField = new JTextField(actionName, 15);

		final String priority = Integer.toString(behaviour.getPriority());
		priorityField = ComponentFactory.buildNumberTextField();
		priorityField.setText(priority);
		priorityField.setColumns(5);

		// Add listeners for the text fields

		commitText = new Runnable() {
			@Override
			public void run() {
				final String oldDisplayText = behaviour.getDisplayText();

				final String oldActionName = oldDisplayText.substring(
						oldDisplayText.indexOf("<") + 12,
						oldDisplayText.indexOf(" <Responder>"));

				if (actionField.getText().contains("<")
						|| actionField.getText().contains(">"))
					return;

				behaviour.setDisplayText(behaviour.getDisplayText().replace(
						oldActionName, actionField.getText()));

			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(actionField,
				commitText, false);

		commitPriority = new Runnable() {
			@Override
			public void run() {
				final KnowIt priorityKnowIt = behaviour.getMainCodeBlock()
						.getParameters().get(2);
				final String priority = priorityField.getText();

				behaviour.setPriority(Integer.parseInt(priority));

				priorityKnowIt.setBinding(new SimpleResource(priorityKnowIt
						.getTypes(), priority));

				priorityKnowIt.revalidateKnowItBindings();
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(priorityField,
				commitPriority, false);

		namePanel.add(initiatorLabel);
		namePanel.add(actionField);
		namePanel.add(priorityLabel);
		namePanel.add(priorityField);

		return namePanel;
	}

	@SuppressWarnings("serial")
	private JPanel buildIndependentBehaviourNamePanel(final Behaviour behaviour) {
		final JPanel namePanel;

		final JLabel initiatorLabel;
		final JLabel priorityLabel;

		final JTextField actionField;
		final JTextField priorityField;

		final Runnable commitText;
		final Runnable commitPriority;

		namePanel = new JPanel() {
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

		namePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		namePanel.setBorder(BorderFactory.createTitledBorder("Behaviour Name"));

		initiatorLabel = new JLabel("Initiator ");
		initiatorLabel.setFont(LibraryEditorPanelFactory.labelFont);
		priorityLabel = new JLabel(" with priority ");
		priorityLabel.setFont(LibraryEditorPanelFactory.labelFont);

		final String displayText = behaviour.getDisplayText();

		final String actionName = displayText.substring(
				displayText.indexOf("<") + 12,
				displayText.indexOf(" with priority"));
		actionField = new JTextField(actionName, 15);

		final String priority = Integer.toString(behaviour.getPriority());
		priorityField = ComponentFactory.buildNumberTextField();
		priorityField.setText(priority);
		priorityField.setColumns(5);

		// Add listeners for the text fields

		commitText = new Runnable() {
			@Override
			public void run() {
				final String oldDisplayText = behaviour.getDisplayText();

				final String oldActionName = oldDisplayText.substring(
						oldDisplayText.indexOf("<") + 12,
						oldDisplayText.indexOf(" with priority"));

				if (actionField.getText().contains("<")
						|| actionField.getText().contains(">"))
					return;

				behaviour.setDisplayText(behaviour.getDisplayText().replace(
						oldActionName, actionField.getText()));
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(actionField,
				commitText, false);

		commitPriority = new Runnable() {
			@Override
			public void run() {
				final KnowIt priorityKnowIt = behaviour.getMainCodeBlock()
						.getParameters().get(1);
				final String priority = priorityField.getText();

				behaviour.setPriority(Integer.parseInt(priority));

				priorityKnowIt.setBinding(new SimpleResource(priorityKnowIt
						.getTypes(), priority));

				priorityKnowIt.revalidateKnowItBindings();

				behaviour.setDisplayText(behaviour.getDisplayText());

			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(priorityField,
				commitPriority, false);

		namePanel.add(initiatorLabel);
		namePanel.add(actionField);
		namePanel.add(priorityLabel);
		namePanel.add(priorityField);

		return namePanel;
	}

	// *************** DESCRIPTION EDITING PANEL *************************

	/**
	 * Builds a panel used to edit a KnowItBindingDescribeIt.
	 * 
	 * @param describeIt
	 * @param knowIt
	 * @return
	 */
	public JComponent buildDescribeItEditingPanel(final DescribeIt describeIt,
			final KnowIt knowIt) {

		final JPanel bindingPanel;
		final JPanel describeItGraphPanel;

		final EffectHolderPanel effectHolder;
		final SetEffectObserver effectObserver;
		final SEGraph<DescribeItNode> graph;

		final boolean isEditable;

		bindingPanel = new JPanel();
		describeItGraphPanel = new JPanel();

		effectHolder = new EffectHolderPanel(describeIt.getTypes());

		isEditable = ScriptEase.DEBUG_MODE
				|| !knowIt.getLibrary().getReadOnly();

		graph = SEGraphFactory.buildDescribeItEditorGraph(
				describeIt.getStartNode(), isEditable);

		describeItGraphPanel.setOpaque(false);
		effectHolder.setBackground(ScriptEaseUI.SECONDARY_UI);
		bindingPanel.setOpaque(false);

		// Set the effectHolder to reflect the initial path of the describeIt
		// (since it doesn't throw a path selection even in SEGraph the
		// constructor)
		final ScriptIt initialScriptIt = describeIt.getScriptItForPath(graph
				.getSelectedNodes());
		effectHolder.setEffect(initialScriptIt);

		effectObserver = new SetEffectObserver() {
			@Override
			public void effectChanged(ScriptIt newEffect) {
				// We need to make a copy or else the path is ALWAYS the current
				// selected nodes, which is not what we want at all.
				final Collection<DescribeItNode> selectedNodes;

				selectedNodes = new ArrayList<DescribeItNode>(
						graph.getSelectedNodes());

				describeIt.assignScriptItToPath(selectedNodes, newEffect);

				final ScriptIt scriptItForPath = describeIt
						.getScriptItForPath(describeIt.getShortestPath());
				if (scriptItForPath != null) {
					knowIt.setBinding(scriptItForPath);
				} else {
					knowIt.clearBinding();
				}

			}
		};

		graph.addSEGraphObserver(new SEGraphAdapter<DescribeItNode>() {

			@Override
			public void nodesSelected(Collection<DescribeItNode> nodes) {
				final ScriptIt pathScriptIt;
				pathScriptIt = describeIt.getScriptItForPath(nodes);

				effectHolder.removeSetEffectObserver(effectObserver);
				effectHolder.setEffect(pathScriptIt);
				effectHolder.addSetEffectObserver(effectObserver);
			}
		});

		effectHolder.addSetEffectObserver(effectObserver);

		final JScrollPane graphScroll = new JScrollPane(graph);

		graphScroll.setBorder(BorderFactory.createEmptyBorder());

		// Set up the JPanel containing the graph
		describeItGraphPanel.setLayout(new BorderLayout());
		describeItGraphPanel.add(graph.getToolBar(), BorderLayout.WEST);
		describeItGraphPanel.add(graphScroll, BorderLayout.CENTER);

		bindingPanel
				.setLayout(new BoxLayout(bindingPanel, BoxLayout.PAGE_AXIS));
		bindingPanel.setBorder(BorderFactory
				.createTitledBorder("DescribeIt Binding"));

		bindingPanel.add(effectHolder);
		bindingPanel.add(describeItGraphPanel);

		return bindingPanel;
	}

	/**
	 * Builds a JTextField used to edit the name of the story component. The
	 * TextField gets updated if the Story Component's name changes for other
	 * reasons, such as undoing.
	 * 
	 * @param component
	 * @return
	 */
	private JTextField buildNameEditorPanel(final StoryComponent component) {
		final JTextField nameField;
		final Runnable commitText;

		nameField = new JTextField(component.getDisplayText());

		commitText = new Runnable() {
			@Override
			public void run() {
				if (!UndoManager.getInstance().hasOpenUndoableAction()) {
					String text = nameField.getText();
					if (!text.equals(component.getDisplayText())) {
						UndoManager.getInstance().startUndoableAction(
								"Change " + component + "'s display text to "
										+ text);
						component.setDisplayText(text);

						if (UndoManager.getInstance().hasOpenUndoableAction())
							UndoManager.getInstance().endUndoableAction();
					}
				}
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(nameField, commitText,
				false);

		nameField.setHorizontalAlignment(JTextField.LEADING);

		component.addStoryComponentObserver(nameField,
				new StoryComponentObserver() {
					@Override
					public void componentChanged(StoryComponentEvent event) {
						if (event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME) {
							nameField.setText(component.getDisplayText());
						}
					}
				});

		return nameField;
	}

	/**
	 * Builds a JTextField used to edit the labels of a story component.
	 * 
	 * @param component
	 * @return
	 */
	private JTextField buildLabelEditorField(final StoryComponent component) {
		final String SEPARATOR = ", ";

		final JTextField labelField;
		final String labelToolTip;
		final Runnable commitText;

		labelField = new JTextField(StringOp.getCollectionAsString(
				component.getLabels(), SEPARATOR));
		labelToolTip = "<html><b>Labels</b> are seperated by commas.<br>"
				+ "Leading and trailing spaces are<br>"
				+ "removed automatically.</html>";

		commitText = new Runnable() {
			@Override
			public void run() {
				if (!UndoManager.getInstance().hasOpenUndoableAction()) {
					final Set<String> labels = new HashSet<String>();
					final String labelFieldText = labelField.getText();
					final String[] labelArray = labelFieldText.split(SEPARATOR);
					for (String label : labelArray) {
						labels.add(label.trim());
					}

					UndoManager.getInstance().startUndoableAction(
							"Setting " + component + "'s labels to "
									+ labelFieldText);
					component.setLabels(labels);
					UndoManager.getInstance().endUndoableAction();
				}
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(labelField,
				commitText, false);

		labelField.setToolTipText(labelToolTip);

		labelField.setHorizontalAlignment(JTextField.LEADING);

		component.addStoryComponentObserver(labelField,
				new StoryComponentObserver() {
					@Override
					public void componentChanged(StoryComponentEvent event) {
						StoryComponentChangeEnum eventType = event.getType();
						if (eventType == StoryComponentChangeEnum.CHANGE_LABELS_CHANGED) {
							labelField.setText(StringOp.getCollectionAsString(
									component.getLabels(), SEPARATOR));
						}
					}
				});

		return labelField;
	}

	/**
	 * Builds a JCheckBox to set a component's visibility.
	 * 
	 * @param component
	 * @return
	 */
	private JCheckBox buildVisibleBox(final StoryComponent component) {
		final JCheckBox visibleBox;

		visibleBox = new JCheckBox();
		visibleBox.setSelected(component.isVisible());
		visibleBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!UndoManager.getInstance().hasOpenUndoableAction()) {
					UndoManager.getInstance().startUndoableAction(
							"Toggle " + component + "'s visiblity");
					component.setVisible(visibleBox.isSelected());
					UndoManager.getInstance().endUndoableAction();
				}
			}
		});
		component.addStoryComponentObserver(visibleBox,
				new StoryComponentObserver() {
					@Override
					public void componentChanged(StoryComponentEvent event) {
						if (event.getType() == StoryComponentChangeEnum.CHANGE_VISIBILITY) {
							visibleBox.setSelected(component.isVisible());
						}
					}
				});

		visibleBox.setOpaque(false);

		return visibleBox;
	}

	/**
	 * Builds a panel containing a name, label, and visibility editor.
	 * 
	 * @param component
	 * @return
	 */
	public JPanel buildDescriptorPanel(StoryComponent component) {
		final JPanel descriptorPanel;
		final GroupLayout descriptorPanelLayout;

		final JLabel nameLabel;
		final JLabel labelLabel;
		final JLabel visibleLabel;
		final JLabel readOnlyLabel;

		final JTextField nameField;
		final JTextField labelsField;
		final JCheckBox visibleBox;

		final boolean isEditable;

		descriptorPanel = new JPanel();
		descriptorPanelLayout = new GroupLayout(descriptorPanel);

		nameLabel = new JLabel("Name: ");
		labelLabel = new JLabel("Labels: ");
		visibleLabel = new JLabel("Visible: ");
		readOnlyLabel = new JLabel(
				"This element is from a read-only library and cannot be edited.");
		
		nameField = this.buildNameEditorPanel(component);
		labelsField = this.buildLabelEditorField(component);
		visibleBox = this.buildVisibleBox(component);

		// Check whether or not this StoryComponent should be editable (debug
		// mode, or not read-only)
		isEditable = ScriptEase.DEBUG_MODE
				|| !component.getLibrary().getReadOnly();

		// Set up the labels
		nameLabel.setFont(labelFont);
		labelLabel.setFont(labelFont);
		visibleLabel.setFont(labelFont);
		readOnlyLabel.setFont(labelFont);
		readOnlyLabel.setForeground(ScriptEaseUI.SE_BLUE);

		labelLabel.setToolTipText(labelsField.getToolTipText());

		// Set up the descriptorPanel
		descriptorPanel.setLayout(descriptorPanelLayout);
		descriptorPanel.setOpaque(false);

		descriptorPanelLayout.setAutoCreateGaps(true);
		descriptorPanelLayout.setAutoCreateContainerGaps(true);
		descriptorPanelLayout.setHonorsVisibility(true);

		// Add JComponents to DescriptorPanel using GroupLayout
		if (isEditable) {
			descriptorPanelLayout
					.setHorizontalGroup(descriptorPanelLayout
							.createParallelGroup()
							.addGroup(
									descriptorPanelLayout
											.createSequentialGroup()
											.addGroup(
													descriptorPanelLayout
															.createParallelGroup()
															.addComponent(
																	nameLabel)
															.addComponent(
																	visibleLabel)
															.addComponent(
																	labelLabel))
											.addGroup(
													descriptorPanelLayout
															.createParallelGroup()
															.addComponent(
																	visibleBox)
															.addComponent(
																	nameField)
															.addComponent(
																	labelsField))));

			descriptorPanelLayout.setVerticalGroup(descriptorPanelLayout
					.createSequentialGroup()
					.addGroup(
							descriptorPanelLayout
									.createParallelGroup(
											GroupLayout.Alignment.BASELINE)
									.addComponent(visibleLabel)
									.addComponent(visibleBox))
					.addGroup(
							descriptorPanelLayout
									.createParallelGroup(
											GroupLayout.Alignment.BASELINE)
									.addComponent(nameLabel)
									.addComponent(nameField))
					.addGroup(
							descriptorPanelLayout
									.createParallelGroup(
											GroupLayout.Alignment.BASELINE)
									.addComponent(labelLabel)
									.addComponent(labelsField)));
		} else {
			visibleBox.setEnabled(false);
			nameField.setEnabled(false);
			labelsField.setEnabled(false);
			descriptorPanelLayout
					.setHorizontalGroup(descriptorPanelLayout
							.createParallelGroup()
							.addGroup(
									descriptorPanelLayout
											.createSequentialGroup()
											.addGroup(
													descriptorPanelLayout
															.createParallelGroup()
															.addComponent(
																	nameLabel)
															.addComponent(
																	visibleLabel)
															.addComponent(
																	labelLabel))
											.addGroup(
													descriptorPanelLayout
															.createParallelGroup()
															.addComponent(
																	readOnlyLabel)
															.addComponent(
																	visibleBox)
															.addComponent(
																	nameField)
															.addComponent(
																	labelsField))));

			descriptorPanelLayout.setVerticalGroup(descriptorPanelLayout
					.createSequentialGroup()
					.addGroup(
							descriptorPanelLayout.createParallelGroup(
									GroupLayout.Alignment.BASELINE)
									.addComponent(readOnlyLabel))
					.addGroup(
							descriptorPanelLayout
									.createParallelGroup(
											GroupLayout.Alignment.BASELINE)
									.addComponent(visibleLabel)
									.addComponent(visibleBox))
					.addGroup(
							descriptorPanelLayout
									.createParallelGroup(
											GroupLayout.Alignment.BASELINE)
									.addComponent(nameLabel)
									.addComponent(nameField))
					.addGroup(
							descriptorPanelLayout
									.createParallelGroup(
											GroupLayout.Alignment.BASELINE)
									.addComponent(labelLabel)
									.addComponent(labelsField)));
		}

		return descriptorPanel;
	}

	/**
	 * Builds a parameter panel.
	 * 
	 * @param scriptIt
	 * @param codeBlock
	 * @param knowIt
	 * @return
	 */
	public ParameterPanel buildParameterPanel(ScriptIt scriptIt,
			CodeBlock codeBlock, KnowIt knowIt) {
		return new ParameterPanel(scriptIt, codeBlock, knowIt);
	}

	/**
	 * Builds a parameter panel for behaviour implicits.
	 * 
	 * @param scriptIt
	 * @param codeBlock
	 * @param knowIt
	 * @return
	 */
	public ParameterPanel buildParameterPanel(KnowIt knowIt) {
		return new ParameterPanel(knowIt);
	}
}
