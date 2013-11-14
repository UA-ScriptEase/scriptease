package scriptease.gui.libraryeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import scriptease.ScriptEase;
import scriptease.controller.observer.SetEffectObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.FunctionIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;
import scriptease.model.complex.behaviours.Task;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.ScriptEaseKeywords;
import scriptease.translator.io.model.SimpleResource;
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

	// Stores the current observers for the selected StoryComponent so that they
	// do not get garbage collected.
	private StoryComponentObserver currentNameObserver;
	private StoryComponentObserver currentLabelObserver;

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
		final JPanel editorPanel;
		editorPanel = new LibraryEditorPanel();

		return editorPanel;
	}

	// ******************* FUNCTIONIT EDITING PANEL ************************* //
	
	public JPanel buildFunctionItEditingPanel(final FunctionIt functionIt) {
		final JPanel functionPanel;

		functionPanel = new JPanel();
		functionPanel.setLayout(new BoxLayout(functionPanel, BoxLayout.Y_AXIS));

		final StoryComponentPanel panel = StoryComponentPanelFactory
				.getInstance().buildStoryComponentPanel(functionIt);

		functionPanel.add(this.buildDescriptorPanel(functionIt));
		functionPanel.add(this.buildCodeBlockPanel(
				functionIt.getMainCodeBlock(), functionIt));
		functionPanel.add(new StoryComponentPanelTree(panel));

		return functionPanel;
	}

	// ******************* BEHAVIOUR EDITING PANEL ************************* //

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

		graphPanel.setBorder(BorderFactory.createTitledBorder(graphName));
		graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.X_AXIS));

		graphPanel.add(new JScrollPane(graph), BorderLayout.CENTER);

		return graphPanel;
	}

	@SuppressWarnings("serial")
	private JPanel buildBehaviourToolbarPanel(SEGraph<Task> graph) {
		final JPanel toolbarPanel;

		toolbarPanel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				final Dimension dimension = super.getPreferredSize();
				dimension.height = 80;
				return dimension;
			}

			@Override
			public Dimension getMaximumSize() {
				final Dimension dimension = super.getMaximumSize();
				dimension.height = 80;
				return dimension;
			}

			@Override
			public Dimension getMinimumSize() {
				final Dimension dimension = super.getMinimumSize();
				dimension.height = 80;
				return dimension;
			}
		};

		toolbarPanel.setBorder(BorderFactory
				.createTitledBorder("Graph Toolbar"));
		toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		toolbarPanel.add(graph.getToolBar());
		graph.getToolBar().setHorizontal();

		return toolbarPanel;
	}

	private SEGraph<Task> buildBehaviourGraph(final Behaviour behaviour,
			final JPanel behaviourPanel, final String type) {
		final SEGraph<Task> graph;
		final Task startTask;

		if (behaviour.getStartTask() != null) {
			// If behaviour is defined, build its existing task graph
			startTask = behaviour.getStartTask();
		} else {
			// Else create a new one.
			if (type == ScriptEaseKeywords.INDEPENDENT)
				startTask = new IndependentTask("");
			else
				startTask = new CollaborativeTask("", "");

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
							&& panel.getComponent(0) instanceof JScrollPane) {
						behaviourPanel.remove(lastComponent);
					}
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

					taskPanel.add(initiatorPanelTree);
					taskPanel.add(responderPanelTree);
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

	@SuppressWarnings("serial")
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

	private void buildIndependentBehaviourPanel(final Behaviour behaviour,
			final JPanel behaviourPanel) {
		final SEGraph<Task> graph = this.buildBehaviourGraph(behaviour,
				behaviourPanel, ScriptEaseKeywords.INDEPENDENT);

		final List<KnowIt> implicitList = new ArrayList<KnowIt>();

		final KnowIt initiatorImplicit = behaviour.getImplicits().iterator()
				.next();

		graph.setBorder(BorderFactory.createEmptyBorder());

		implicitList.add(initiatorImplicit);

		if (behaviour.getMainCodeBlock().getParameters().isEmpty()) {
			final KnowIt initiator = new KnowIt();
			final KnowIt priority = new KnowIt();

			behaviour
					.setDisplayText("<Initiator> does action with priority <Priority>");

			initiator.setDisplayText("Initiator");
			initiator.addType("creature");

			priority.setDisplayText("Priority");
			priority.addType("float");
			priority.setBinding(new SimpleResource(priority.getTypes(), Integer
					.toString(behaviour.getPriority())));

			behaviour.getMainCodeBlock().addParameter(initiator);
			behaviour.getMainCodeBlock().addParameter(priority);
		}

		behaviourPanel.add(this.buildIndependentBehaviourNamePanel(behaviour));
		behaviourPanel.add(this.buildBehaviourToolbarPanel(graph));
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

		graph.setBorder(BorderFactory.createEmptyBorder());

		implicitList.add(initiatorImplicit);
		implicitList.add(responderImplicit);

		if (behaviour.getMainCodeBlock().getParameters().isEmpty()) {
			final KnowIt initiator = new KnowIt();
			final KnowIt responder = new KnowIt();
			final KnowIt priority = new KnowIt();

			behaviour
					.setDisplayText("<Initiator> interacts with <Responder> with priority <Priority>");

			initiator.setDisplayText("Initiator");
			initiator.addType("creature");

			responder.setDisplayText("Responder");
			responder.addType("creature");

			priority.setDisplayText("Priority");
			priority.addType("float");
			priority.setBinding(new SimpleResource(priority.getTypes(), Integer
					.toString(behaviour.getPriority())));

			behaviour.getMainCodeBlock().addParameter(initiator);
			behaviour.getMainCodeBlock().addParameter(responder);
			behaviour.getMainCodeBlock().addParameter(priority);
		}

		behaviourPanel
				.add(this.buildCollaborativeBehaviourNamePanel(behaviour));
		behaviourPanel.add(this.buildBehaviourToolbarPanel(graph));
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
		actionField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
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
		});

		priorityField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final KnowIt priorityKnowIt = behaviour.getMainCodeBlock()
						.getParameters().get(2);
				final String priority = priorityField.getText();

				behaviour.setPriority(Integer.parseInt(priority));

				priorityKnowIt.setBinding(new SimpleResource(priorityKnowIt
						.getTypes(), priority));

				priorityKnowIt.revalidateKnowItBindings();
			}
		});

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
		actionField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
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
		});

		priorityField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final KnowIt priorityKnowIt = behaviour.getMainCodeBlock()
						.getParameters().get(1);
				final String priority = priorityField.getText();

				behaviour.setPriority(Integer.parseInt(priority));

				priorityKnowIt.setBinding(new SimpleResource(priorityKnowIt
						.getTypes(), priority));

				priorityKnowIt.revalidateKnowItBindings();
			}
		});

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

		bindingPanel = new JPanel();
		describeItGraphPanel = new JPanel();

		effectHolder = new EffectHolderPanel(describeIt.getTypes());

		graph = SEGraphFactory.buildDescribeItEditorGraph(describeIt
				.getStartNode());

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

		// Set up the JPanel containing the graph
		describeItGraphPanel.setLayout(new BorderLayout());
		describeItGraphPanel.add(graph.getToolBar(), BorderLayout.WEST);
		describeItGraphPanel.add(new JScrollPane(graph), BorderLayout.CENTER);

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

		this.currentNameObserver = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME) {
					nameField.setText(component.getDisplayText());
				}
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(nameField, commitText,
				false, Color.white);

		nameField.setHorizontalAlignment(JTextField.LEADING);

		component.addStoryComponentObserver(this.currentNameObserver);

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

		this.currentLabelObserver = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				StoryComponentChangeEnum eventType = event.getType();
				if (eventType == StoryComponentChangeEnum.CHANGE_LABELS_CHANGED) {
					labelField.setText(StringOp.getCollectionAsString(
							component.getLabels(), SEPARATOR));
				}
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(labelField,
				commitText, false, Color.white);

		labelField.setToolTipText(labelToolTip);

		labelField.setHorizontalAlignment(JTextField.LEADING);

		component.addStoryComponentObserver(this.currentLabelObserver);

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
		component.addStoryComponentObserver(new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_VISIBILITY) {
					visibleBox.setSelected(component.isVisible());
				}
			}
		});

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

		final JTextField nameField;
		final JTextField labelsField;
		final JCheckBox visibleBox;

		descriptorPanel = new JPanel();
		descriptorPanelLayout = new GroupLayout(descriptorPanel);

		nameLabel = new JLabel("Name: ");
		labelLabel = new JLabel("Labels: ");
		visibleLabel = new JLabel("Visible: ");

		nameField = this.buildNameEditorPanel(component);
		labelsField = this.buildLabelEditorField(component);
		visibleBox = this.buildVisibleBox(component);

		// Set up the labels
		nameLabel.setFont(labelFont);
		nameLabel.setLabelFor(nameField);

		labelLabel.setFont(labelFont);
		labelLabel.setLabelFor(labelsField);
		labelLabel.setToolTipText(labelsField.getToolTipText());

		visibleLabel.setFont(labelFont);
		visibleLabel.setLabelFor(visibleBox);

		// Set up the descriptorPanel
		descriptorPanel.setLayout(descriptorPanelLayout);
		descriptorPanel.setBorder(new TitledBorder("Component Descriptors"));

		descriptorPanelLayout.setAutoCreateGaps(true);
		descriptorPanelLayout.setAutoCreateContainerGaps(true);
		descriptorPanelLayout.setHonorsVisibility(true);

		// Add JComponents to DescriptorPanel using GroupLayout
		descriptorPanelLayout.setHorizontalGroup(descriptorPanelLayout
				.createParallelGroup().addGroup(
						descriptorPanelLayout
								.createSequentialGroup()
								.addGroup(
										descriptorPanelLayout
												.createParallelGroup()
												.addComponent(nameLabel)
												.addComponent(visibleLabel)
												.addComponent(labelLabel))
								.addGroup(
										descriptorPanelLayout
												.createParallelGroup()
												.addComponent(visibleBox)
												.addComponent(nameField)
												.addComponent(labelsField))));

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
	public JPanel buildParameterPanel(ScriptIt scriptIt, CodeBlock codeBlock,
			KnowIt knowIt) {
		return new ParameterPanel(scriptIt, codeBlock, knowIt);
	}

	/**
	 * Builds a codeblock panel
	 * 
	 * @param codeBlock
	 * @param scriptIt
	 * @return
	 */
	public CodeBlockPanel buildCodeBlockPanel(CodeBlock codeBlock,
			ScriptIt scriptIt) {
		return this.buildCodeBlockPanel(codeBlock, scriptIt, true, true);
	}

	public CodeBlockPanel buildCodeBlockPanel(CodeBlock codeBlock,
			ScriptIt scriptIt, boolean typeVisible, boolean codeVisible) {
		return new CodeBlockPanel(codeBlock, scriptIt, typeVisible, codeVisible);
	}

	/**
	 * Sets up a JPanel used to edit CodeBlocks. This shows the id, slot,
	 * includes, types, parameters, and code for the passed in CodeBlock, and
	 * allows the user to edit it. The panel also observes the provided
	 * codeBlock for changes
	 * 
	 * @param scriptIt
	 * @param codeBlock
	 * @return
	 */
	@SuppressWarnings("serial")
	private class CodeBlockPanel extends JPanel implements
			StoryComponentObserver {
		private TypeAction typeAction;
		private CodeBlock codeBlock;

		@SuppressWarnings("rawtypes")
		public CodeBlockPanel(final CodeBlock codeBlock,
				final ScriptIt scriptIt, boolean typeVisible,
				boolean codeVisible) {
			final JLabel subjectLabel;
			final JLabel slotLabel;
			final JLabel implicitsLabelLabel;
			final JLabel includesLabel;
			final JLabel typesLabel;
			final JLabel parametersLabel;
			final JLabel codeLabel;

			final JPanel parameterPanel;
			final JScrollPane parameterScrollPane;

			final JTextField includesField;
			final JComboBox subjectBox;
			final JComboBox slotBox;
			final JLabel implicitsLabel;
			final CodeEditorPanel codePanel;

			final JButton deleteCodeBlockButton;
			final JButton addParameterButton;
			final JButton typesButton;

			final GroupLayout codeBlockEditorLayout;
			final Font labelFont;

			final List<KnowIt> parameters;

			this.codeBlock = codeBlock;
			codeBlock.addStoryComponentObserver(this);

			subjectLabel = new JLabel("Subject: ");
			slotLabel = new JLabel("Slot: ");
			implicitsLabelLabel = new JLabel("Implicits: ");
			includesLabel = new JLabel("Includes: ");
			typesLabel = new JLabel("Types: ");
			parametersLabel = new JLabel("Options: ");
			codeLabel = new JLabel("Code: ");
			implicitsLabel = new JLabel();

			parameterPanel = new JPanel();
			parameterScrollPane = new JScrollPane(parameterPanel);

			typeAction = new TypeAction();
			includesField = new IncludesField(codeBlock);

			if (scriptIt instanceof CauseIt) {
				subjectBox = new SubjectComboBox(codeBlock);
				slotBox = new SlotComboBox(codeBlock);
			} else {
				subjectBox = new JComboBox();
				subjectLabel.setVisible(false);
				subjectBox.setVisible(false);
				slotLabel.setVisible(false);
				slotBox = new JComboBox();
				slotBox.setVisible(false);
				implicitsLabel.setVisible(false);
				implicitsLabelLabel.setVisible(false);
				includesLabel.setVisible(false);
				includesField.setVisible(false);
			}

			codePanel = new CodeEditorPanel(codeBlock);

			deleteCodeBlockButton = new JButton("Delete CodeBlock");
			addParameterButton = ComponentFactory.buildAddButton();
			typesButton = new JButton(typeAction);

			codeBlockEditorLayout = new GroupLayout(this);
			labelFont = new Font("SansSerif", Font.BOLD,
					Integer.parseInt(ScriptEase.getInstance().getPreference(
							ScriptEase.FONT_SIZE_KEY)) + 1);

			parameters = codeBlock.getParameters();

			// Set up the layout
			this.setLayout(codeBlockEditorLayout);
			this.setBorder(new TitledBorder("Code Block #" + codeBlock.getId()));

			codeBlockEditorLayout.setAutoCreateGaps(true);
			codeBlockEditorLayout.setAutoCreateContainerGaps(true);
			codeBlockEditorLayout.setHonorsVisibility(true);

			parameterPanel.setLayout(new BoxLayout(parameterPanel,
					BoxLayout.PAGE_AXIS));

			parameterScrollPane.setPreferredSize(new Dimension(400, 250));
			parameterScrollPane.getVerticalScrollBar().setUnitIncrement(16);

			// Set up the label fonts and colors
			subjectLabel.setFont(labelFont);
			slotLabel.setFont(labelFont);
			implicitsLabelLabel.setFont(labelFont);
			includesLabel.setFont(labelFont);
			typesLabel.setFont(labelFont);
			parametersLabel.setFont(labelFont);
			codeLabel.setFont(labelFont);

			scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
					.getInstance().buildCodeBlockComponentObserver(
							deleteCodeBlockButton));

			scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
					.getInstance().buildParameterObserver(codeBlock,
							parameterPanel));

			scriptIt.addStoryComponentObserver(LibraryEditorListenerFactory
					.getInstance().buildSlotObserver(codeBlock, implicitsLabel));

			implicitsLabel.setForeground(Color.DARK_GRAY);

			final ArrayList<String> types = new ArrayList<String>(
					codeBlock.getTypes());
			typeAction.getTypeSelectionDialogBuilder().deselectAll();
			typeAction.getTypeSelectionDialogBuilder().selectTypesByKeyword(
					types, true);

			String implicits = "";

			for (KnowIt implicit : codeBlock.getImplicits())
				implicits += "[" + implicit.getDisplayText() + "] ";

			implicitsLabel.setText(implicits.trim());

			typeAction.setAction(new Runnable() {
				@Override
				public void run() {
					if (!UndoManager.getInstance().hasOpenUndoableAction()) {
						final Collection<String> selectedTypes = typeAction
								.getTypeSelectionDialogBuilder()
								.getSelectedTypeKeywords();
						UndoManager.getInstance().startUndoableAction(
								"Setting CodeBlock " + codeBlock + " types to "
										+ selectedTypes);
						codeBlock.setTypes(selectedTypes);
						UndoManager.getInstance().endUndoableAction();
					}
				}
			});

			addParameterButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final KnowIt knowIt = new KnowIt();
					knowIt.setLibrary(codeBlock.getLibrary());
					if (!UndoManager.getInstance().hasOpenUndoableAction()) {
						UndoManager.getInstance().startUndoableAction(
								"Add parameter " + knowIt + " to " + codeBlock);
						codeBlock.addParameter(knowIt);
						UndoManager.getInstance().endUndoableAction();
					}
				}
			});

			slotBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final String selectedSlot = (String) slotBox
							.getSelectedItem();

					if (selectedSlot != null)
						codeBlock.setSlot((String) slotBox.getSelectedItem());
					else
						codeBlock.setSlot("");

					scriptIt.notifyObservers(new StoryComponentEvent(scriptIt,
							StoryComponentChangeEnum.CODE_BLOCK_SLOT_SET));
				}
			});

			if (scriptIt instanceof CauseIt) {
				deleteCodeBlockButton.setVisible(false);
				subjectLabel.setVisible(false);
				subjectBox.setVisible(false);

				slotLabel.setVisible(false);
				slotBox.setVisible(false);
				implicitsLabel.setVisible(false);
				implicitsLabelLabel.setVisible(false);
			} else {
				deleteCodeBlockButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (!UndoManager.getInstance().hasOpenUndoableAction())
							UndoManager.getInstance().startUndoableAction(
									"Removing CodeBlock to "
											+ scriptIt.getDisplayText());
						scriptIt.removeCodeBlock(codeBlock);
						UndoManager.getInstance().endUndoableAction();
					}
				});

				if (!scriptIt.getMainCodeBlock().equals(codeBlock)) {
					subjectBox.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							final String subjectName;
							subjectName = (String) subjectBox.getSelectedItem();

							codeBlock.setSubject(subjectName);

							scriptIt.notifyObservers(new StoryComponentEvent(
									scriptIt,
									StoryComponentChangeEnum.CODE_BLOCK_SUBJECT_SET));
						}
					});
				} else {
					subjectLabel.setVisible(false);
					subjectBox.setVisible(false);

					slotLabel.setVisible(false);
					slotBox.setVisible(false);
					implicitsLabel.setVisible(false);
					implicitsLabelLabel.setVisible(false);
				}

				if (scriptIt.getCodeBlocks().size() < 2) {
					deleteCodeBlockButton.setEnabled(false);
				}
			}

			for (KnowIt parameter : parameters) {
				parameterPanel.add(LibraryEditorPanelFactory.getInstance()
						.buildParameterPanel(scriptIt, codeBlock, parameter));
			}

			codeBlockEditorLayout.setHorizontalGroup(codeBlockEditorLayout
					.createSequentialGroup()
					.addGroup(
							codeBlockEditorLayout.createParallelGroup()
									.addComponent(subjectLabel)
									.addComponent(slotLabel)
									.addComponent(implicitsLabelLabel)
									.addComponent(includesLabel)
									.addComponent(typesLabel)
									.addComponent(parametersLabel)
									.addComponent(addParameterButton)
									.addComponent(codeLabel))
					.addGroup(
							codeBlockEditorLayout
									.createParallelGroup()
									.addComponent(deleteCodeBlockButton,
											GroupLayout.Alignment.TRAILING)
									.addComponent(subjectBox)
									.addComponent(slotBox)
									.addComponent(implicitsLabel)
									.addComponent(includesField)
									.addComponent(typesButton)
									.addComponent(parameterScrollPane)
									.addComponent(codePanel)));

			codeBlockEditorLayout
					.setVerticalGroup(codeBlockEditorLayout
							.createSequentialGroup()
							.addComponent(deleteCodeBlockButton)
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addComponent(subjectLabel)
											.addComponent(subjectBox))
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addComponent(slotLabel)
											.addComponent(slotBox))
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addComponent(implicitsLabelLabel)
											.addComponent(implicitsLabel))
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addComponent(includesLabel)
											.addComponent(includesField))
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addComponent(typesLabel)
											.addComponent(typesButton))
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addGroup(
													codeBlockEditorLayout
															.createSequentialGroup()
															.addComponent(
																	parametersLabel)
															.addComponent(
																	addParameterButton))
											.addComponent(parameterScrollPane))
							.addGroup(
									codeBlockEditorLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addComponent(codeLabel)
											.addComponent(codePanel)));
		}

		@Override
		public void componentChanged(StoryComponentEvent event) {
			final ArrayList<String> types = new ArrayList<String>(
					this.codeBlock.getTypes());
			typeAction.getTypeSelectionDialogBuilder().deselectAll();
			typeAction.getTypeSelectionDialogBuilder().selectTypesByKeyword(
					types, true);
			typeAction.updateName();
		}
	}

	@SuppressWarnings("serial")
	private class IncludesField extends JTextField implements
			StoryComponentObserver, ActionListener, FocusListener {
		private CodeBlock codeBlock;

		public IncludesField(CodeBlock codeBlock) {
			this.codeBlock = codeBlock;
			codeBlock.addStoryComponentObserver(this);
			this.addActionListener(this);
			this.addFocusListener(this);
		}

		@Override
		public void focusGained(FocusEvent e) {
		}

		@Override
		public void focusLost(FocusEvent e) {
			updateIncludes();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			updateIncludes();
		}

		@Override
		public void componentChanged(StoryComponentEvent event) {
			final StoryComponent source = event.getSource();
			if (source == codeBlock) {
				updateField();
			}
		}

		private void updateField() {
			this.setText(StringOp.getCollectionAsString(
					codeBlock.getIncludes(), ", "));
			this.revalidate();
		}

		private void updateIncludes() {
			final String labelFieldText;
			final String[] labelArray;
			final Collection<String> labels;

			labelFieldText = this.getText();
			labelArray = labelFieldText.split(",");
			labels = new ArrayList<String>();

			for (String label : labelArray) {
				labels.add(label.trim());
			}

			if (!labels.equals(codeBlock.getIncludes())) {
				// mfchurch TODO method type erasure problem with AspectJ
				// if (!UndoManager.getInstance().hasOpenUndoableAction())
				// UndoManager.getInstance().startUndoableAction(
				// "Setting Codeblock Includes to " + labels);
				codeBlock.setIncludes(labels);
				// UndoManager.getInstance().endUndoableAction();
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "serial" })
	private class SubjectComboBox extends JComboBox implements
			StoryComponentObserver {
		private boolean backgroundUpdate;
		private CodeBlock codeBlock;

		public SubjectComboBox(final CodeBlock codeBlock) {
			this.codeBlock = codeBlock;
			backgroundUpdate = false;
			buildItems();
			this.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!backgroundUpdate) {
						final String currentlySelected = (String) getSelectedItem();
						if (!isCurrentSubjectSelected(currentlySelected)) {
							if (!UndoManager.getInstance()
									.hasOpenUndoableAction()) {
								UndoManager.getInstance().startUndoableAction(
										"Setting CodeBlock subject to "
												+ currentlySelected);
							}
							codeBlock.setSubject(currentlySelected);
							UndoManager.getInstance().endUndoableAction();
						}
					}
				}
			});
			this.codeBlock.addStoryComponentObserver(this);
			final ScriptIt scriptIt = codeBlock.getOwner();
			if (scriptIt != null) {
				scriptIt.addStoryComponentObserver(this);
			} else {
				throw new IllegalArgumentException("CodeBlock " + codeBlock
						+ " has no owner");
			}
		}

		@SuppressWarnings("unchecked")
		private void buildItems() {
			this.removeAllItems();
			final ScriptIt scriptIt = codeBlock.getOwner();
			if (scriptIt != null) {
				final Collection<KnowIt> parameters = scriptIt.getParameters();
				for (KnowIt parameter : parameters) {
					final Collection<String> slots = getCommonSlotsForTypes(parameter);

					if (!slots.isEmpty())
						this.addItem(parameter.getDisplayText());
				}
				// this.addItem(null);
				this.setSelectedItem(codeBlock.getSubjectName());
			}
		}

		private boolean isCurrentSubjectSelected(String value) {
			final String currentSubject = codeBlock.getSubjectName();
			if (currentSubject != null) {
				return currentSubject.equals(value);
			} else {
				return currentSubject == value;
			}
		}

		@Override
		public void componentChanged(StoryComponentEvent event) {
			backgroundUpdate = true;
			final StoryComponentChangeEnum type = event.getType();
			if (type == StoryComponentChangeEnum.CODE_BLOCK_SUBJECT_SET) {
				buildItems();
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_ADD) {
				buildItems();
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_LIST_REMOVE) {
				buildItems();
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_NAME_SET) {
				buildItems();
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_DEFAULT_TYPE_SET) {
				buildItems();
			}
			this.revalidate();
			backgroundUpdate = false;
		}
	}

	@SuppressWarnings({ "rawtypes", "serial" })
	private class SlotComboBox extends JComboBox implements
			StoryComponentObserver {
		private boolean backgroundUpdate;
		private CodeBlock codeBlock;

		public SlotComboBox(final CodeBlock codeBlock) {
			this.codeBlock = codeBlock;
			backgroundUpdate = false;
			buildItems();
			this.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!backgroundUpdate) {
						final String currentlySelected = (String) getSelectedItem();
						if (!isCurrentSlotSelected(currentlySelected)) {
							if (!UndoManager.getInstance()
									.hasOpenUndoableAction()) {
								UndoManager.getInstance().startUndoableAction(
										"Setting CodeBlock slot to "
												+ currentlySelected);
							}
							codeBlock.setSlot(currentlySelected);
							UndoManager.getInstance().endUndoableAction();
						}
					}
				}
			});
			this.codeBlock.addStoryComponentObserver(this);
		}

		@SuppressWarnings("unchecked")
		private void buildItems() {
			this.removeAllItems();
			final KnowIt subject = codeBlock.getSubject();
			if (subject != null) {
				final Collection<String> slots = getCommonSlotsForTypes(subject);
				for (String slot : slots) {
					this.addItem(slot);
				}
				this.setSelectedItem(codeBlock.getSlot());
			}
		}

		private boolean isCurrentSlotSelected(String value) {
			final String currentSlot = codeBlock.getSlot();
			if (currentSlot != null) {
				return currentSlot.equals(value);
			} else {
				return currentSlot == value;
			}
		}

		@Override
		public void componentChanged(StoryComponentEvent event) {
			backgroundUpdate = true;
			final StoryComponentChangeEnum type = event.getType();
			if (type == StoryComponentChangeEnum.CODE_BLOCK_SLOT_SET) {
				buildItems();
			} else if (type == StoryComponentChangeEnum.CODE_BLOCK_SUBJECT_SET) {
				buildItems();
			} else if (type == StoryComponentChangeEnum.CHANGE_PARAMETER_DEFAULT_TYPE_SET) {
				buildItems();
			}
			this.revalidate();
			backgroundUpdate = false;
		}
	}

	/**
	 * Returns a list of slots that are common in all of the types in the knowit
	 * passed in.
	 * 
	 * @param subject
	 * @return
	 */
	private Collection<String> getCommonSlotsForTypes(KnowIt subject) {
		final SEModel model = SEModelManager.getInstance().getActiveModel();

		final Collection<String> slots;

		slots = model.getTypeSlots(subject.getDefaultType());

		for (String type : subject.getTypes()) {
			final Collection<String> otherSlots;

			otherSlots = new ArrayList<String>();

			for (String slot : model.getTypeSlots(type)) {
				if (slots.contains(slot))
					otherSlots.add(slot);
			}

			slots.removeAll(slots);
			slots.addAll(otherSlots);
		}
		return slots;
	}
}
